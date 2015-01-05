package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.saleorder.SplitSaleItemCommand.SplitSaleItemResult;
import com.kaching123.tcr.commands.store.user.AddCommissionsCommand;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler2;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.SaleItemInfo;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemJdbcConverter;
import com.kaching123.tcr.jdbc.converters.UnitsJdbcConverter;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.Unit.Status;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.kaching123.tcr.util.CalculationUtil;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by gdubina on 11/11/13.
 */
public class SuccessOrderCommand extends UpdateSaleOrderCommand {

    private static final Uri URI_SALE_ITEM_NO_NOTIFY = ShopProvider.getNoNotifyContentUri(ShopStore.SaleItemTable.URI_CONTENT);
    private static final Uri URI_ITEM = ShopProvider.getContentUri(ShopStore.ItemTable.URI_CONTENT);
    private static final Uri URI_UNIT = ShopProvider.getContentUri(ShopStore.UnitTable.URI_CONTENT);

    private static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    private static final String ARG_IS_MANUAL_RETURN_ORDER = "ARG_IS_MANUAL_RETURN_ORDER";
    private static final String ARG_SALESMAN_GUIDS = "ARG_SALESMAN_GUIDS";

    private ArrayList<SaleOrderItemModel> itemsModels;
    private ArrayList<SyncResult> splitItemsResults;
    private SyncResult addCommissionsResult;
    private SyncResult addItemsMovementResult;

    public static void start(Context context, String orderGuid) {
        start(context, orderGuid, null);
    }

    public static void start(Context context, String orderGuid, String[] salesmanGuids) {
        create(SuccessOrderCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_SALESMAN_GUIDS, salesmanGuids)
                .queueUsing(context);
    }

