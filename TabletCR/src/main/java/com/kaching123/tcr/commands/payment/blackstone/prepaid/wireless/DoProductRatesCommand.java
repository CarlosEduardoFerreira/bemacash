package com.kaching123.tcr.commands.payment.blackstone.prepaid.wireless;

import android.content.Context;

import com.kaching123.tcr.commands.payment.SOAPWebCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.CheckForUpdateRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.DoProductRatesRequest;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.ProductRatesResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class DoProductRatesCommand extends SOAPWebCommand<DoProductRatesRequest> {


    public final static String ARG_RESULT = "ARG_RESULT";

    private final static String ARG_REQUEST = "request";

    private DoProductRatesRequest request;

    protected ProductRatesResponse response;

    public final static TaskHandler start(Context context, ProductRatesCommandCallback callback, DoProductRatesRequest request) {
        return create(DoProductRatesCommand.class)
                .arg(ARG_REQUEST, request)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }

    public ProductRatesResponse sync(Context context, DoProductRatesRequest request) {
        this.request = request;
        //no need in command cache(creds passed on start)
        TaskResult result = super.sync(context, null, null);
        return isFailed(result) ? null : response;
    }

    @Override
    protected TaskResult doInBackground() {
        return super.doInBackground().add(ARG_RESULT, response);
    }

    @Override
    protected Long doCommand(Broker brokerApi, DoProductRatesRequest request) {
        response = brokerApi.GetProductRates(
                request.mID,
                request.tID,
                request.password,
                request.productMainCode,
                request.transactionId,
                getSign(request.mID,
                        request.tID,
                        request.password,
                        BigDecimal.ZERO,
                        request.transactionId));
        return response != null ? response.resultId : null;
    }

    @Override
    protected DoProductRatesRequest getRequest() {
        return request == null ? (DoProductRatesRequest) getArgs().getSerializable(ARG_REQUEST) : request;
    }

    public static abstract class ProductRatesCommandCallback {

        @OnSuccess(DoProductRatesCommand.class)
        public void onSuccess(@Param(ARG_RESULT) ProductRatesResponse response) {
            handleSuccess(response);
        }

        @OnFailure(DoProductRatesCommand.class)
        public void onFailure(@Param(PINCommand.ARG_RESULT) ProductRatesResponse result) {
            handleFailure(result);
        }

        protected abstract void handleSuccess(ProductRatesResponse response);

        protected abstract void handleFailure(ProductRatesResponse result);

    }
}
