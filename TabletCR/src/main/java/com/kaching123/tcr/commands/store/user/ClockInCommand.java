package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeeTimesheetModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.Util;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;

/**
 * Created by pkabakov on 24/12/13.
 */
public class ClockInCommand extends BaseClockInOutCommand {

    private EmployeeTimesheetModel model;

    @Override
    protected TaskResult doInternalCommand(EmployeeModel employee) {
        if (!checkClockOut(employee.guid))
            return failed().add(EXTRA_ERROR, ClockInOutError.ALREADY_CLOCKED_IN);

        model = new EmployeeTimesheetModel(
                UUID.randomUUID().toString(),
                Util.cropSeconds(new Date()),
                null,
                employee.guid
        );

        return succeeded().add(RESULT_GUID, model.employeeGuid).add(RESULT_TIME, model.clockIn).add(RESULT_NAME, employee.fullName());
    }

    private boolean checkClockOut(String userGuid) {
        Date clockOut = null;
        Cursor c = ProviderAction.query(ShopProvider.getContentWithLimitUri(ShopStore.EmployeeTimesheetTable.URI_CONTENT, 1))
                .projection(ShopStore.EmployeeTimesheetTable.CLOCK_OUT)
                .where(ShopStore.EmployeeTimesheetTable.EMPLOYEE_GUID + " = ?", userGuid)
                .orderBy(ShopStore.EmployeeTimesheetTable.CLOCK_IN + " DESC")
                .perform(getContext());
        boolean isEmpty = c.getCount() == 0;
        if (c.moveToFirst()) {
            clockOut = _nullableDate(c, 0);
        }
        c.close();
        return isEmpty || clockOut != null;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newInsert(EMPLOYEE_TIMESHEET_URI).withValues(model.toValues()).build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(model).insertSQL(model, getAppCommandContext());
    }

    public static void start(Context context, String login/*, String password*/, BaseClockInCallback callback) {
        create(ClockInCommand.class).arg(ARG_LOGIN, login)/*.arg(ARG_PASSWORD, password)*/.callback(callback).queueUsing(context);
    }

    public static abstract class BaseClockInCallback {

        @OnSuccess(ClockInCommand.class)
        public void onSuccess(@Param(RESULT_GUID) String employeeGuid, @Param(RESULT_TIME) Date time, @Param(RESULT_NAME) String fullName) {
            onClockIn(employeeGuid, fullName, time);
        }

        @OnFailure(ClockInCommand.class)
        public void onFailure(@Param(EXTRA_ERROR) ClockInOutError error) {
            onClockInError(error);
        }

        protected abstract void onClockIn(String guid, String fullName, Date time);

        protected abstract void onClockInError(ClockInOutError error);

    }
}
