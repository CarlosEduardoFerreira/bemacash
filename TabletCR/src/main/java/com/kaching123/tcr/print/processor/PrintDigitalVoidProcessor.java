package com.kaching123.tcr.print.processor;

import android.content.Context;

import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.TcrApplication;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.util.List;

/**
 * Created by pkabakov on 26.12.13.
 */
public class PrintDigitalVoidProcessor extends PrintVoidProcessor{

    public PrintDigitalVoidProcessor(String orderGuid, List<String> transactionsGuids, IAppCommandContext appCommandContext) {
        super(orderGuid, transactionsGuids, false, appCommandContext);
    }

    @Override
    protected void printFooter(Context context, TcrApplication app, ITextPrinter printerWrapper) {
        printerWrapper.emptyLine();
        super.printFooter(context, app, printerWrapper);
    }
}
