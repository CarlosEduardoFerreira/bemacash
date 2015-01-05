package com.kaching123.tcr.commands.device;

import com.kaching123.pos.PosPrinter;
import com.kaching123.pos.data.PrinterStatusEx;
import com.kaching123.tcr.Logger;
import com.telly.groundy.TaskResult;

import java.io.IOException;

/**
 * Created by pkabakov on 17.04.2014.
 */
public abstract class BaseDeviceCommand extends PrinterCommand {

    @Override
    protected TaskResult execute(PosPrinter printer) throws IOException {
        try {
            return executeInner(printer);
        } catch (IOException e) {
            Logger.e("BaseDeviceCommand execute error: ", e);
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.DISCONNECTED);
        }
    }

    protected abstract TaskResult executeInner(PosPrinter printer) throws IOException;

    @Override
    protected TaskResult validatePrinterStateExt(PrinterStatusEx status) {
        if (!status.offlineStatus.coverIsClosed) {
            Logger.e("BaseDeviceCommand validate statues execute: cover is opened!");
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.COVER_IS_OPENED);
        }
        if (status.offlineStatus.noPaper) {
            Logger.e("BaseDeviceCommand validate statues execute: noPaper!");
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.NO_PAPER);
        }
        if (status.errorStatus.cutterIsAbsent || status.errorStatus.cutterErrorIsDetected) {
            Logger.e("BaseDeviceCommand validate statues execute: cutter error!");
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.CUTTER_ERROR);
        }
        if (status.printerHead.headIsOverhead) {
            Logger.e("BaseDeviceCommand validate statues execute: headIsOverhead!");
            return failed().add(EXTRA_ERROR_PRINTER, PrinterError.HEAD_OVERHEATED);
        }
        return null;
    }
}
