package com.kaching123.tcr.commands.print.pos;

import android.content.Context;

import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.commands.print.SaleReportsProcessor;
import com.kaching123.tcr.print.printer.PosReportsMatrixPrinter;
import com.kaching123.tcr.print.printer.PosReportsPrinter;
import com.kaching123.tcr.print.processor.PrintReportsProcessor;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

/**
 * Created by vkompaniets on 27.01.14.
 */
public class PrintReportsCommand extends BasePrintCommand<PosReportsPrinter> {

    private static final String ARG_START_TIME = "ARG_START_TIME";
    private static final String ARG_END_TIME = "ARG_END_TIME";
    private static final String ARG_REGISTER_GUID = "ARG_REGISTER_GUID";
    private static final String ARG_EMPLOYEE_GUID = "ARG_EMPLOYEE_GUID";
    private static final String ARG_REPORT_TYPE = "ARG_REPORT_TYPE";
    private static final String ARG_CASH_DRAWER_TYPE = "ARG_CASH_DRAWER_TYPE";
    private static final String ARG_EMPLOYEE_NAME = "ARG_EMPLOYEE_NAME";

    @Override
    protected PosReportsPrinter createTextPrinter() {
        return getPrinter().printerType.equalsIgnoreCase("Thermal") ? new PosReportsPrinter():new PosReportsMatrixPrinter();
    }

    @Override
    protected void printBody(PosReportsPrinter printer) {
        long startTime = getLongArg(ARG_START_TIME);
        long endTime = getLongArg(ARG_END_TIME);
        long resisterId = getLongArg(ARG_REGISTER_GUID);
        String employeeGuid = null;
        if (getStringArg(ARG_EMPLOYEE_GUID) != null)
            employeeGuid = getStringArg(ARG_EMPLOYEE_GUID);
        int type = getIntArg(ARG_CASH_DRAWER_TYPE);
        String name = getStringArg(ARG_EMPLOYEE_NAME);
        ReportType reportType = (ReportType) getArgs().getSerializable(ARG_REPORT_TYPE);

        PrintReportsProcessor processor = null;
        if (reportType != ReportType.DROPS_AND_PAYOUTS)
            processor = SaleReportsProcessor.print(getContext(), reportType, startTime, endTime, resisterId, employeeGuid, getAppCommandContext());
        else
            processor = SaleReportsProcessor.print(getContext(), reportType, startTime, endTime, resisterId, employeeGuid, name, getAppCommandContext(), type);
        if (processor != null) {
            processor.print(getContext(), getApp(), printer);
        }
    }

    public static void start(Context context, boolean skipPaperWarning, boolean searchByMac, ReportType reportType, long start, long end, long resisterId, BasePrintCallback callback) {
        create(PrintReportsCommand.class).arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning).arg(ARG_SEARCH_BY_MAC, searchByMac).arg(ARG_REPORT_TYPE, reportType).arg(ARG_START_TIME, start).arg(ARG_END_TIME, end).arg(ARG_REGISTER_GUID, resisterId).callback(callback).queueUsing(context);
    }

    public static void start(Context context, boolean skipPaperWarning, boolean searchByMac, ReportType reportType, long start, long end, String managerGuid, int type, String name, BasePrintCallback callback) {
        create(PrintReportsCommand.class).arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning).arg(ARG_SEARCH_BY_MAC, searchByMac).arg(ARG_REPORT_TYPE, reportType).arg(ARG_START_TIME, start).arg(ARG_EMPLOYEE_NAME, name).arg(ARG_CASH_DRAWER_TYPE, type).arg(ARG_END_TIME, end).arg(ARG_EMPLOYEE_GUID, managerGuid).callback(callback).queueUsing(context);
    }

    public static void start(Context context, boolean skipPaperWarning, boolean searchByMac, ReportType reportType, long start, long end, String employeeGuid, BasePrintCallback callback) {
        create(PrintReportsCommand.class).arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning).arg(ARG_SEARCH_BY_MAC, searchByMac).arg(ARG_REPORT_TYPE, reportType).arg(ARG_START_TIME, start).arg(ARG_END_TIME, end).arg(ARG_EMPLOYEE_GUID, employeeGuid).callback(callback).queueUsing(context);
    }

    public static void start(Context context, boolean skipPaperWarning, boolean searchByMac, ReportType reportType, BasePrintCallback callback) {
        create(PrintReportsCommand.class).arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning).arg(ARG_SEARCH_BY_MAC, searchByMac).arg(ARG_REPORT_TYPE, reportType).callback(callback).queueUsing(context);
    }

    public static abstract class BasePrintReportsCallback {

        @OnSuccess(PrintReportsCommand.class)
        public void handleSuccess() {
            onPrintSuccess();
        }

        @OnFailure(PrintReportsCommand.class)
        public void handleFailure(
                @Param(EXTRA_ERROR_PRINTER)
                PrinterError printerError) {

            if (printerError != null && printerError == PrinterError.DISCONNECTED) {
                onPrinterDisconnected();
                return;
            }
            if (printerError != null && printerError == PrinterError.NOT_CONFIGURED) {
                onPrinterNotConfigured();
                return;
            }
            if (printerError != null && printerError == PrinterError.PAPER_IS_NEAR_END) {
                onPrinterPaperNearTheEnd();
                return;
            }
            onPrintError(printerError);
        }

        protected abstract void onPrintSuccess();

        protected abstract void onPrintError(PrinterError error);

        protected abstract void onPrinterNotConfigured();

        protected abstract void onPrinterDisconnected();

        protected abstract void onPrinterPaperNearTheEnd();
    }


}
