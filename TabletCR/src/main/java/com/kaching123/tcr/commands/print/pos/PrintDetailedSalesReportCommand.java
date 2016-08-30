package com.kaching123.tcr.commands.print.pos;

import android.content.Context;
import android.os.Bundle;

import com.kaching123.pos.PosPrinter;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;
import com.kaching123.tcr.print.processor.DetailedReportPrintOrdersProcessor;
import com.kaching123.tcr.reports.DetailedReportQuery;
import com.telly.groundy.TaskResult;

import java.io.IOException;
import java.util.List;

/**
 * Created by mboychenko on 25.08.2016.
 */
public class PrintDetailedSalesReportCommand extends BasePrintOrderCommand {

    public static final String NO_SALES_FOR_THIS_FILTER = "NO_SALES_FOR_THIS_FILTER";
    private static final String ARG_REGISTER_ID = "ARG_REGISTER_ID";
    private static final String ARG_FROM_DATE = "ARG_FROM_DATE";
    private static final String ARG_TO_DATE = "ARG_TO_DATE";

    private List<String> ordersGuid;
    private int receiptsSize;
    private String reportTotal;
    private long toDate;
    private long fromDate;

    @Override
    protected TaskResult execute(PosPrinter printer) throws IOException {

        final PosOrderTextPrinter printerWrapper = createTextPrinter();

        long registerID = getLongArg(ARG_REGISTER_ID);
        fromDate= getLongArg(ARG_FROM_DATE);
        toDate= getLongArg(ARG_TO_DATE);

        ordersGuid = DetailedReportQuery.loadReceiptsReport(getContext(), registerID, fromDate, toDate);
        receiptsSize = ordersGuid.size();

        if(receiptsSize == 0) {
            Bundle b = new Bundle();
            b.putString(EXTRA_PRINTER_INFO, NO_SALES_FOR_THIS_FILTER);
            callback(EXTRA_PRINTER_INFO, b);
            return succeeded();
        }

        reportTotal = DetailedReportQuery.loadSumOfOrdersTotal(getContext(), registerID, fromDate, toDate);

        printBody(printerWrapper);
        printerWrapper.emptyLine(7);

        try {
            printerWrapper.print(printer);
        } catch (IOException e) {
            Logger.e("BasePrintCommand execute error: ", e);
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.DISCONNECTED);
        }
        return succeeded();
    }

    @Override
    protected void printBody(PosOrderTextPrinter printer) {
        boolean hasHeader = false;

        for (String guid : ordersGuid) {
            receiptsSize--;
            DetailedReportPrintOrdersProcessor printProcessor = getPrintDigitalOrderProcessor(guid, getAppCommandContext());
            printProcessor.hasAddressInfo(hasHeader, fromDate, toDate);

            if(receiptsSize == 0) {
                printProcessor.lastReceipt(reportTotal);
                printProcessor.print(getContext(), getApp(), printer);
                break;
            }

            printProcessor.print(getContext(), getApp(), printer);

            printer.emptyLine();
            printer.drawDoubleLine();
            printer.emptyLine();

            hasHeader = true;
        }
    }

    protected DetailedReportPrintOrdersProcessor getPrintDigitalOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        return new DetailedReportPrintOrdersProcessor(orderGuid, appCommandContext);
    }

    public static void start(Context context, long registerId, long fromDate, long toDate, boolean ignorePaperEnd, boolean searchByMac, BasePrintCallback callback) {
        create(PrintDetailedSalesReportCommand.class)
                .arg(ARG_SKIP_PAPER_WARNING, ignorePaperEnd)
                .arg(ARG_SEARCH_BY_MAC, searchByMac)
                .arg(ARG_REGISTER_ID, registerId)
                .arg(ARG_FROM_DATE, fromDate)
                .arg(ARG_TO_DATE, toDate)
                .callback(callback).queueUsing(context);
    }

}
