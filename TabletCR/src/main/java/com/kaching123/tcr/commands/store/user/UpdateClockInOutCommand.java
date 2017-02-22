package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.EmployeeTimesheetModel;
import com.kaching123.tcr.reports.ClockInOutReportQuery;
import com.kaching123.tcr.reports.ClockInOutReportQuery.TimeInfo;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTimesheetTable;
import com.kaching123.tcr.util.Util;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;
import java.util.Date;

import static com.kaching123.tcr.reports.ClockInOutReportQuery.isOverlapped;

/**
 * Created by gdubina on 05/02/14.
 */
public class UpdateClockInOutCommand extends AsyncCommand{

    private static final Uri URI = ShopProvider.getContentUri(EmployeeTimesheetTable.URI_CONTENT);

    private static final String ARG_EMPLOYEE_GUID = "ARG_EMPLOYEE_GUID";
    private static final String ARG_TIME_GUID = "ARG_TIME_GUID";
    private static final String ARG_TIME = "ARG_TIME";
    private static final String ARG_IN = "ARG_IN";

    private EmployeeTimesheetModel model;

    @Override
    protected TaskResult doCommand() {
        String employeeGuid = getStringArg(ARG_EMPLOYEE_GUID);
        String timeGuid = getStringArg(ARG_TIME_GUID);
        long time = getLongArg(ARG_TIME);
        boolean isIn = getBooleanArg(ARG_IN);
        model = findModel(timeGuid);
        if(model == null)
            return failed();

        if(isIn){
            model.clockIn = Util.cropSeconds(new Date(time));
        }else{
            model.clockOut = Util.cropSeconds(new Date(time));
        }

        if (isOverlapped(getContext(), new TimeInfo(timeGuid, model.clockIn, model.clockOut), employeeGuid))
            return failed().add(ClockInOutReportQuery.EXTRA_OVERLAPS, ClockInOutReportQuery.Error.OVERLAPS);

        return succeeded();
    }

    private EmployeeTimesheetModel findModel(String guid){
        Cursor c = ProviderAction
                .query(URI)
                .projection(
                        EmployeeTimesheetTable.GUID,
                        EmployeeTimesheetTable.CLOCK_IN,
                        EmployeeTimesheetTable.CLOCK_OUT,
                        EmployeeTimesheetTable.EMPLOYEE_GUID
                )
                .where(EmployeeTimesheetTable.GUID + " = ?", guid)
                .perform(getContext());

        EmployeeTimesheetModel model = null;
        if(c.moveToFirst()){
            long in = c.getLong(1);
            long out = c.getLong(2);
            model = new EmployeeTimesheetModel(
                    c.getString(0),
                    new Date(in),
                    out == 0 ? null : new Date(out),
                    c.getString(3),
                    null
            );
        }
        c.close();
        return model;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(URI)
                .withValues(model.toValues())
                .withSelection(EmployeeTimesheetTable.GUID + " = ?", new String[]{model.guid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(model).updateSQL(model, getAppCommandContext());
    }

    public static void start(Context context, String employeeGuid, String timeGuid, boolean in, long datime, BaseUpdateClockInCommandCallback callback) {
        create(UpdateClockInOutCommand.class).arg(ARG_EMPLOYEE_GUID, employeeGuid).arg(ARG_TIME_GUID, timeGuid).arg(ARG_IN, in).arg(ARG_TIME, datime)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseUpdateClockInCommandCallback {

        @OnFailure(UpdateClockInOutCommand.class)
        public void handleFailure(@Param(ClockInOutReportQuery.EXTRA_OVERLAPS)ClockInOutReportQuery.Error error){
            if (error == ClockInOutReportQuery.Error.OVERLAPS)
                onOverlaps();
        }

        protected abstract void onOverlaps();
    }
}
