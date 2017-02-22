package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.model.TBPxRegisterModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vkompaniets on 14.08.2016.
 */
public class TBPxRegisterJdbcConverter extends JdbcConverter<TBPxRegisterModel> {

    public static final String TABLE_NAME = "TIME_BASED_PRICING_REGISTER";

    private static final String ID = "ID";
    private static final String TBP_ID = "TIME_BASED_PRICING_ID";
    private static final String REGISTER_ID = "REGISTER_ID";

    @Override
    public TBPxRegisterModel toValues(JdbcJSONObject rs) throws JSONException {
        return new TBPxRegisterModel(
                rs.getLong(ID),
                rs.getString(TBP_ID),
                rs.getLong(REGISTER_ID)
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
    public JSONObject getJSONObject(TBPxRegisterModel model) {
        return null;
    }

    @Override
    public SingleSqlCommand insertSQL(TBPxRegisterModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(TBPxRegisterModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}
