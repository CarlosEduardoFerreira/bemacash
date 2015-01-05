package com.kaching123.tcr.print.processor;

import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.WirelessInfo;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

/**
 * Created by vkompaniets on 20.03.14.
 */
public class PrintDigitalWirelessPrepaidProcessor extends PrintWirelessPrepaidProcessor {

    public PrintDigitalWirelessPrepaidProcessor(String orderGuid, WirelessInfo info, IAppCommandContext appCommandContext) {
        super(orderGuid, info, appCommandContext);
    }

    @Override
    protected void printFooter(TcrApplication app, ITextPrinter printerWrapper) {
        printerWrapper.emptyLine();
        super.printFooter(app, printerWrapper);
    }
}
