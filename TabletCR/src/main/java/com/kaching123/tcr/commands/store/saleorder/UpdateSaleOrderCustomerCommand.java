package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrdersJdbcConverter;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnCallback;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by pkabakov on 11.02.14.
 */
public class UpdateSaleOrderCustomerCommand extends AsyncCommand {

    private static final Uri URI_ORDER = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);

    private static final String ARG_SALE_ORDER_GUID = "arg_sale_order_guid";
    private static final String ARG_CUSTOMER_GUID = "arg_customer_guid";
    private static final String ARG_IS_GIFT_CARD = "ARG_IS_GIFT_CARD";
    private static final String ARG_AMOUNT = "ARG_AMOUNT";
    private static final String RESULT_IS_GIFT_CARD = "RESULT_IS_GIFT_CARD";
    private static final String RESULT_AMOUNT = "RESULT_AMOUNT";
    private static final String EXTRA_ORDER_GUID = "extra_order_guid";
    private static final String CALLBACK_ADD_ORDER = "callback_add_order";

    private String saleOrderGuid;
    private String customerGuid;

    @Override
    protected TaskResult doCommand() {
        saleOrderGuid = getStringArg(ARG_SALE_ORDER_GUID);
        customerGuid = getStringArg(ARG_CUSTOMER_GUID);

        if (saleOrderGuid == null){
            SaleOrderModel order = createSaleOrder();
            if (!new AddSaleOrderCommand().sync(getContext(), order, true, getAppCommandContext()))
                return failed();
            saleOrderGuid = order.guid;
            fireAddOrderEvent(saleOrderGuid);
            return succeeded();
        }

        return succeeded().add(RESULT_IS_GIFT_CARD, getArgs().getBoolean(ARG_IS_GIFT_CARD)).add(RESULT_AMOUNT, getArgs().getStringArrayList(ARG_AMOUNT) == null ? BigDecimal.ZERO.toString() : getArgs().getStringArrayList(ARG_AMOUNT));
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(URI_ORDER)
                .withSelection(SaleOrderTable.GUID + " = ?", new String[]{saleOrderGuid})
                .withValue(SaleOrderTable.CUSTOMER_GUID, customerGuid)
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        SaleOrderModel model = new SaleOrderModel(saleOrderGuid);
        model.customerGuid = customerGuid;

        SaleOrdersJdbcConverter converter = (SaleOrdersJdbcConverter) JdbcFactory.getConverter(model);

        return converter.updateCustomer(model, getAppCommandContext());
    }

    private void fireAddOrderEvent(String orderGuid) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ORDER_GUID, orderGuid);
        callback(CALLBACK_ADD_ORDER, bundle);
    }

    public static void start(Context context, String saleOrderGuid, String customerId, boolean isGiftCard, BaseUpdateOrderCustomerCallback callback) {
        create(UpdateSaleOrderCustomerCommand.class)
                .arg(ARG_SALE_ORDER_GUID, saleOrderGuid)
                .arg(ARG_CUSTOMER_GUID, customerId)
                .arg(ARG_IS_GIFT_CARD, isGiftCard)
                .callback(callback)
                .queueUsing(context);
    }

    private SaleOrderModel createSaleOrder() {
        return AddSaleOrderCommand.createSaleOrder(getContext(), getAppCommandContext().getRegisterId(), getAppCommandContext().getEmployeeGuid(), getAppCommandContext().getShiftGuid(), customerGuid, OrderType.SALE, BigDecimal.ZERO);
    }

    public static abstract class BaseUpdateOrderCustomerCallback {

        @OnSuccess(UpdateSaleOrderCustomerCommand.class)
        public void onSuccess(@Param(UpdateSaleOrderCustomerCommand.RESULT_IS_GIFT_CARD) boolean giftCard, @Param(UpdateSaleOrderCustomerCommand.RESULT_AMOUNT) String amount) {
            onOrderCustomerUpdated(giftCard, amount);
        }

        @OnFailure(UpdateSaleOrderCustomerCommand.class)
        public void onFailure() {
            onOrderCustomerUpdateError();
        }

        @OnCallback(value = UpdateSaleOrderCustomerCommand.class, name = CALLBACK_ADD_ORDER)
        public void onCallback(@Param(EXTRA_ORDER_GUID)String orderGuid){
            onOrderAdded(orderGuid);
        }

        protected abstract void onOrderCustomerUpdated(boolean isGiftCard, String amount);

        protected abstract void onOrderCustomerUpdateError();

        protected abstract void onOrderAdded(String orderGuid);

    }
}
