package com.kaching123.tcr.print.processor;

import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.processor.MoneybackProcessor.RefundSaleItemInfo;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import java.util.List;

/**
 * Created by pkabakov on 26.12.13.
 */
public class PrintDigitalRefundProcessor extends PrintRefundProcessor{

    public PrintDigitalRefundProcessor(String orderGuid, List<RefundSaleItemInfo> items, List<String> transactionsGuids, IAppCommandContext appCommandContext) {
        super(orderGuid, items, transactionsGuids, false, appCommandContext);
    }

    @Override
    protected void printFooter(TcrApplication app, ITextPrinter printerWrapper) {
        printerWrapper.emptyLine();
        super.printFooter(app, printerWrapper);
    }
}
