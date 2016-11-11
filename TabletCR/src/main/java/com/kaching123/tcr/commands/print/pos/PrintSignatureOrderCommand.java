package com.kaching123.tcr.commands.print.pos;

import android.content.Context;

import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.print.printer.PosSignatureTextPrinter;
import com.kaching123.tcr.print.processor.PrintSignatureProcessor;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 13.01.14.
 */
public class PrintSignatureOrderCommand extends BasePrintCommand<PosSignatureTextPrinter> {

    public static enum ReceiptType {CUSTOMER, MERCHANT, DEBIT, EBT_CASH, EBT}

    private static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    private static final String ARG_TRANSACTIONS = "ARG_TRANSACTIONS";
    private static final String ARG_TYPE = "ARG_TYPE";

    @Override
    protected void printBody(PosSignatureTextPrinter printerWrapper) {
        String orderGuid = getStringArg(ARG_ORDER_GUID);
        ReceiptType type = (ReceiptType)getArgs().getSerializable(ARG_TYPE);
        ArrayList<PaymentTransactionModel> transactions = (ArrayList<PaymentTransactionModel>) getArgs().getSerializable(ARG_TRANSACTIONS);
        PrintSignatureProcessor printProcessor = new PrintSignatureProcessor(orderGuid, transactions, type, getAppCommandContext());
        printProcessor.print(getContext(), getApp(), printerWrapper);
    }

    public static void start(Context context, boolean skipPaperWarning, boolean searchByMac, String orderGuid, ArrayList<PaymentTransactionModel> transactions, ReceiptType receiptType, BasePrintCallback callback) {


        create(PrintSignatureOrderCommand.class).arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning).arg(ARG_SEARCH_BY_MAC, searchByMac).arg(ARG_ORDER_GUID, orderGuid).arg(ARG_TRANSACTIONS, transactions).arg(ARG_TYPE, receiptType).callback(callback).queueUsing(context);



    }

    @Override
    protected PosSignatureTextPrinter createTextPrinter() {
        return new PosSignatureTextPrinter(getContext());
    }
}
