package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemKdsModel;
import com.kaching123.tcr.model.KDSAliasModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by long.jiao on 06.22.16.
 */
public class ItemKDSJdbcConverter extends JdbcConverter<ItemKdsModel> {

    public static final String TABLE_NAME = "ITEM_KDS";

    private static final String GUID = "ID";
    private static final String ITEM_ID = "ITEM_ID";
    private static final String KDS_ID = "KDS_ID";


    @Override
    public ItemKdsModel toValues(JdbcJSONObject rs) throws JSONException {
        return new ItemKdsModel(
                rs.getString(GUID),
                rs.getString(ITEM_ID),
                rs.getString(KDS_ID)
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
    public String getLocalGuidColumn() {
        return ShopStore.ItemKDSTable.ID;
    }

    @Override
    public JSONObject getJSONObject(ItemKdsModel model) {
        JSONObject json = null;
        try {
            json = new JSONObject()
                    .put(ITEM_ID, model.guid)
                    .put(KDS_ID, model.kdsID);
        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }
        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(ItemKdsModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ITEM_ID, model.itemID)
                .add(KDS_ID, model.kdsID)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(ItemKdsModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ITEM_ID, model.itemID)
                .where(KDS_ID, model.kdsID)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }

}
