package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopStore;

import java.util.ArrayList;

/**
 * Created by pkabakov on 10.02.14.
 */
public class EditCustomerCommand extends BaseCustomerCommand {

    @Override
    protected boolean ignoreChecks() {
        return false;
    }

    @Override
    protected void doQuery(ArrayList<ContentProviderOperation> operations) {
        operations.add(ContentProviderOperation.newUpdate(CUSTOMER_URI)
                .withValues(model.toUpdateValues())
                .withSelection(ShopStore.CustomerTable.GUID + " = ?", new String[]{model.guid})
                .build());
        if (pointsMovementResult != null && pointsMovementResult.getLocalDbOperations() != null){
            operations.addAll(pointsMovementResult.getLocalDbOperations());
        }
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchUpdate(model);
        batch.add(JdbcFactory.getConverter(model).updateSQL(model, getAppCommandContext()));
        if (pointsMovementResult != null && pointsMovementResult.getSqlCmd() != null){
            batch.add(pointsMovementResult.getSqlCmd());
        }
        return batch;
    }

    public static void start(Context context, BaseCustomerCallback callback, CustomerModel customer) {
        create(EditCustomerCommand.class).arg(ARG_CUSTOMER, customer).callback(callback).queueUsing(context);
    }
}
