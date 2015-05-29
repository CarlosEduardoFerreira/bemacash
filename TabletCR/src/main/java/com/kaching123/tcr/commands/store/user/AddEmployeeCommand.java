package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.EmployeePermissionJdbcConverter;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeePermissionModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;

import java.util.ArrayList;

/**
 * Created by gdubina on 08/11/13.
 */
public class AddEmployeeCommand extends BaseEmployeeCommand {

    @Override
    protected void doQuery(ArrayList<ContentProviderOperation> operations) {
        operations.add(ContentProviderOperation.newInsert(URI_EMPLOYEE).withValues(model.toValues()).build());
    }

    @Override
    protected void savePermissions(ArrayList<ContentProviderOperation> operations) {
        /* enable selected */
        for(Permission p : permissions){
            ContentValues v = new ContentValues();
            v.put(EmployeePermissionTable.USER_GUID, model.guid);
            v.put(EmployeePermissionTable.PERMISSION_ID, p.getId());
            v.put(EmployeePermissionTable.ENABLED, true);
            operations.add(ContentProviderOperation.newInsert(URI_PERMISSIONS).withValues(v).build());
        }
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        model.isSynced = true;
        EmployeePermissionJdbcConverter permissionJdbcConverter = (EmployeePermissionJdbcConverter)JdbcFactory.getConverter(EmployeePermissionTable.TABLE_NAME);
        BatchSqlCommand batch = batchInsert(model).add(JdbcFactory.getConverter(model).insertSQL(model, getAppCommandContext()));
        for(Permission p : permissions){
            batch.add(permissionJdbcConverter.insertSQL(new EmployeePermissionModel(model.guid, p.getId(), true), getAppCommandContext()));
        }
        return batch;
    }

    public static void start(Context context, EmployeeModel model, ArrayList<Permission> permissions, BaseEmployeeCallback callback) {
        create(AddEmployeeCommand.class).arg(ARG_EMPLOYEE, model).arg(ARG_PERMISSIONS, permissions).callback(callback).queueUsing(context);
    }

}
