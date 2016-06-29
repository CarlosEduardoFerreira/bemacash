package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.LoyaltyIncentiveItemModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vkompaniets on 29.06.2016.
 */
public class LoyaltyIncentiveItemJdbcConverter extends JdbcConverter<LoyaltyIncentiveItemModel> {

    private static final String TABLE_NAME = "LOYALTY_INCENTIVE_ITEM";

    private static final String ID = "ID";
    private static final String INCENTIVE_ID = "INCENTIVE_ID";
    private static final String ITEM_ID = "ITEM_ID";
    private static final String PRICE = "PRICE";
    private static final String QTY = "QTY";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return null;
    }

    @Override
    public LoyaltyIncentiveItemModel toValues(JdbcJSONObject rs) throws JSONException {
        return new LoyaltyIncentiveItemModel(
                rs.getString(ID),
                rs.getString(INCENTIVE_ID),
                rs.getString(ITEM_ID),
                rs.getBigDecimal(PRICE),
                rs.getBigDecimal(QTY, ContentValuesUtil.QUANTITY_SCALE)
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
    public SingleSqlCommand insertSQL(LoyaltyIncentiveItemModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(LoyaltyIncentiveItemModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}
