package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.model.TBPModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vkompaniets on 14.08.2016.
 */
public class TBPJdbcConverter extends JdbcConverter<TBPModel> {

    private static final String TABLE_NAME = "TIME_BASED_PRICING";

    private static final String ID = "ID";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String PRICE_LEVEL = "PRICE_LEVEL";
    private static final String IS_ACTIVE = "IS_ACTIVE";
    private static final String START_DATE = "START_DATE";
    private static final String END_DATE = "END_DATE";
    private static final String MON_START = "MON_START";
    private static final String MON_END = "MON_END";
    private static final String TUE_START = "TUE_START";
    private static final String TUE_END = "TUE_END";
    private static final String WED_START = "WED_START";
    private static final String WED_END = "WED_END";
    private static final String THU_START = "THU_START";
    private static final String THU_END = "THU_END";
    private static final String FRI_START = "FRI_START";
    private static final String FRI_END = "FRI_END";
    private static final String SAT_START = "SAT_START";
    private static final String SAT_END = "SAT_END";
    private static final String SUN_START = "SUN_START";
    private static final String SUN_END = "SUN_END";


    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return null;
    }

    @Override
    public TBPModel toValues(JdbcJSONObject rs) throws JSONException {
        return new TBPModel(
                rs.getLong(ID),
                rs.getString(DESCRIPTION),
                rs.getInt(PRICE_LEVEL),
                rs.getBoolean(IS_ACTIVE),
                rs.getSimpleDate(START_DATE),
                rs.getSimpleDate(END_DATE),
                rs.getString(MON_START),
                rs.getString(MON_END),
                rs.getString(TUE_START),
                rs.getString(TUE_END),
                rs.getString(WED_START),
                rs.getString(WED_END),
                rs.getString(THU_START),
                rs.getString(THU_END),
                rs.getString(FRI_START),
                rs.getString(FRI_END),
                rs.getString(SAT_START),
                rs.getString(SAT_END),
                rs.getString(SUN_START),
                rs.getString(SUN_END)
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
    public SingleSqlCommand insertSQL(TBPModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(TBPModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}
