package com.kaching123.tcr.commands.payment.blackstone.prepaid.bill;

import android.content.Context;

import com.kaching123.tcr.commands.payment.SOAPWebCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.GetBillerCategoriesRequest;
import com.kaching123.tcr.websvc.api.prepaid.BillerCategoriesResponse;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class GetBillerCategoriesCommand extends SOAPWebCommand<GetBillerCategoriesRequest> {

    protected final static String ARG_USER = "user";
    protected final static String ARG_REQUEST = "request";
    public final static String ARG_RESULT = "ARG_RESULT";

    protected BillerCategoriesResponse response;

    public final static TaskHandler start(Context context, Object callback, GetBillerCategoriesRequest request) {
        return create(GetBillerCategoriesCommand.class)
                .arg(ARG_REQUEST, request)
                .callback(callback)
                .queueUsing(context);
    }

    public BillerCategoriesResponse sync(Context context, GetBillerCategoriesRequest request) {
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
    protected Long doCommand(Broker brokerApi, GetBillerCategoriesRequest request) {
        response = brokerApi.GetBillerCategories(
                request.MID,
                request.TID,
                request.Password,
                request.Cashier,
                request.transactionId,
                request.TransactionMode,
                getSign(request.MID,
                        request.TID,
                        request.Password,
                        request.amount,
                        request.transactionId));
        return response != null ? response.resultId : null;
    }

    @Override
    protected GetBillerCategoriesRequest getRequest() {
        return request == null ? (GetBillerCategoriesRequest) getArgs().getSerializable(ARG_REQUEST) : request;
    }
}