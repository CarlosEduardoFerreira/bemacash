package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrdersJdbcConverter;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

public class RevertSuccessOrderCommand extends UpdateSaleOrderCommand {

    private static final Uri URI_ORDER = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);

    private static final String ARG_SALE_ORDER_GUID = "arg_sale_order_guid";

    private String saleOrderGuid;

    @Override
    protected TaskResult doCommand() {
        saleOrderGuid = getStringArg(ARG_SALE_ORDER_GUID);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(URI_ORDER)
                .withSelection(SaleOrderTable.GUID + " = ?", new String[]{saleOrderGuid})
                .withValue(SaleOrderTable.STATUS, _enum(OrderStatus.ACTIVE))
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        SaleOrderModel model = new SaleOrderModel(saleOrderGuid);
        model.orderStatus = OrderStatus.ACTIVE;

        SaleOrdersJdbcConverter converter = (SaleOrdersJdbcConverter) JdbcFactory.getConverter(model);

        return converter.updateStatus(model, getAppCommandContext());
    }

    public static void start(Context context, String saleOrderGuid) {
        create(RevertSuccessOrderCommand.class)
                .arg(ARG_SALE_ORDER_GUID, saleOrderGuid)
                .queueUsing(context);
    }
}
