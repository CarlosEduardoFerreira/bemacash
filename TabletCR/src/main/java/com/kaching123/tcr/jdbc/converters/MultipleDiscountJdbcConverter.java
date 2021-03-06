package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.model.MultipleDiscountModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vkompaniets on 23.08.2016.
 */
public class MultipleDiscountJdbcConverter extends JdbcConverter<MultipleDiscountModel> {

    public static final String TABLE_NAME = "MULTIPLE_DISCOUNT_ITEM_VIEW";

    private static final String ID = "ID";
    private static final String BUNDLE_ID = "MULTIPLE_DISCOUNT_ID";
    private static final String ITEM_ID = "ITEM_ID";
    private static final String QTY = "QTY";
    private static final String DISCOUNT = "DISCOUNT";
    private static final String STATUS = "STATUS";

    @Override
    public MultipleDiscountModel toValues(JdbcJSONObject rs) throws JSONException {
        return new MultipleDiscountModel(
                rs.getString(ID),
                rs.getString(BUNDLE_ID),
                rs.getString(ITEM_ID),
                rs.getBigDecimal(QTY, 3),
                rs.getBigDecimal(DISCOUNT),
                "ACTIVE".equals(rs.getString(STATUS))
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
    public JSONObject getJSONObject(MultipleDiscountModel model) {
        return null;
    }

    @Override
    public SingleSqlCommand insertSQL(MultipleDiscountModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(MultipleDiscountModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}
