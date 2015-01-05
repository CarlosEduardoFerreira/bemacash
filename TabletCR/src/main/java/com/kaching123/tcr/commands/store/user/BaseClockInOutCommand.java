package com.kaching123.tcr.commands.store.user;

import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeeStatus;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.telly.groundy.TaskResult;

import static com.kaching123.tcr.model.ContentValuesUtil._employeeStatus;

/**
 * Created by gdubina on 16/01/14.
 */
public abstract class BaseClockInOutCommand extends AsyncCommand {

    public static enum ClockInOutError {USER_DOES_NOT_EXIST, EMPLOYEE_NOT_ACTIVE, ALREADY_CLOCKED_IN, ALREADY_CLOCKED_OUT}

    protected static final Uri EMPLOYEE_TIMESHEET_URI = ShopProvider.getContentUri(ShopStore.EmployeeTimesheetTable.URI_CONTENT);

    protected static final String ARG_LOGIN = "ARG_LOGIN";

    protected static final String EXTRA_ERROR = "EXTRA_ERROR";
    protected static final String RESULT_TIME = "RESULT_TIME";
    protected static final String RESULT_NAME = "RESULT_NAME";
    protected static final String RESULT_GUID = "RESULT_GUID";


    @Override
    protected TaskResult doCommand() {
        String login = getStringArg(ARG_LOGIN);
        EmployeeModel employee = getUserGuid(login);
        if (employee == null){
            return failed().add(EXTRA_ERROR, ClockInOutError.USER_DOES_NOT_EXIST);
        }
        if (employee.status != EmployeeStatus.ACTIVE){
            return failed().add(EXTRA_ERROR, ClockInOutError.EMPLOYEE_NOT_ACTIVE);
        }
        return doInternalCommand(employee);
    }

    protected abstract TaskResult doInternalCommand(EmployeeModel employee);

    private EmployeeModel getUserGuid(String login/*, String password*/) {
        Cursor c = ProviderAction.query(ShopProvider.getContentUri(ShopStore.EmployeeTable.URI_CONTENT))
                .projection(EmployeeTable.GUID, EmployeeTable.FIRST_NAME, EmployeeTable.LAST_NAME, EmployeeTable.STATUS)
                .where(ShopStore.EmployeeTable.LOGIN + " = ?", login)
                        //.where(ShopStore.EmployeeTable.PASSWORD + " = ?", password)
                .perform(getContext());
        EmployeeModel employee = null;
        if (c.moveToFirst()) {
            employee = new EmployeeModel(c.getString(0), c.getString(1), c.getString(2), login, _employeeStatus(c, 3));
        }
        c.close();
        return employee;
    }

}
