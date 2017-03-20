package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.List;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;

/**
 * Created by pkabakov on 17.12.13.
 */
public class EmployeePermissionModel implements IValueModel {

    public String userGuid;
    public long permissionId;
    public boolean enabled;

    private List<String> mIgnoreFields;

    public EmployeePermissionModel(Cursor c) {
        this(c.getString(c.getColumnIndex(EmployeePermissionTable.USER_GUID)),
                c.getLong(c.getColumnIndex(EmployeePermissionTable.PERMISSION_ID)),
                c.getInt(c.getColumnIndex(EmployeePermissionTable.ENABLED)) > 0, null);
    }

    public EmployeePermissionModel(String userGuid, long permissionId, boolean enabled, List<String> ignoreFields) {
        this.userGuid = userGuid;
        this.permissionId = permissionId;
        this.enabled = enabled;

        this.mIgnoreFields = ignoreFields;
    }


    @Override
    public String getGuid() {
        return String.valueOf(permissionId);
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeePermissionTable.USER_GUID)) v.put(EmployeePermissionTable.USER_GUID, userGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeePermissionTable.PERMISSION_ID)) v.put(EmployeePermissionTable.PERMISSION_ID, permissionId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeePermissionTable.ENABLED)) v.put(EmployeePermissionTable.ENABLED, enabled);
        return v;
    }

    @Override
    public String getIdColumn() {
        return EmployeePermissionTable.USER_GUID;
    }
}
