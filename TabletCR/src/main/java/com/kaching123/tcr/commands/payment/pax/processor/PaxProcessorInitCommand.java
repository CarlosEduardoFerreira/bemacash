package com.kaching123.tcr.commands.payment.pax.processor;

import android.content.Context;
import android.os.Bundle;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.ManageResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

public class PaxProcessorInitCommand extends PaxProcessorBaseCommand {

    public static final String RESULT_DETAILS = "RESULT_DETAILS";
    public static final String RESULT_CODE = "RESULT_CODE";
    private PaxModel paxModel;

    public static void start(Context context, PaxModel paxTerminal, PaxProcessorInitCommandCallback callback) {
        create(PaxProcessorInitCommand.class).arg(ARG_DATA_PAX, paxTerminal).allowNonUiCallbacks().callback(callback).queueUsing(context);
    }

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }

    public TaskResult sync(Context context, PaxModel model) {
        Bundle b = new Bundle();
        b.putParcelable(ARG_DATA_PAX, model);
        //no need in commands cache (creds on start)
        paxModel = model;
        return super.sync(context, b, null);
    }

    @Override
    protected PaxModel getPaxModel() {
        return paxModel;
    }

    @Override
    protected TaskResult doCommand() {
        String errorMsg = null;
        int errorCode = 0;

        PaxGateway.Error error = PaxGateway.Error.UNDEFINED;
        TransactionStatusCode responseCode = TransactionStatusCode.EMPTY_REQUEST;
        try {

            ManageRequest manageRequest = new ManageRequest();
            manageRequest.TransType = MANAGE_TRANSACTION_TYPE_INIT;

            PosLink posLink = createPosLink();
            posLink.ManageRequest = manageRequest;

            ProcessTransResult ptr = posLink.ProcessTrans();
            if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {

                ManageResponse response = posLink.ManageResponse;
                PaxProcessorResponse paxResp = new PaxProcessorResponse(response);
                responseCode = paxResp.getStatusCode();
                // TODO PosLink ResultCode "0000"?
                if (response.ResultCode.equalsIgnoreCase(RESULT_CODE_SUCCESS)) {
                    Logger.d("PaxProcessorInitCommand ResultCode: " + response.ResultCode);
                    return succeeded().add(RESULT_DETAILS, response.ResultCode).add(RESULT_CODE, errorCode);
                } else {
                    error = PaxGateway.Error.PAX;
                    return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
                }

            } else if (ptr.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
                error = PaxGateway.Error.TIMEOUT;
                Logger.e("PaxProcessorInitCommand failed, pax error code: " + ptr.Code);
                return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
            } else {
                error = PaxGateway.Error.SERVICE;
                Logger.e("PaxProcessorInitCommand failed, pax error code: " + ptr.Code);
                return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
            }
        } catch (Exception e) {
            Logger.e("PaxProcessorInitCommand failed", e);
            error = PaxGateway.Error.CONNECTIVITY;
        }
        return failed().add(RESULT_DETAILS, errorMsg).add(RESULT_CODE, errorCode);
    }

    public static abstract class PaxProcessorInitCommandCallback {

        @OnSuccess(PaxProcessorInitCommand.class)
        public final void onSuccess(@Param(RESULT_DETAILS) String details) {
            handleSuccess(details);
        }

        protected abstract void handleSuccess(String details);

        @OnFailure(PaxProcessorInitCommand.class)
        public final void onFailure(@Param(RESULT_DETAILS) String error) {
            handleError(error);
        }

        protected abstract void handleError(String error);
    }
}
