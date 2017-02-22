package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.LoyaltyIncentiveModel;
import com.kaching123.tcr.model.LoyaltyRewardType;
import com.kaching123.tcr.model.LoyaltyType;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by vkompaniets on 28.06.2016.
 */
public class LoyaltyIncentiveJdbcConverter extends JdbcConverter<LoyaltyIncentiveModel> {

    public static final String TABLE_NAME = "LOYALTY_INCENTIVE";

    private static final String ID = "ID";
    private static final String NAME = "NAME";
    private static final String TYPE = "TYPE";
    private static final String REWARD_TYPE = "REWARD_TYPE";
    private static final String BIRTHDAY_OFFSET = "BIRTHDAY_OFFSET";
    private static final String POINTS_THRESHOLD = "POINTS_THRESHOLD";
    private static final String REWARD_VALUE = "REWARD_VALUE";
    private static final String REWARD_VALUE_TYPE = "REWARD_VALUE_TYPE";

    @Override
    public LoyaltyIncentiveModel toValues(JdbcJSONObject rs) throws JSONException {
        return new LoyaltyIncentiveModel(
                rs.getString(ID),
                rs.getString(NAME),
                _enum(LoyaltyType.class, rs.getString(TYPE), null),
                _enum(LoyaltyRewardType.class, rs.getString(REWARD_TYPE), null),
                rs.getInt(BIRTHDAY_OFFSET),
                rs.getBigDecimal(POINTS_THRESHOLD),
                rs.getBigDecimal(REWARD_VALUE),
                _enum(DiscountType.class, rs.getString(REWARD_VALUE_TYPE), null)
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
        return null;
    }

    @Override
    public JSONObject getJSONObject(LoyaltyIncentiveModel model) {
        return null;
    }

    @Override
    public SingleSqlCommand insertSQL(LoyaltyIncentiveModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(LoyaltyIncentiveModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}
