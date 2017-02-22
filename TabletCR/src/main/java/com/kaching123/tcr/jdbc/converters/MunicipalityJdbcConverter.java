package com.kaching123.tcr.jdbc.converters;

import com.telly.groundy.PublicGroundyTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.MunicipalityModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;

/**
 * Created by idyuzheva on 06.10.2014.
 */
public class MunicipalityJdbcConverter extends JdbcConverter<MunicipalityModel> {

    public static final String TABLE_NAME = "MUNICIPALITY";

    private static final String ID = "ID";
    private static final String NAME = "NAME";
    private static final String CODE = "CODE";
    private static final String COUNTRY_ID = "COUNTRY_ID";
    private static final String STATE_ID = "STATE_ID";

    @Override
    public MunicipalityModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.MunicipalityTable.ID);
        if (!rs.has(CODE)) ignoreFields.add(ShopStore.MunicipalityTable.CODE);
        if (!rs.has(NAME)) ignoreFields.add(ShopStore.MunicipalityTable.NAME);
        if (!rs.has(STATE_ID)) ignoreFields.add(ShopStore.MunicipalityTable.STATE_ID);
        if (!rs.has(COUNTRY_ID)) ignoreFields.add(ShopStore.MunicipalityTable.COUNTRY_ID);

        return new MunicipalityModel(
                rs.getLong(ID),
                rs.getString(CODE),
                rs.getString(NAME),
                rs.getLong(STATE_ID),
                rs.getString(COUNTRY_ID),
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
        return ShopStore.MunicipalityTable.ID;
    }

    @Override
    public JSONObject getJSONObject(MunicipalityModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.id)
                    .put(CODE, model.code)
                    .put(NAME, model.name)
                    .put(STATE_ID, model.stateId)
                    .put(COUNTRY_ID, model.countryId);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(MunicipalityModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(MunicipalityModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}