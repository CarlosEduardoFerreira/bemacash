package com.kaching123.tcr.commands.payment.blackstone.IVULoto;

import android.content.Context;

import com.kaching123.tcr.commands.payment.blackstone.prepaid.BasePrepaidPaymentCommand;
import com.kaching123.tcr.model.payment.blackstone.IVULoto.IVULotoDataRequest;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.IVULotoDataResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

/**
 * Created by teli on 6/26/2015.
 */
public class IVULotoDataCommand extends BasePrepaidPaymentCommand<IVULotoDataRequest> {
    public final static String ARG_RESULT = "ARG_RESULT";

    protected IVULotoDataResponse response;

    public final static TaskHandler start(Context context, BillpaymentCommandCallback callback, IVULotoDataRequest request) {
        return create(IVULotoDataCommand.class)
                .arg(ARG_REQUEST, request)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        if (getTrainingMode())
            return succeeded().add(ARG_RESULT, response);
        return super.doInBackground().add(ARG_RESULT, response);
    }

    private boolean getTrainingMode() {
        return getApp().isTrainingMode();
    }

    @Override
    protected Long doCommand(Broker brokerApi, IVULotoDataRequest ivuLotoDataRequest) {
        response = brokerApi.GetIVULotoData(
                request.mID,
                request.tID,
                request.password,
                request.transactionId,
                getSign(request.mID,
                        request.tID,
                        request.password,
                        request.paymentAmount,
                        request.transactionId),
                null);

        return response != null ? response.resultId : null;
    }

    public static abstract class BillpaymentCommandCallback {
        @OnSuccess(IVULotoDataCommand.class)
        public void onSuccess() {
            handleSuccess();
        }

        @OnFailure(IVULotoDataCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleSuccess();

        protected abstract void handleFailure();
    }
}
