package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.rest.sync.GetPrepaidOrderIdResponse;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.BillPaymentDescriptionJdbcConverter;
import com.kaching123.tcr.model.BillPaymentDescriptionModel;
import com.kaching123.tcr.model.BillPaymentDescriptionModel.PrepaidType;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.prepaid.Broker;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by gdubina on 06/02/14.
 */
public class AddBillPaymentOrderCommand extends AsyncCommand {

    private static final Uri URI_BILL_DESC = ShopProvider.getContentUri(BillPaymentDescriptionTable.URI_CONTENT);
    private static final Uri URI_ORDER = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);
    private static final Uri URI_ITEM = ShopProvider.getContentUri(SaleItemTable.URI_CONTENT);

    private static final String ARG_AMOUNT = "ARG_AMOUNT";
    private static final String ARG_IS_FIXED = "ARG_IS_FIXED";

    private static final String ARG_PREPAID_DESC = "ARG_PREPAID_DESC";
    private static final String ARG_PREPAID_TYPE = "ARG_PREPAID_TYPE";
    private static final String ARG_BROKER = "ARG_BROKER";
    private static final String ARG_TRANSACTION_FEE = "ARG_TRANSACTION_FEE";

    private static final String EXTRA_ORDER_GUID = "EXTRA_ORDER_GUID";
    private static final String EXTRA_PREPAID_ORDER_ID = "EXTRA_PREPAID_ORDER_ID";

    private BillPaymentDescriptionModel prepaidModel;
    private SaleOrderModel orderModel;
    private SaleOrderItemModel itemModel;

    @Override
    protected TaskResult doCommand() {
        boolean isFixed = getBooleanArg(ARG_IS_FIXED);
        BigDecimal amount = (BigDecimal) getArgs().getSerializable(ARG_AMOUNT);
        String prepaidDescription = getStringArg(ARG_PREPAID_DESC);
        PrepaidType prepaidType = (PrepaidType) getArgs().getSerializable(ARG_PREPAID_TYPE);
        Broker broker = (Broker) getArgs().getSerializable(ARG_BROKER);

        String prepaidGuid = UUID.randomUUID().toString();

        Long prepaidOrderId = getPrepaidOrderId(prepaidGuid, prepaidDescription, prepaidType);
        if (prepaidOrderId == null) {
            return failed();
        }

        orderModel = createSaleOrder((BigDecimal) getArgs().getSerializable(ARG_TRANSACTION_FEE));

        prepaidModel = new BillPaymentDescriptionModel(prepaidGuid, prepaidDescription, prepaidType, prepaidOrderId, orderModel.guid);

        itemModel = new SaleOrderItemModel(
                UUID.randomUUID().toString(),
                orderModel.guid,
                prepaidGuid,
                BigDecimal.ONE,
                BigDecimal.ZERO,
                isFixed ? PriceType.FIXED : PriceType.OPEN,
                amount,
                false,
                null,
                null,
                true,
                getApp().getPrepaidTax(broker),
                null,
                System.currentTimeMillis(),
                null,
                amount,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null,
                false,
                true,
                null);

        return succeeded().add(EXTRA_ORDER_GUID, orderModel.guid).add(EXTRA_PREPAID_ORDER_ID, prepaidOrderId);
    }

    private Long getPrepaidOrderId(String guid, String description, PrepaidType type) {
        if (getApp().isTrainingMode()) {
            return getPrepaidOrderIdLocal();
        }

        try {
            JSONObject req = getPrepaidOrderIdRequest(getApp(), description, type, guid);
            Logger.d("AddBillPaymentOrderCommand.getPrepaidOrderId(): req: " + req.toString());
            SyncApi api = getApp().getRestAdapter().create(SyncApi.class);
            GetPrepaidOrderIdResponse resp = api.getPrepaidOrderId(getApp().emailApiKey, SyncUploadRequestBuilder.getReqCredentials(getApp().getOperator(), getApp()), req);
            Logger.d("AddBillPaymentOrderCommand.getPrepaidOrderId() resp: " + resp);
            if (resp == null || !resp.isSuccess()) {
                Logger.e("AddBillPaymentOrderCommand.getPrepaidOrderId() failed, resp: " + resp);
                return null;
            }
            Long orderId = resp.getOrderId();
            Logger.d("AddBillPaymentOrderCommand.getPrepaidOrderId(): orderId: " + orderId);
            return orderId;
        } catch (Exception e) {
            Logger.e("AddBillPaymentOrderCommand.getPrepaidOrderId()", e);
        }
        return null;
    }

    public static JSONObject getPrepaidOrderIdRequest(TcrApplication app, String description, PrepaidType type, String guid) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("shop_id", app.getShopId());
        request.put("description", description);
        request.put("type", type);
        request.put("guid", guid);
        return request;
    }

    private Long getPrepaidOrderIdLocal() {
        Long orderId = _wrap(ProviderAction
                        .query(URI_BILL_DESC)
                        .projection("max(" + BillPaymentDescriptionTable.PREPAID_ORDER_ID + ")")
                        .perform(getContext()),
                new Function<Cursor, Long>() {
                    @Override
                    public Long apply(Cursor cursor) {
                        if (cursor.moveToFirst()) {
                            return cursor.getLong(0) + 1L;
                        }
                        return 1L;
                    }
                }
        );
        return orderId;
    }

    private SaleOrderModel createSaleOrder(BigDecimal transactionFee) {
        return AddSaleOrderCommand.createSaleOrder(getContext(), getAppCommandContext().getRegisterId(), getAppCommandContext().getEmployeeGuid(), getAppCommandContext().getShiftGuid(), null, OrderType.PREPAID, transactionFee);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newInsert(URI_ORDER)
                .withValues(orderModel.toValues())
                .build());

        operations.add(ContentProviderOperation.newInsert(URI_BILL_DESC)
                .withValues(prepaidModel.toValues())
                .build());

        operations.add(ContentProviderOperation.newInsert(URI_ITEM)
                .withValues(itemModel.toValues())
                .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return batchInsert(orderModel)
                .add(JdbcFactory.getConverter(orderModel).insertSQL(orderModel, getAppCommandContext()))
                .add(((BillPaymentDescriptionJdbcConverter) JdbcFactory.getConverter(prepaidModel)).updateOrderIdSQL(prepaidModel, getAppCommandContext()))
                .add(JdbcFactory.getConverter(itemModel).insertSQL(itemModel, getAppCommandContext()));
    }

    public static void start(Context context, boolean isFixed, BigDecimal amount, String prepaidDescription, PrepaidType prepaidType, Broker broker, BigDecimal transactionFee, BaseAddBillPaymentOrderCallback callback) {
        create(AddBillPaymentOrderCommand.class)
                .arg(ARG_IS_FIXED, isFixed)
                .arg(ARG_AMOUNT, amount)
                .arg(ARG_PREPAID_DESC, prepaidDescription)
                .arg(ARG_PREPAID_TYPE, prepaidType)
                .arg(ARG_BROKER, broker)
                .arg(ARG_TRANSACTION_FEE, transactionFee)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseAddBillPaymentOrderCallback {

        @OnSuccess(AddBillPaymentOrderCommand.class)
        public void onSuccess(@Param(EXTRA_ORDER_GUID) String orderGuid, @Param(EXTRA_PREPAID_ORDER_ID) long prepaidOrderId) {
            handleSuccess(orderGuid, prepaidOrderId);
        }

        @OnFailure(AddBillPaymentOrderCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleSuccess(String orderGuid, long prepaidOrderId);

        protected abstract void handleFailure();

    }
}