    public static void start(Context context, String orderGuid, boolean isManualReturn, BaseSuccessOrderCommandCallback callback) {
        create(SuccessOrderCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_IS_MANUAL_RETURN_ORDER, isManualReturn)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected SaleOrderModel readOrder() {
        String guid = getStringArg(ARG_ORDER_GUID);

        Cursor c = ProviderAction.query(URI_ORDER)
                .where(ShopStore.SaleOrderTable.GUID + " = ?", guid)
                .perform(getContext());
        SaleOrderModel order = null;
        if (c.moveToFirst()) {
            order = new SaleOrderModel(c);
        }
        c.close();
        order.createTime = new Date();
        String currentShiftGuid = getAppCommandContext().getShiftGuid();
        if (currentShiftGuid != null) {
            order.shiftGuid = currentShiftGuid;
        }
        boolean isManualReturn = getBooleanArg(ARG_IS_MANUAL_RETURN_ORDER, false);
        if (!isManualReturn) {
            order.orderStatus = OrderStatus.COMPLETED;
        }
        return order;
    }

    @Override
    protected TaskResult doCommand() {
        TaskResult result = super.doCommand();
        if (isFailed(result)) {
            return result;
        }

        if (!recalcFinalFields(getContext(), order.guid))
            return failed();

        final boolean isManualReturnOrder = getBooleanArg(ARG_IS_MANUAL_RETURN_ORDER, false);
        if (isManualReturnOrder) {
            return result;
        }

        if (!applyCommissions())
            return failed();

        if (!updateItemMovement()) {
            return failed();
        }

        return result;
    }

    private boolean recalcFinalFields(final Context context, final String orderGuid) {
        final ArrayList<SaleOrderItemModel> itemsModels = new ArrayList<SaleOrderItemModel>();
        splitItemsResults = new ArrayList<SyncResult>();

        try {
            OrderTotalPriceCursorQuery.loadSync(context, orderGuid, new Handler2() {

                @Override
                public void splitItem(SaleItemInfo item) {
                    SplitSaleItemResult subResult = new SplitSaleItemCommand().sync(context, item.saleItemGuid, item.qty, getAppCommandContext());
                    if (subResult == null) {
                        throw new IllegalStateException("failed to split sale item while calculating final fields!");
                    }
                    item.saleItemGuid = subResult.getNewSaleItemGuid();
                    splitItemsResults.add(subResult);
                }

                @Override
                public void handleItem(SaleItemInfo i, BigDecimal itemFinalPrice, BigDecimal itemFinalDiscount, BigDecimal itemFinalTax) {
                    String saleItemGuid = i.saleItemGuid;

                    SaleOrderItemModel item = new SaleOrderItemModel(saleItemGuid);
                    item.finalGrossPrice = itemFinalPrice.add(itemFinalDiscount).subtract(itemFinalTax);
                    item.finalTax = itemFinalTax;
                    item.finalDiscount = itemFinalDiscount;
                    item.itemGuid = i.itemGiud;
                    item.qty = i.qty;
                    itemsModels.add(item);
                }

            });
        } catch (Exception e) {
            Logger.e("SuccessOrderCommand.recalcFinalFields(): failed!", e);
            return false;
        }
        this.itemsModels = itemsModels;

        return true;
    }

    private boolean applyCommissions() {
        String[] salesmanGuids = getArgs().getStringArray(ARG_SALESMAN_GUIDS);
        if (salesmanGuids == null || salesmanGuids.length == 0)
            return true;

        addCommissionsResult = new AddCommissionsCommand().sync(getContext(), salesmanGuids, itemsModels, order.guid, getAppCommandContext());
        return addCommissionsResult != null;
    }

    private boolean updateItemMovement() {
        HashSet<String> itemGuids = new HashSet<String>();
        for (SaleOrderItemModel saleItem : itemsModels) {
            itemGuids.add(saleItem.itemGuid);
        }

        Cursor cursor = ProviderAction.query(URI_ITEM)
                .projection(
                        ShopStore.ItemTable.GUID,
                        ShopStore.ItemTable.UPDATE_QTY_FLAG,
                        ShopStore.ItemTable.STOCK_TRACKING
                )
                .whereIn(ShopStore.ItemTable.GUID, itemGuids)
                .perform(getContext());

        ArrayList<ItemMovementModel> itemMovements = new ArrayList<ItemMovementModel>();
        while (cursor.moveToNext()) {
            String itemGuid = cursor.getString(0);
            String flag = cursor.getString(1);
            boolean stockTracking = _bool(cursor, 2);
            if (!stockTracking) {
                continue;
            }
            for (SaleOrderItemModel saleItem : itemsModels) {
                if (!saleItem.itemGuid.equals(itemGuid))
                    continue;
                itemMovements.add(new ItemMovementModel(itemGuid, flag, CalculationUtil.negativeQty(saleItem.qty), false, new Date()));
            }
        }
        cursor.close();

        if (itemMovements.isEmpty())
            return true;

        addItemsMovementResult = new AddItemsMovementCommand().syncNow(getContext(), itemMovements, getAppCommandContext());
        return addItemsMovementResult != null;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = super.createDbOperations();

        for (SyncResult subResult : splitItemsResults) {
            if (subResult.getLocalDbOperations() != null)
                operations.addAll(subResult.getLocalDbOperations());
        }

        for (SaleOrderItemModel item : itemsModels) {
            operations.add(
                    ContentProviderOperation.newUpdate(URI_SALE_ITEM_NO_NOTIFY)
                            .withValues(item.updateFinalFields())
                            .withSelection(ShopStore.SaleItemTable.SALE_ITEM_GUID + " = ?", new String[]{item.saleItemGuid})
                            .build()
            );
        }

        if (addCommissionsResult != null && addCommissionsResult.getLocalDbOperations() != null)
            operations.addAll(addCommissionsResult.getLocalDbOperations());

        if (addItemsMovementResult != null && addItemsMovementResult.getLocalDbOperations() != null)
            operations.addAll(addItemsMovementResult.getLocalDbOperations());

        operations.add(ContentProviderOperation.newUpdate(URI_UNIT)
                .withValue(UnitTable.STATUS, _enum(Status.SOLD))
                .withSelection(UnitTable.SALE_ORDER_ID + " = ? AND " + UnitTable.STATUS + " <> ?", new String[]{order.guid, String.valueOf(Unit.Status.SOLD.ordinal())})
                .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchUpdate(SaleOrderModel.class).add(super.createSqlCommand());

        SaleOrderItemJdbcConverter itemConverter = (SaleOrderItemJdbcConverter) JdbcFactory.getConverter(ShopStore.SaleItemTable.TABLE_NAME);

        for (SyncResult subResult : splitItemsResults) {
            batch.add(subResult.getSqlCmd());
        }

        for (SaleOrderItemModel item : itemsModels) {
            batch.add(itemConverter.updateFinalPrices(item, getAppCommandContext()));
        }

        UnitsJdbcConverter unitConverter = (UnitsJdbcConverter)JdbcFactory.getConverter(UnitTable.TABLE_NAME);
        batch.add(unitConverter.setSold(order.getGuid(), getAppCommandContext()));

        if (addCommissionsResult != null)
            batch.add(addCommissionsResult.getSqlCmd());

        if (addItemsMovementResult != null)
            batch.add(addItemsMovementResult.getSqlCmd());

        return batch;
    }

    public static abstract class BaseSuccessOrderCommandCallback {

        @OnSuccess(SuccessOrderCommand.class)
        public final void onSuccess() {
            handleSuccess();
        }

        protected abstract void handleSuccess();

        @OnFailure(SuccessOrderCommand.class)
        public final void onFailure() {
            handleFailure();
        }

        protected abstract void handleFailure();
    }
}
