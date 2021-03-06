package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.EmployeePermissionJdbcConverter;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeePermissionModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;

import java.util.ArrayList;

/**
 * Created by gdubina on 08/11/13.
 */
public class AddEmployeeCommand extends BaseEmployeeCommand {

    @Override
    protected void doQuery(ArrayList<ContentProviderOperation> operations) {
        operations.add(ContentProviderOperation.newInsert(URI_EMPLOYEE).withValues(model.toValues()).build());

        model.isSynced = true;
        EmployeePermissionJdbcConverter permissionJdbcConverter = (EmployeePermissionJdbcConverter)JdbcFactory.getConverter(EmployeePermissionTable.TABLE_NAME);
        sql = batchInsert(model);
        sql.add(JdbcFactory.getConverter(model).insertSQL(model, getAppCommandContext()));
        for(Permission p : permissions){
            sql.add(permissionJdbcConverter.insertSQL(new EmployeePermissionModel(model.guid, p.getId(), true, null), getAppCommandContext()));
        }

    }

    @Override
    protected void savePermissions(ArrayList<ContentProviderOperation> operations) {
        /* enable selected */
        for(Permission p : permissions){
            ContentValues v = new ContentValues();
            v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

            v.put(EmployeePermissionTable.USER_GUID, model.guid);
            v.put(EmployeePermissionTable.PERMISSION_ID, p.getId());
            v.put(EmployeePermissionTable.ENABLED, true);
            operations.add(ContentProviderOperation.newInsert(URI_PERMISSIONS).withValues(v).build());
        }
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sql;
    }

    public static void start(Context context, EmployeeModel model, ArrayList<Permission> permissions, BaseEmployeeCallback callback) {
        create(AddEmployeeCommand.class).arg(ARG_EMPLOYEE, model).arg(ARG_PERMISSIONS, permissions).callback(callback).queueUsing(context);
    }

}
