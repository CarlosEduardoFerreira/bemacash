package com.kaching123.tcr.commands.payment.blackstone.prepaid.sunpass;

import android.content.Context;

import com.kaching123.tcr.commands.payment.blackstone.prepaid.BasePrepaidPaymentCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunPassDocumentPaymentRequest;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.DocumentPaymentResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class DoSunPassDocumentPaymentCommand extends BasePrepaidPaymentCommand<SunPassDocumentPaymentRequest> {

    public final static String ARG_RESULT = "ARG_RESULT";

    protected DocumentPaymentResponse response;

    public final static TaskHandler start(Context context, DoSunPassDocumentPaymentCommandCallback callback, SunPassDocumentPaymentRequest request) {
        return create(DoSunPassDocumentPaymentCommand.class)
                .arg(ARG_REQUEST, request)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        return super.doInBackground().add(ARG_RESULT, response);
    }

    @Override
    protected Long doCommand(Broker brokerApi, SunPassDocumentPaymentRequest request) {
        response = brokerApi.DoSunPassDocumentPayment(
                request.mID,
                request.tID,
                request.password,
                request.cashier,
                request.transactionId,
                request.accountNumber,
                request.licensePateleNumber,
                request.amount.doubleValue(),
                request.feeAmount,
                request.purchaseId,
                request.paidDocuments,
                request.transactionMode,
                getSign(request.mID,
                        request.tID,
                        request.password,
                        request.amount,
                        request.transactionId)
        );
        return response != null ? (long) response.responseCode : null;
    }

    public static abstract class DoSunPassDocumentPaymentCommandCallback {
        @OnSuccess(DoSunPassDocumentPaymentCommand.class)
        public void onDoSunPassDocumentPaymentCommandSuccess(@Param(DoSunPassDocumentPaymentCommand.ARG_RESULT) DocumentPaymentResponse result) {
            handleSuccess(result);
        }

        @OnFailure(DoSunPassDocumentPaymentCommand.class)
        public void onDoSunPassDocumentPaymentCommandFail(@Param(DoSunPassDocumentPaymentCommand.ARG_RESULT) DocumentPaymentResponse result) {
            handleFailure(result);
        }

        protected abstract void handleSuccess(DocumentPaymentResponse result);

        protected abstract void handleFailure(DocumentPaymentResponse result);
    }

}