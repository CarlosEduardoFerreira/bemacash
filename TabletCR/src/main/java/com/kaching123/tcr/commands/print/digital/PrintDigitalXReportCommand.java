package com.kaching123.tcr.commands.print.digital;

import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.model.XReportInfo;
import com.kaching123.tcr.print.builder.DigitalXReportBuilder;
import com.kaching123.tcr.print.processor.PrintXReportProcessor;
import com.kaching123.tcr.reports.XReportQuery;
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
 * Created by vkompaniets on 23.01.14.
 */
public class PrintDigitalXReportCommand extends PublicGroundyTask {

    private static final String ARG_SHIFT_GUID = "ARG_SHIFT_GUID";

    private static final String ARG_XREPORT_TYPE = "ARG_XREPORT_TYPE";

    private static final String EXTRA_XREPORT = "EXTRA_XREPORT";

    private static final String ARG_XREPORT_ENABLE = "ARG_XREPORT_ENABLE";

    private ReportType xReportType;

    @Override
    protected TaskResult doInBackground() {
        String shiftGuid = getStringArg(ARG_SHIFT_GUID);
        xReportType = (ReportType) getArgs().getSerializable(ARG_XREPORT_TYPE);
        final DigitalXReportBuilder builder = new DigitalXReportBuilder();

        final XReportInfo reportInfo;
        if (ReportType.X_REPORT_DAILY_SALES == xReportType) {
            reportInfo = XReportQuery.loadDailySalesXReport(getContext(), getAppCommandContext().getRegisterId());
        } else {
            reportInfo = XReportQuery.loadXReport(getContext(), shiftGuid);
        }

        PrintXReportProcessor processor = new PrintXReportProcessor(reportInfo, xReportType, getAppCommandContext(), getBooleanArg(ARG_XREPORT_ENABLE));
        processor.print(getContext(), getApp(), builder);

        File file = new File(getContext().getExternalCacheDir(), getContext().getString(R.string.report_type_xreport) + ".html");
        try {
            file.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.append(builder.build());
            bufferedWriter.close();
        } catch (IOException e) {
            return failed();
        }

        return succeeded().add(EXTRA_XREPORT, Uri.fromFile(file));
    }

    public static void start(Context context, String shiftGuid, ReportType xReportType, BasePrintDigitalXReportCallback callback, boolean enableEreportDepartSale) {
        create(PrintDigitalXReportCommand.class)
                .arg(ARG_SHIFT_GUID, shiftGuid)
                .arg(ARG_XREPORT_TYPE, xReportType)
                .arg(ARG_XREPORT_ENABLE, enableEreportDepartSale)
                .callback(callback).queueUsing(context);
    }

    public static abstract class BasePrintDigitalXReportCallback {

        @OnSuccess(PrintDigitalXReportCommand.class)
        public void onPrintSuccess(@Param(EXTRA_XREPORT) Uri attachment) {
            onDigitalPrintSuccess(attachment);
        }

        @OnFailure(PrintDigitalXReportCommand.class)
        public void onPrintError() {
            onDigitalPrintError();
        }

        protected abstract void onDigitalPrintSuccess(Uri attachment);

        protected abstract void onDigitalPrintError();
    }
}
