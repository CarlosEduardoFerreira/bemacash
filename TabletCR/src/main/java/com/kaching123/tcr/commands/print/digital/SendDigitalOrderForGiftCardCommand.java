package com.kaching123.tcr.commands.print.digital;

import android.content.Context;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.rest.email.BaseSendEmailCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PrepaidReleaseResult;
import com.kaching123.tcr.print.builder.DigitalOrderBuilder;
import com.kaching123.tcr.print.processor.PrintDigitalOrderProcessor;
import com.kaching123.tcr.print.processor.PrintGiftCardProcessor;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by pkabakov on 21.12.13.
 */
public class SendDigitalOrderForGiftCardCommand extends BaseSendEmailCommand {

    protected static final String ARG_EMAIL = "arg_email";
    protected static final String ARG_AMOUNT = "ARG_AMOUNT";

    @Override
    protected Response execute(SyncApi restApi, String apiKey) {
        String email = getStringArg(ARG_EMAIL);

        PrintGiftCardProcessor printProcessor = new PrintGiftCardProcessor(getAppCommandContext());
//
        DigitalOrderBuilder orderBuilder = new DigitalOrderBuilder();

        printProcessor.setAmount(new BigDecimal(getArgs().getString(ARG_AMOUNT)));
        printProcessor.print(getContext(), getApp(), orderBuilder);

        String html = orderBuilder.build();
        String subject = getSubject(printProcessor.getPrintOrderNumber());

        return sendEmail(restApi, apiKey, new String[]{email}, subject, html);
    }

    private String getSubject(String orderNumber) {
        return getContext().getString(R.string.order_email_subject, orderNumber);
    }

    public static void start(Context context, String email, String amount, BaseSendDigitalOrderCallback callback) {
        create(SendDigitalOrderForGiftCardCommand.class).arg(ARG_EMAIL, email).arg(ARG_AMOUNT, amount).callback(callback).queueUsing(context);
    }

    public static abstract class BaseSendDigitalOrderCallback {

        @OnSuccess(SendDigitalOrderForGiftCardCommand.class)
        public void onSuccess() {
            onDigitalOrderSent();
        }

        @OnFailure(SendDigitalOrderForGiftCardCommand.class)
        public void onError() {
            onDigitalOrderSendError();
        }

        protected abstract void onDigitalOrderSent();

        protected abstract void onDigitalOrderSendError();


    }

}
