package com.kaching123.tcr.commands.payment.blackstone.prepaid.wireless;

import android.content.Context;

import com.kaching123.tcr.commands.payment.blackstone.prepaid.BasePrepaidPaymentCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.DoTopUpRequest;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.PIN;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class PINCommand extends BasePrepaidPaymentCommand<DoTopUpRequest> {

    public final static String ARG_RESULT = "ARG_RESULT";

    protected PIN response;

    public  final static TaskHandler start(Context context, PinCommandCallback callback, DoTopUpRequest request) {
        return create(PINCommand.class)
                      .arg(ARG_REQUEST, request)
                      .callback(callback)
                      .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        return super.doInBackground().add(ARG_RESULT, response);
    }

    @Override
    protected Long doCommand(Broker brokerApi, DoTopUpRequest request) {
        response = brokerApi.GetSinglePIN(
                request.mID,
                request.tID,
                request.password,
                request.cashier,
                request.productMaincode,
                request.topUpAmount.doubleValue(),
                request.orderID,
                request.profileID,
                request.transactionMode,
                getSign(request.mID,
                        request.tID,
                        request.password,
                        request.topUpAmount,
                        request.orderID));
        return response != null ? (long) response.errorCode : null;
    }
    public static abstract class PinCommandCallback {

        @OnSuccess(PINCommand.class)
        public void onSuccess(@Param(ARG_RESULT) PIN response) {
            handleSuccess(response);
        }

        @OnFailure(PINCommand.class)
        public void onFailure(@Param(PINCommand.ARG_RESULT) PIN result) {
            handleFailure(result);
        }

        protected abstract void handleSuccess(PIN response);

        protected abstract void handleFailure(PIN result);

    }
}