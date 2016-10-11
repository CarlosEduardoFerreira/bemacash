package com.kaching123.tcr.commands.print.digital;

import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.model.ZReportInfo;
import com.kaching123.tcr.print.builder.DigitalXReportBuilder;
import com.kaching123.tcr.print.processor.PrintZReportProcessor;
import com.kaching123.tcr.reports.ZReportQuery;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by alboyko on 26.11.2015.
 */
public class PrintDigitalZReportCommand extends PublicGroundyTask {

    private static final String ARG_SHIFT_GUID = "ARG_SHIFT_GUID";

    private static final String ARG_ZREPORT_TYPE = "ARG_ZREPORT_TYPE";

    private static final String EXTRA_ZREPORT = "EXTRA_ZREPORT";
    private static final String ARG_ZREPORT_REGISTER_ID = "ARG_ZREPORT_REGISTER_ID";
    private static final String ARG_ZREPORT_FROMDATE = "ARG_ZREPORT_FROMDATE";
    private static final String ARG_ZREPORT_TODATE = "ARG_ZREPORT_TODATE";
    private ReportType zReportType;

    @Override
    protected TaskResult doInBackground() {
        String shiftGuid = getStringArg(ARG_SHIFT_GUID);

        long registerID = getLongArg(ARG_ZREPORT_REGISTER_ID);
        long fromDate = getLongArg(ARG_ZREPORT_FROMDATE);
        long toDate = getLongArg(ARG_ZREPORT_TODATE);

        zReportType = (ReportType) getArgs().getSerializable(ARG_ZREPORT_TYPE);
        final DigitalXReportBuilder builder = new DigitalXReportBuilder();

        final ZReportInfo reportInfo;
        if (ReportType.Z_REPORT_DAILY_SALES == zReportType) {
            reportInfo = ZReportQuery.loadDailySalesZReport(getContext(), registerID, fromDate, toDate);
        } else {
            reportInfo = ZReportQuery.loadZReport(getContext(), shiftGuid);
        }

        PrintZReportProcessor processor = new PrintZReportProcessor(reportInfo, zReportType, getAppCommandContext());
        processor.print(getContext(), getApp(), builder);

        File file = new File(getContext().getExternalCacheDir(), getContext().getString(R.string.report_type_zreport) + ".html");
        try {
            file.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.append(builder.build());
            bufferedWriter.close();
        } catch (IOException e) {
            return failed();
        }

        return succeeded().add(EXTRA_ZREPORT, Uri.fromFile(file));
    }

    public static void start(Context context, String shiftGuid, ReportType zReportType, BasePrintDigitalZReportCallback callback, long registerID, long fromDate, long toDate) {
        create(PrintDigitalZReportCommand.class)
                .arg(ARG_SHIFT_GUID, shiftGuid)
                .arg(ARG_ZREPORT_TYPE, zReportType)
                .arg(ARG_ZREPORT_REGISTER_ID, registerID)
                .arg(ARG_ZREPORT_FROMDATE, fromDate)
                .arg(ARG_ZREPORT_TODATE, toDate)
                .callback(callback).queueUsing(context);
    }

    public static abstract class BasePrintDigitalZReportCallback {

        @OnSuccess(PrintDigitalZReportCommand.class)
        public void onPrintSuccess(@Param(EXTRA_ZREPORT) Uri attachment) {
            onDigitalPrintSuccess(attachment);
        }

        @OnFailure(PrintDigitalZReportCommand.class)
        public void onPrintError() {
            onDigitalPrintError();
        }

        protected abstract void onDigitalPrintSuccess(Uri attachment);

        protected abstract void onDigitalPrintError();
    }
}
