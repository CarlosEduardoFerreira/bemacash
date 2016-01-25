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
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;

/**
 * Created by gdubina on 15/11/13.
 */
public class UpdateSaleOrderTaxStatusCommand extends AsyncCommand {

    private static final Uri URI_ORDER = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);

    private static final String ARG_SALE_ORDER_GUID = "arg_sale_order_guid";
    private static final String ARG_TAXABLE = "arg_tax_vat";

    private static final String PARAM_SALE_ITEM_GUID = "param_sale_item_guid";

    private String saleOrderId;
    private boolean taxable;

    @Override
    protected TaskResult doCommand() {
        saleOrderId = getStringArg(ARG_SALE_ORDER_GUID);
        taxable = getBooleanArg(ARG_TAXABLE);
        return succeeded().add(PARAM_SALE_ITEM_GUID, saleOrderId);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>(1);
        operations.add(ContentProviderOperation.newUpdate(URI_ORDER)
                .withSelection(SaleOrderTable.GUID + " = ?", new String[]{saleOrderId})
                .withValue(SaleOrderTable.TAXABLE, taxable)
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        SaleOrderModel model = new SaleOrderModel(saleOrderId);
        model.taxable = taxable;

        SaleOrdersJdbcConverter converter = (SaleOrdersJdbcConverter) JdbcFactory.getConverter(model);
        return converter.updateTax(model, getAppCommandContext());
    }

    public static void start(Context context, String saleOrderGuid, boolean taxable,
                             TaxCallback callback) {
        create(UpdateSaleOrderTaxStatusCommand.class)
                .arg(ARG_SALE_ORDER_GUID, saleOrderGuid)
                .arg(ARG_TAXABLE, taxable)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class TaxCallback {

        @OnSuccess(UpdateSaleOrderTaxStatusCommand.class)
        public void handleSuccess(@Param(PARAM_SALE_ITEM_GUID) String guid){
            onSuccess(guid);
        }

        protected abstract void onSuccess(String saleItemGuid);
    }
}
