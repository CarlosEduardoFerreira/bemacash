package com.kaching123.tcr.commands.payment.pax.processor;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.util.CalculationUtil;
import com.pax.poslink.BatchRequest;
import com.pax.poslink.BatchResponse;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mayer
 */
public class PaxProcessorBatchOutCommand extends PaxProcessorBaseCommand {

    public static final int TRANSACTION_ID_BALANCE = 9;

    private static final String RESULT_AMOUNT = "RES_TRANSACTION";
    private static final String RESULT_LAST4 = "RESULT_LAST4";
    private static final String RESULT_ERROR_REASON = "ERROR";
    private static final String ARG_PURPOSE = "ARG_AMOUNT_1";
    public static final String SUCCESS = "SUCCESS";


    public static final TaskHandler start(Context context,
                                          PaxProcessorBatchOutCommandCallback callback) {
        return create(PaxProcessorBatchOutCommand.class)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected PaxModel getPaxModel() {
        return PaxModel.get();
    }

    @Override
    protected TaskResult doCommand() {

        int transactionId = getIntArg(ARG_PURPOSE);
        BigDecimal amount = null;
        String lastFour = "";
        String errorReason = "";

        PaxGateway.Error error = PaxGateway.Error.UNDEFINED;
        TransactionStatusCode responseCode = TransactionStatusCode.EMPTY_REQUEST;
        try {

            BatchRequest batchRequest = new BatchRequest();
            batchRequest.TransType = TRANSACTION_TYPE_BATCHCLOSE;
            batchRequest.EDCType = TRANSACTION_ID_ALL;
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMDDhhmmss");
            batchRequest.Timestamp = df.format(new Date());

            PosLink posLink = createPosLink();
            posLink.BatchRequest = batchRequest;


            ProcessTransResult ptr = posLink.ProcessTrans();
            Logger.e("PaxProcessor ptr.Code:" + ptr.Code);

            if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {

                BatchResponse response = posLink.BatchResponse;
                if (response.ResultCode.compareTo(RESULT_CODE_SUCCESS) == 0) {
                    PaxProcessorResponse paxResp = new PaxProcessorResponse(response);
                    responseCode = paxResp.getStatusCode();
                    errorReason = SUCCESS;
                    return succeeded().add(RESULT_ERROR_REASON, errorReason);
                } else {
                    error = PaxGateway.Error.SERVICE;
                    errorReason = "Result Code: " + response.ResultCode + " (" + response.ResultTxt + ")";
                    Logger.e("PaxProcessor AddTipsCommand failed, pax error code: " + ptr.Code);
                    return failed().add(RESULT_ERROR_REASON, errorReason);
                }
            } else {
                error = PaxGateway.Error.SERVICE;
                errorReason = "Exception Code: " + ptr.Code + ", Message: " + ptr.Msg;
                Logger.e("PaxProcessor AddTipsCommand failed, pax error code: " + ptr.Code);
                return failed().add(RESULT_ERROR_REASON, errorReason);
            }
        } catch (Exception e) {
            Logger.e("PaxProcessorAddTipsCommand failed", e);
            errorReason = "Exception occured." + e.getMessage();
            error = PaxGateway.Error.CONNECTIVITY;

        }
        return succeeded().add(RESULT_ERROR_REASON, errorReason);
    }

    public static abstract class PaxProcessorBatchOutCommandCallback {

        @OnSuccess(PaxProcessorBatchOutCommand.class)
        public final void onSuccess(@Param(PaxProcessorBatchOutCommand.RESULT_ERROR_REASON) String response) {
            handleSuccess(response);
        }

        protected abstract void handleSuccess(String response);

        @OnFailure(PaxProcessorBatchOutCommand.class)
        public final void onFailure(@Param(PaxProcessorBatchOutCommand.RESULT_ERROR_REASON) String response) {
            handleError(response);
        }

        protected abstract void handleError(String response);
    }

}
