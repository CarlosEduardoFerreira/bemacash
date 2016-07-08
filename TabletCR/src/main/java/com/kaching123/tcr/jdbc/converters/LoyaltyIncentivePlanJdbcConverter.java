package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.model.LoyaltyIncentivePlanModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vkompaniets on 29.06.2016.
 */
public class LoyaltyIncentivePlanJdbcConverter extends JdbcConverter<LoyaltyIncentivePlanModel> {

    private static final String TABLE_NAME = "LOYALTY_PLAN_INCENTIVE";

    private static final String ID = "ID";
    private static final String INCENTIVE_ID = "LOYALTY_INCENTIVE_ID";
    private static final String PLAN_ID = "LOYALTY_PLAN_ID";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return null;
    }

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
    public SingleSqlCommand insertSQL(LoyaltyIncentivePlanModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(LoyaltyIncentivePlanModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}
