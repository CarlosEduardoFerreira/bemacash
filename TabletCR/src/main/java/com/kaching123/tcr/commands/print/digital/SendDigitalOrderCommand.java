package com.kaching123.tcr.commands.print.digital;

import android.content.Context;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.rest.email.BaseSendEmailCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.print.builder.DigitalOrderBuilder;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;

/**
 * Created by pkabakov on 21.12.13.
 */
public class SendDigitalOrderCommand extends BaseSendEmailCommand {

    protected static final String ARG_ORDER_GUID = "arg_order_guid";
    protected static final String ARG_EMAIL = "arg_email";
    protected static final String ARG_TRANSACTIONS = "ARG_TRANSACTIONS";
    protected static final String ARG_RELEASE_RESULT_LIST = "ARG_RELEASE_RESULT_LIST";

    @Override
    protected Response execute(SyncApi restApi, String apiKey) {
        String orderGuid = getStringArg(ARG_ORDER_GUID);
        String email = getStringArg(ARG_EMAIL);

        DigitalOrderBuilder orderBuilder = TcrApplication.getCountryFunctionality().getDigitalOrderBuilder();//new DigitalOrderBuilder();
        PrintOrderProcessor printProcessor = getPrintDigitalOrderProcessor(orderGuid, getAppCommandContext());
        printProcessor.setPaxTransactions((ArrayList<PaymentTransactionModel>) getArgs().getSerializable(ARG_TRANSACTIONS));
        printProcessor.setPrepaidReleaseResults((ArrayList<PrepaidReleaseResult>) getArgs().getSerializable(ARG_RELEASE_RESULT_LIST));
        printProcessor.print(getContext(), getApp(), orderBuilder);
        printProcessor.setPrepaidReleaseResults((ArrayList<PrepaidReleaseResult>)getArgs().getSerializable(ARG_RELEASE_RESULT_LIST));

        String html = orderBuilder.build();
        String subject = getSubject(printProcessor.getPrintOrderNumber());

        return sendEmail(restApi, apiKey, new String[]{email}, subject, html);
    }

    protected PrintOrderProcessor getPrintDigitalOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        return TcrApplication.getCountryFunctionality().getOrderOrderProcessor(orderGuid, appCommandContext);
        //new PrintDigitalOrderProcessor(orderGuid, appCommandContext);
    }

    private String getSubject(String orderNumber) {
        return getContext().getString(R.string.order_email_subject, orderNumber);
    }

    public static void start(Context context, String orderGuid, String email, BaseSendDigitalOrderCallback callback, ArrayList<PaymentTransactionModel> transactions, ArrayList<PrepaidReleaseResult> releaseResultList) {
        create(SendDigitalOrderCommand.class).arg(ARG_ORDER_GUID, orderGuid).arg(ARG_EMAIL, email).arg(ARG_TRANSACTIONS, transactions).arg(ARG_RELEASE_RESULT_LIST, releaseResultList).callback(callback).queueUsing(context);
    }

    public static abstract class BaseSendDigitalOrderCallback {

        @OnSuccess(SendDigitalOrderCommand.class)
        public void onSuccess() {
            onDigitalOrderSent();
        }

        @OnFailure(SendDigitalOrderCommand.class)
        public void onError() {
            onDigitalOrderSendError();
        }

        protected abstract void onDigitalOrderSent();

        protected abstract void onDigitalOrderSendError();


    }

}
