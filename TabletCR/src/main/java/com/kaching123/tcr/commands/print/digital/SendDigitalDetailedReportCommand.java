package com.kaching123.tcr.commands.print.digital;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.rest.email.BaseSendEmailCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.print.builder.DigitalDetailReportOrdersBuilder;
import com.kaching123.tcr.print.processor.DetailedReportPrintOrdersProcessor;
import com.kaching123.tcr.reports.DetailedReportQuery;
import com.telly.groundy.annotations.OnCallback;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.List;

/**
 * Created by mboychenko on 25.08.2016.
 */
public class SendDigitalDetailedReportCommand extends BaseSendEmailCommand {

    public static final String NO_SALES_FOR_THIS_FILTER = "NO_SALES_FOR_THIS_FILTER";
    private static final String ARG_REGISTER_ID = "ARG_REGISTER_ID";
    private static final String ARG_FROM_DATE = "ARG_FROM_DATE";
    private static final String ARG_TO_DATE = "ARG_TO_DATE";
    private static final String EXTRA_DETAILED_REPORT = "EXTRA_DETAILED_REPORT";
    private static final String EXTRA_DETAILED_REPORT_INFO = "EXTRA_DETAILED_REPORT_INFO";

    @Override
    protected Response execute(SyncApi restApi, String apiKey) {
        String email = getApp().getShopInfo().ownerEmail;
        if (TextUtils.isEmpty(email))
            return Response.responseFailed();

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
            return new Response(Response.STATUS_SUCCESS, null);
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

        String html = orderBuilder.build();

        String subject = getContext().getString(R.string.report_type_detailed_report);

        return sendEmail(restApi, apiKey, new String[]{email}, subject, html);
    }

    protected DetailedReportPrintOrdersProcessor getPrintDigitalOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        return new DetailedReportPrintOrdersProcessor(orderGuid, appCommandContext);
    }

    public static void start(Context context, long registerId, long fromDate, long toDate, BaseSendPrintDigitalReportCallback callback) {
        create(SendDigitalDetailedReportCommand.class)
                .arg(ARG_REGISTER_ID, registerId)
                .arg(ARG_FROM_DATE, fromDate)
                .arg(ARG_TO_DATE, toDate)
                .callback(callback).queueUsing(context);
    }


    public static abstract class BaseSendPrintDigitalReportCallback {

        @OnSuccess(SendDigitalDetailedReportCommand.class)
        public final void onPrintSuccess() {
            handleSuccess();
        }

        @OnCallback(value = SendDigitalDetailedReportCommand.class, name = EXTRA_DETAILED_REPORT)
        public final void onPrintSuccessWithInfo(@Param(EXTRA_DETAILED_REPORT_INFO) String msg) {
            handleSuccessWithInfo(msg);
        }

        @OnFailure(SendDigitalDetailedReportCommand.class)
        public final void onPrintError() {
            handleFailure();
        }

        protected abstract void handleSuccess();

        protected abstract void handleSuccessWithInfo(String msg);

        protected abstract void handleFailure();
    }
}
