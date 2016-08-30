package com.kaching123.tcr.commands.print.pos;

import com.kaching123.pos.PosPrinter;
import com.kaching123.pos.data.PrinterStatusEx;
import com.kaching123.pos.util.IPrinter;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnCallback;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.IOException;

/**
 * Created by gdubina on 23.12.13.
 */
public abstract class BasePrintCommand<T extends IPrinter> extends PrinterCommand {

    public static final String ARG_SKIP_PAPER_WARNING = "ARG_SKIP_PAPER_WARNING";

    @Override
    protected TaskResult execute(PosPrinter printer) throws IOException {

        final T printerWrapper = createTextPrinter();

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

    protected abstract T createTextPrinter();

    protected abstract void printBody(T printerWrapper);

    @Override
    protected TaskResult validatePrinterStateExt(PrinterStatusEx status) {
        if (!status.offlineStatus.coverIsClosed) {
            Logger.e("BasePrintCommand validate statues execute: cover is opened!");
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.COVER_IS_OPENED);
        }
        if (status.offlineStatus.noPaper) {
            Logger.e("BasePrintCommand validate statues execute: noPaper!");
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.NO_PAPER);
        }
        boolean skipPaperWarning = getBooleanArg(ARG_SKIP_PAPER_WARNING, false);
        boolean searchByMac = getBooleanArg(ARG_SEARCH_BY_MAC, false);
        if (!(searchByMac || skipPaperWarning) && status.offlineStatus.paperIsNearEnd) {
            Logger.e("BasePrintCommand validate statues execute: paperIsNearEnd!");
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.PAPER_IS_NEAR_END);
        }
        if (status.errorStatus.cutterIsAbsent || status.errorStatus.cutterErrorIsDetected) {
            Logger.e("BasePrintCommand validate statues execute: cutter error!");
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.CUTTER_ERROR);
        }
        if (status.printerHead.headIsOverhead) {
            Logger.e("BasePrintCommand validate statues execute: headIsOverhead!");
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.HEAD_OVERHEATED);
        }
        return null;
    }

    public static abstract class BasePrintCallback {
        @OnSuccess(PrinterCommand.class)
        public void handleSuccess() {
            onPrintSuccess();
        }

        @OnCallback(value = PrinterCommand.class, name = EXTRA_PRINTER_INFO)
        public void handleSuccessWithInfo(@Param(EXTRA_PRINTER_INFO) String msg) {
            onPrintSuccessWithInfo(msg);
        }

        @OnFailure(PrinterCommand.class)
        public void handleFailure(
                @Param(EXTRA_ERROR_PRINTER)
                PrinterError printerError) {

            if (printerError != null && printerError == PrinterError.DISCONNECTED) {
                onPrinterDisconnected();
                return;
            }
            if (printerError != null && printerError == PrinterError.IP_NOT_FOUND) {
                onPrinterIPnotFound();
                return;
            }
            if (printerError != null && printerError == PrinterError.PAPER_IS_NEAR_END) {
                onPrinterPaperNearTheEnd();
                return;
            }
            if (printerError != null && printerError == PrinterError.NOT_CONFIGURED) {
                onPrinterNotConfigured();
                return;
            }
            onPrintError(printerError);
        }

        protected abstract void onPrintSuccess();
        protected abstract void onPrintError(PrinterError error);
        protected abstract void onPrinterDisconnected();
        protected abstract void onPrinterIPnotFound();
        protected abstract void onPrinterNotConfigured();
        protected abstract void onPrinterPaperNearTheEnd();
        protected void onPrintSuccessWithInfo(String msg){};
    }

    public static interface IPrintCallback {

        void onPrintSuccess();
        void onPrintError(PrinterError error);
        void onPrinterNotConfigured();
        void onPrinterDisconnected();
        void onPrinterPaperNearTheEnd();

    }

}
