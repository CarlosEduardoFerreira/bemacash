package com.kaching123.tcr.commands.print.digital;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.kaching123.tcr.R;
import com.kaching123.tcr.print.builder.DigitalDetailReportOrdersBuilder;
import com.kaching123.tcr.print.processor.DetailedReportPrintOrdersProcessor;
import com.kaching123.tcr.reports.DetailedReportQuery;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnCallback;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by mboychenko on 25.08.2016.
 */
public class PrintDigitalDetailedReportCommand extends PublicGroundyTask {

    public static final String NO_SALES_FOR_THIS_FILTER = "NO_SALES_FOR_THIS_FILTER";
    private static final String ARG_REGISTER_ID = "ARG_REGISTER_ID";
    private static final String ARG_FROM_DATE = "ARG_FROM_DATE";
    private static final String ARG_TO_DATE = "ARG_TO_DATE";
    private static final String EXTRA_DETAILED_REPORT = "EXTRA_DETAILED_REPORT";
    private static final String EXTRA_DETAILED_REPORT_INFO = "EXTRA_DETAILED_REPORT_INFO";


    @Override
    protected TaskResult doInBackground() {
        boolean hasHeader = false;
        long registerID = getLongArg(ARG_REGISTER_ID);
        long fromDate= getLongArg(ARG_FROM_DATE);
        long toDate= getLongArg(ARG_TO_DATE);

        final List<String> ordersGuid = DetailedReportQuery.loadReceiptsReport(getContext(), registerID, fromDate, toDate);
        final String reportTotal = DetailedReportQuery.loadSumOfOrdersTotal(getContext(), registerID, fromDate, toDate);
        DigitalDetailReportOrdersBuilder orderBuilder = new DigitalDetailReportOrdersBuilder();
        int receiptsSize = ordersGuid.size();

        if(receiptsSize == 0) {
            Bundle b = new Bundle();
            b.putString(EXTRA_DETAILED_REPORT_INFO, NO_SALES_FOR_THIS_FILTER);
            callback(EXTRA_DETAILED_REPORT, b);
            return succeeded();
        }

        for (String guid : ordersGuid) {
            receiptsSize--;
            DetailedReportPrintOrdersProcessor printProcessor = getPrintDigitalOrderProcessor(guid, getAppCommandContext());
            printProcessor.hasAddressInfo(hasHeader, fromDate, toDate);

            if(receiptsSize == 0) {
                printProcessor.lastReceipt(reportTotal);
                printProcessor.print(getContext(), getApp(), orderBuilder);
                break;
            }

            printProcessor.print(getContext(), getApp(), orderBuilder);

            orderBuilder.emptyLine();
            orderBuilder.drawDoubleLine();
            orderBuilder.emptyLine();

            hasHeader = true;
        }

        File file = new File(getContext().getExternalCacheDir(), getContext().getString(R.string.report_type_detailed_report) + ".html");
        try {
            file.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.append(orderBuilder.build());
            bufferedWriter.close();
        } catch (IOException e) {
            return failed();
        }


        return succeeded().add(EXTRA_DETAILED_REPORT, Uri.fromFile(file));
    }

    protected DetailedReportPrintOrdersProcessor getPrintDigitalOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        return new DetailedReportPrintOrdersProcessor(orderGuid, appCommandContext);
    }

    public static void start(Context context, long registerId, long fromDate, long toDate, BasePrintDigitalDetailedReportCallback callback) {
        create(PrintDigitalDetailedReportCommand.class)
                .arg(ARG_REGISTER_ID, registerId)
                .arg(ARG_FROM_DATE, fromDate)
                .arg(ARG_TO_DATE, toDate)
                .callback(callback).queueUsing(context);
    }

    public static abstract class BasePrintDigitalDetailedReportCallback {

        @OnSuccess(PrintDigitalDetailedReportCommand.class)
        public void onPrintSuccess(@Param(EXTRA_DETAILED_REPORT) Uri attachment) {
            onDigitalPrintSuccess(attachment);
        }

        @OnCallback(value = PrintDigitalDetailedReportCommand.class, name = EXTRA_DETAILED_REPORT)
        public void onPrintSuccessWithInfo(@Param(EXTRA_DETAILED_REPORT_INFO) String info) {
            handleSuccessWithInfo(info);
        }

        @OnFailure(PrintDigitalDetailedReportCommand.class)
        public void onPrintError() {
            onDigitalPrintError();
        }

        protected abstract void onDigitalPrintSuccess(Uri attachment);
        protected abstract void handleSuccessWithInfo(String info);
        protected abstract void onDigitalPrintError();
    }
}
