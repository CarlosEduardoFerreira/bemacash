package com.kaching123.tcr.commands.payment.blackstone.prepaid.bill;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.SOAPWebCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.GetMasterBillersByCategoryRequest;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.MasterBillersByCategoryResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class GetMasterBillersByCategoryCommand extends SOAPWebCommand<GetMasterBillersByCategoryRequest> {

    protected final static String ARG_USER = "user";
    protected final static String ARG_REQUEST = "request";
    public final static String ARG_RESULT = "ARG_RESULT";

    protected MasterBillersByCategoryResponse response;

    public final static TaskHandler start(Context context, MasterBillerCategoryCommand callback, GetMasterBillersByCategoryRequest request) {
        return create(GetMasterBillersByCategoryCommand.class)
                .callback(callback)
                .arg(ARG_REQUEST, request)
                .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        return super.doInBackground().add(ARG_RESULT, response);
    }

    @Override
    protected Long doCommand(Broker brokerApi, GetMasterBillersByCategoryRequest request) {
        response = brokerApi.GetMasterBillersByCategory(
                request.MID,
                request.TID,
                request.Password,
                request.Cashier,
                request.CaregoryId,
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
    protected GetMasterBillersByCategoryRequest getRequest() {
        return (GetMasterBillersByCategoryRequest) getArgs().getSerializable(ARG_REQUEST);
    }

    public static abstract class MasterBillerCategoryCommand {

        @OnSuccess(GetMasterBillersByCategoryCommand.class)
        public void handleSuccess(@Param(GetMasterBillersByCategoryCommand.ARG_RESULT) MasterBillersByCategoryResponse result) {
            onSuccess(result);
        }

        @OnFailure(GetMasterBillersByCategoryCommand.class)
        public void handleFailure(@Param(GetMasterBillersByCategoryCommand.ARG_RESULT) MasterBillersByCategoryResponse result) {
            onFailure(result);
        }

        protected abstract void onSuccess(MasterBillersByCategoryResponse result);

        protected abstract void onFailure(MasterBillersByCategoryResponse result);

    }
}