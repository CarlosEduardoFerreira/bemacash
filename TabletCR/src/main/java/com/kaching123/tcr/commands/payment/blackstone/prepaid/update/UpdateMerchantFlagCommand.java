package com.kaching123.tcr.commands.payment.blackstone.prepaid.update;

import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.payment.SOAPWebCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.wireless.request.CheckForUpdateRequest;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.WirelessTable;
import com.kaching123.tcr.websvc.api.prepaid.Broker;
import com.kaching123.tcr.websvc.api.prepaid.MerchantFlagsResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class UpdateMerchantFlagCommand extends SOAPWebCommand<CheckForUpdateRequest> {

    private static final Uri URI_WIRELESS = ShopProvider.getContentUri(WirelessTable.URI_CONTENT);

    protected final static String ARG_REQUEST = "request";
    public final static String ARG_RESULT = "ARG_RESULT";

    protected MerchantFlagsResponse response;
    protected CheckForUpdateRequest request;

    public final static TaskHandler start(Context context, Object callback, CheckForUpdateRequest request) {
        return create(UpdateMerchantFlagCommand.class)
                .arg(ARG_REQUEST, request)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }

    /**
     * it's not a asynccommand, need for sync. can be standalone *
     */
    public MerchantFlagsResponse sync(Context context, CheckForUpdateRequest request) {
        this.request = request;
        //no need in command cache(creds passed on start)
        TaskResult result = syncStandalone(context, null, null);
        return isFailed(result) ? null : response;
    }

    @Override
    protected TaskResult doInBackground() {
        return super.doInBackground().add(ARG_RESULT, response);
    }

    @Override
    protected Long doCommand(Broker brokerApi, CheckForUpdateRequest request) {
        response = brokerApi.GetMerchantFlags(
                request.mID,
                request.tID,
                request.password,
                request.transactionId,
                getSign(request.mID,
                        request.tID,
                        request.password,
                        null,
                        request.transactionId));
        return response != null ? response.resultId : null;
    }


    @Override
    protected CheckForUpdateRequest getRequest() {
        return request == null ? (CheckForUpdateRequest) getArgs().getSerializable(ARG_REQUEST) : request;
    }
}
