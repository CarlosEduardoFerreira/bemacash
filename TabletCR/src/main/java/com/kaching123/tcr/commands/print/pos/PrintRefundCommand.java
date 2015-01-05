package com.kaching123.tcr.commands.print.pos;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;
import com.kaching123.tcr.print.processor.BasePrintProcessor;
import com.kaching123.tcr.print.processor.PrintRefundProcessor;
import com.kaching123.tcr.print.processor.PrintVoidProcessor;
import com.kaching123.tcr.processor.MoneybackProcessor.RefundSaleItemInfo;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gdubina on 23.12.13.
 */
public class PrintRefundCommand extends BasePrintOrderCommand {

    public static final String ARG_ORDER_GUID = "ARG_ORDER_GUID";
    public static final String ARG_CHILD_ORDER_GUID = "ARG_CHILD_ORDER_GUID";
    public static final String ARG_ITEMS_INFO = "ARG_ITEMS_INFO";
    public static final String ARG_REPRINT = "ARG_REPRINT";

    @Override
    protected void printBody(PosOrderTextPrinter printerWrapper) {
        ArrayList<RefundSaleItemInfo> items = (ArrayList<RefundSaleItemInfo>) getArgs().getSerializable(ARG_ITEMS_INFO);
        String orderGuid = getStringArg(ARG_ORDER_GUID);
        String childOrderGuid = getStringArg(ARG_CHILD_ORDER_GUID);
        ArrayList<String> transactionsGuids = getTransactionsGuids(getContext(), childOrderGuid);
        boolean reprint = getBooleanArg(ARG_REPRINT, false);
        BasePrintProcessor printProcessor = null;
        if (items == null) {
            printProcessor = new PrintVoidProcessor(orderGuid, transactionsGuids, reprint, getAppCommandContext());
        } else {
            printProcessor = new PrintRefundProcessor(orderGuid, items, transactionsGuids, reprint, getAppCommandContext()).setChildOrderGuid(childOrderGuid);
        }
        printProcessor.print(getContext(), getApp(), printerWrapper);
    }

    private static ArrayList<String> getTransactionsGuids(Context context, String orderGuid){
        Cursor c = ProviderAction.query(ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT))
                .projection(PaymentTransactionTable.GUID)
                .where(PaymentTransactionTable.ORDER_GUID + " = ?", orderGuid)
                .perform(context);

        ArrayList<String> guids = new ArrayList<String>(c.getCount());
        while (c.moveToNext()){
            guids.add(c.getString(0));
        }
        c.close();

        return guids;
    }

    public static void start(Context context, boolean skipPaperWarning, boolean searchByMac, String orderGuid, String childOrderGuid, ArrayList<RefundSaleItemInfo> items, BasePrintCallback callback) {
        create(PrintRefundCommand.class).arg(ARG_SKIP_PAPER_WARNING, skipPaperWarning).arg(ARG_SEARCH_BY_MAC, searchByMac).arg(ARG_ORDER_GUID, orderGuid).arg(ARG_CHILD_ORDER_GUID, childOrderGuid).arg(ARG_ITEMS_INFO, items).callback(callback).queueUsing(context);
    }

    public static TaskResult sync(Context context, boolean skipPaperWarning, boolean searchByMac, boolean reprint, String orderGuid, String childOrderGuid, List<RefundSaleItemInfo> items, IAppCommandContext appCommandContext){
        Bundle args = new Bundle();
        args.putBoolean(ARG_SKIP_PAPER_WARNING, skipPaperWarning);
        args.putBoolean(ARG_SEARCH_BY_MAC, searchByMac);
        args.putBoolean(ARG_REPRINT, reprint);
        args.putString(ARG_ORDER_GUID, orderGuid);
        args.putString(ARG_CHILD_ORDER_GUID, childOrderGuid);
        args.putSerializable(ARG_ITEMS_INFO, new ArrayList<RefundSaleItemInfo>(items));
        return new PrintRefundCommand().sync(context, args, appCommandContext);
    }
}
