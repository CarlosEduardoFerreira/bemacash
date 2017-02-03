package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.model.DefinedOnHoldModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask;

import org.json.JSONException;

/**
 * Created by mboychenko on 2/3/2017.
 */

public class DefinedOnHoldJdbcConverter extends JdbcConverter<DefinedOnHoldModel> {

    private static final String DEFINED_ON_HOLD_TABLE_NAME = "DEFINED_ON_HOLD";

    private static final String ID = "ID";
    private static final String NAME = "NAME";

    @Override
    public DefinedOnHoldModel toValues(JdbcJSONObject rs) throws JSONException {
        return new DefinedOnHoldModel(rs.getString(ID), rs.getString(NAME));
    }

    @Override
    public String getTableName() {
        return DEFINED_ON_HOLD_TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return ID;
    }

    @Override
    public SingleSqlCommand insertSQL(DefinedOnHoldModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        return null;
    }

    @Override
    public SingleSqlCommand updateSQL(DefinedOnHoldModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        return null;
    }
}
