package com.kaching123.tcr.commands.print.digital;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.tcr.commands.payment.credit.PrintCreditReceiptCommand.CreditReceiptViewModel;
import com.kaching123.tcr.commands.rest.email.BaseSendEmailCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.model.CreditReceiptModel;
import com.kaching123.tcr.print.builder.DigitalOrderBuilder;
import com.kaching123.tcr.print.processor.PrintCreditProcessor;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import static com.kaching123.tcr.commands.payment.credit.PrintCreditReceiptCommand.loadReceipt;

/**
 * Created by vkompaniets on 19.03.14.
 */
public class SendDigitalCreditCommand extends BaseSendEmailCommand {

    protected static final String ARG_RECEIPT_GUID = "arg_receipt_guid";
    protected static final String ARG_EMAIL_ADDRESS = "arg_email_address";

    private CreditReceiptModel model;
    private String registerTitle;

    @Override
    protected Response execute(SyncApi restApi, String apiKey) {
        String guid = getStringArg(ARG_RECEIPT_GUID);
        String email = getStringArg(ARG_EMAIL_ADDRESS);
        if (TextUtils.isEmpty(email))
            return Response.responseFailed();

        CreditReceiptViewModel view = loadReceipt(getContext(), guid);
        if(view == null){
            return null;
        }

        model = view.model;
        registerTitle = view.registerName;

        final DigitalOrderBuilder builder = new DigitalOrderBuilder();
        PrintCreditProcessor processor = new PrintCreditProcessor(model, registerTitle, true);
        processor.print(getContext(), getApp(), builder);

        String html = builder.build();
        String subject = "Credit receipt";

        return sendEmail(restApi, apiKey, new String[]{email}, subject, html);
    }

    public static void start(Context context, String receiptGuid, String email){
        create(SendDigitalCreditCommand.class)
                .arg(ARG_RECEIPT_GUID, receiptGuid)
                .arg(ARG_EMAIL_ADDRESS, email)
                .queueUsing(context);
    }

    public static abstract class BaseSendDigitalOrderCallback {

        @OnSuccess(SendDigitalCreditCommand.class)
        public void onSuccess() {
            onDigitalOrderSent();
        }

        @OnFailure(SendDigitalCreditCommand.class)
        public void onError() {
            onDigitalOrderSendError();
        }

        protected abstract void onDigitalOrderSent();

        protected abstract void onDigitalOrderSendError();


    }



}
