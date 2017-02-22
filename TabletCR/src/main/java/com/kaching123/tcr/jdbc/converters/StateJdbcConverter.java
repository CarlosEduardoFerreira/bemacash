package com.kaching123.tcr.jdbc.converters;

import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.StateModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;

import static com.kaching123.tcr.util.ContentValuesUtilBase._enum;

/**
 * Created by idyuzheva on 06.10.2014.
 */
public class StateJdbcConverter extends JdbcConverter<StateModel> {

    public static final String TABLE_NAME = "STATE";

    private static final String ID = "ID";
    private static final String NAME = "NAME";
    private static final String COUNTRY_ID = "COUNTRY_ID";
    private static final String ABBREVIATION = "ABBREVIATION";
    private static final String CODE = "CODE";
    private static final String FISCAL_TECH = "FISCAL_TECH";
    private static final String MAX_SALES_AMOUNT = "MAX_SALES_AMOUNT";


    @Override
    public StateModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.StateTable.ID);
        if (!rs.has(NAME)) ignoreFields.add(ShopStore.StateTable.NAME);
        if (!rs.has(COUNTRY_ID)) ignoreFields.add(ShopStore.StateTable.COUNTRY_ID);
        if (!rs.has(ABBREVIATION)) ignoreFields.add(ShopStore.StateTable.ABBREVIATION);
        if (!rs.has(CODE)) ignoreFields.add(ShopStore.StateTable.CODE);
        if (!rs.has(FISCAL_TECH)) ignoreFields.add(ShopStore.StateTable.FISCAL_TECH);
        if (!rs.has(MAX_SALES_AMOUNT)) ignoreFields.add(ShopStore.StateTable.MAX_SALES_AMOUNT);

        return new StateModel(
                rs.getLong(ID),
                rs.getString(NAME),
                rs.getString(COUNTRY_ID),
                rs.getString(ABBREVIATION),
                rs.getInt(CODE),
                rs.getBigDecimal(MAX_SALES_AMOUNT),
                ignoreFields);
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
        return ID;
    }

    @Override
    public String getLocalGuidColumn() {
        return ShopStore.StateTable.ID;
    }

    @Override
    public JSONObject getJSONObject(StateModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.id)
                    .put(NAME, model.name)
                    .put(COUNTRY_ID, model.countryId)
                    .put(CODE, model.code)
                    .put(ABBREVIATION, model.abbreviation)
                    .put(MAX_SALES_AMOUNT, model.maxAmount);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(StateModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(StateModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

}