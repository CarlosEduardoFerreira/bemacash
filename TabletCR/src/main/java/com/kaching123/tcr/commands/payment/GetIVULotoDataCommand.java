package com.kaching123.tcr.commands.payment;

import android.content.Context;

import com.kaching123.tcr.model.payment.GetIVULotoDataRequest;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.IVULotoDataResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class GetIVULotoDataCommand extends SOAPWebCommand<GetIVULotoDataRequest> {

    protected final static String ARG_USER = "user";
    protected final static String ARG_REQUEST = "request";
    public final static String ARG_RESULT = "ARG_RESULT";

    protected IVULotoDataResponse response;

    public final static TaskHandler start(Context context, GetIVULotoDataRequest request, IVULotoDataCallBack callback) {
        return create(GetIVULotoDataCommand.class)
                .callback(callback)
                .arg(ARG_REQUEST, request)
                .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        return super.doInBackground().add(ARG_RESULT, response);
    }

    @Override
    protected Long doCommand(Broker brokerApi, GetIVULotoDataRequest request) {
        response = brokerApi.GetIVULotoData(
                request.MID,
                request.TID,
                request.Password,
                request.transactionId,
                getSign(request.MID,
                        request.TID,
                        request.Password,
                        request.amount,
                        request.transactionId),
                request.receipt,
                null);

        return response != null ? response.resultId : null;
    }

    @Override
    protected GetIVULotoDataRequest getRequest() {
        return (GetIVULotoDataRequest) getArgs().getSerializable(ARG_REQUEST);
    }

    public static abstract class IVULotoDataCallBack {
        @OnSuccess(GetIVULotoDataCommand.class)
        public void handleSuccess(@Param(GetIVULotoDataCommand.ARG_RESULT) IVULotoDataResponse result) {
            onSuccess(result);
        }

        @OnFailure(GetIVULotoDataCommand.class)
        public void handleFailure() {
            onFailure();
        }

        protected abstract void onSuccess(IVULotoDataResponse result);

        protected abstract void onFailure();
    }
}
