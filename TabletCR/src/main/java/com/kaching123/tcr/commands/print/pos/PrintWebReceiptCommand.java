package com.kaching123.tcr.commands.print.pos;

import android.content.Context;

import com.kaching123.pos.util.IReportsPrinter;
import com.kaching123.pos.util.ITextPrinter;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;
import com.kaching123.tcr.print.processor.PrintDropPayoutProcessor;
import com.kaching123.tcr.print.processor.PrintWebReceiptProcessor;
import com.kaching123.tcr.store.ShopProvider;
import java.util.ArrayList;

/**
 * Created by b1107005 on 12/7/2014.
 */
public class PrintWebReceiptCommand extends BasePrintCommand<ITextPrinter> {

    private static final String ARG_RECEIPT_BUFFER = "ARG_RECEIPT_BUFFER";

    @Override
    protected ITextPrinter createTextPrinter() {
        return new PosOrderTextPrinter();
    }

    @Override
    protected void printBody(ITextPrinter printer) {

        PrintWebReceiptProcessor processor = new PrintWebReceiptProcessor();
        String [] receiptBuffer = getArgs().getStringArray(ARG_RECEIPT_BUFFER);
        if ( receiptBuffer == null || receiptBuffer.length <1) {
            return;
        }
        processor.print(getContext(),getApp(),printer,receiptBuffer);
    }
    public static void start(Context context, String [] receiptBuffer , boolean skipPaperWarning, boolean searchByMac, BasePrintCallback callback){
        create(PrintWebReceiptCommand.class)
                .arg(ARG_RECEIPT_BUFFER, receiptBuffer)
                .arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning)
                .arg(ARG_SEARCH_BY_MAC, searchByMac)
                .callback(callback)
                .queueUsing(context);
    }

}
