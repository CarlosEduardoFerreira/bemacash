package com.kaching123.tcr.print.processor;

import android.content.Context;

import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.TcrApplication;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

/**
 * Created by pkabakov on 24.12.13.
 */
public class PrintDigitalOrderProcessor extends PrintOrderProcessor{

    public PrintDigitalOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        super(orderGuid, appCommandContext);
    }

    public PrintDigitalOrderProcessor(String orderGuid, boolean reprint, IAppCommandContext appCommandContext) {
        super(orderGuid, reprint, appCommandContext);
    }

    @Override
    protected void printFooter(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        printerWrapper.emptyLine();
        super.printFooter(context, app, printerWrapper);
    }
}
