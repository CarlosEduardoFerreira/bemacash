package com.kaching123.tcr.commands.print.digital;

import android.content.Context;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.rest.email.BaseSendEmailCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.print.builder.DigitalOrderBuilder;
import com.kaching123.tcr.print.processor.BasePrintProcessor;
import com.kaching123.tcr.print.processor.PrintDigitalRefundProcessor;
import com.kaching123.tcr.print.processor.PrintDigitalVoidProcessor;
import com.kaching123.tcr.processor.MoneybackProcessor;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;

/**
 * Created by pkabakov on 21.12.13.
 */
public class SendDigitalRefundCommand extends BaseSendEmailCommand {

    private static final String ARG_ORDER_GUID = "arg_order_guid";
    private static final String ARG_ITEMS_INFO = "arg_items_info";
    private static final String ARG_TRANSACTIONS = "arg_transactions";
    private static final String ARG_EMAIL = "arg_email";

    @Override
    protected Response execute(SyncApi restApi, String apiKey) {

        ArrayList<MoneybackProcessor.RefundSaleItemInfo> items = (ArrayList<MoneybackProcessor.RefundSaleItemInfo>) getArgs().getSerializable(ARG_ITEMS_INFO);
        ArrayList<String> transactionsGuids = getArgs().getStringArrayList(ARG_TRANSACTIONS);
        String orderGuid = getStringArg(ARG_ORDER_GUID);
        String email = getStringArg(ARG_EMAIL);

        final DigitalOrderBuilder orderBuilder = new DigitalOrderBuilder();
        BasePrintProcessor printProcessor;
        if (items == null) {
            printProcessor = new PrintDigitalVoidProcessor(orderGuid, transactionsGuids, getAppCommandContext());
        } else {
            printProcessor = new PrintDigitalRefundProcessor(orderGuid, items, transactionsGuids, getAppCommandContext());
        }
        printProcessor.print(getContext(), getApp(), orderBuilder);

        String html = orderBuilder.build();
        String subject = getSubject(printProcessor.getPrintOrderNumber());

        return sendEmail(restApi, apiKey, new String[]{email}, subject, html);
    }

    private String getSubject(String orderNumber) {
        return getContext().getString(R.string.refund_email_subject, orderNumber);
    }

    public static void start(Context context, String orderGuid, ArrayList<MoneybackProcessor.RefundSaleItemInfo> items, ArrayList<String> transactionsGuids, String email, BaseSendDigitalRefundCallback callback) {
        create(SendDigitalRefundCommand.class)
                .arg(ARG_ORDER_GUID, orderGuid)
                .arg(ARG_ITEMS_INFO, items)
                .arg(ARG_TRANSACTIONS, transactionsGuids)
                .arg(ARG_EMAIL, email)
                .callback(callback).queueUsing(context);
    }

    public static abstract class BaseSendDigitalRefundCallback {

        @OnSuccess(SendDigitalRefundCommand.class)
        public void onSuccess() {
            onDigitalRefundSent();
        }

        @OnFailure(SendDigitalRefundCommand.class)
        public void onError() {
            onDigitalRefundSendError();
        }

        protected abstract void onDigitalRefundSent();

        protected abstract void onDigitalRefundSendError();

    }

}
