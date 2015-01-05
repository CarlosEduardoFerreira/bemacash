package com.kaching123.tcr.commands.payment.blackstone.prepaid.sunpass;

import android.content.Context;

import com.kaching123.tcr.commands.payment.blackstone.prepaid.BasePrepaidPaymentCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunReplenishmentRequest;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.ReplenishmentResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class DoSunPassReplenishmentCommand extends BasePrepaidPaymentCommand<SunReplenishmentRequest> {

    public final static String ARG_RESULT = "ARG_RESULT";

    protected ReplenishmentResponse response;

    public  final static TaskHandler start(Context context, DoSunPassREplenishmentCommandCallback callback, SunReplenishmentRequest request) {
        return create(DoSunPassReplenishmentCommand.class)
                    .arg(ARG_REQUEST, request)
                    .callback(callback)
                    .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        return super.doInBackground().add(ARG_RESULT, response);
    }

    @Override
    protected Long doCommand(Broker brokerApi, SunReplenishmentRequest request) {
        response = brokerApi.DoSunPassReplenishment(
                request.mID,
                request.tID,
                request.password,
                request.cashier,
                request.transactionId,
                request.accountNumber,
                request.amount.doubleValue(),
                request.feeAmount,
                request.purchaseId,
                request.transactionMode,
                getSign(request.mID,
                        request.tID,
                        request.password,
                        request.amount,
                        request.transactionId));
        return response != null ? (long) response.responseCode : null;
    }

    public static abstract class DoSunPassREplenishmentCommandCallback{

        @OnSuccess(DoSunPassReplenishmentCommand.class)
        public void onDoSunPassReplenishmentCommandSuccess(@Param(DoSunPassReplenishmentCommand.ARG_RESULT) ReplenishmentResponse result) {
            handleSuccess(result);
        }

        @OnFailure(DoSunPassReplenishmentCommand.class)
        public void onDoSunPassReplenishmentCommandFail(@Param(DoSunPassReplenishmentCommand.ARG_RESULT) ReplenishmentResponse result) {
            handleFailure(result);
        }
        protected abstract void handleSuccess(ReplenishmentResponse result);

        protected abstract void handleFailure(ReplenishmentResponse result);
    }

}