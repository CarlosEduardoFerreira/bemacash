package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.model.ActivationCarrierModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vkompaniets on 03.07.2014.
 */
public class ActivationCarrierJdbcConverter extends JdbcConverter<ActivationCarrierModel> {

    public static final String VIEW_NAME = "ACTIVATION_VIEW";

    private static final String ID = "ID";
    private static final String NAME = "NAME";
    private static final String URL = "URL";
    private static final String IS_ACTIVE = "IS_ACTIVE";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new ActivationCarrierModel(
                rs.getLong(ID),
                rs.getString(NAME),
                rs.getString(URL),
                rs.getBoolean(IS_ACTIVE)
        ).toValues();
    }

    @Override
    public ActivationCarrierModel toValues(JdbcJSONObject rs) throws JSONException {
        return new ActivationCarrierModel(
                rs.getLong(ID),
                rs.getString(NAME),
                rs.getString(URL),
                rs.getBoolean(IS_ACTIVE)
        );
    }

    @Override
    public String getTableName() {
        return VIEW_NAME;
    }

    @Override
    public String getGuidColumn() {
        return ID;
    }

    @Override
    public SingleSqlCommand insertSQL(ActivationCarrierModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(ActivationCarrierModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportUpdateTimeFlag() {
        return false;
    }

    @Override
    public boolean supportDraftFlag() {
        return false;
    }

    @Override
    public boolean supportDeleteFlag() {
        return false;
    }
}
