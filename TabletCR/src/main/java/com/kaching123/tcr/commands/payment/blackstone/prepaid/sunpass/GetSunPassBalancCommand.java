package com.kaching123.tcr.commands.payment.blackstone.prepaid.sunpass;

import android.content.Context;

import com.kaching123.tcr.commands.payment.SOAPWebCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.sunpass.request.SunEntryRequest;
import com.kaching123.tcr.websvc.api.prepaid.BalanceResponse;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class GetSunPassBalancCommand extends SOAPWebCommand<SunEntryRequest> {

    protected final static String ARG_USER = "user";
    protected final static String ARG_REQUEST = "request";
    public final static String ARG_RESULT = "ARG_RESULT";

    protected BalanceResponse response;

    public  final static TaskHandler start(Context context, Object callback, SunEntryRequest request) {
        return create(GetSunPassBalancCommand.class)
                      .arg(ARG_REQUEST, request)
                      .callback(callback)
                      .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        return super.doInBackground().add(ARG_RESULT, response);
    }

    @Override
    protected Long doCommand(Broker brokerApi, SunEntryRequest request) {
        response = brokerApi.GetSunPassBalance(
                request.mID,
                request.tID,
                request.password,
                request.cashier,
                request.accountNumber,
                request.transactionMode,
                request.transactionId,
                getSign(request.mID,
                        request.tID,
                        request.password,
                        null,
                        request.transactionId));
        return response != null ? (long)response.responseCode : null;
    }

    @Override
    protected SunEntryRequest getRequest() {
        return (SunEntryRequest) getArgs().getSerializable(ARG_REQUEST);
    }
}