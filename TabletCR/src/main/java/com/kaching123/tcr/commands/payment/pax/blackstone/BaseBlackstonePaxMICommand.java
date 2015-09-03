package com.kaching123.tcr.commands.payment.pax.blackstone;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.processor.PaxPokeProcessor;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.BaseMIRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.MIResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.MerchantDetails;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.MerchantDetailsResponse;
import com.telly.groundy.TaskResult;

import org.apache.http.HttpStatus;

import retrofit.RetrofitError;

/**
 * Created by pkabakov on 25.06.2014.
 */
public abstract class BaseBlackstonePaxMICommand<T extends BaseMIRequest> extends PaxBlackstoneBaseCommand {

    protected MerchantDetails merchantDetails;

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }

    public MerchantDetails sync(Context context, PaxModel paxTerminal) {
        this.paxTerminal = paxTerminal;
        //no need in commands cache (creds on start)
        TaskResult result = super.sync(context, null, null);
        if (isFailed(result))
            return null;

        return merchantDetails;
    }

    @Override
    protected TaskResult doCommand(PaxWebApi api) {
        boolean paxSuccess = false;
        boolean apiSuccess = false;
        boolean success = false;
        try {
            MIResponse response = getResponse(api, getRequest());
            PaxPokeProcessor.get().start(getContext());
            MerchantDetailsResponse merchantDetailsResponse = response == null ? null : response.getDetails();
            merchantDetails = merchantDetailsResponse == null ? null : merchantDetailsResponse.getDetails();

            Integer paxResponseCode = response == null ? null : response.getResponse();
            TransactionStatusCode responseCode = merchantDetailsResponse == null ? null : TransactionStatusCode.valueOf(merchantDetailsResponse.getResponse());
            boolean merchantInfoReceived = merchantDetails != null;

            paxSuccess = paxResponseCode != null && paxResponseCode == HttpStatus.SC_OK;
            apiSuccess = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY == responseCode && merchantInfoReceived;
            success = paxSuccess && apiSuccess;
            Logger.d("BasePaxMICommand response: paxResponseCode: " + paxResponseCode + "; responseCode: " + (responseCode == null ? null : responseCode.getCode()) + "; merchantInfoReceived: " + merchantInfoReceived);

            if (!success) {
                Logger.e("BasePaxMICommand failed, pax error code: " + paxResponseCode + "; error code: " + (responseCode == null ? null : responseCode.getCode()) + "; merchant info received: " + merchantInfoReceived);
            }
        } catch (Pax404Exception e) {
            Logger.e("BasePaxMICommand failed", e);
        } catch (RetrofitError e) {
            Logger.e("BasePaxMICommand failed", e);
        } finally {
            if (shouldStoreData())
                storeData(success, paxSuccess, apiSuccess);
        }

        if (!success)
            return failed();
        return succeeded();
    }

    protected boolean shouldStoreData() {
        return true;
    }

    protected void storeData(boolean success, boolean paxSuccess, boolean apiSuccess) {
        if (paxSuccess && !apiSuccess) {
            Logger.d("BasePaxMICommand.storeData(): downloaded nothing");
            return;
        }
        if (!paxSuccess) {
            Logger.e("BasePaxMICommand.storeData(): result unknown - resetting info to safe values");
        }
        boolean tipsEnabled = success ? merchantDetails.getTipsEnabled() > 0 : false;
        Logger.d("BasePaxMICommand.storeData(): tipsEnabled: " + tipsEnabled);
        getApp().setPaxTipsEnabled(tipsEnabled);
    }

    protected abstract T getRequest();

    protected abstract MIResponse getResponse(PaxWebApi api, T request);

}
