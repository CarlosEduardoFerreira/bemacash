package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.LoyaltyIncentiveItemModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vkompaniets on 29.06.2016.
 */
public class LoyaltyIncentiveItemJdbcConverter extends JdbcConverter<LoyaltyIncentiveItemModel> {

    public static final String TABLE_NAME = "LOYALTY_INCENTIVE_ITEM";

    private static final String ID = "ID";
    private static final String INCENTIVE_ID = "INCENTIVE_ID";
    private static final String ITEM_ID = "ITEM_ID";
    private static final String PRICE = "PRICE";
    private static final String QTY = "QTY";

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
    public String getLocalGuidColumn() {
        return ShopStore.LoyaltyIncentiveItemTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(LoyaltyIncentiveItemModel model) {
        JSONObject json = null;
        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(INCENTIVE_ID, model.incentiveGuid)
                    .put(ITEM_ID, model.itemGuid)
                    .put(PRICE, model.price)
                    .put(QTY, model.qty);
        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }
        return json;
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
