package com.kaching123.tcr.commands.store.user;

import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeePermissionsModel;
import com.kaching123.tcr.model.EmployeeStatus;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.telly.groundy.Groundy;
import com.telly.groundy.GroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TempLoginCommand extends GroundyTask {

    public static final String EXTRA_EMPLOYEE = "extra_employee";
    public static final String EXTRA_ERROR = "extra_error";

    private static final String ARG_PASSWORD = "arg_password";
    private static final String ARG_USER = "arg_username";
    private static final String ARG_PERMISSON = "arg_permission";

    public static enum Error {LOGIN_FAILED, DUPLICATE_LOGIN, NO_PERMISSION, EMPLOYEE_NOT_ACTIVE};

    @Override
    protected TaskResult doInBackground() {
        Logger.d("TempLoginCommand.doInBackground");
        String userName = getStringArg(ARG_USER);
        String password = getStringArg(ARG_PASSWORD);
        ArrayList<Permission> requiredPermissions = (ArrayList<Permission>)getArgs().getSerializable(ARG_PERMISSON);

        if(isCurrentUser(userName)){
            return failed().add(EXTRA_ERROR, Error.DUPLICATE_LOGIN);
        }

        Set<Permission> permissions = null;
        EmployeeModel employeeModel = loginLocal(userName, password);
        if (employeeModel == null){
            return failed().add(EXTRA_ERROR, Error.LOGIN_FAILED);
        }
        if(employeeModel.status != EmployeeStatus.ACTIVE){
            return failed().add(EXTRA_ERROR, Error.EMPLOYEE_NOT_ACTIVE);
        }

        permissions = getEmployeePermissions(employeeModel);

        if(permissions == null || !permissions.containsAll(requiredPermissions)){
            return failed().add(EXTRA_ERROR, Error.NO_PERMISSION);
        }
        return succeeded().add(EXTRA_EMPLOYEE, new EmployeePermissionsModel(employeeModel, permissions));
    }

    private EmployeeModel loginLocal(String userName, String password) {
        EmployeeModel model = null;
        Cursor c = ProviderAction.query(ShopProvider.getContentUri(EmployeeTable.URI_CONTENT))
                .where(EmployeeTable.LOGIN + " = ?", userName)
                .where(EmployeeTable.PASSWORD + " = ?", password)
                .perform(getContext());

        if (c.moveToFirst()) {
            model = new EmployeeModel(c);
        }
        c.close();

        return model;
    }

    private boolean isCurrentUser(String login) {
        EmployeeModel currentUser = ((TcrApplication) getContext().getApplicationContext()).getOperator();
        return currentUser != null && currentUser.login.equals(login);
    }

    private Set<Permission> getEmployeePermissions(EmployeeModel employee) {
        HashSet<Permission> permissions = new HashSet<Permission>();
        Cursor c = ProviderAction.query(ShopProvider.getContentUri(ShopStore.EmployeePermissionTable.URI_CONTENT))
                .projection(ShopStore.EmployeePermissionTable.PERMISSION_ID)
                .where(ShopStore.EmployeePermissionTable.USER_GUID + " = ?", employee.guid)
                .where(EmployeePermissionTable.ENABLED + " = ?", "1")
                .perform(getContext());
        while (c.moveToNext()) {
            long id = c.getLong(0);
            Permission p = Permission.valueOfOrNull(id);
            if(p != null){
                permissions.add(p);
            }
        }
        return permissions;
    }

    public static void start(Context context, BaseTempLoginCommandCallback callback, String login, String password, ArrayList<Permission> permissions) {
        Groundy.create(TempLoginCommand.class).arg(ARG_USER, login).arg(ARG_PASSWORD, password).arg(ARG_PERMISSON, permissions).callback(callback).queueUsing(context);
    }

    public static abstract class BaseTempLoginCommandCallback {

        @OnSuccess(TempLoginCommand.class)
        public void handleSuccess(@Param(EXTRA_EMPLOYEE) EmployeePermissionsModel employeeModel){
            onLoginSuccess(employeeModel);
        }

        @OnFailure(TempLoginCommand.class)
        public void handleFailure(@Param(EXTRA_ERROR) Error error){
            if(error == null){
                onLoginError();
                return;
            }
            switch (error){
                case DUPLICATE_LOGIN:
                    onDuplicateLoginError();
                    break;
                case NO_PERMISSION:
                    onNoPermissionLoginError();
                    break;
                case EMPLOYEE_NOT_ACTIVE:
                    onEmployeeNotActive();
                    break;
                default:
                    onLoginError();
            }
        }

        protected abstract void onEmployeeNotActive();

        protected abstract void onLoginSuccess(EmployeePermissionsModel employeeModel);

        protected abstract void onLoginError();
        protected abstract void onDuplicateLoginError();
        protected abstract void onNoPermissionLoginError();
    }
}
