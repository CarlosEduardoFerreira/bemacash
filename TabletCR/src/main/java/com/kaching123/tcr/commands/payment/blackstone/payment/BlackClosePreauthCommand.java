package com.kaching123.tcr.commands.payment.blackstone.payment;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.ClosePreauthCommand;
import com.kaching123.tcr.commands.payment.RESTWebCommand;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.request.ClosePreauthRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.response.PreauthResponse;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.PreauthResult;
import com.kaching123.tcr.websvc.service.BlackStoneWebService;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Created by pkabakov on 17.05.2014.
 */
public class BlackClosePreauthCommand extends RESTWebCommand<PreauthResponse, PreauthResult> {

    public static final String ARG_COMMENTS = "ARG_COMMENTS";
    public static final String ARG_TIPPED_EMPLOYEE = "ARG_TIPPED_EMPLOYEE";

    private Transaction transaction;
    private PaymentTransactionModel transactionModel;

    @Override
    protected PreauthResult getEmptyResult() {
        return new PreauthResult();
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
    protected boolean doCommand(PreauthResult result) throws IOException {
        ClosePreauthRequest data = getArgs().getParcelable(ARG_DATA);
        transactionModel = data.getTransactionModel();
        BigDecimal tipsAmount = data.getAdditionalTipsAmount();
        String tipsComments = getStringArg(ARG_COMMENTS);
        String tippedEmployeeId = getStringArg(ARG_TIPPED_EMPLOYEE);

        Logger.d("BlackClosePreauthCommand.doCommand 62: " + transactionModel.toDebugString());

        transaction = transactionModel.toTransaction();
        transaction.userTransactionNumber = UUID.randomUUID().toString();
        transaction.setAmount(transactionModel.availableAmount);

        data.setTransaction(transaction);
        BlackStoneWebService.closePreauth(data, result);

        //mockSuccessResponse(result);

        TransactionStatusCode responseCode = result == null || result.getData() == null ? null : result.getData().getResponseCode();
        boolean success = isResultSuccessful();
        if (!success) {
            Logger.e("BlackClosePreauthCommand.doCommand(): error result: " + (result.getData() == null ? null : result.getData().toDebugString()));
        }

        transaction.updateFromClosePreauth(result.getData());
        transaction.setAmount(transactionModel.amount.add(tipsAmount));

        transactionModel = new PaymentTransactionModel(transactionModel.guid, transactionModel.shiftGuid, transactionModel.createTime, transaction);
        Logger.d("BlackClosePreauthCommand.doCommand 83: " + transactionModel.toDebugString());

        transaction.userTransactionNumber = transactionModel.guid;

        boolean subSuccess = new ClosePreauthCommand().sync(getContext(), transactionModel, responseCode, tipsAmount, tipsComments, tippedEmployeeId, getAppCommandContext());
        if (!subSuccess) {
            Logger.e("BlackClosePreauthCommand failed, failed to close preauth in the system!");
            return false;
        }

        return result.isValid();
    }

    private void mockSuccessResponse(PreauthResult result) {
        result.setResultCode(200);
        PreauthResponse response = new PreauthResponse();
        response.setResponseCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);
        response.setServiceReferenceNumber("TEST1");
        response.setAuthorizationNumber("TEST1");
        response.setCardType("TEST");
        result.setData(response);
    }

    /*package*/ final static TaskHandler start(Context context, BaseClosePreauthCallback callback, ClosePreauthRequest data, String comments, String tippedEmployeeId) {
        return create(BlackClosePreauthCommand.class)
                .arg(ARG_DATA, data)
                .arg(ARG_COMMENTS, comments)
                .arg(ARG_TIPPED_EMPLOYEE, tippedEmployeeId)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseClosePreauthCallback {

        @OnSuccess(BlackClosePreauthCommand.class)
        public void onSuccess(@Param(RESTWebCommand.RESULT_DATA) TransactionStatusCode responseCode) {
            handleSuccess(responseCode);
        }

        @OnFailure(BlackClosePreauthCommand.class)
        public void onFailure(@Param(RESTWebCommand.RESULT_DATA) TransactionStatusCode responseCode,
                              @Param(RESTWebCommand.RESULT_REASON) ErrorReason errorReason) {
            handleFailure(responseCode, errorReason);
        }

        protected abstract void handleSuccess(TransactionStatusCode responseCode);

        protected abstract void handleFailure(TransactionStatusCode responseCode, ErrorReason errorReason);

    }
}
