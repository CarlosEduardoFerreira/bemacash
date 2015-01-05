package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by gdubina on 07/11/13.
 */
public abstract class UpdateSaleOrderCommand extends AsyncCommand {

    protected static final Uri URI_ORDER = ShopProvider.getContentUri(ShopStore.SaleOrderTable.URI_CONTENT);

    protected static final String ARG_ORDER = "arg_order";

    protected SaleOrderModel order;

    @Override
    protected TaskResult doCommand() {
        Logger.d("UpdateSaleOrderCommand doCommand");
        order = readOrder();
        if (order == null)
            return failed();
        return succeeded();
    }

    protected SaleOrderModel readOrder() {
        return (SaleOrderModel) getArgs().getSerializable(ARG_ORDER);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newUpdate(URI_ORDER)
                .withValues(order.toValues())
                .withSelection(ShopStore.SaleOrderTable.GUID + " = ?", new String[]{order.guid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(order).updateSQL(order, getAppCommandContext());
    }

}
