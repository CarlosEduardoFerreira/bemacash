package com.kaching123.tcr.commands.payment.blackstone.payment;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.RESTWebCommand;
import com.kaching123.tcr.commands.payment.SettlementCommand;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.request.DoSettlementRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.response.DoSettlementResponse;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.DoSettlementResult;
import com.kaching123.tcr.websvc.service.BlackStoneWebService;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.IOException;

/**
 * Created by pkabakov on 23.05.2014.
 */
public class BlackDoSettlementCommand extends RESTWebCommand<DoSettlementResponse, DoSettlementResult> {

    public static final String ARG_PREAUTH_TRANSACTIONS_CLOSED = "ARG_PREAUTH_TRANSACTIONS_CLOSED";

    private boolean isPreauthTransactionsClosed;

    @Override
    protected DoSettlementResult getEmptyResult() {
        return new DoSettlementResult();
    }

    @Override
    protected TaskResult doInBackground() {
        return super.doInBackground().add(ARG_PREAUTH_TRANSACTIONS_CLOSED, isPreauthTransactionsClosed);
    }

    @Override
    protected TaskResult afterAction() {
        if (result.getData() != null) {
            if (TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY.equals(result.getData().getResponseCode())) {
                return succeeded().add(RESULT_DATA, result.getData().getResponseCode());
            } else {
                return failed().add(RESULT_DATA, result.getData().getResponseCode());
            }
        } else {
            return failed().add(RESULT_REASON, ErrorReason.DUE_TO_MALFUNCTION);
        }
    }

    @Override
    protected boolean doCommand(DoSettlementResult result) throws IOException {
        DoSettlementRequest request = getArgs().getParcelable(ARG_DATA);
        BlackStoneWebService.doSettlement(request, result);

        //mockSuccessResponse(result);

        boolean success = isResultSuccessful();
        if (!success) {
            Logger.e("BlackDoSettlementCommand.doCommand(): error result: " + (result.getData() == null ? null : result.getData().toDebugString()));
            return result.isValid();
        }

        isPreauthTransactionsClosed = new SettlementCommand().sync(getContext(), getAppCommandContext());

        return result.isValid();
    }

    private void mockSuccessResponse(DoSettlementResult result) {
        result.setResultCode(200);
        DoSettlementResponse response = new DoSettlementResponse(null, TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);
        result.setData(response);
    }

    /*package*/
    final static TaskHandler start(Context context, BaseDoSettlementCallback callback, DoSettlementRequest data) {
        return create(BlackDoSettlementCommand.class).arg(ARG_DATA, data).callback(callback).queueUsing(context);
    }

    public static abstract class BaseDoSettlementCallback {

        @OnSuccess(BlackDoSettlementCommand.class)
        public void onSuccess(@Param(RESTWebCommand.RESULT_DATA) TransactionStatusCode responseCode,
                              @Param(BlackDoSettlementCommand.ARG_PREAUTH_TRANSACTIONS_CLOSED) boolean transactionsClosed) {
            handleSuccess(responseCode, transactionsClosed);
        }

        @OnFailure(BlackDoSettlementCommand.class)
        public void onFailure(@Param(RESTWebCommand.RESULT_DATA) TransactionStatusCode responseCode,
                              @Param(RESTWebCommand.RESULT_REASON) ErrorReason errorReason) {
            handleFailure(responseCode, errorReason);
        }

        protected abstract void handleSuccess(TransactionStatusCode responseCode, boolean transactionsClosed);

        protected abstract void handleFailure(TransactionStatusCode responseCode, ErrorReason errorReason);

    }
}
