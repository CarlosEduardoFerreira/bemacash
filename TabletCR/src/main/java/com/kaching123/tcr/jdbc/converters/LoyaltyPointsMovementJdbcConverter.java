package com.kaching123.tcr.jdbc.converters;

import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.LoyaltyPointsMovementModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by vkompaniets on 01.07.2016.
 */
public class LoyaltyPointsMovementJdbcConverter extends JdbcConverter<LoyaltyPointsMovementModel> {

    public static final String TABLE_NAME = "LOYALTY_POINTS_MOVEMENT";

    private static final String ID = "ID";
    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static final String LOYALTY_POINTS = "LOYALTY_POINTS";
    private static final String SHOP_ID = "SHOP_ID";

    @Override
    public LoyaltyPointsMovementModel toValues(JdbcJSONObject rs) throws JSONException {
        return new LoyaltyPointsMovementModel(
                rs.getString(ID),
                rs.getString(CUSTOMER_ID),
                rs.getBigDecimal(LOYALTY_POINTS),
                rs.getLong(SHOP_ID)
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
        return ShopStore.LoyaltyPointsMovementTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(LoyaltyPointsMovementModel model) {
        JSONObject json = null;
        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(CUSTOMER_ID, model.customerId)
                    .put(LOYALTY_POINTS, model.loyaltyPoints)
                    .put(SHOP_ID, model.shopId);
        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }
        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(LoyaltyPointsMovementModel model, IAppCommandContext appCommandContext) {
        Log.d("BemaCarl22","LoyaltyPointsMovementJdbcConverter.insertSQL.model.loyaltyPoints: " + model.loyaltyPoints);
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(CUSTOMER_ID, model.customerId)
                .add(LOYALTY_POINTS, model.loyaltyPoints)
                .add(SHOP_ID, model.shopId)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(LoyaltyPointsMovementModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(CUSTOMER_ID, model.customerId)
                .add(LOYALTY_POINTS, model.loyaltyPoints)
                .add(SHOP_ID, model.shopId)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
