package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcBuilder.InsertOrUpdateBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.EmployeePermissionModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insertOrUpdate;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by gdubina on 03/12/13.
 */
public class EmployeePermissionJdbcConverter extends JdbcConverter<EmployeePermissionModel> {

    private static final String TABLE_NAME = "EMPLOYEE_PERMISSION";

    private static final String USER_ID = "USER_ID";
    private static final String PERM_ID = "PERM_ID";
    private static final String ENABLED = "ENABLED";


    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new EmployeePermissionModel(
                rs.getString(USER_ID),
                rs.getLong(PERM_ID),
                rs.getBoolean(ENABLED)
        ).toValues();
    }

    @Override
    public EmployeePermissionModel toValues(JdbcJSONObject rs) throws JSONException {
        return new EmployeePermissionModel(
                rs.getString(USER_ID),
                rs.getLong(PERM_ID),
                rs.getBoolean(ENABLED)
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
        //throw new UnsupportedOperationException("don't support delete functionality");
    }

    @Override
    public SingleSqlCommand insertSQL(EmployeePermissionModel model, IAppCommandContext appCommandContext) {
        InsertOrUpdateBuilder builder = _insertOrUpdate(TABLE_NAME, appCommandContext);

        builder//.insert()
                .add(USER_ID, model.userGuid)
                .add(PERM_ID, model.permissionId)
                .add(ENABLED, model.enabled);
        /*builder.update()
                .add(ENABLED, model.enabled);*/
        return builder.build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(EmployeePermissionModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    public SingleSqlCommand disableAllSQL(String userGuid, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ENABLED, false)
                .where(USER_ID, userGuid)
                .build(JdbcFactory.getApiMethod(EmployeePermissionModel.class));
    }

}
