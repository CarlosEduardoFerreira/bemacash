package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by alboyko on 26.11.2015.
 */

public class UnitLabelJdbcConverter extends JdbcConverter<UnitLabelModel> {

    private static final String TABLE_NAME = "UNIT_LABEL";

    private static final String ID = "ID";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String SHORTCUT = "SHORTCUT";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new UnitLabelModel(
                rs.getString(ID),
                rs.getString(DESCRIPTION),
                rs.getString(SHORTCUT)

        ).toValues();
    }

    @Override
    public UnitLabelModel toValues(JdbcJSONObject rs) throws JSONException {
        return new UnitLabelModel(
                rs.getString(ID),
                rs.getString(DESCRIPTION),
                rs.getString(SHORTCUT)
        );
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return ID;
    }

    @Override
    public SingleSqlCommand insertSQL(UnitLabelModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(DESCRIPTION, model.description)
                .add(SHORTCUT, model.shortcut)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(UnitLabelModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(DESCRIPTION, model.description)
                .add(SHORTCUT, model.shortcut)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

}
