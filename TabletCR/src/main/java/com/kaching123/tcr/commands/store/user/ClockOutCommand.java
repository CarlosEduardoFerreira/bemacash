package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.EmployeeTimesheetJdbcConverter;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeeTimesheetModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
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

import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;
import static com.kaching123.tcr.util.CursorUtil._wrapOrNull;

/**
 * Created by pkabakov on 24/12/13.
 */
public class ClockOutCommand extends BaseClockInOutCommand {

    private EmployeeTimesheetModel model;

    @Override
    protected TaskResult doInternalCommand(EmployeeModel employee) {
        EmployeeTimesheetModel lastTimesheet = getLastTimesheet(employee.guid);
        boolean isClockedIn = lastTimesheet != null && lastTimesheet.clockOut == null;
        if (!isClockedIn)
            return failed().add(EXTRA_ERROR, ClockInOutError.ALREADY_CLOCKED_OUT);

        model = new EmployeeTimesheetModel(
                lastTimesheet.guid,
                null,
                Util.cropSeconds(new Date()),
                null,
                null
        );

        return succeeded().add(RESULT_GUID, model.employeeGuid).add(RESULT_TIME, model.clockOut).add(RESULT_NAME, employee.fullName());
    }

    private EmployeeTimesheetModel getLastTimesheet(String userGuid) {
        return _wrapOrNull(ProviderAction.query(ShopProvider.getContentWithLimitUri(ShopStore.EmployeeTimesheetTable.URI_CONTENT, 1))
                .where(ShopStore.EmployeeTimesheetTable.EMPLOYEE_GUID + " = ?", userGuid)
                .orderBy(ShopStore.EmployeeTimesheetTable.CLOCK_IN + " DESC")
                .perform(getContext()),
                new ListConverterFunction<EmployeeTimesheetModel>() {
                    @Override
                    public EmployeeTimesheetModel apply(Cursor cursor) {
                        super.apply(cursor);
                        EmployeeTimesheetModel model = new EmployeeTimesheetModel(
                                cursor.getString(indexHolder.get(ShopStore.EmployeeTimesheetTable.GUID)),
                                _nullableDate(cursor, indexHolder.get(ShopStore.EmployeeTimesheetTable.CLOCK_IN)),
                                _nullableDate(cursor, indexHolder.get(ShopStore.EmployeeTimesheetTable.CLOCK_OUT)),
                                cursor.getString(indexHolder.get(ShopStore.EmployeeTimesheetTable.EMPLOYEE_GUID)),
                                null
                        );
                        return model;
                    }
                }
        );

    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(EMPLOYEE_TIMESHEET_URI)
                .withValues(model.toUpdateValues())
                .withSelection(ShopStore.EmployeeTimesheetTable.GUID + " = ?", new String[]{model.guid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return ((EmployeeTimesheetJdbcConverter) JdbcFactory.getConverter(model)).updateOutSQL(model, getAppCommandContext());
    }

    public static void start(Context context, String login/*, String password*/, BaseClockOutCallback callback) {
        create(ClockOutCommand.class).arg(ARG_LOGIN, login)/*.arg(ARG_PASSWORD, password)*/.callback(callback).queueUsing(context);
    }

    public static abstract class BaseClockOutCallback {

        @OnSuccess(ClockOutCommand.class)
        public void onSuccess(@Param(RESULT_GUID) String employeeGuid, @Param(RESULT_TIME) Date time, @Param(RESULT_NAME) String fullName) {
            onClockOut(employeeGuid, fullName, time);
        }

        @OnFailure(ClockOutCommand.class)
        public void onFailure(@Param(EXTRA_ERROR) ClockInOutError error) {
            onClockOutError(error);
        }

        protected abstract void onClockOut(String guid, String fullName, Date time);

        protected abstract void onClockOutError(ClockInOutError error);

    }
}
