package com.kaching123.tcr.commands.print.digital;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.commands.print.SaleReportsProcessor;
import com.kaching123.tcr.commands.rest.email.BaseSendEmailCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.print.builder.DigitalReportItemBuilder;
import com.kaching123.tcr.print.builder.DigitalReportItemWideBuilder;
import com.kaching123.tcr.print.processor.PrintReportsProcessor;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

/**
 * Created by gdubina on 23.01.14.
 */
public class SendDigitalReportsCommand extends BaseSendEmailCommand {

    private static final String ARG_START_TIME = "ARG_START_TIME";
    private static final String ARG_END_TIME = "ARG_END_TIME";
    private static final String ARG_REGISTER_GUID = "ARG_REGISTER_GUID";
    private static final String ARG_REPORT_TYPE = "ARG_REPORT_TYPE";
    private static final String ARG_EMPLOYEE_GUID = "ARG_EMPLOYEE_GUID";
    private static final String ARG_CASH_DRAWER_TYPE = "ARG_CASH_DRAWER_TYPE";
    private static final String ARG_EMPLOYEE_NAME = "ARG_EMPLOYEE_NAME";

    @Override
    protected Response execute(SyncApi restApi, String apiKey) {
        String email = getApp().getShopInfo().ownerEmail;
        if (TextUtils.isEmpty(email))
            return Response.responseFailed();

        long startTime = getLongArg(ARG_START_TIME);
        long endTime = getLongArg(ARG_END_TIME);
        long resisterId = getLongArg(ARG_REGISTER_GUID);
        int type = getIntArg(ARG_CASH_DRAWER_TYPE);
        String name = getStringArg(ARG_EMPLOYEE_NAME);

        String employeeGuid = getStringArg(ARG_EMPLOYEE_GUID);
        ReportType reportType = (ReportType) getArgs().getSerializable(ARG_REPORT_TYPE);
        PrintReportsProcessor processor = null;
        if (reportType != ReportType.DROPS_AND_PAYOUTS)
            processor = SaleReportsProcessor.print(getContext(), reportType, startTime, endTime, resisterId, employeeGuid, getAppCommandContext());
        else
            processor = SaleReportsProcessor.print(getContext(), reportType, startTime, endTime, resisterId, employeeGuid, name, getAppCommandContext(), type);
        if (processor == null) {
            return Response.responseFailed();
        }

        DigitalReportItemBuilder builder = (reportType != ReportType.INVENTORY_STATUS && reportType != ReportType.SALES_BY_ITEMS) ? new DigitalReportItemBuilder() : new DigitalReportItemWideBuilder();
        processor.print(getContext(), getApp(), builder);

        String html = builder.build();
        String subject = getContext().getString(reportType.getLabelRes());

        return sendEmail(restApi, apiKey, new String[]{email}, subject, html);
    }

    public static void start(Context context, ReportType reportType, long start, long end, long resisterId, BaseSendDigitalReportsCallback callback) {
        create(SendDigitalReportsCommand.class).arg(ARG_REPORT_TYPE, reportType).arg(ARG_START_TIME, start).arg(ARG_END_TIME, end).arg(ARG_REGISTER_GUID, resisterId).callback(callback).queueUsing(context);
    }

    public static void start(Context context, ReportType reportType, long start, long end, String employeeGuid, BaseSendDigitalReportsCallback callback) {
        create(SendDigitalReportsCommand.class).arg(ARG_REPORT_TYPE, reportType).arg(ARG_START_TIME, start).arg(ARG_END_TIME, end).arg(ARG_EMPLOYEE_GUID, employeeGuid).callback(callback).queueUsing(context);
    }

    public static void start(Context context, ReportType reportType, long start, long end, String employeeGuid, long type, String name, BaseSendDigitalReportsCallback callback) {
        create(SendDigitalReportsCommand.class).arg(ARG_REPORT_TYPE, reportType).arg(ARG_START_TIME, start).arg(ARG_END_TIME, end).arg(ARG_EMPLOYEE_GUID, employeeGuid).arg(ARG_EMPLOYEE_NAME, name).arg(ARG_CASH_DRAWER_TYPE, type).callback(callback).queueUsing(context);
    }

    public static void start(Context context, ReportType reportType, BaseSendDigitalReportsCallback callback) {
        create(SendDigitalReportsCommand.class).arg(ARG_REPORT_TYPE, reportType).callback(callback).queueUsing(context);
    }

    public static abstract class BaseSendDigitalReportsCallback {

        @OnSuccess(SendDigitalReportsCommand.class)
        public void onSuccess() {
            handleSuccess();
        }

        @OnFailure(SendDigitalReportsCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleSuccess();

        protected abstract void handleFailure();
    }
}
