package com.kaching123.tcr.commands.payment.blackstone.prepaid.bill;

import android.content.Context;

import com.kaching123.tcr.commands.payment.blackstone.prepaid.BasePrepaidPaymentCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.BillPaymentRequest;
import com.kaching123.tcr.websvc.api.prepaid.BillPaymentResponse;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class BillPaymentCommand extends BasePrepaidPaymentCommand<BillPaymentRequest> {

    public final static String ARG_RESULT = "ARG_RESULT";

    protected BillPaymentResponse response;

    public final static TaskHandler start(Context context, BillpaymentCommandCallback callback, BillPaymentRequest request) {
        return create(BillPaymentCommand.class)
                .arg(ARG_REQUEST, request)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        return super.doInBackground().add(ARG_RESULT, response);
    }

    @Override
    protected Long doCommand(Broker brokerApi, BillPaymentRequest request) {
        response = brokerApi.DoBillPayment(
                request.mID,
                request.tID,
                request.password,
                request.cashier,
                request.vendorId,
                request.accountNumber,
                request.altAccountNumber,
                request.additAccountNumber1,
                request.additAccountNumber2,
                request.paymentAmount.stripTrailingZeros().doubleValue(),
                request.feeAmount,
                request.customerFirstName,
                request.customerLastName,
                request.paymentType,
                request.senderFirstName,
                request.senderLastName,
                request.transactionId,
                request.transactionMode,
                getSign(request.mID,
                        request.tID,
                        request.password,
                        request.paymentAmount,
                        request.transactionId));

        return response != null ? response.resultId : null;
    }

    public static abstract class BillpaymentCommandCallback {
        @OnSuccess(BillPaymentCommand.class)
        public void onBillCommandSuccess(@Param(BillPaymentCommand.ARG_RESULT) BillPaymentResponse result) {
            handleSuccess(result);
        }

        @OnFailure(BillPaymentCommand.class)
        public void onBillCommandFail(@Param(BillPaymentCommand.ARG_RESULT) BillPaymentResponse result) {
            handleFailure(result);
        }

        protected abstract void handleSuccess(BillPaymentResponse result);

        protected abstract void handleFailure(BillPaymentResponse result);
    }

}