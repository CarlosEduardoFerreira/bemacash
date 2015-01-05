package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrdersJdbcConverter;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;

/**
 * Created by pkabakov on 11.02.14.
 */
public class UpdateSaleOrderCustomerCommand extends AsyncCommand {

    private static final Uri URI_ORDER = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);

    private static final String ARG_SALE_ORDER_GUID = "arg_sale_order_guid";
    private static final String ARG_CUSTOMER_GUID = "arg_customer_guid";

    private String saleOrderGuid;
    private String customerGuid;

    @Override
    protected TaskResult doCommand() {
        saleOrderGuid = getStringArg(ARG_SALE_ORDER_GUID);
        customerGuid = getStringArg(ARG_CUSTOMER_GUID);

        return succeeded();
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

    public static void start(Context context, String saleOrderGuid, String customerId, BaseUpdateOrderCustomerCallback callback) {
        create(UpdateSaleOrderCustomerCommand.class)
                .arg(ARG_SALE_ORDER_GUID, saleOrderGuid)
                .arg(ARG_CUSTOMER_GUID, customerId)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseUpdateOrderCustomerCallback {

        @OnSuccess(UpdateSaleOrderCustomerCommand.class)
        public void onSuccess() {
            onOrderCustomerUpdated();
        }

        @OnFailure(UpdateSaleOrderCustomerCommand.class)
        public void onFailure() {
            onOrderCustomerUpdateError();
        }

        protected abstract void onOrderCustomerUpdated();

        protected abstract void onOrderCustomerUpdateError();

    }
}
