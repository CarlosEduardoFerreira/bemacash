package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;

/**
 * Created by pkabakov on 17.12.13.
 */
public class EmployeePermissionModel implements IValueModel {

    public String userGuid;
    public long permissionId;
    public boolean enabled;

    public EmployeePermissionModel(Cursor c) {
        this(c.getString(c.getColumnIndex(EmployeePermissionTable.USER_GUID)),
                c.getLong(c.getColumnIndex(EmployeePermissionTable.PERMISSION_ID)),
                c.getInt(c.getColumnIndex(EmployeePermissionTable.ENABLED)) > 0);
    }

    public EmployeePermissionModel(String userGuid, long permissionId, boolean enabled) {
        this.userGuid = userGuid;
        this.permissionId = permissionId;
        this.enabled = enabled;
    }


    @Override
    public String getGuid() {
        return String.valueOf(permissionId);
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(EmployeePermissionTable.USER_GUID, userGuid);
        v.put(EmployeePermissionTable.PERMISSION_ID, permissionId);
        v.put(EmployeePermissionTable.ENABLED, enabled);
        return v;
    }
}
