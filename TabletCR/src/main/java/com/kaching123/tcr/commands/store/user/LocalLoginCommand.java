package com.kaching123.tcr.commands.store.user;

import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.telly.groundy.Groundy;
import com.telly.groundy.GroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

public class LocalLoginCommand extends GroundyTask {

    private static final String ARG_PASSWORD = "arg_password";

    @Override
    protected TaskResult doInBackground() {
        Logger.d("LocalLoginCommand.doInBackground");
        String password = getStringArg(ARG_PASSWORD);

        if (!loginLocal(password)){
            return failed();
        }

        return succeeded();
    }

    private boolean loginLocal(String password) {
        EmployeeModel currentUser = ((TcrApplication) getContext().getApplicationContext()).getOperator();
        String userName = currentUser.login;

        Cursor c = ProviderAction.query(ShopProvider.getContentUri(EmployeeTable.URI_CONTENT))
                .projection(EmployeeTable.GUID)
                .where(EmployeeTable.LOGIN + " = ?", userName)
                .where(EmployeeTable.PASSWORD + " = ?", password)
                .perform(getContext());

        boolean result = c.moveToFirst();
        c.close();

        return result;
    }

    public static void start(Context context, BaseLocalLoginCommandCallback callback, String password) {
        Groundy.create(LocalLoginCommand.class).arg(ARG_PASSWORD, password).callback(callback).queueUsing(context);
    }

    public static abstract class BaseLocalLoginCommandCallback {

        @OnSuccess(LocalLoginCommand.class)
        public void handleSuccess(){
            onLoginSuccess();
        }

        @OnFailure(LocalLoginCommand.class)
        public void handleFailure(){
            onLoginError();
        }

        protected abstract void onLoginSuccess();
        protected abstract void onLoginError();
    }
}
