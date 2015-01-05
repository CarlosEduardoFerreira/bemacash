package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

public abstract class JdbcConverter<T extends IValueModel> {

    public abstract ContentValues toValues(ResultSet rs) throws SQLException;

    public abstract T toValues(JdbcJSONObject rs) throws JSONException;

    public abstract String getTableName();

    public abstract String getGuidColumn();

    public String getParentGuidColumn() {
        return null;
    }

    public abstract SingleSqlCommand insertSQL(T model, IAppCommandContext appCommandContext);

    public abstract SingleSqlCommand updateSQL(T model, IAppCommandContext appCommandContext);

    public boolean supportUpdateTimeFlag() {
        return true;
    }

    public boolean supportDeleteFlag() {
        return true;
    }

    public boolean supportDraftFlag() {
        return true;
    }

	public SingleSqlCommand deleteSQL(T model, IAppCommandContext appCommandContext) {
        return deleteSQL(JdbcFactory.getApiMethod(model), model.getGuid(), appCommandContext);
    }

    public final SingleSqlCommand deleteSQL(String method, String guid, IAppCommandContext appCommandContext) {
        return _update(getTableName(), appCommandContext)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .where(getGuidColumn(), guid)
                .build(method);
    }

}
