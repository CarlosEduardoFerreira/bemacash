package com.kaching123.tcr.commands.print.pos;

import android.content.Context;

import com.kaching123.pos.PosPrinter;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.ecuador.EcuadorPrintProcessor;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;
import com.kaching123.tcr.print.processor.PrintGiftCardProcessor;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;
import com.telly.groundy.TaskResult;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by gdubina on 03.12.13.
 */
public class PrintGiftCardBalanceCommand extends BasePrintOrderCommand {

    protected static final String ARG_AMOUNT = "ARG_AMOUNT";

    @Override
    protected TaskResult execute(PosPrinter printer) throws IOException {

        return super.execute(printer);
    }

    @Override
    protected void printBody(PosOrderTextPrinter printerWrapper) {
        PrintGiftCardProcessor printProcessor = new PrintGiftCardProcessor(getAppCommandContext());
//
        printProcessor.print(getContext(), getApp(), printerWrapper);
    }

    protected PrintOrderProcessor getPrintOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        return TcrApplication.isEcuadorVersion() ?
                new EcuadorPrintProcessor(orderGuid, appCommandContext) : new PrintOrderProcessor(orderGuid, appCommandContext);
    }

    public static void start(Context context, BigDecimal amount, BasePrintCallback callback) {
        create(PrintGiftCardBalanceCommand.class).arg(ARG_AMOUNT, amount).callback(callback).queueUsing(context);
    }

//    public static void start(Context context, boolean skipPaperWarning, boolean searchByMac, String orderGuid, BasePrintCallback callback, String title, String subTotal, String discountTotal, String taxTotal, String amountTotal) {
//        create(PrintGiftCardBalanceCommand.class).arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning).arg(ARG_SEARCH_BY_MAC, searchByMac).arg(ARG_ORDER_GUID, orderGuid).arg(ARG_ORDER_TITLE, title).arg(ARG_ORDER_SUBTOTAL, subTotal).arg(ARG_ORDER_DISCOUNTTOTAL, discountTotal).arg(ARG_ORDER_TAXTOTAL, taxTotal).arg(ARG_ORDER_TOTALAMOUNT, amountTotal).callback(callback).queueUsing(context);
//    }

}
