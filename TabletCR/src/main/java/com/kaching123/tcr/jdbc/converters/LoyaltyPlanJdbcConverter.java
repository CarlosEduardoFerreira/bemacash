package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.LoyaltyPlanModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vkompaniets on 29.06.2016.
 */
public class LoyaltyPlanJdbcConverter extends JdbcConverter<LoyaltyPlanModel> {

    public static final String TABLE_NAME = "LOYALTY_PLAN";

    private static final String ID = "ID";
    private static final String NAME = "NAME";

    @Override
    public LoyaltyPlanModel toValues(JdbcJSONObject rs) throws JSONException {
        return new LoyaltyPlanModel(
                rs.getString(ID),
                rs.getString(NAME)
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
        return ShopStore.LoyaltyPlanTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(LoyaltyPlanModel model) {
        JSONObject json = null;
        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(NAME, model.name);
        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }
        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(LoyaltyPlanModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }



    @Override
    public SingleSqlCommand updateSQL(LoyaltyPlanModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}
