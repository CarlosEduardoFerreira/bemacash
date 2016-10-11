package com.kaching123.tcr.commands.print.pos;

import android.content.Context;

import com.kaching123.pos.util.IXReportPrinter;
import com.kaching123.tcr.activity.ReportsActivity;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.model.ZReportInfo;
import com.kaching123.tcr.print.printer.PosXReportTextPrinter;
import com.kaching123.tcr.print.processor.PrintZReportProcessor;
import com.kaching123.tcr.reports.ZReportQuery;

/**
 * Created by alboyko on 26.11.2015.
 */
public class PrintZReportCommand extends BasePrintCommand<IXReportPrinter> {

    private static final String ARG_SHIFT_GUID = "ARG_SHIFT_GUID";

    private static final String ARG_ZREPORT_TYPE = "ARG_ZREPORT_TYPE";
    private static final String ARG_ZREPORT_REGISTER_ID = "ARG_ZREPORT_REGISTER_ID";
    private static final String ARG_ZREPORT_FROMDATE = "ARG_ZREPORT_FROMDATE";
    private static final String ARG_ZREPORT_TODATE = "ARG_ZREPORT_TODATE";

    @Override
    protected IXReportPrinter createTextPrinter() {
        return new PosXReportTextPrinter();
    }

    @Override
    protected void printBody(IXReportPrinter printerWrapper) {
        String shiftGuid = getStringArg(ARG_SHIFT_GUID);

        long registerID = getLongArg(ARG_ZREPORT_REGISTER_ID);
        long fromDate = getLongArg(ARG_ZREPORT_FROMDATE);
        long toDate = getLongArg(ARG_ZREPORT_TODATE);
        ReportType zReportType = (ReportType) getArgs().getSerializable(ARG_ZREPORT_TYPE);

        //final DigitalXReportBuilder builder = new DigitalXReportBuilder();
        ZReportInfo reportInfo;
        if (ReportsActivity.ReportType.Z_REPORT_DAILY_SALES == zReportType) {
            reportInfo = ZReportQuery.loadDailySalesZReport(getContext(), registerID, fromDate, toDate);
        } else {
            reportInfo = ZReportQuery.loadZReport(getContext(), shiftGuid);
        }
        PrintZReportProcessor processor = new PrintZReportProcessor(reportInfo, zReportType, getAppCommandContext());
        processor.print(getContext(), getApp(), printerWrapper);
    }

    public static void start(Context context, String shiftGuid, ReportsActivity.ReportType zReportType, boolean ignorePaperEnd, boolean searchByMac, BasePrintCallback callback, long registerID, long fromDate, long toDate) {
        create(PrintZReportCommand.class)
                .arg(ARG_SHIFT_GUID, shiftGuid)
                .arg(ARG_SKIP_PAPER_WARNING, ignorePaperEnd)
                .arg(ARG_SEARCH_BY_MAC, searchByMac)
                .arg(ARG_ZREPORT_TYPE, zReportType)
                .arg(ARG_ZREPORT_REGISTER_ID, registerID)
                .arg(ARG_ZREPORT_FROMDATE, fromDate)
                .arg(ARG_ZREPORT_TODATE, toDate)
                .callback(callback).queueUsing(context);
    }
}
