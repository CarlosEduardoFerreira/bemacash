package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrdersJdbcConverter;
import com.kaching123.tcr.jdbc.converters.UnitsJdbcConverter;
import com.kaching123.tcr.model.BillPaymentDescriptionModel;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

public class RemoveSaleOrderCommand extends AsyncCommand {

    private static final Uri URI_SALE_ITEMS = ShopProvider.getContentUri(SaleItemTable.URI_CONTENT);
    private static final Uri URI_UNIT = ShopProvider.getContentUri(ShopStore.UnitTable.URI_CONTENT);
    private static final Uri URI_ORDER = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);
    private static final Uri URI_BILL_PAYMENT = ShopProvider.getContentUri(BillPaymentDescriptionTable.URI_CONTENT);

    private static final String ARG_ORDER_GUID = "arg_order_guid";
    private static final String ARG_ORDER_TYPE = "arg_order_type";

    private String orderId;
    private OrderType orderType;
    private String prepaidOrderGuid;

    private SyncResult[] removeSaleOrderItemResults;
    private SyncResult returnLoyaltyPointsResult;

    @Override
    protected TaskResult doCommand() {
        orderId = getArgs().getString(ARG_ORDER_GUID);
        orderType = (OrderType) getArgs().getSerializable(ARG_ORDER_TYPE);

        if (!removeItems())
            return failed();

        if (!returnLoyaltyPoints())
            return failed();

        return succeeded();
    }

    private boolean removeItems() {
        Cursor c = ProviderAction.query(URI_SALE_ITEMS)
                .projection(SaleItemTable.SALE_ITEM_GUID, SaleItemTable.ITEM_GUID)
                .where(SaleItemTable.ORDER_GUID + " = ?", orderId)
                .perform(getContext());
        try {
            removeSaleOrderItemResults = new SyncResult[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                String saleItemGuid = c.getString(0);
                SyncResult subResult = new RemoveSaleOrderItemCommand().sync(getContext(), saleItemGuid, getAppCommandContext());
                if (subResult == null)
                    return false;
                removeSaleOrderItemResults[i++] = subResult;
            }

            if (orderType == OrderType.PREPAID && c.moveToFirst()) {
                prepaidOrderGuid = c.getString(1);
            }

            return true;
        } finally {
            c.close();
        }
    }

    private boolean returnLoyaltyPoints(){
        return true;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
//        if(order != null && order.orderStatus == OrderStatus.ACTIVE){
//            return operations;
//        }

        operations.add(ContentProviderOperation.newUpdate(URI_UNIT)
                .withValue(UnitTable.SALE_ORDER_ID, null)
                .withSelection(UnitTable.SALE_ORDER_ID + " = ?", new String[]{orderId})
                .build());

        for (SyncResult subResult: removeSaleOrderItemResults) {
            if (subResult.getLocalDbOperations() != null)
                operations.addAll(subResult.getLocalDbOperations());
        }

        if (returnLoyaltyPointsResult.getLocalDbOperations() != null){
            operations.addAll(returnLoyaltyPointsResult.getLocalDbOperations());
        }

        if (prepaidOrderGuid != null) {
            operations.add(ContentProviderOperation.newUpdate(URI_BILL_PAYMENT)
                    .withValues(ShopStore.DELETE_VALUES)
                    .withSelection(BillPaymentDescriptionTable.GUID + " = ?", new String[]{prepaidOrderGuid})
                    .build());
        }

        ContentValues values = new ContentValues();
        values.putAll(ShopStore.DELETE_VALUES);
        values.put(SaleOrderTable.STATUS, OrderStatus.CANCELED.ordinal());
        operations.add(ContentProviderOperation.newUpdate(URI_ORDER)
                .withValues(values)
                .withSelection(SaleOrderTable.GUID + " = ?", new String[]{orderId})
                .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batchSqlCommand = batchDelete(SaleOrderModel.class);

        UnitsJdbcConverter unitConverter = (UnitsJdbcConverter)JdbcFactory.getConverter(UnitTable.TABLE_NAME);
        batchSqlCommand.add(unitConverter.removeFromOrder(orderId, getAppCommandContext()));

        for (SyncResult subResult: removeSaleOrderItemResults) {
            batchSqlCommand.add(subResult.getSqlCmd());
        }

        if (returnLoyaltyPointsResult.getSqlCmd() != null){
            batchSqlCommand.add(returnLoyaltyPointsResult.getSqlCmd());
        }

        if (prepaidOrderGuid != null) {
            BillPaymentDescriptionModel prepaidOrderModel = new BillPaymentDescriptionModel(prepaidOrderGuid);
            batchSqlCommand.add(JdbcFactory.getConverter(prepaidOrderModel).deleteSQL(prepaidOrderModel, getAppCommandContext()));
        }

        SaleOrderModel model = new SaleOrderModel(orderId);
        model.orderStatus = OrderStatus.CANCELED;
        SaleOrdersJdbcConverter converter = (SaleOrdersJdbcConverter) JdbcFactory.getConverter(model);
        batchSqlCommand.add(converter.deleteUpdateStatus(model, getAppCommandContext()));

        return batchSqlCommand;
    }

    public static void start(Context context, Object callback, String orderGuid) {
        start(context, callback, orderGuid, OrderType.SALE);
    }

    public static void start(Context context, Object callback, String orderGuid, OrderType orderType) {
        create(RemoveSaleOrderCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_ORDER_TYPE, orderType)
                .callback(callback)
                .queueUsing(context);
    }
}
