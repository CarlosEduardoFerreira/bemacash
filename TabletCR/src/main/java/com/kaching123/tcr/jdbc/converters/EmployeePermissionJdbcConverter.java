package com.kaching123.tcr.jdbc.converters;

import android.util.Log;

import com.kaching123.tcr.Logger;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.jdbc.JdbcBuilder.InsertOrUpdateBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.EmployeePermissionModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import static com.kaching123.tcr.jdbc.JdbcBuilder._insertOrUpdate;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by gdubina on 03/12/13.
 */
public class EmployeePermissionJdbcConverter extends JdbcConverter<EmployeePermissionModel> {

    public static final String TABLE_NAME = "EMPLOYEE_PERMISSION";

    private static final String USER_ID = "USER_ID";
    private static final String PERM_ID = "PERM_ID";
    private static final String ENABLED = "ENABLED";


    @Override
    public EmployeePermissionModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(USER_ID)) ignoreFields.add(ShopStore.EmployeePermissionTable.USER_GUID);
        if (!rs.has(PERM_ID)) ignoreFields.add(ShopStore.EmployeePermissionTable.PERMISSION_ID);
        if (!rs.has(ENABLED)) ignoreFields.add(ShopStore.EmployeePermissionTable.ENABLED);

        return new EmployeePermissionModel(
                rs.getString(USER_ID),
                rs.getLong(PERM_ID),
                rs.getBoolean(ENABLED),
                ignoreFields
        );
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public boolean supportDeleteFlag() {
        return false;
    }

    @Override
    public String getGuidColumn() {
        return PERM_ID;
    }

    @Override
    public String getLocalGuidColumn() {
        return ShopStore.EmployeePermissionTable.PERMISSION_ID;
    }

    @Override
    public JSONObject getJSONObject(EmployeePermissionModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(USER_ID, model.userGuid)
                    .put(PERM_ID, model.permissionId)
                    .put(ENABLED, model.enabled);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(EmployeePermissionModel model, IAppCommandContext appCommandContext) {
        InsertOrUpdateBuilder builder = _insertOrUpdate(TABLE_NAME, appCommandContext);
        Log.d("BemaCarl4","EmployeePermissionJdbcConverter.insertSQL.model.toValues(): " + model.toValues());
        builder
                .add(USER_ID, model.userGuid)
                .add(PERM_ID, model.permissionId)
                .add(ENABLED, model.enabled);
        return builder.build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(EmployeePermissionModel model, IAppCommandContext appCommandContext) {
        Log.d("BemaCarl4","EmployeePermissionJdbcConverter.updateSQL.model.toValues(): " + model.toValues());
        throw new UnsupportedOperationException();
    }

    public SingleSqlCommand disableAllSQL(String userGuid, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ENABLED, false)
                .where(USER_ID, userGuid)
                .build(JdbcFactory.getApiMethod(EmployeePermissionModel.class));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
