package com.kaching123.tcr.commands.payment.pax;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.SettlementCommand;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.SettlementRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.Settlement;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SettlementResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.apache.http.HttpStatus;

import retrofit.RetrofitError;

/**
 * Created by mayer
 */
public class PaxSettlementCommand extends PaxBaseCommand {

    private static final String RESULT_PREAUTH_TRANSACTIONS_CLOSED = "RESULT_PREAUTH_TRANSACTIONS_CLOSED";

    private boolean isPreauthTransactionsClosed;


    public static final TaskHandler start(Context context,
                                              PaxModel paxTerminal,
                                              SettlementCommandBaseCallback callback) {
        return  create(PaxSettlementCommand.class)
                .arg(ARG_DATA_PAX, paxTerminal)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected TaskResult doCommand(PaxWebApi api) {
        Error error = Error.UNDEFINED;
        TransactionStatusCode responseCode = null;
        try {
            SettlementResponse response = api.settlement(new SettlementRequest());

            Settlement details = response == null ? null : response.getDetails();
            Integer paxResponseCode = response == null ? null : response.getResponse();
            responseCode = details == null ? null : TransactionStatusCode.valueOf(details.getResponseCode());

            boolean paxSuccess = paxResponseCode != null && paxResponseCode == HttpStatus.SC_OK;
            boolean apiSuccess = responseCode != null && responseCode == TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY;
            boolean success = paxSuccess && apiSuccess;

            if (!success) {
                if (paxResponseCode != null)
                    error = Error.PAX;
                if (responseCode != null)
                    error = Error.SERVICE;
                Logger.e("PaxSettlementCommand failed, pax error code: " + paxResponseCode + "; error code: " + (responseCode == null ? null : responseCode.getCode()));
                return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
            }

            isPreauthTransactionsClosed = new SettlementCommand().sync(getContext(), getAppCommandContext());

            return succeeded().add(RESULT_DATA, responseCode).add(RESULT_PREAUTH_TRANSACTIONS_CLOSED, isPreauthTransactionsClosed);
        } catch (Pax404Exception e) {
            Logger.e("PaxSettlementCommand failed", e);
            error = Error.PAX404;
        } catch (RetrofitError e) {
            Logger.e("PaxSettlementCommand failed", e);
            error = Error.CONNECTIVITY;
        }

        return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
    }

    public static abstract class SettlementCommandBaseCallback {

        @OnSuccess(PaxSettlementCommand.class)
        public final void onSuccess(@Param(PaxSettlementCommand.RESULT_DATA) TransactionStatusCode responseCode,
                                    @Param(PaxSettlementCommand.RESULT_PREAUTH_TRANSACTIONS_CLOSED) boolean transactionsClosed) {
            handleSuccess(responseCode, transactionsClosed);
        }

        protected abstract void handleSuccess(TransactionStatusCode responseCode, boolean transactionsClosed);

        @OnFailure(PaxSettlementCommand.class)
        public final void onFailure(@Param(PaxSettlementCommand.RESULT_ERROR) Error error, @Param(PaxSettlementCommand.RESULT_ERROR_CODE) TransactionStatusCode errorCode) {
            handleError(error, errorCode);
        }

        protected abstract void handleError(Error error, TransactionStatusCode errorCode);
    }

}
