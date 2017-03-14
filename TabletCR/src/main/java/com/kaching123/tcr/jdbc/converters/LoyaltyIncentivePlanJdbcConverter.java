package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.LoyaltyIncentivePlanModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vkompaniets on 29.06.2016.
 */
public class LoyaltyIncentivePlanJdbcConverter extends JdbcConverter<LoyaltyIncentivePlanModel> {

    public static final String TABLE_NAME = "LOYALTY_PLAN_INCENTIVE";

    private static final String ID = "ID";
    private static final String INCENTIVE_ID = "LOYALTY_INCENTIVE_ID";
    private static final String PLAN_ID = "LOYALTY_PLAN_ID";

    @Override
    public LoyaltyIncentivePlanModel toValues(JdbcJSONObject rs) throws JSONException {
        return new LoyaltyIncentivePlanModel(
                rs.getString(ID),
                rs.getString(INCENTIVE_ID),
                rs.getString(PLAN_ID)
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
        return ShopStore.LoyaltyIncentivePlanTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(LoyaltyIncentivePlanModel model) {
        JSONObject json = null;
        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(INCENTIVE_ID, model.incentiveGuid)
                    .put(PLAN_ID, model.planGuid);
        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }
        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(LoyaltyIncentivePlanModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(LoyaltyIncentivePlanModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}
