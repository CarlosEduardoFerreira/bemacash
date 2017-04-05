package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.loyalty.AddLoyaltyPointsMovementCommand;
import com.kaching123.tcr.commands.loyalty.DeleteOrderIncentivesCommand;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.user.UpdateCustomerBirthdayRewardDateCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrdersJdbcConverter;
import com.kaching123.tcr.jdbc.converters.UnitsJdbcConverter;
import com.kaching123.tcr.model.BillPaymentDescriptionModel;
import com.kaching123.tcr.model.LoyaltyType;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopStore.SaleIncentiveTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._orderStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._sum;

public class RemoveSaleOrderCommand extends AsyncCommand {

    private static final Uri URI_SALE_ITEMS = ShopProvider.getContentUri(SaleItemTable.URI_CONTENT);
    private static final Uri URI_UNIT = ShopProvider.getContentUri(ShopStore.UnitTable.URI_CONTENT);
    private static final Uri URI_ORDER = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);
    private static final Uri URI_DEFINED_ON_HOLD = ShopProvider.getContentUri(ShopStore.DefinedOnHoldTable.URI_CONTENT);
    private static final Uri URI_BILL_PAYMENT = ShopProvider.getContentUri(BillPaymentDescriptionTable.URI_CONTENT);

    private static final String ARG_ORDER_GUID = "arg_order_guid";
    private static final String ARG_ORDER_TYPE = "arg_order_type";
    private static final String ARG_ORDER_SKIP_PRINT = "arg_order_skip_print";

    private String orderId;
    private String orderName;
    private OrderStatus orderStatus;
    private OrderType orderType;
    private String prepaidOrderGuid;
    private boolean skipPrint;

    private SyncResult[] removeSaleOrderItemResults;
    private SyncResult removeSaleIncentivesResult;
    private SyncResult addLoyaltyPointsResult;

    @Override
    protected TaskResult doCommand() {
        orderId = getArgs().getString(ARG_ORDER_GUID);
        skipPrint = getArgs().getBoolean(ARG_ORDER_SKIP_PRINT, false);
        orderType = (OrderType) getArgs().getSerializable(ARG_ORDER_TYPE);

        loadOrderInfo();

        if (!skipPrint) {
            TaskResult taskResult = new PrintItemsForKitchenCommand().sync(getContext(), true, false, orderId, null, true, true, orderName, true, getAppCommandContext());
            if (isFailed(taskResult)) {
                return taskResult;
            }
        }

        if (orderStatus == OrderStatus.HOLDON) {
            TaskResult result = new UpdateSaleOrderItemMovementsCommand().startSync(getContext(), orderId, true, getAppCommandContext());
            if (isFailed(result)) {
                return failed();
            }
        }

        if (!removeItems()) {
            return failed();
        }

        if (!resetCustomerBirthdayRewardDate())
            return failed();

        if (!removeSaleIncentives()) {
            return failed();
        }

        if (!returnLoyaltyPoints()) {
            return failed();
        }

        return succeeded();
    }

    private void loadOrderInfo(){
        Cursor orderCursor = ProviderAction.query(URI_ORDER)
                .projection(SaleOrderTable.DEFINED_ON_HOLD_ID, SaleOrderTable.HOLD_NAME, SaleOrderTable.STATUS)
                .where(SaleOrderTable.GUID + " = ?", orderId)
                .perform(getContext());
        try {
            if (orderCursor.moveToFirst()) {
                String definedOnHoldGuid = orderCursor.getString(0);
                orderName = orderCursor.getString(1);
                orderStatus =  _orderStatus(orderCursor, orderCursor.getColumnIndex(ShopStore.SaleOrderTable.STATUS));
                if (!TextUtils.isEmpty(definedOnHoldGuid)) {
                    loadDefinedOnHoldName(definedOnHoldGuid);
                }
            }
        } finally {
            orderCursor.close();
        }
    }

    private void loadDefinedOnHoldName(String definedOnHoldGuid) {
        Cursor definedOnHoldCursor = ProviderAction.query(URI_DEFINED_ON_HOLD)
                .projection(ShopStore.DefinedOnHoldTable.NAME)
                .where(ShopStore.DefinedOnHoldTable.ID + " = ?", definedOnHoldGuid)
                .perform(getContext());
        try {
            if (definedOnHoldCursor.moveToFirst()) {
                orderName = definedOnHoldCursor.getString(0);
            }
        } finally {
            definedOnHoldCursor.close();
        }
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
                SyncResult subResult = new RemoveSaleOrderItemCommand().sync(getContext(), saleItemGuid, RemoveSaleOrderItemCommand.ActionType.VOID, getAppCommandContext());
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

    private boolean removeSaleIncentives(){
        removeSaleIncentivesResult = new DeleteOrderIncentivesCommand().sync(getContext(), orderId, getAppCommandContext());
        return removeSaleIncentivesResult != null;
    }

    private boolean returnLoyaltyPoints(){
        Cursor c = ProviderAction.query(ShopProvider.contentUriGroupBy(SaleIncentiveTable.URI_CONTENT, SaleIncentiveTable.ORDER_ID))
                .projection(SaleIncentiveTable.CUSTOMER_ID, _sum(SaleIncentiveTable.POINT_THRESHOLD))
                .where(SaleIncentiveTable.ORDER_ID + " = ?", orderId)
                .perform(getContext());

        try{
            if (c.moveToFirst()){
                String customerId = c.getString(0);
                BigDecimal points = _decimal(c, 1, BigDecimal.ZERO);
                addLoyaltyPointsResult = new AddLoyaltyPointsMovementCommand().sync(getContext(), customerId, points, getAppCommandContext());
                return addLoyaltyPointsResult != null;
            }else{
                return true;
            }
        }finally {
            c.close();
        }
    }

    private boolean resetCustomerBirthdayRewardDate() {
        Cursor c = ProviderAction.query(ShopProvider.contentUri(SaleIncentiveTable.URI_CONTENT))
                .projection(SaleIncentiveTable.CUSTOMER_ID, SaleIncentiveTable.TYPE)
                .where(SaleIncentiveTable.ORDER_ID + " = ?", orderId)
                .perform(getContext());

        String customerId = null;
        while (c.moveToNext()){
            if (LoyaltyType.valueOf(c.getInt(1)) == LoyaltyType.BIRTHDAY){
                customerId = c.getString(0);
                break;
            }
        }
        c.close();

        if (customerId == null)
            return true;
        else
            return new UpdateCustomerBirthdayRewardDateCommand().sync(getContext(), customerId, null, getAppCommandContext());

    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newUpdate(URI_UNIT)
                .withValue(UnitTable.SALE_ORDER_ID, null)
                .withSelection(UnitTable.SALE_ORDER_ID + " = ?", new String[]{orderId})
                .build());

        for (SyncResult subResult: removeSaleOrderItemResults) {
            if (subResult.getLocalDbOperations() != null)
                operations.addAll(subResult.getLocalDbOperations());
        }

        if (removeSaleIncentivesResult != null){
            operations.addAll(removeSaleIncentivesResult.getLocalDbOperations());
        }

        if (addLoyaltyPointsResult != null){
            operations.addAll(addLoyaltyPointsResult.getLocalDbOperations());
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

        if (removeSaleIncentivesResult != null){
            batchSqlCommand.add(removeSaleIncentivesResult.getSqlCmd());
        }

        if (addLoyaltyPointsResult != null){
            batchSqlCommand.add(addLoyaltyPointsResult.getSqlCmd());
        }

        if (prepaidOrderGuid != null) {
            BillPaymentDescriptionModel prepaidOrderModel = new BillPaymentDescriptionModel(prepaidOrderGuid);
            batchSqlCommand.add(JdbcFactory.getConverter(prepaidOrderModel).deleteSQL(prepaidOrderModel, getAppCommandContext()));
        }

        SaleOrderModel model = new SaleOrderModel(orderId);
        Log.d("BemaCarl6","RemoveSaleOrderCommand.createSqlCommand.model.orderStatus1" + model.orderStatus);
        model.orderStatus = OrderStatus.CANCELED;
        SaleOrdersJdbcConverter converter = (SaleOrdersJdbcConverter) JdbcFactory.getConverter(model);
        batchSqlCommand.add(converter.deleteUpdateStatus(model, getAppCommandContext()));

        Log.d("BemaCarl6","RemoveSaleOrderCommand.createSqlCommand.model.orderStatus2" + model.orderStatus);

        new AtomicUpload().upload(batchSqlCommand, AtomicUpload.UploadType.WEB);

        return batchSqlCommand;
    }

    public static void start(Context context, Object callback, String orderGuid, OrderType orderType) {
        start(context, callback, orderGuid, orderType, false);
    }

    public static void start(Context context, Object callback, String orderGuid) {
        start(context, callback, orderGuid, OrderType.SALE);
    }

    public static void start(Context context, Object callback, String orderGuid, boolean skipPrint) {
        start(context, callback, orderGuid, OrderType.SALE, skipPrint);
    }

    public static void start(Context context, Object callback, String orderGuid, OrderType orderType, boolean skipPrint) {
        create(RemoveSaleOrderCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_ORDER_TYPE, orderType)
                .arg(ARG_ORDER_SKIP_PRINT, skipPrint)
                .callback(callback)
                .queueUsing(context);
    }
}
