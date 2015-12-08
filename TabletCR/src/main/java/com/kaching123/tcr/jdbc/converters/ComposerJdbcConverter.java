package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

public class ComposerJdbcConverter extends JdbcConverter<ComposerModel> {

    private static final String TABLE_NAME = "COMPOSER";

    private static final String ID = "ID";
    private static final String ITEM_HOST_ID = "ITEM_HOST_ID";
    private static final String ITEM_CHILD_ID = "ITEM_CHILD_ID";
    private static final String QUANTITY = "QUANTITY";
    private static final String STORE_TRACKING_ENABLED = "STORE_TRACKING_ENABLED";
    private static final String FREE_OF_CHARGE_COMPOSER = "FREE_OF_CHARGE_COMPOSER";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new ComposerModel(
                rs.getString(ID),
                rs.getString(ITEM_HOST_ID),
                rs.getString(ITEM_CHILD_ID),
                rs.getBigDecimal(QUANTITY),
                rs.getBoolean(STORE_TRACKING_ENABLED),
                rs.getBoolean(FREE_OF_CHARGE_COMPOSER)
        ).toValues();
    }

    @Override
    public ComposerModel toValues(JdbcJSONObject rs) throws JSONException {
        return new ComposerModel(
                rs.getString(ID),
                rs.getString(ITEM_HOST_ID),
                rs.getString(ITEM_CHILD_ID),
                rs.getBigDecimal(QUANTITY),
                rs.getBoolean(STORE_TRACKING_ENABLED),
                rs.getBoolean(FREE_OF_CHARGE_COMPOSER)
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
    public SingleSqlCommand insertSQL(ComposerModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(ITEM_HOST_ID, model.itemHostId)
                .add(ITEM_CHILD_ID, model.itemChildId)
                .add(QUANTITY, model.qty)
                .add(STORE_TRACKING_ENABLED, model.tracked)
                .add(FREE_OF_CHARGE_COMPOSER, model.restricted)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand deleteSQL(ComposerModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(ComposerModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ITEM_HOST_ID, model.itemHostId)
                .add(ITEM_CHILD_ID, model.itemChildId)
                .add(QUANTITY, model.qty)
                .add(STORE_TRACKING_ENABLED, model.tracked)
                .add(FREE_OF_CHARGE_COMPOSER, model.restricted)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }
}