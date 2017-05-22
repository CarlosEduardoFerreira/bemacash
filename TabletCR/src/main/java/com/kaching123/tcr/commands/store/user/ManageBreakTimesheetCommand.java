package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.EmployeeBreakTimesheetModel;
import com.kaching123.tcr.model.EmployeeTimesheetModel;
import com.kaching123.tcr.model.converter.ListConverterFunction;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.Util;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;
import static com.kaching123.tcr.util.CursorUtil._wrapOrNull;

/**
 * Created by mboychenko on 5/22/2017.
 */

public class ManageBreakTimesheetCommand extends AsyncCommand {

    private static final Uri EMPLOYEE_BREAKS_TIMESHEET_URI = ShopProvider.getContentUri(ShopStore.EmployeeBreaksTimesheetTable.URI_CONTENT);

    public static final String ARG_EMPLOYEE = "ARG_EMPLOYEE";
    public static final String ARG_ACTION = "ARG_ACTION";

    public enum Action {
        START_BREAK,
        STOP_BREAK
    }

    private String employeeGuid;
    private Action action;
    private EmployeeBreakTimesheetModel breakModel;

    @Override
    protected TaskResult doCommand() {
        employeeGuid = getStringArg(ARG_EMPLOYEE);
        action = (Action) getArgs().getSerializable(ARG_ACTION);
        EmployeeTimesheetModel lastTimesheetModel = getLastTimesheet(employeeGuid);
        if (lastTimesheetModel != null && lastTimesheetModel.clockOut == null) {
            if (action == Action.START_BREAK) {
                breakModel = new EmployeeBreakTimesheetModel(
                        UUID.randomUUID().toString(),
                        Util.cropSeconds(new Date()),
                        null,
                        employeeGuid,
                        lastTimesheetModel.guid,
                        null
                );
            } else {
                Cursor c = ProviderAction.query(ShopProvider.getContentWithLimitUri(ShopStore.EmployeeBreaksTimesheetTable.URI_CONTENT, 1))
                        .where(ShopStore.EmployeeBreaksTimesheetTable.EMPLOYEE_GUID + " = ?", employeeGuid)
                        .where(ShopStore.EmployeeBreaksTimesheetTable.CLOCK_IN_GUID + " = ?", lastTimesheetModel.getGuid())
                        .orderBy(ShopStore.EmployeeBreaksTimesheetTable.BREAK_START + " DESC")
                        .perform(getContext());
                if (c != null && c.moveToFirst()) {
                    breakModel = new EmployeeBreakTimesheetModel(c);
                    breakModel.breakEnd = Util.cropSeconds(new Date());
                    c.close();
                }
            }
        } else {
            return failed();
        }
        return succeeded();
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
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(breakModel).insertSQL(breakModel, getAppCommandContext());
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newInsert(EMPLOYEE_BREAKS_TIMESHEET_URI).withValues(breakModel.toValues()).build());
        return operations;
    }

    public static void start(Context context, String employeeGuid, Action action, BreakTimesheetCallback callback) {
        create(ManageBreakTimesheetCommand.class).arg(ARG_EMPLOYEE, employeeGuid).arg(ARG_ACTION, action)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BreakTimesheetCallback {

        @OnSuccess(ManageBreakTimesheetCommand.class)
        public void onSuccess() {
            onFinish();
        }

        @OnFailure(ManageBreakTimesheetCommand.class)
        public void onFailure() {
            onFinish();
        }

        protected abstract void onFinish();

    }
}
