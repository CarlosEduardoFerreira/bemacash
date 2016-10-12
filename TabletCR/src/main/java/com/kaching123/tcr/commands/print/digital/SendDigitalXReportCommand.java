package com.kaching123.tcr.commands.print.digital;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.commands.rest.email.BaseSendEmailCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.model.XReportInfo;
import com.kaching123.tcr.print.builder.DigitalXReportBuilder;
import com.kaching123.tcr.print.processor.PrintXReportProcessor;
import com.kaching123.tcr.reports.XReportQuery;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

/**
 * Created by gdubina on 14.03.14.
 */
public class SendDigitalXReportCommand extends BaseSendEmailCommand {

    private static final String ARG_SHIFT_GUID = "ARG_SHIFT_GUID";

    private static final String ARG_XREPORT_TYPE = "ARG_XREPORT_TYPE";

    private static final String ARG_XREPORT_ENABLE = "ARG_XREPORT_ENABLE";

    private static final String ARG_ITEM_XREPORT_ENABLE = "ARG_ITEM_XREPORT_ENABLE";
    private static final String ARG_ZREPORT_REGISTER_ID = "ARG_ZREPORT_REGISTER_ID";
    private static final String ARG_ZREPORT_FROMDATE = "ARG_ZREPORT_FROMDATE";
    private static final String ARG_ZREPORT_TODATE = "ARG_ZREPORT_TODATE";
    @Override
    protected Response execute(SyncApi restApi, String apiKey) {
        String email = getApp().getShopInfo().ownerEmail;
        if (TextUtils.isEmpty(email))
            return Response.responseFailed();

        String shiftGuid = getStringArg(ARG_SHIFT_GUID);
        long registerID = getLongArg(ARG_ZREPORT_REGISTER_ID);
        long fromDate = getLongArg(ARG_ZREPORT_FROMDATE);
        long toDate = getLongArg(ARG_ZREPORT_TODATE);
        final ReportType xReportType = (ReportType) getArgs().getSerializable(ARG_XREPORT_TYPE);

        final DigitalXReportBuilder builder = new DigitalXReportBuilder();
        XReportInfo reportInfo;
        if (ReportType.X_REPORT_DAILY_SALES == xReportType) {
            reportInfo = XReportQuery.loadDailySalesXReport(getContext(),registerID, fromDate, toDate);
        } else {
            reportInfo = XReportQuery.loadXReport(getContext(), shiftGuid);
        }
        PrintXReportProcessor processor = new PrintXReportProcessor(reportInfo, xReportType, getAppCommandContext(), getBooleanArg(ARG_XREPORT_ENABLE), getBooleanArg(ARG_ITEM_XREPORT_ENABLE));
        processor.print(getContext(), getApp(), builder);

        String html = builder.build();

        String subject = getSubject(xReportType);

        return sendEmail(restApi, apiKey, new String[]{email}, subject, html);
    }

    private String getSubject(ReportType xReportType) {
        int subjId = (ReportType.X_REPORT_DAILY_SALES == xReportType || ReportType.X_REPORT_CURRENT_SHIFT == xReportType) ?
                R.string.report_type_x_report : R.string.report_type_xreport ;
        return getContext().getString(subjId);
    }


    public static void start(Context context, String shiftGuid, ReportType xReportType, boolean enableEreportDepartSale, boolean itemXreportSaleEnabled, SendPrintDigitalXReportCallback callback, long registerID, long fromDate, long toDate) {
        create(SendDigitalXReportCommand.class)
                .arg(ARG_SHIFT_GUID, shiftGuid)
                .arg(ARG_XREPORT_TYPE, xReportType)
                .arg(ARG_XREPORT_ENABLE, enableEreportDepartSale)
                .arg(ARG_ITEM_XREPORT_ENABLE,itemXreportSaleEnabled)
                .arg(ARG_ZREPORT_REGISTER_ID, registerID)
                .arg(ARG_ZREPORT_FROMDATE, fromDate)
                .arg(ARG_ZREPORT_TODATE, toDate)
                .callback(callback).queueUsing(context);
    }

    public static abstract class SendPrintDigitalXReportCallback {

        @OnSuccess(SendDigitalXReportCommand.class)
        public final void onPrintSuccess() {
            handleSuccess();
        }

        @OnFailure(SendDigitalXReportCommand.class)
        public final void onPrintError() {
            handleFailure();
        }

        protected abstract void handleSuccess();

        protected abstract void handleFailure();
    }
}
