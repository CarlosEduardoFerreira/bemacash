package com.kaching123.tcr.commands.store.history;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.loyalty.AddLoyaltyPointsMovementCommand;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.saleorder.AddItemsMovementCommand;
import com.kaching123.tcr.commands.wireless.EditUnitCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemJdbcConverter;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.converter.SaleOrderItemFunction;
import com.kaching123.tcr.processor.MoneybackProcessor.RefundSaleItemInfo;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.util.MovementUtils;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class UpdateSaleOrderItemRefundQtyCommand extends AsyncCommand {

    private static final Uri URI_SALE_ITEMS = ShopProvider.getContentUri(SaleItemTable.URI_CONTENT);
    private static final Uri URI_ITEMS = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    private static final String ARG_ITEMS = "arg_sale_item_guid";
    private static final String ARG_UNITS = "arg_units";

    private ArrayList<ContentProviderOperation> operations;

    private List<SaleOrderItemModel> returnItems;

    private SyncResult addMovementsResult;
    private SyncResult addLoyaltyPointsResult;
    private ArrayList<SyncResult> editUnitResults;
    protected SaleOrderModel returnOrder;
    private HashMap<String, BigDecimal> info;
    @Override
    protected TaskResult doCommand() {
        operations = new ArrayList<>();

        ArrayList<RefundSaleItemInfo> itemsInfo = (ArrayList<RefundSaleItemInfo>) getArgs().getSerializable(ARG_ITEMS);
        List<Unit> units = (List<Unit>) getArgs().getSerializable(ARG_UNITS);

        info = new HashMap<>();
        for (RefundSaleItemInfo item : itemsInfo) {
            if(!item.qty.equals(BigDecimal.ZERO)) {
                info.put(item.saleItemGuid, item.qty);
            }
        }

        returnOrder = (SaleOrderModel) getArgs().getSerializable(AddReturnOrderCommand.ARG_ORDER_MODEL_CHILD);

        returnItems = ProviderAction.query(URI_SALE_ITEMS)
                .whereIn(SaleItemTable.SALE_ITEM_GUID, info.keySet())
                .perform(getContext())
                .toFluentIterable(new SaleOrderItemFunction())
                .toImmutableList();


        for (SaleOrderItemModel i : returnItems) {
            i.parentGuid = i.saleItemGuid;
            i.saleItemGuid = UUID.randomUUID().toString();
            i.orderGuid = returnOrder.guid;
            i.qty = CalculationUtil.negativeQty(info.get(i.parentGuid));

            operations.add(
                    ContentProviderOperation.newInsert(URI_SALE_ITEMS)
                            .withValues(i.toValues())
                            .build());
        }


        if (units != null && units.size() > 0) {
            editUnitResults = new ArrayList<>();
            for (Unit unit : units) {
                unit.childOrderId = returnOrder.guid;
                SyncResult subResult = new EditUnitCommand().sync(getContext(), unit, getAppCommandContext());
                if (subResult == null)
                    return failed();
                editUnitResults.add(subResult);
            }
        }

        if (!addMovementsPartial())
            return failed();

        if (!returnLoyaltyPoints())
            return failed();

        return succeeded();
    }

    private boolean addMovementsPartial() {
        HashSet<String> items = new HashSet<>();
        for (SaleOrderItemModel item : returnItems) {
            items.add(item.itemGuid);
        }
        Cursor c = ProviderAction.query(URI_ITEMS)
                .projection(ItemTable.GUID, ItemTable.UPDATE_QTY_FLAG, ItemTable.STOCK_TRACKING)
                .whereIn(ItemTable.GUID, items)
                .perform(getContext());

        ArrayList<ItemMovementModel> itemMovements = new ArrayList<>();
        while (c.moveToNext()) {
            String itemGuid = c.getString(c.getColumnIndex(ItemTable.GUID));
            String flag = c.getString(c.getColumnIndex(ItemTable.UPDATE_QTY_FLAG));
            boolean stockTracking = _bool(c, c.getColumnIndex(ItemTable.STOCK_TRACKING));
            if (!stockTracking) {
                continue;
            }
            for (SaleOrderItemModel item : returnItems) {
                if (!item.itemGuid.equals(itemGuid)) {
                    continue;
                }
                ArrayList<ItemMovementModel> currentMovements = new ArrayList<>();
                MovementUtils.processAllRefund(
                        getContext(),
                        getAppCommandContext(),
                        returnOrder.parentGuid, item.parentGuid,
                        currentMovements);
                itemMovements.addAll(currentMovements);
            }
        }
        c.close();

        if (itemMovements.isEmpty()) {
            return true;
        }

        addMovementsResult = new AddItemsMovementCommand().syncNow(getContext(), itemMovements, getAppCommandContext());
        if (addMovementsResult == null)
            return false;

        return true;
    }

    private boolean returnLoyaltyPoints(){
        if (returnOrder.customerGuid == null)
            return true;

        BigDecimal points = BigDecimal.ZERO;
        for (SaleOrderItemModel model : returnItems){
            if (model.pointsForDollarAmount){
                points = points.add(CalculationUtil.getSubTotal(model.qty, model.finalGrossPrice.subtract(model.finalDiscount)));
            }else{
                points = points.add(CalculationUtil.getSubTotal(model.qty, model.loyaltyPoints));
            }
        }

        if (BigDecimal.ZERO.compareTo(points) != 0){
            addLoyaltyPointsResult = new AddLoyaltyPointsMovementCommand().sync(getContext(), returnOrder.customerGuid, points, getAppCommandContext());
            return addLoyaltyPointsResult != null;
        }
        return true;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        if (addMovementsResult != null && addMovementsResult.getLocalDbOperations() != null)
            operations.addAll(addMovementsResult.getLocalDbOperations());
        if (addLoyaltyPointsResult != null)
            operations.addAll(addLoyaltyPointsResult.getLocalDbOperations());
        if (editUnitResults != null) {
            for (SyncResult subResult : editUnitResults) {
                if (subResult.getLocalDbOperations() != null) {
                    operations.addAll(subResult.getLocalDbOperations());
                }
            }
        }
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        SaleOrderItemJdbcConverter converter = (SaleOrderItemJdbcConverter) JdbcFactory.getConverter(SaleItemTable.TABLE_NAME);
        BatchSqlCommand batch = batchUpdate(SaleOrderItemModel.class);
        for (SaleOrderItemModel item : returnItems) {
            batch.add(converter.insertSQL(item, getAppCommandContext()));
        }
        if (addMovementsResult != null)
            batch.add(addMovementsResult.getSqlCmd());
        if (addLoyaltyPointsResult != null)
            batch.add(addLoyaltyPointsResult.getSqlCmd());
        if (editUnitResults != null) {
            for (SyncResult subResult : editUnitResults) {
                batch.add(subResult.getSqlCmd());
            }
        }
        return batch;
    }

    public static void start(Context context, Object callback, SaleOrderModel childOrderModel, ArrayList<Unit> units, ArrayList<RefundSaleItemInfo> items) {
        create(UpdateSaleOrderItemRefundQtyCommand.class)
                .arg(AddReturnOrderCommand.ARG_ORDER_MODEL_CHILD, childOrderModel)
                .arg(ARG_UNITS, new ArrayList<>(units))
                .arg(ARG_ITEMS, items)
                .callback(callback)
                .queueUsing(context);
    }
}
