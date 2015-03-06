package com.kaching123.tcr.commands.display;

import android.content.Context;

import com.kaching123.display.DisplayPrinter;
import com.kaching123.display.printers.IDisplayPrinterWrapper;
import com.kaching123.tcr.service.DisplayService.Command;

import java.io.IOException;

/**
 * Created by pkabakov on 26.02.14.
 */
public abstract class BaseDisplayCommand<T extends IDisplayPrinterWrapper> implements Command {

    @Override
    public void execute(Context context, DisplayPrinter printer) throws IOException {
        final T printerWrapper = getPrinterWrapper(context);

        printerWrapper.clear();
        printBody(context, printerWrapper);
        printerWrapper.print(printer);
    }

    protected abstract T getPrinterWrapper(Context context);

    protected abstract void printBody(Context context, T printerWrapper);

    protected abstract boolean getSerialPortDisplaySet(Context context);

}
