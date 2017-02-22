package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.EmployeeTimesheetModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by gdubina on 03/12/13.
 */
public class EmployeeTimesheetJdbcConverter extends JdbcConverter<EmployeeTimesheetModel> {

    public static final String TABLE_NAME = "EMPLOYEE_TIMESHEET";

    private static final String ID = "ID";
    private static final String EMPLOYEE_ID = "EMPLOYEE_ID";
    private static final String CLOCK_IN = "CLOCK_IN";
    private static final String CLOCK_OUT = "CLOCK_OUT";

    @Override
    public EmployeeTimesheetModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.EmployeeTimesheetTable.GUID);
        if (!rs.has(CLOCK_IN)) ignoreFields.add(ShopStore.EmployeeTimesheetTable.CLOCK_IN);
        if (!rs.has(CLOCK_OUT)) ignoreFields.add(ShopStore.EmployeeTimesheetTable.CLOCK_OUT);
        if (!rs.has(EMPLOYEE_ID)) ignoreFields.add(ShopStore.EmployeeTimesheetTable.EMPLOYEE_GUID);

        return new EmployeeTimesheetModel(
                rs.getString(ID),
                rs.getDate(CLOCK_IN),
                rs.getDate(CLOCK_OUT),
                rs.getString(EMPLOYEE_ID),
                ignoreFields
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
        return ShopStore.EmployeeTimesheetTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(EmployeeTimesheetModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(EMPLOYEE_ID, model.employeeGuid)
                    .put(CLOCK_IN, model.clockIn)
                    .put(CLOCK_OUT, model.clockOut);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(EmployeeTimesheetModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(EMPLOYEE_ID, model.employeeGuid)
                .add(CLOCK_IN, model.clockIn)
                .add(CLOCK_OUT, model.clockOut)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(EmployeeTimesheetModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(CLOCK_IN, model.clockIn)
                .add(CLOCK_OUT, model.clockOut)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand updateOutSQL(EmployeeTimesheetModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(CLOCK_OUT, model.clockOut)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }

}
