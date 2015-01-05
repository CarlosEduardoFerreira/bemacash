package com.kaching123.tcr.commands.store.export;

import android.content.Context;

import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

/**
 * Created by gdubina on 29.01.14.
 */
public class ExportReportsCommand extends PublicGroundyTask {

    private static final String ARG_REPORT_TYPE = "ARG_REPORT_TYPE";

    @Override
    protected TaskResult doInBackground() {
        ReportType type = (ReportType)getArgs().getSerializable(ARG_REPORT_TYPE);
        switch (type){
            case SALES_TOP_10_QTY:
                return new ExportTop10QtyItemsCommand().sync(getContext(), getArgs(), getAppCommandContext());
            case SALES_TOP_10_REVENUES:
                return new ExportTop10RevenuesItemsCommand().sync(getContext(), getArgs(), getAppCommandContext());
            case SALES_BY_ITEMS:
                return new ExportSoldItemsCommand().sync(getContext(), getArgs(), getAppCommandContext());
            case RETURNED_ITEMS:
                return new ExportReturnedtemsCommand().sync(getContext(), getArgs(), getAppCommandContext());
            case RETURNED_ORDERS:
                return new ExportReturnedOrderCommand().sync(getContext(), getArgs(), getAppCommandContext());
            case SOLD_ORDERS:
                return new ExportSoldOrderCommand().sync(getContext(), getArgs(), getAppCommandContext());
            case SALES_BY_CUSTOMERS:
                return new ExportSalesByCustomersCommand().sync(getContext(), getArgs(), getAppCommandContext());
            case INVENTORY_STATUS:
                return new ExportInventoryStatusCommand().sync(getContext(), getArgs(), getAppCommandContext());
            case EMPLOYEE_ATTENDANCE:
                return new ExportEmployeeAttendanceCommand().sync(getContext(), getArgs(), getAppCommandContext());
        }
        return failed();
    }

    public static void start(Context context, String fileName, ReportType type, long start, long end, long registerId, String employeeGuid, String departmentGuid, ExportCommandBaseCallback callback) {
        create(ExportReportsCommand.class)
                .arg(ExportCursorToFileCommand.ARG_FILENAME, fileName)
                .arg(ARG_REPORT_TYPE, type)
                .arg(ReportArgs.ARG_START_TIME, start)
                .arg(ReportArgs.ARG_END_TIME, end)
                .arg(ReportArgs.ARG_REGISTER_GUID, registerId)
                .arg(ReportArgs.ARG_EMPLOYEE_GUID, employeeGuid)
                .arg(ReportArgs.ARG_DEPARTMENT_GUID, departmentGuid)
                .callback(callback).queueUsing(context);
    }

    public static abstract class ExportCommandBaseCallback {

        @OnSuccess(ExportReportsCommand.class)
        public void onSuccess(@Param(ExportCursorToFileCommand.RESULT_COUNT) int count) {
            handleSuccess(count);
        }

        protected abstract void handleSuccess(int count);

        @OnFailure(ExportReportsCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleFailure();
    }

}
