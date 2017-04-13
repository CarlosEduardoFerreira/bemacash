package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CustomerTable;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 20.02.14.
 */
public class DeleteCustomerCommand extends BaseCustomerCommand {

    @Override
    protected boolean ignoreChecks() {
        return true;
    }

    @Override
    protected void doQuery(ArrayList<ContentProviderOperation> operations) {
        operations.add(ContentProviderOperation.newUpdate(CUSTOMER_URI)
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(CustomerTable.GUID + " = ?", new String[]{model.guid})
                .build());

        sql = batchUpdate(model);
        sql.add(JdbcFactory.getConverter(CustomerTable.TABLE_NAME).deleteSQL(model, getAppCommandContext()));
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sql;
    }

    public static void start(Context context, CustomerModel model, BaseCustomerCallback callback){
        create(DeleteCustomerCommand.class).arg(ARG_CUSTOMER, model).callback(callback).queueUsing(context);
    }
}
