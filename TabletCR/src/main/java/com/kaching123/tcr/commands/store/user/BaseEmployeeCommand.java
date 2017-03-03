package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 20.01.14.
 */
public abstract class BaseEmployeeCommand extends AsyncCommand {

    public static enum Error {EMPLOYEE_EXISTS, EMAIL_EXISTS};

    protected static final Uri URI_EMPLOYEE = ShopProvider.getContentUri(EmployeeTable.URI_CONTENT);
    protected static final Uri URI_PERMISSIONS = ShopProvider.getContentUri(EmployeePermissionTable.URI_CONTENT);

    protected static final String ARG_EMPLOYEE = "ARG_EMPLOYEE";
    protected static final String ARG_PERMISSIONS = "ARG_PERMISSIONS";

    protected static final String EXTRA_EMPLOYEE_ERROR = "EXTRA_EMPLOYEE_ERROR";

    protected ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

    protected EmployeeModel model;
    protected ArrayList<Permission> permissions;

    @Override
    protected TaskResult doCommand() {
        model = (EmployeeModel) getArgs().getSerializable(ARG_EMPLOYEE);
        permissions = (ArrayList<Permission>) getArgs().getSerializable(ARG_PERMISSIONS);
        if (checkLogin()) {
            return failed().add(EXTRA_EMPLOYEE_ERROR, Error.EMPLOYEE_EXISTS);
        }
        if (checkEmail()) {
            return failed().add(EXTRA_EMPLOYEE_ERROR, Error.EMAIL_EXISTS);
        }
        doQuery(operations);
        savePermissions(operations);
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    protected abstract void doQuery(ArrayList<ContentProviderOperation> operations);
    protected abstract void savePermissions(ArrayList<ContentProviderOperation> operations);

    private boolean checkLogin() {
        Cursor c = ProviderAction
                .query(URI_EMPLOYEE)
                .where(EmployeeTable.LOGIN + " = ?", model.login)
                .where(EmployeeTable.GUID + " <> ?", model.guid)
                .perform(getContext());
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    private boolean checkEmail(){
        if (TextUtils.isEmpty(model.email))
            return false;

        Cursor c = ProviderAction
                .query(URI_EMPLOYEE)
                .where(EmployeeTable.EMAIL + " = ?", model.email)
                .where(EmployeeTable.GUID + " <> ?", model.guid)
                .where(EmployeeTable.IS_MERCHANT + " = ?", 0)
                .perform(getContext());
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public static abstract class BaseEmployeeCallback{
        @OnSuccess(BaseEmployeeCommand.class)
        public void handleSuccess() {
            onSuccess();
        }

        @OnFailure(BaseEmployeeCommand.class)
        public void handleFailure(@Param(EXTRA_EMPLOYEE_ERROR) Error error) {
            if (Error.EMPLOYEE_EXISTS == error){
                onEmployeeAlreadyExists();
            }else if (Error.EMAIL_EXISTS == error){
                onEmailAlreadyExists();
            }else{
                onEmployeeError();
            }
        }

        protected abstract void onSuccess();
        protected abstract void onEmployeeError();
        protected abstract void onEmployeeAlreadyExists();
        protected abstract void onEmailAlreadyExists();
    }
}
