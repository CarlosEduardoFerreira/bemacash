package com.kaching123.tcr.commands.print.digital;

import android.content.Context;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.rest.email.BaseSendEmailCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.print.builder.DigitalOrderBuilder;
import com.kaching123.tcr.print.processor.PrintDigitalOrderProcessor;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;
import com.kaching123.tcr.websvc.api.prepaid.IVULotoDataResponse;
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
    protected static final String ARG_IUVLOTO_RESPONSE = "ARG_IUVLOTO_RESPONSE";
    protected static final String ARG_IVU_ACTIVATED = "ARG_IVU_ACTIVATED";

    @Override
    protected Response execute(SyncApi restApi, String apiKey) {
        String orderGuid = getStringArg(ARG_ORDER_GUID);
        String email = getStringArg(ARG_EMAIL);
        IVULotoDataResponse response = (IVULotoDataResponse)getArgs().getSerializable(ARG_IUVLOTO_RESPONSE);

        DigitalOrderBuilder orderBuilder = new DigitalOrderBuilder();
        PrintOrderProcessor printProcessor = getPrintDigitalOrderProcessor(orderGuid, getAppCommandContext());
        printProcessor.setPaxTransactions((ArrayList<PaymentTransactionModel>) getArgs().getSerializable(ARG_TRANSACTIONS));
        printProcessor.setIVULotoDataResponse(response);
        printProcessor.setIVULotoActivated(getBooleanArg(ARG_IVU_ACTIVATED));
        printProcessor.print(getContext(), getApp(), orderBuilder);

        String html = orderBuilder.build();
        String subject = getSubject(printProcessor.getPrintOrderNumber());

        return sendEmail(restApi, apiKey, new String[]{email}, subject, html);
    }

    protected PrintOrderProcessor getPrintDigitalOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        return new PrintDigitalOrderProcessor(orderGuid, appCommandContext);
    }

    private String getSubject(String orderNumber) {
        return getContext().getString(R.string.order_email_subject, orderNumber);
    }

    public static void start(Context context, String orderGuid, String email, BaseSendDigitalOrderCallback callback, ArrayList<PaymentTransactionModel> transactions, IVULotoDataResponse ivuLotoDataResponse, boolean IVULotoActivated) {
        create(SendDigitalOrderCommand.class).arg(ARG_ORDER_GUID, orderGuid).arg(ARG_EMAIL, email).arg(ARG_TRANSACTIONS, transactions).arg(ARG_IUVLOTO_RESPONSE, ivuLotoDataResponse).arg(ARG_IVU_ACTIVATED,IVULotoActivated).callback(callback).queueUsing(context);
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
