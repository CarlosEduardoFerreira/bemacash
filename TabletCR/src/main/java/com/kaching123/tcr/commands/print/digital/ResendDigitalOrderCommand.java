package com.kaching123.tcr.commands.print.digital;

import android.content.Context;

import com.kaching123.tcr.print.processor.PrintDigitalOrderProcessor;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

/**
 * Created by pkabakov on 25.02.14.
 */
public class ResendDigitalOrderCommand extends SendDigitalOrderCommand {

    public static void start(Context context, String orderGuid, String email, BaseResendDigitalOrderCallback callback) {
        create(ResendDigitalOrderCommand.class).arg(ARG_ORDER_GUID, orderGuid).arg(ARG_EMAIL, email).callback(callback).queueUsing(context);
    }

    @Override
    protected PrintDigitalOrderProcessor getPrintDigitalOrderProcessor(String orderGuid, IAppCommandContext appCommandContext) {
        return new PrintDigitalOrderProcessor(orderGuid, true, appCommandContext);
    }

    public static abstract class BaseResendDigitalOrderCallback {

        @OnSuccess(ResendDigitalOrderCommand.class)
        public void onSuccess() {
            onDigitalOrderSent();
        }

        @OnFailure(ResendDigitalOrderCommand.class)
        public void onError() {
            onDigitalOrderSendError();
        }

        protected abstract void onDigitalOrderSent();

        protected abstract void onDigitalOrderSendError();


    }
}
