package com.kaching123.tcr.model.converter;

import com.telly.groundy.PublicGroundyTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.ShopModuleModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore.ShopModuleTable;
import com.kaching123.tcr.util.JdbcJSONObject;

/**
 * Created by Rodrigo Busata on 10/20/16.
 */
public class ShopModuleJdbcConverter extends JdbcConverter<ShopModuleModel> {

    public static final String TABLE_NAME = "SHOP_MODULES";

    private static final String ID = "ID";
    private static final String SHOP_ID = "SHOP_ID";
    private static final String MODULE_ID = "MODULE_ID";
    private static final String ENABLED = "ENABLED";

    @Override
    public ShopModuleModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopModuleTable.ID);
        if (!rs.has(SHOP_ID)) ignoreFields.add(ShopModuleTable.SHOP_ID);
        if (!rs.has(MODULE_ID)) ignoreFields.add(ShopModuleTable.MODULE_ID);
        if (!rs.has(ENABLED)) ignoreFields.add(ShopModuleTable.MODULE_ID);

        return new ShopModuleModel(
                rs.getInt(ID),
                rs.getInt(SHOP_ID),
                rs.getInt(MODULE_ID),
                rs.getBoolean(ENABLED),
                ignoreFields);
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
    public String getLocalGuidColumn() {
        return ShopModuleTable.ID;
    }

    @Override
    public JSONObject getJSONObject(ShopModuleModel model) {
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.id)
                    .put(SHOP_ID, model.shopId)
                    .put(MODULE_ID, model.moduleId)
                    .put(ENABLED, model.enabled);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(ShopModuleModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(ShopModuleModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}