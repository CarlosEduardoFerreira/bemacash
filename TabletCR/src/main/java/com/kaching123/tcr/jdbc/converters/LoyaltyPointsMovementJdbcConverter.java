package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.LoyaltyPointsMovementModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by vkompaniets on 01.07.2016.
 */
public class LoyaltyPointsMovementJdbcConverter extends JdbcConverter<LoyaltyPointsMovementModel> {

    private static final String TABLE_NAME = "LOYALTY_POINTS_MOVEMENT";

    private static final String ID = "ID";
    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static final String LOYALTY_POINTS = "LOYALTY_POINTS";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return null;
    }

    @Override
    public LoyaltyPointsMovementModel toValues(JdbcJSONObject rs) throws JSONException {
        return new LoyaltyPointsMovementModel(
                rs.getString(ID),
                rs.getString(CUSTOMER_ID),
                rs.getBigDecimal(LOYALTY_POINTS)
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
    public SingleSqlCommand insertSQL(LoyaltyPointsMovementModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(CUSTOMER_ID, model.customerId)
                .add(LOYALTY_POINTS, model.loyaltyPoints)
                .build(JdbcFactory.getApiMethod(model));

    }

    @Override
    public SingleSqlCommand updateSQL(LoyaltyPointsMovementModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(CUSTOMER_ID, model.customerId)
                .add(LOYALTY_POINTS, model.loyaltyPoints)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }
}
