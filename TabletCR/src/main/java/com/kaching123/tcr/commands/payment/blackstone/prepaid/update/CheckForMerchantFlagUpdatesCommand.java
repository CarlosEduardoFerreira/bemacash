package com.kaching123.tcr.commands.payment.blackstone.prepaid.update;

import android.content.Context;

import com.kaching123.tcr.commands.payment.SOAPWebCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.CheckForMerchantFlagUpdateRequest;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.MerchantFlagsResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class CheckForMerchantFlagUpdatesCommand extends SOAPWebCommand<CheckForMerchantFlagUpdateRequest> {

    public final static String ARG_RESULT = "ARG_RESULT";

    private final static String ARG_REQUEST = "request";

    private CheckForMerchantFlagUpdateRequest request;

    protected MerchantFlagsResponse response;

    public final static TaskHandler start(Context context, Object callback, CheckForMerchantFlagUpdateRequest request) {
        return create(CheckForMerchantFlagUpdatesCommand.class)
                .arg(ARG_REQUEST, request)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }

    public MerchantFlagsResponse sync(Context context, CheckForMerchantFlagUpdateRequest request) {
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
    protected Long doCommand(Broker brokerApi, CheckForMerchantFlagUpdateRequest request) {
        response = brokerApi.GetMerchantFlags(
                request.mID,
                request.tID,
                request.password,
                request.transactionId,
                getSign(request.mID,
                        request.tID,
                        request.password,
                        BigDecimal.ZERO,
                        request.transactionId),
                null);
        return response != null ? response.resultId : null;
    }

    @Override
    protected CheckForMerchantFlagUpdateRequest getRequest() {
        return request == null ? (CheckForMerchantFlagUpdateRequest) getArgs().getSerializable(ARG_REQUEST) : request;
    }
}
