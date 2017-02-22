package com.kaching123.tcr.model.converter;

import com.telly.groundy.PublicGroundyTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.CardBrandModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CardBrandTable;
import com.kaching123.tcr.util.JdbcJSONObject;

/**
 * Created by Rodrigo Busata on 07/22/16.
 */
public class CardBrandJdbcConverter extends JdbcConverter<CardBrandModel> {

    public static final String TABLE_NAME = "CARD_BRAND";

    private static final String ID = "ID";
    private static final String NAME = "NAME";
    private static final String TYPE = "TYPE";

    @Override
    public CardBrandModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(CardBrandTable.ID);
        if (!rs.has(NAME)) ignoreFields.add(CardBrandTable.NAME);
        if (!rs.has(TYPE)) ignoreFields.add(CardBrandTable.TYPE);

        return new CardBrandModel(
                rs.getString(ID),
                rs.getString(NAME),
                rs.getString(TYPE),
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
        return ShopStore.CardBrandTable.ID;
    }

    @Override
    public JSONObject getJSONObject(CardBrandModel model) {
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.id)
                    .put(NAME, model.name)
                    .put(TYPE, model.type);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(CardBrandModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(CardBrandModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}