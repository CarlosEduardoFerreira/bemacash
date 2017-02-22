package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

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
import java.util.UUID;

import static com.kaching123.tcr.reports.ClockInOutReportQuery.isOverlapped;

/**
 * Created by gdubina on 05/02/14.
 */
public class AddClockInCommand extends AsyncCommand{

    private static final Uri URI = ShopProvider.getContentUri(EmployeeTimesheetTable.URI_CONTENT);

    private static final String ARG_EMPLOYEE = "ARG_EMPLOYEE";
    private static final String ARG_IN = "ARG_IN";
    private static final String ARG_OUT = "ARG_OUT";

    private EmployeeTimesheetModel model;

    @Override
    protected TaskResult doCommand() {
        String employeeGuid = getStringArg(ARG_EMPLOYEE);
        long in = getLongArg(ARG_IN);
        long out = getLongArg(ARG_OUT);

        String guid = UUID.randomUUID().toString();
        if (isOverlapped(getContext(), new TimeInfo(guid, new Date(in), out == 0 ? null : new Date(out)), employeeGuid))
            return failed().add(ClockInOutReportQuery.EXTRA_OVERLAPS, ClockInOutReportQuery.Error.OVERLAPS);

        model = new EmployeeTimesheetModel(
                guid,
                Util.cropSeconds(new Date(in)),
                out == 0 ? null : Util.cropSeconds(new Date(out)),
                employeeGuid,
                null);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newInsert(URI).withValues(model.toValues()).build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(model).insertSQL(model, getAppCommandContext());
    }

    public static void start(Context context, String employeeGuid, long in, long out, BaseAddClockInCommandCallback callback) {
        create(AddClockInCommand.class).arg(ARG_EMPLOYEE, employeeGuid).arg(ARG_IN, in).arg(ARG_OUT, out)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseAddClockInCommandCallback {

        @OnFailure(AddClockInCommand.class)
        public void handleFailure(@Param(ClockInOutReportQuery.EXTRA_OVERLAPS)ClockInOutReportQuery.Error error){
            if (error == ClockInOutReportQuery.Error.OVERLAPS)
                onOverlaps();
        }

        protected abstract void onOverlaps();
    }



}
