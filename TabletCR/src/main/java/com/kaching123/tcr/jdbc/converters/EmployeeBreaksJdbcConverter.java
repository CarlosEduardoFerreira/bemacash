package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.EmployeeBreakTimesheetModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by mboychenko on 5/22/2017.
 */

public class EmployeeBreaksJdbcConverter extends JdbcConverter<EmployeeBreakTimesheetModel> {

    public static final String TABLE_NAME = "EMPLOYEE_BREAKS_TIMESHEET";

    private static final String ID = "ID";
    private static final String EMPLOYEE_ID = "EMPLOYEE_ID";
    private static final String CLOCK_IN_ID = "CLOCK_IN_ID";
    private static final String BREAK_START = "BREAK_START";
    private static final String BREAK_END = "BREAK_END";

    @Override
    public EmployeeBreakTimesheetModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.EmployeeBreaksTimesheetTable.GUID);
        if (!rs.has(BREAK_START)) ignoreFields.add(ShopStore.EmployeeBreaksTimesheetTable.BREAK_START);
        if (!rs.has(BREAK_END)) ignoreFields.add(ShopStore.EmployeeBreaksTimesheetTable.BREAK_END);
        if (!rs.has(EMPLOYEE_ID)) ignoreFields.add(ShopStore.EmployeeBreaksTimesheetTable.EMPLOYEE_GUID);
        if (!rs.has(CLOCK_IN_ID)) ignoreFields.add(ShopStore.EmployeeBreaksTimesheetTable.CLOCK_IN_GUID);

        return new EmployeeBreakTimesheetModel(
                rs.getString(ID),
                rs.getDate(BREAK_START),
                rs.getDate(BREAK_END),
                rs.getString(EMPLOYEE_ID),
                rs.getString(CLOCK_IN_ID),
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
        return ShopStore.EmployeeBreaksTimesheetTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(EmployeeBreakTimesheetModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(EMPLOYEE_ID, model.employeeGuid)
                    .put(CLOCK_IN_ID, model.clockInGuid)
                    .put(BREAK_START, model.startBreak)
                    .put(BREAK_END, model.breakEnd);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(EmployeeBreakTimesheetModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(EMPLOYEE_ID, model.employeeGuid)
                .add(CLOCK_IN_ID, model.clockInGuid)
                .add(BREAK_START, model.startBreak)
                .add(BREAK_END, model.breakEnd)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(EmployeeBreakTimesheetModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(BREAK_START, model.startBreak)
                .add(BREAK_END, model.breakEnd)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand updateEndOfBreakSQL(EmployeeBreakTimesheetModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(BREAK_END, model.breakEnd)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
