package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.EmployeeJdbcConverter;
import com.kaching123.tcr.jdbc.converters.EmployeePermissionJdbcConverter;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeePermissionModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by vkompaniets on 27.12.13.
 */
public class EditEmployeeCommand extends BaseEmployeeCommand {

    @Override
    protected void doQuery(ArrayList<ContentProviderOperation> operations) {
        // inserting update into local db
        operations.add(ContentProviderOperation.newUpdate(URI_EMPLOYEE)
                .withValues(model.toValues())
                .withSelection(EmployeeTable.GUID + " = ?", new String[]{model.guid})
                .build());
    }

    @Override
    protected void savePermissions(ArrayList<ContentProviderOperation> operations) {
        /* reset old permissions */
        ContentValues update = new ContentValues();
        update.put(EmployeePermissionTable.ENABLED, false);
        operations.add(ContentProviderOperation.newUpdate(URI_PERMISSIONS)
                .withValues(update)
                .withSelection(EmployeePermissionTable.USER_GUID + " = ?", new String[]{model.guid})
                .build());

        /* enable selected */
        for (Permission p : permissions) {
            ContentValues v = new ContentValues();
            v.put(EmployeePermissionTable.USER_GUID, model.guid);
            v.put(EmployeePermissionTable.PERMISSION_ID, p.getId());
            v.put(EmployeePermissionTable.ENABLED, true);
            operations.add(ContentProviderOperation.newInsert(URI_PERMISSIONS)
                    .withValues(v)
                    .build());
        }
    }

    @Override
    protected void afterCommand(ContentProviderResult[] dbOperationResults) {
        tryUpdateOperatorPermissions();
    }

    private void tryUpdateOperatorPermissions() {
        //need to get current operator - not that who started this command
        String operatorGuid = getApp().getOperatorGuid();
        if (operatorGuid == null || !operatorGuid.equals(model.getGuid()))
            return;

        HashSet<Permission> newPermissions = permissions == null ? new HashSet<Permission>() : new HashSet<Permission>(permissions);
        getApp().setOperatorPermissions(newPermissions);
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        // create sql for uploading.
        EmployeePermissionJdbcConverter permissionJdbcConverter = (EmployeePermissionJdbcConverter) JdbcFactory.getConverter(EmployeePermissionTable.TABLE_NAME);
        BatchSqlCommand batch = batchUpdate(model);
        if (model.isSynced) {
            batch.add(EmployeeJdbcConverter.updateEmployeeNoCreSQL(model, getAppCommandContext()));
        } else {
            model.isSynced = true;
            batch.add(JdbcFactory.getConverter(model).updateSQL(model, getAppCommandContext()));
        }

        batch.add(permissionJdbcConverter.disableAllSQL(model.guid, getAppCommandContext()));
        for (Permission p : permissions) {
            batch.add(permissionJdbcConverter.insertSQL(new EmployeePermissionModel(model.guid, p.getId(), true), getAppCommandContext()));
        }
        return batch;
    }

    public static void start(Context context, EmployeeModel model, ArrayList<Permission> permissions, BaseEmployeeCallback callback) {
        create(EditEmployeeCommand.class).arg(ARG_EMPLOYEE, model).arg(ARG_PERMISSIONS, permissions).callback(callback).queueUsing(context);
    }


}
