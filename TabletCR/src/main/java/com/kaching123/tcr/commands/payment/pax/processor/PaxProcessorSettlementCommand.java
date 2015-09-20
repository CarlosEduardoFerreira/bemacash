package com.kaching123.tcr.commands.payment.pax.processor;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.pax.poslink.BatchRequest;
import com.pax.poslink.BatchResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.UUID;

/**
 * Created by mayer
 */
public class PaxProcessorSettlementCommand extends PaxProcessorBaseCommand {

    private static final String RESULT_PREAUTH_TRANSACTIONS_CLOSED = "RESULT_PREAUTH_TRANSACTIONS_CLOSED";

    private static final String ARG_PURPOSE = "ARG_AMOUNT_1";

    public static final TaskHandler start(Context context,
                                          PaxModel paxTerminal,
                                          SettlementCommandBaseCallback callback) {
        return create(PaxProcessorSettlementCommand.class)
                .arg(ARG_DATA_PAX, paxTerminal)
                .callback(callback)
                .queueUsing(context);
    }

    private String generateUniqueNum() {
        return UUID.randomUUID().toString();
    }

    @Override
    protected PaxModel getPaxModel() {
        return (PaxModel) getArgs().getParcelable(ARG_DATA_PAX);
    }

    @Override
    protected TaskResult doCommand() {
        PaxGateway.Error error = PaxGateway.Error.UNDEFINED;
        TransactionStatusCode responseCode = null;

        try {
            Logger.d("PaxProcessorSettlementCommand transaction ");
            BatchRequest batchRequest = new BatchRequest();

            batchRequest.TransType = TRANSACTION_TYPE_BATCHCLOSE;
            batchRequest.Timestamp = getTimeStamp();
            batchRequest.EDCType = EDC_TYPE_ALL;


            PosLink posLink = createPosLink();
            posLink.BatchRequest = batchRequest;

            ProcessTransResult ptr = posLink.ProcessTrans();
            if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {

                BatchResponse batchResponse = posLink.BatchResponse;
                PaxProcessorResponse paxResp = new PaxProcessorResponse(batchResponse);
                responseCode = paxResp.getStatusCode();
                if (batchResponse.ResultCode.compareTo(RESULT_CODE_SUCCESS) == 0) {
                    return succeeded().add(RESULT_DATA, responseCode).add(RESULT_PREAUTH_TRANSACTIONS_CLOSED, true);
                } else {
                    Logger.e(batchResponse.Message + ":" + batchResponse.ResultTxt);
                    error = PaxGateway.Error.SERVICE;

                }
            } else {
                error = PaxGateway.Error.SERVICE;
                Logger.e("PaxProcessorSettlementCommand failed, pax error code: " + ptr.Code);
                return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
            }
        } catch (Exception e) {
            Logger.e("PaxProcessorSettlementCommand failed", e);
            error = PaxGateway.Error.CONNECTIVITY;
        }


        return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
    }

    public static abstract class SettlementCommandBaseCallback {

        @OnSuccess(PaxProcessorSettlementCommand.class)
        public final void onSuccess(@Param(PaxProcessorSettlementCommand.RESULT_DATA) TransactionStatusCode responseCode,
                                    @Param(PaxProcessorSettlementCommand.RESULT_PREAUTH_TRANSACTIONS_CLOSED) boolean transactionsClosed) {
            handleSuccess(responseCode, transactionsClosed);
        }

        protected abstract void handleSuccess(TransactionStatusCode responseCode, boolean transactionsClosed);

        @OnFailure(PaxProcessorSettlementCommand.class)
        public final void onFailure(@Param(PaxProcessorSettlementCommand.RESULT_ERROR) PaxGateway.Error error, @Param(PaxProcessorSettlementCommand.RESULT_ERROR_CODE) TransactionStatusCode errorCode) {
            handleError(error, errorCode);
        }

        protected abstract void handleError(PaxGateway.Error error, TransactionStatusCode errorCode);
    }

}
