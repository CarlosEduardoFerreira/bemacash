package com.kaching123.tcr.commands.payment.blackstone.prepaid.bill;

import android.content.Context;

import com.kaching123.tcr.commands.payment.SOAPWebCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.pinserve.request.GetMasterBillerPaymentOptionsRequest;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.MasterBillerPaymentOptionsResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class GetMasterBillerPaymentOptionsCommand extends SOAPWebCommand<GetMasterBillerPaymentOptionsRequest> {

    protected final static String ARG_USER = "user";
    protected final static String ARG_REQUEST = "request";
    public final static String ARG_RESULT = "ARG_RESULT";

    protected MasterBillerPaymentOptionsResponse response;

    public  final static TaskHandler start(Context context, Object callback, GetMasterBillerPaymentOptionsRequest request) {
        return create(GetMasterBillerPaymentOptionsCommand.class)
                        .callback(callback)
                        .arg(ARG_REQUEST, request)
                        .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        return super.doInBackground().add(ARG_RESULT, response);
    }

    @Override
    protected Long doCommand(Broker brokerApi, GetMasterBillerPaymentOptionsRequest request) {
        response = brokerApi.GetMasterBillerPaymentOptions(
                request.MID,
                request.TID,
                request.Password,
                request.Cashier,
                request.masterBillerCaregoryId,
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
    protected GetMasterBillerPaymentOptionsRequest getRequest() {
        return (GetMasterBillerPaymentOptionsRequest) getArgs().getSerializable(ARG_REQUEST);
    }
}