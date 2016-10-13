package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.EmployeeTimesheetModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by gdubina on 03/12/13.
 */
public class EmployeeTimesheetJdbcConverter extends JdbcConverter<EmployeeTimesheetModel> {

    private static final String TABLE_NAME = "EMPLOYEE_TIMESHEET";

    private static final String ID = "ID";
    private static final String EMPLOYEE_ID = "EMPLOYEE_ID";
    private static final String CLOCK_IN = "CLOCK_IN";
    private static final String CLOCK_OUT = "CLOCK_OUT";

    @Override
    public EmployeeTimesheetModel toValues(JdbcJSONObject rs) throws JSONException {
        return new EmployeeTimesheetModel(
                rs.getString(ID),
                rs.getDate(CLOCK_IN),
                rs.getDate(CLOCK_OUT),
                rs.getString(EMPLOYEE_ID)
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

}
