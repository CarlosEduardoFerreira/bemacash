package com.kaching123.tcr.commands.payment.blackstone.prepaid.wireless;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.payment.SOAPWebCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.BillPaymentDescriptionModel;
import com.kaching123.tcr.model.BillPaymentDescriptionModel.PrepaidType;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.payment.blackstone.prepaid.TransactionMode;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.ResponseCode;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.VoidOrderRequest;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.PrepaidOrderView2;
import com.kaching123.tcr.store.ShopSchema2.PrepaidOrderView2.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.PrepaidOrderView;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.PIN;

import java.util.ArrayList;

/**
 * Created by pkabakov on 11.04.2014.
 */
public class VoidOrderCommand extends SOAPWebCommand<VoidOrderRequest> {

    private static final Uri URI_PREPAID_ORDER = ShopProvider.getContentUri(PrepaidOrderView.URI_CONTENT);
    private static final Uri URI_BILL_PAYMENT_DESCRIPTION = ShopProvider.getContentUri(ShopStore.BillPaymentDescriptionTable.URI_CONTENT);

    protected final static String ARG_ORDER_GUID = "arg_order_guid";

    private String orderGuid;

    private String prepaidOrderGuid;

    private boolean ignore = false;

    public SyncResult sync(Context context, String orderGuid, IAppCommandContext appCommandContext) {
        this.orderGuid = orderGuid;
        return syncDependent(context, null, appCommandContext);
    }

    @Override
    protected boolean allowRetries() {
        return false;
    }

    @Override
    protected Long doCommand(Broker brokerApi, VoidOrderRequest request) {
        if (request == null) {
            return ignore ? ResponseCode.TRANSACTION_APPROVED.getId() : null;
        }

        PIN response = brokerApi.VoidOrder(
                request.mID,
                request.tID,
                request.password,
                request.cashier,
                request.orderID,
                request.transactionMode,
                getSign(request.mID,
                        request.tID,
                        request.password,
                        null,
                        request.orderID)
        );
        return response != null ? (long) response.errorCode : null;
    }

    @Override
    protected VoidOrderRequest getRequest() {
        Cursor cursor = null;
        try {
            cursor = ProviderAction.query(URI_PREPAID_ORDER)
                    .projection(
                            PrepaidOrderView2.SaleOrderTable.ORDER_TYPE,
                            PrepaidOrderView2.BillPaymentDescriptionTable.TYPE,
                            BillPaymentDescriptionTable.IS_VOIDED,
                            PrepaidOrderView2.BillPaymentDescriptionTable.GUID,
                            PrepaidOrderView2.BillPaymentDescriptionTable.ORDER_ID,
                            BillPaymentDescriptionTable.IS_FAILED)
                    .where(PrepaidOrderView2.SaleOrderTable.GUID + " = ?", orderGuid)
                    .perform(getContext());
            if (!cursor.moveToFirst()) {
                return null;
            }

            OrderType orderType = ContentValuesUtil._orderType(cursor, 0);
            if (orderType != OrderType.PREPAID) {
                ignore = true;
                return null;
            }

            PrepaidType prepaidType = ContentValuesUtil._prepaidType(cursor, 1);
            boolean isFailed = ContentValuesUtil._bool(cursor, 5);
            if (prepaidType == null)
                return null;
            if (prepaidType != PrepaidType.WIRELESS_PIN && !isFailed) {
                ignore = true;
                return null;
            }

            boolean isVoided = ContentValuesUtil._bool(cursor,2);
            if (isVoided) {
                ignore = true;
                return null;
            }

            prepaidOrderGuid = cursor.getString(3);

            long prepaidOrderId = cursor.getLong(4);

            VoidOrderRequest request = new VoidOrderRequest();
            request.mID = String.valueOf(getAppCommandContext().getPrepaidUser().getMid());
            request.tID = String.valueOf(getAppCommandContext().getPrepaidUser().getTid());
            request.password = getAppCommandContext().getPrepaidUser().getPassword();
            request.cashier = getAppCommandContext().getEmployeeLogin();
            request.transactionMode = getApp().isTrainingMode() ? TransactionMode.getTransactionMode(true) : getApp().getShopInfo().prepaidTransactionMode;
            request.orderID = prepaidOrderId;

            return request;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        if (ignore)
            return null;

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(URI_BILL_PAYMENT_DESCRIPTION)
                .withValue(ShopStore.BillPaymentDescriptionTable.IS_VOIDED, true)
                .withSelection(ShopStore.BillPaymentDescriptionTable.GUID + " = ?", new String[]{prepaidOrderGuid})
                .build());
        return operations;
    }

    @Override
    protected boolean validateAppCommandContext() {
        return true;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        if (ignore)
            return null;

        BillPaymentDescriptionModel model = new BillPaymentDescriptionModel(prepaidOrderGuid, null, null, 0L, true, false);
        return JdbcFactory.getConverter(model).updateSQL(model, getAppCommandContext());
    }
}
