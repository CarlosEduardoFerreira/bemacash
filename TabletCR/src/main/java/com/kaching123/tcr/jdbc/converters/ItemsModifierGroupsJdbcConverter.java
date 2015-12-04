package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by gdubina on 06/11/13.
 */
public class ItemsModifierGroupsJdbcConverter extends JdbcConverter<ModifierGroupModel> {

    private static final String TABLE_NAME = "ITEM_MODIFIER_GROUP";

    private static final String GUID = "GUID";
    private static final String ITEM_GUID = "ITEM_GUID";
    private static final String GROUP_NAME = "GROUP_NAME";
    private static final String DEFAULT_GUID = "DEFAULT_GUID";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new ModifierGroupModel(
                rs.getString(GUID),
                rs.getString(ITEM_GUID),
                rs.getString(GROUP_NAME),
                rs.getString(DEFAULT_GUID)
        ).toValues();
    }

    @Override
    public ModifierGroupModel toValues(JdbcJSONObject rs) throws JSONException {
        return new ModifierGroupModel(
                rs.getString(GUID),
                rs.getString(ITEM_GUID),
                rs.getString(GROUP_NAME),
                rs.getString(DEFAULT_GUID)
        );
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return GUID;
    }

    @Override
    public SingleSqlCommand insertSQL(ModifierGroupModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(GUID, model.guid)
                .add(ITEM_GUID, model.itemGuid)
                .add(GROUP_NAME, model.title)
                .add(DEFAULT_GUID, model.defaultGuid)

                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(ModifierGroupModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ITEM_GUID, model.itemGuid)
                .add(GROUP_NAME, model.title)
                .add(DEFAULT_GUID, model.defaultGuid)
                .where(GUID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand setDefault(String guid, String modGuid, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(DEFAULT_GUID, modGuid)
                .where(ITEM_GUID, guid)
                .build(JdbcFactory.getApiMethod(new ModifierExModel()));
    }
}