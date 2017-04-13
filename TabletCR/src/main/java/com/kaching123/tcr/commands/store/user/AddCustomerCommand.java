package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;

import java.util.ArrayList;

/**
 * Created by pkabakov on 10.02.14.
 */
public class AddCustomerCommand extends BaseCustomerCommand {

    @Override
    protected boolean ignoreChecks() {
        return false;
    }

    @Override
    protected void doQuery(ArrayList<ContentProviderOperation> operations) {
        operations.add(ContentProviderOperation.newInsert(CUSTOMER_URI).withValues(model.toValues()).build());
        if (pointsMovementResult != null && pointsMovementResult.getLocalDbOperations() != null){
            operations.addAll(pointsMovementResult.getLocalDbOperations());
        }

        sql = batchInsert(model);
        sql.add(JdbcFactory.getConverter(model).insertSQL(model, getAppCommandContext()));

        if (pointsMovementResult != null && pointsMovementResult.getSqlCmd() != null){
            sql.add(pointsMovementResult.getSqlCmd());
        }
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sql;
    }

    public static void start(Context context, BaseCustomerCallback callback, CustomerModel customer) {
        create(AddCustomerCommand.class).arg(ARG_CUSTOMER, customer).callback(callback).queueUsing(context);
    }
}
