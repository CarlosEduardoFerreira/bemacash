package com.kaching123.tcr.commands.payment.pax.processor;

import android.content.Context;
import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.util.CalculationUtil;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.jsoup.select.Evaluator;

import java.math.BigDecimal;

/**
 * Created by mayer
 */
public class PaxProcessorBalanceCommand extends PaxProcessorBaseCommand {

    public static final int TRANSACTION_ID_BALANCE = 9;

    private static final String RESULT_AMOUNT = "RES_TRANSACTION";
    private static final String RESULT_LAST4 = "RESULT_LAST4";
    private static final String RESULT_ERROR_REASON = "ERROR";
    private static final String ARG_PURPOSE = "ARG_AMOUNT_1";


    public static final TaskHandler start(Context context,
                                          PaxModel paxTerminal,
                                          PaxBalanceCommandBaseCallback callback) {
        return create(PaxProcessorBalanceCommand.class)
                .arg(ARG_DATA_PAX, paxTerminal)
                .arg(ARG_PURPOSE, TRANSACTION_ID_BALANCE)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected PaxModel getPaxModel() {
        return (PaxModel) getArgs().getParcelable(ARG_DATA_PAX);
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

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TransType = TRANS_TYPE_INQUIRY;
            paymentRequest.TenderType = EDC_TYPE_EBT;
            paymentRequest.ECRRefNum = ECRREFNUM_DEFAULT;

            PosLink posLink = createPosLink();
            posLink.PaymentRequest = paymentRequest;


            ProcessTransResult ptr = posLink.ProcessTrans();
            Logger.e("PaxProcessor ptr.Code:" + ptr.Code);

            if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {

                PaymentResponse response = posLink.PaymentResponse;
                if (response.ResultCode.compareTo(RESULT_CODE_SUCCESS) == 0) {
                    PaxProcessorResponse paxResp = new PaxProcessorResponse(response);
                    responseCode = paxResp.getStatusCode();
                    Logger.d("PaxProcessorBalanceCommand getBalance success, balance: " + response.RemainingBalance);

                    amount = new BigDecimal(response.RemainingBalance).divide(CalculationUtil.ONE_HUNDRED);
                    lastFour = response.BogusAccountNum;
                } else {
                    error = PaxGateway.Error.SERVICE;
                    Logger.e("PaxProcessor AddTipsCommand failed, pax error code: " + ptr.Code);
                    return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
                }
            } else {
                error = PaxGateway.Error.SERVICE;
                Logger.e("PaxProcessor AddTipsCommand failed, pax error code: " + ptr.Code);
                return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
            }
        } catch (Exception e) {
            Logger.e("PaxProcessorAddTipsCommand failed", e);

            error = PaxGateway.Error.CONNECTIVITY;
        }
        return succeeded().add(RESULT_LAST4, lastFour).add(RESULT_AMOUNT, amount).add(RESULT_ERROR_REASON, errorReason);
    }

    public static abstract class PaxBalanceCommandBaseCallback {

        @OnSuccess(PaxProcessorBalanceCommand.class)
        public final void onSuccess(@Param(PaxProcessorBalanceCommand.RESULT_AMOUNT) BigDecimal result,
                                    @Param(PaxProcessorBalanceCommand.RESULT_LAST4) String last4,
                                    @Param(PaxProcessorBalanceCommand.RESULT_ERROR_REASON) String errorReason) {
            handleSuccess(result, last4, errorReason);
        }

        protected abstract void handleSuccess(BigDecimal result, String last4, String errorReason);

        @OnFailure(PaxProcessorBalanceCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }

}
