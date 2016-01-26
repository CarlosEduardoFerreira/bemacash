package com.kaching123.tcr.commands.store.history;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.saleorder.AddItemsMovementCommand;
import com.kaching123.tcr.commands.wireless.EditUnitCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemJdbcConverter;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.ItemMovementModelFactory;
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
import java.util.Date;
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

    private static final Uri URI_SALE_ITEMS = ShopProvider.contentUri(SaleItemTable.URI_CONTENT);
    private static final Uri URI_ITEMS = ShopProvider.contentUri(ItemTable.URI_CONTENT);

    private static final String ARG_UNIT = "ARG_UNIT";

    private ArrayList<ContentProviderOperation> operations;

    private List<SaleOrderItemModel> returnItems;

    private SyncResult addMovementsResult;
    private ArrayList<SyncResult> editUnitResults;
    protected SaleOrderModel returnOrder;

    @Override
    protected TaskResult doCommand() {
        operations = new ArrayList<>();

        returnOrder = (SaleOrderModel) getArgs().getSerializable(AddReturnOrderCommand.ARG_ORDER_MODEL_CHILD);

        returnItems = ProviderAction.query(URI_SALE_ITEMS)
                .where(SaleItemTable.ORDER_GUID + " = ?", returnOrder.parentGuid)
                .perform(getContext())
                .toFluentIterable(new SaleOrderItemFunction())
                .toImmutableList();

        for (SaleOrderItemModel i : returnItems) {
            i.parentGuid = i.saleItemGuid;
            i.saleItemGuid = UUID.randomUUID().toString();
            i.orderGuid = returnOrder.guid;
            i.qty = CalculationUtil.negativeQty(i.qty);

            operations.add(
                    ContentProviderOperation.newInsert(URI_SALE_ITEMS)
                            .withValues(i.toValues())
                            .build());
        }

        List<Unit> units = (List<Unit>) getArgs().getSerializable(ARG_UNIT);

        if (units != null && units.size() > 0) {
            editUnitResults = new ArrayList<>();
            for (Unit unit: units) {
                unit.childOrderId = returnOrder.guid;
                SyncResult subResult = new EditUnitCommand().sync(getContext(), unit, getAppCommandContext());
                if (subResult == null)
                    return failed();
                editUnitResults.add(subResult);
            }
        }

        if (!addMovements())
            return failed();

        return succeeded();
    }

    private boolean addMovements() {
        HashMap<String, BigDecimal> saleItems = new HashMap<>(returnItems.size());
        HashSet<String> items = new HashSet<>();
        for (SaleOrderItemModel item : returnItems) {
            saleItems.put(item.saleItemGuid, CalculationUtil.negativeQty(item.qty));//item.qty is negative, we need to write positive value for return
            items.add(item.itemGuid);
        }

        ArrayList<ItemMovementModel> itemMovements = new ArrayList<>();

        MovementUtils.processAllRefund(
                getContext(),
                getAppCommandContext(),
                returnOrder.parentGuid,
                itemMovements);

        if (itemMovements.isEmpty()) {
            return true;
        }

        addMovementsResult = new AddItemsMovementCommand().syncNow(getContext(), itemMovements, getAppCommandContext());
        if (addMovementsResult == null)
            return false;

        return true;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        if (addMovementsResult != null && addMovementsResult.getLocalDbOperations() != null)
            operations.addAll(addMovementsResult.getLocalDbOperations());
        if (editUnitResults != null) {
            for (SyncResult subResult : editUnitResults) {
                if (subResult.getLocalDbOperations() != null)
                    operations.addAll(subResult.getLocalDbOperations());
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
                .arg(ARG_UNIT, new ArrayList<>(units))
                .callback(callback)
                .queueUsing(context);
    }
}
