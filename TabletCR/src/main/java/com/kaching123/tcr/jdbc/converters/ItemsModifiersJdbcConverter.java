package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by gdubina on 06/11/13.
 */
public class ItemsModifiersJdbcConverter extends JdbcConverter<ModifierModel> {

    private static final String TABLE_NAME = "ITEM_MODIFIER";

    private static final String MODIFIER_GUID = "MODIFIER_GUID";
    private static final String ITEM_GUID = "ITEM_GUID";
    private static final String TYPE = "TYPE";
    private static final String TITLE = "TITLE";
    private static final String EXTRA_COST = "EXTRA_COST";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new ModifierModel(
                rs.getString(MODIFIER_GUID),
                rs.getString(ITEM_GUID),
                _enum(ModifierType.class, rs.getString(TYPE), ModifierType.ADDON),
                rs.getString(TITLE),
                rs.getBigDecimal(EXTRA_COST)
        ).toValues();
    }

    @Override
    public ModifierModel toValues(JdbcJSONObject rs) throws JSONException {
        return new ModifierModel(
                rs.getString(MODIFIER_GUID),
                rs.getString(ITEM_GUID),
                _enum(ModifierType.class, rs.getString(TYPE), ModifierType.ADDON),
                rs.getString(TITLE),
                rs.getBigDecimal(EXTRA_COST)
        );
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return MODIFIER_GUID;
    }

    @Override
    public SingleSqlCommand insertSQL(ModifierModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(MODIFIER_GUID, model.modifierGuid)
                .add(ITEM_GUID, model.itemGuid)
                .add(TYPE, model.type)
                .add(TITLE, model.title)
                .add(EXTRA_COST, model.cost)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(ModifierModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(TITLE, model.title)
                .add(EXTRA_COST, model.cost)
                .where(MODIFIER_GUID, model.modifierGuid)
                .build(JdbcFactory.getApiMethod(model));
    }

}
