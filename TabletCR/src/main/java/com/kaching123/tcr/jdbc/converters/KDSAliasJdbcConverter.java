package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.KDSAliasModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by long.jiao on 06.22.16.
 */
public class KDSAliasJdbcConverter extends JdbcConverter<KDSAliasModel> {

    public static final String TABLE_NAME = "KDS_ALIAS";

    private static final String ID = "ID";
    private static final String ALIAS = "ALIAS";

    @Override
    public KDSAliasModel toValues(JdbcJSONObject rs) throws JSONException {
        return new KDSAliasModel(
                rs.getString(ID),
                rs.getString(ALIAS)
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
    public String getLocalGuidColumn() {
        return ShopStore.KDSAliasTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(KDSAliasModel model) {
        JSONObject json = null;
        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(ALIAS, model.alias);
        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }
        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(KDSAliasModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(ALIAS, model.alias)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(KDSAliasModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ALIAS, model.alias)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }

}
