package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;

/**
 * Created by pkabakov on 10.02.14.
 */
public class AddCustomerCommand extends BaseCustomerCommand {

    @Override
    protected boolean ignoreEmailCheck() {
        return false;
    }

    @Override
    protected void doQuery(ArrayList<ContentProviderOperation> operations) {
        operations.add(ContentProviderOperation.newInsert(CUSTOMER_URI).withValues(model.toValues()).build());
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(model).insertSQL(model, getAppCommandContext());
    }

    public static void start(Context context, BaseAddCustomerCallback callback, CustomerModel customer) {
        create(AddCustomerCommand.class).arg(ARG_CUSTOMER, customer).callback(callback).queueUsing(context);
    }

    public static abstract class BaseAddCustomerCallback {

        @OnSuccess(AddCustomerCommand.class)
        public void onSuccess() {
            onCustomerAdded();
        }

        @OnFailure(AddCustomerCommand.class)
        public void onFailure(@Param(EXTRA_ERROR) Error error) {
            if (error == null){
                onCustomerAddError();
            }else if (error == Error.EMAIL_EXISTS){
                onEmailExists();
            }

        }

        protected abstract void onCustomerAdded();

        protected abstract void onCustomerAddError();

        protected abstract void onEmailExists();

    }
}
