package com.kaching123.tcr.commands.print.pos;

import android.content.Context;

import com.kaching123.pos.PosPrinter;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.ecuador.EcuadorPrintProcessor;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;
import com.telly.groundy.TaskResult;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by gdubina on 03.12.13.
 */
public class PrintOrderCommand extends BasePrintOrderCommand {

    protected static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    protected static final String ARG_ORDER_TITLE = "ARG_ORDER_TITLE";
    protected static final String ARG_ORDER_SUBTOTAL = "ARG_ORDER_SUBTOTAL";
    protected static final String ARG_ORDER_DISCOUNTTOTAL = "ARG_ORDER_DISCOUNTTOTAL";
    protected static final String ARG_ORDER_TAXTOTAL = "ARG_ORDER_TAXTOTAL";
    protected static final String ARG_ORDER_TOTALAMOUNT = "ARG_ORDER_TOTALAMOUNT";
    protected static final String ARG_ORDER_TRANSACTIONS = "ARG_ORDER_TRANSACTIONS";

    @Override
    protected TaskResult execute(PosPrinter printer) throws IOException {

        return super.execute(printer);
    }

    @Override
    protected void printBody(PosOrderTextPrinter printerWrapper) {
        String orderGuid = getStringArg(ARG_ORDER_GUID);
        ArrayList<PaymentTransactionModel> transactions = (ArrayList<PaymentTransactionModel>) getArgs().getSerializable(ARG_ORDER_TRANSACTIONS);

        PrintOrderProcessor printProcessor = getPrintOrderProcessor(orderGuid, getAppCommandContext());
        printProcessor.setTitle(getStringArg(ARG_ORDER_TITLE));
        printProcessor.setSubtotal(getStringArg(ARG_ORDER_SUBTOTAL));
        printProcessor.setDiscountTotal(getStringArg(ARG_ORDER_DISCOUNTTOTAL));
        printProcessor.setTaxTotal(getStringArg(ARG_ORDER_TAXTOTAL));
        printProcessor.setPaxTransactions(transactions);
        printProcessor.setAmountTotal(getStringArg(ARG_ORDER_TOTALAMOUNT));
        printProcessor.setPrepaidReleaseResults((ArrayList<PrepaidReleaseResult>) getArgs().getSerializable(ARG_PREPAID_RECEIPTS));

        printProcessor.print(getContext(), getApp(), printerWrapper);
    }

    protected PrintOrderProcessor getPrintOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        return TcrApplication.isEcuadorVersion() ?
                new EcuadorPrintProcessor(orderGuid, appCommandContext) : new PrintOrderProcessor(orderGuid, appCommandContext);
    }

    public static void start(Context context, boolean skipPaperWarning, boolean searchByMac, String orderGuid, ArrayList<PaymentTransactionModel> transactions, ArrayList<PrepaidReleaseResult> receipts, BasePrintCallback callback) {
        create(PrintOrderCommand.class).arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning).arg(ARG_PREPAID_RECEIPTS, receipts).arg(ARG_SEARCH_BY_MAC, searchByMac).arg(ARG_ORDER_GUID, orderGuid).arg(ARG_ORDER_TRANSACTIONS, transactions).callback(callback).queueUsing(context);
    }

    public static void start(Context context, boolean skipPaperWarning, boolean searchByMac, String orderGuid, BasePrintCallback callback, String title, String subTotal, String discountTotal, String taxTotal, String amountTotal) {
        create(PrintOrderCommand.class).arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning).arg(ARG_SEARCH_BY_MAC, searchByMac).arg(ARG_ORDER_GUID, orderGuid).arg(ARG_ORDER_TITLE, title).arg(ARG_ORDER_SUBTOTAL, subTotal).arg(ARG_ORDER_DISCOUNTTOTAL, discountTotal).arg(ARG_ORDER_TAXTOTAL, taxTotal).arg(ARG_ORDER_TOTALAMOUNT, amountTotal).callback(callback).queueUsing(context);
    }

}
