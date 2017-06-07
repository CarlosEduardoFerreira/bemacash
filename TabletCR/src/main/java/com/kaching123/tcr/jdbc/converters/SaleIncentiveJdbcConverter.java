package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.LoyaltyRewardType;
import com.kaching123.tcr.model.LoyaltyType;
import com.kaching123.tcr.model.SaleIncentiveModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by vkompaniets on 18.07.2016.
 */
public class SaleIncentiveJdbcConverter extends JdbcConverter<SaleIncentiveModel> {

    public static final String TABLE_NAME = "SALE_INCENTIVE";

    private static final String ID = "ID";
    private static final String INCENTIVE_ID = "INCENTIVE_ID";
    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static final String ORDER_ID = "ORDER_ID";
    private static final String TYPE = "TYPE";
    private static final String REWARD_TYPE = "REWARD_TYPE";
    private static final String POINTS_THRESHOLD = "POINTS_THRESHOLD";
    private static final String REWARD_VALUE = "REWARD_VALUE";
    private static final String REWARD_VALUE_TYPE = "REWARD_VALUE_TYPE";
    private static final String SALE_ITEM_ID = "SALE_ITEM_ID";

    @Override
    public SaleIncentiveModel toValues(JdbcJSONObject rs) throws JSONException {
        return new SaleIncentiveModel(
                rs.getString(ID),
                rs.getString(INCENTIVE_ID),
                rs.getString(CUSTOMER_ID),
                rs.getString(ORDER_ID),
                _enum(LoyaltyType.class, rs.getString(TYPE), null),
                _enum(LoyaltyRewardType.class, rs.getString(REWARD_TYPE), null),
                rs.getBigDecimal(REWARD_VALUE),
                _enum(DiscountType.class, rs.getString(REWARD_VALUE_TYPE), null),
                rs.getString(SALE_ITEM_ID),
                rs.getBigDecimal(POINTS_THRESHOLD)
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
    public JSONObject getJSONObject(SaleIncentiveModel model) {
        return null;
    }

    @Override
    public SingleSqlCommand insertSQL(SaleIncentiveModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(INCENTIVE_ID, model.incentiveId)
                .add(CUSTOMER_ID, model.customerId)
                .add(ORDER_ID, model.orderId)
                .add(TYPE, model.type)
                .add(REWARD_TYPE, model.rewardType)
                .add(REWARD_VALUE, model.rewardValue)
                .add(REWARD_VALUE_TYPE, model.rewardValueType)
                .add(SALE_ITEM_ID, model.saleItemId)
                .add(POINTS_THRESHOLD, model.pointsThreshold)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(SaleIncentiveModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
