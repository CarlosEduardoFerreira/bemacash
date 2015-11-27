package com.kaching123.tcr.commands.print.digital;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.commands.rest.email.BaseSendEmailCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.model.ZReportInfo;
import com.kaching123.tcr.print.builder.DigitalXReportBuilder;
import com.kaching123.tcr.print.processor.PrintZReportProcessor;
import com.kaching123.tcr.reports.ZReportQuery;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

/**
 * Created by alboyko on 26.11.2015.
 */
public class SendDigitalZReportCommand extends BaseSendEmailCommand {

    private static final String ARG_SHIFT_GUID = "ARG_SHIFT_GUID";

    private static final String ARG_ZREPORT_TYPE = "ARG_ZREPORT_TYPE";

    @Override
    protected Response execute(SyncApi restApi, String apiKey) {
        String email = getApp().getShopInfo().ownerEmail;
        if (TextUtils.isEmpty(email))
            return Response.responseFailed();

        String shiftGuid = getStringArg(ARG_SHIFT_GUID);
        final ReportType zReportType = (ReportType) getArgs().getSerializable(ARG_ZREPORT_TYPE);

        final DigitalXReportBuilder builder = new DigitalXReportBuilder();
        ZReportInfo reportInfo;
        if (ReportType.Z_REPORT_DAILY_SALES == zReportType) {
            reportInfo = ZReportQuery.loadDailySalesZReport(getContext(), getAppCommandContext().getRegisterId());
        } else {
            reportInfo = ZReportQuery.loadZReport(getContext(), shiftGuid);
        }
        PrintZReportProcessor processor = new PrintZReportProcessor(reportInfo, zReportType, getAppCommandContext());
        processor.print(getContext(), getApp(), builder);

        String html = builder.build();

        String subject = getSubject(zReportType);

        return sendEmail(restApi, apiKey, new String[]{email}, subject, html);
    }

    private String getSubject(ReportType zReportType) {
        int subjId = (ReportType.Z_REPORT_DAILY_SALES == zReportType || ReportType.Z_REPORT_CURRENT_SHIFT == zReportType) ?
                R.string.report_type_z_report : R.string.report_type_xreport;
        return getContext().getString(subjId);
    }


    public static void start(Context context, String shiftGuid, ReportType zReportType, SendPrintDigitalZReportCallback callback) {
        create(SendDigitalZReportCommand.class)
                .arg(ARG_SHIFT_GUID, shiftGuid)
                .arg(ARG_ZREPORT_TYPE, zReportType)
                .callback(callback).queueUsing(context);
    }

    public static abstract class SendPrintDigitalZReportCallback {

        @OnSuccess(SendDigitalZReportCommand.class)
        public final void onPrintSuccess() {
            handleSuccess();
        }

        @OnFailure(SendDigitalZReportCommand.class)
        public final void onPrintError() {
            handleFailure();
        }

        protected abstract void handleSuccess();

        protected abstract void handleFailure();
    }
}
