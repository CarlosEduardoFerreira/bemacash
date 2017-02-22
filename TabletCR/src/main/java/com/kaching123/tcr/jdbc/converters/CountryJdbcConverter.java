package com.kaching123.tcr.jdbc.converters;

import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.CountryModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;

/**
 * Created by idyuzheva on 06.10.2014.
 */
public class CountryJdbcConverter extends JdbcConverter<CountryModel> {

    public static final String TABLE_NAME = "COUNTRY";

    private static final String SID = "SID";
    private static final String ID = "ID";
    private static final String NAME = "NAME";

    @Override
    public CountryModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.CountryTable.ID);
        if (!rs.has(SID)) ignoreFields.add(ShopStore.CountryTable.SID);
        if (!rs.has(NAME)) ignoreFields.add(ShopStore.CountryTable.NAME);

        return new CountryModel(
                rs.getLong(SID),
                rs.getString(ID),
                rs.getString(NAME),
                ignoreFields
        );
    }

    @Override
    public boolean supportUpdateTimeFlag() {
        return false;
    }

    @Override
    public boolean supportDeleteFlag() {
        return false;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return SID;
    }

    @Override
    public String getLocalGuidColumn() {
        return ShopStore.CountryTable.ID;
    }

    @Override
    public JSONObject getJSONObject(CountryModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.id)
                    .put(SID, model.sid)
                    .put(NAME, model.name);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(CountryModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(CountryModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}