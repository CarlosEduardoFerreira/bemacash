package com.kaching123.tcr.commands.payment.pax.processor;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.ClosePreauthCommand;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;
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

/**
 * Created by mayer
 */
public class PaxProcessorAddTipsCommand extends PaxProcessorBaseCommand {

    public static final String ARG_PAYMENT_TRANSACTION = "ARG_PAYMENT_TRANSACTION";
    public static final String ARG_TIPS_AMOUNT = "ARG_TIPS_AMOUNT";
    public static final String ARG_TIPS_COMMENTS = "ARG_TIPS_COMMENTS";
    public static final String ARG_TIPPED_EMPLOYEE = "ARG_TIPPED_EMPLOYEE";
    private static final String ARG_RELOAD_RESPONSE = "ARG_RELOAD_RESPONSE";

    public static final TaskHandler start(Context context,
                                          PaxModel paxTerminal,
                                          PaymentTransactionModel transactionModel,
                                          BigDecimal tipsAmount,
                                          String tipsComments,
                                          String tippedEmployeeId,
                                          PaxTipsCommandBaseCallback callback) {
        return start(context, paxTerminal, transactionModel, tipsAmount, tipsComments, tippedEmployeeId, null, callback);
    }

    public static final TaskHandler start(Context context,
                                          PaxModel paxTerminal,
                                          PaymentTransactionModel transactionModel,
                                          BigDecimal tipsAmount,
                                          String tipsComments,
                                          String tippedEmployeeId,
                                          SaleActionResponse reloadResponse,
                                          PaxTipsCommandBaseCallback callback) {
        return create(PaxProcessorAddTipsCommand.class)
                .arg(ARG_DATA_PAX, paxTerminal)
                .arg(ARG_PAYMENT_TRANSACTION, transactionModel)
                .arg(ARG_TIPS_AMOUNT, tipsAmount)
                .arg(ARG_TIPS_COMMENTS, tipsComments)
                .arg(ARG_TIPPED_EMPLOYEE, tippedEmployeeId)
                .arg(ARG_RELOAD_RESPONSE, reloadResponse)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected PaxModel getPaxModel() {
        return (PaxModel) getArgs().getParcelable(ARG_DATA_PAX);
    }

    @Override
    protected TaskResult doCommand() {
        PaymentTransactionModel transactionModel = (PaymentTransactionModel) getArgs().getSerializable(ARG_PAYMENT_TRANSACTION);
        BigDecimal tipsAmount = (BigDecimal) getArgs().getSerializable(ARG_TIPS_AMOUNT);
        String tipsComments = getStringArg(ARG_TIPS_COMMENTS);
        String tippedEmployeeId = getStringArg(ARG_TIPPED_EMPLOYEE);
        String sAmount = String.valueOf((tipsAmount.multiply(CalculationUtil.ONE_HUNDRED)).intValue());


        PaxGateway.Error error = PaxGateway.Error.UNDEFINED;
        TransactionStatusCode responseCode = TransactionStatusCode.EMPTY_REQUEST;
        try {
            Logger.d("PaxProcessorAddTipsCommand transaction details: " + transactionModel.toDebugString());
            Logger.d("PaxProcessorAddTipsCommand tips amount: " + tipsAmount);
//
            Transaction transaction = transactionModel.toTransaction();

            PaymentRequest request = new PaymentRequest();
            request.Amount = sAmount;
            request.TransType = TRANS_TYPE_ADJUST;
            request.OrigRefNum = transaction.serviceTransactionNumber;
            request.Amount = sAmount;
            request.ECRRefNum = ECRREFNUM_DEFAULT;
            request.TenderType = EDC_TYPE_CREDIT;

            PosLink posLink = createPosLink();
            posLink.PaymentRequest = request;

            ProcessTransResult ptr = posLink.ProcessTrans();
            if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {

                PaymentResponse response = posLink.PaymentResponse;
                PaxProcessorResponse paxResp = new PaxProcessorResponse(response);
                responseCode = paxResp.getStatusCode();
                if (response.ResultCode.compareTo(RESULT_CODE_SUCCESS) == 0) {

                    transaction.updateFromClosePreauth(paxResp);
                    transaction.setAmount(transactionModel.amount.add(tipsAmount));

                    transactionModel = new PaymentTransactionModel(transactionModel.guid, transactionModel.shiftGuid, transactionModel.createTime, transaction);

                    boolean localSuccess = new ClosePreauthCommand().sync(getContext(), transactionModel, paxResp.getStatusCode(), tipsAmount, tipsComments, tippedEmployeeId, getAppCommandContext());

                    if (!localSuccess) {
                        Logger.e("PaxProcessorAddTipsCommand failed, failed to close preauth in the system!");
                        return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, "ERR");
                    }
                    return succeeded().add(RESULT_DATA, paxResp.getStatusCode());
                } else {
                    Logger.e(response.Message + ":" + response.ResultTxt);

                    if (response.ResultCode.compareTo(RESULT_CODE_ABORTED) == 0) {
                        transactionModel.allowReload = true;
                    }
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

        return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
    }


    public static abstract class PaxTipsCommandBaseCallback {

        @OnSuccess(PaxProcessorAddTipsCommand.class)
        public final void onSuccess(@Param(PaxProcessorAddTipsCommand.RESULT_DATA) TransactionStatusCode responseCode) {
            handleSuccess(responseCode);
        }

        protected abstract void handleSuccess(TransactionStatusCode responseCode);

        @OnFailure(PaxProcessorAddTipsCommand.class)
        public final void onFailure(@Param(PaxProcessorAddTipsCommand.RESULT_ERROR) PaxGateway.Error error, @Param(PaxProcessorAddTipsCommand.RESULT_ERROR_CODE) TransactionStatusCode errorCode) {
            handleError(error, errorCode);
        }

        protected abstract void handleError(PaxGateway.Error error, TransactionStatusCode errorCode);
    }

}
