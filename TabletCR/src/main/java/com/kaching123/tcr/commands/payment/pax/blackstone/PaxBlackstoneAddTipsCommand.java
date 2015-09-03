package com.kaching123.tcr.commands.payment.pax.blackstone;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.ClosePreauthCommand;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.util.RequestsUtil;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.AddTipsRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.AddTipsResponse;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.Details;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.Sale;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.apache.http.HttpStatus;

import java.math.BigDecimal;

import retrofit.RetrofitError;

/**
 * Created by mayer
 */
public class PaxBlackstoneAddTipsCommand extends PaxBlackstoneBaseCommand {

    public static final String ARG_PAYMENT_TRANSACTION = "ARG_PAYMENT_TRANSACTION";
    public static final String ARG_TIPS_AMOUNT = "ARG_TIPS_AMOUNT";
    public static final String ARG_TIPS_COMMENTS = "ARG_TIPS_COMMENTS";
    public static final String ARG_TIPPED_EMPLOYEE = "ARG_TIPPED_EMPLOYEE";
    private static final String ARG_RELOAD_RESPONSE= "ARG_RELOAD_RESPONSE";

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
        return  create(PaxBlackstoneAddTipsCommand.class)
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
    protected TaskResult doCommand(PaxWebApi api) {
        PaymentTransactionModel transactionModel = (PaymentTransactionModel) getArgs().getSerializable(ARG_PAYMENT_TRANSACTION);
        BigDecimal tipsAmount = (BigDecimal) getArgs().getSerializable(ARG_TIPS_AMOUNT);
        String tipsComments = getStringArg(ARG_TIPS_COMMENTS);
        String tippedEmployeeId = getStringArg(ARG_TIPPED_EMPLOYEE);

        PaxGateway.Error error = PaxGateway.Error.UNDEFINED;
        TransactionStatusCode responseCode = null;
        try {
            Logger.d("PaxBlackstoneAddTipsCommand transaction details: " + transactionModel.toDebugString());
            Logger.d("PaxBlackstoneAddTipsCommand tips amount: " + tipsAmount);

            Transaction transaction = transactionModel.toTransaction();

            AddTipsResponse response = getResponse(api, getRequest(transaction, tipsAmount));

            Sale details = response == null ? null : response.getDetails();
            Integer paxResponseCode = response == null ? null : response.getResponse();
            responseCode = details == null ? null : TransactionStatusCode.valueOf(details.getResponseCode());

            boolean paxSuccess = paxResponseCode != null && paxResponseCode == HttpStatus.SC_OK;
            boolean apiSuccess = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY == responseCode;
            boolean success = paxSuccess && apiSuccess;

            transaction.updateFromClosePreauth(details);
            transaction.setAmount(transactionModel.amount.add(tipsAmount));

            transactionModel = new PaymentTransactionModel(transactionModel.guid, transactionModel.shiftGuid, transactionModel.createTime, transaction);

            boolean localSuccess = new ClosePreauthCommand().sync(getContext(), transactionModel, responseCode, tipsAmount, tipsComments, tippedEmployeeId, getAppCommandContext());

            if (!localSuccess) {
                Logger.e("PaxBlackstoneAddTipsCommand failed, failed to close preauth in the system!");
                if (success) {
                    return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
                }
            }

            if (success)
                return succeeded().add(RESULT_DATA, responseCode);

            if (paxResponseCode != null)
                error = PaxGateway.Error.PAX;
            if (responseCode != null)
                error = PaxGateway.Error.SERVICE;
            Logger.e("PaxBlackstoneAddTipsCommand failed, pax error code: " + paxResponseCode + "; error code: "+ (responseCode == null ? null : responseCode.getCode()));
            return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
        } catch (Pax404Exception e) {
            Logger.e("PaxBlackstoneAddTipsCommand failed", e);
            error = PaxGateway.Error.PAX404;
        } catch (RetrofitError e) {
            Logger.e("PaxAddTipsCommand failed", e);
            error = PaxGateway.Error.CONNECTIVITY;
        }

        return failed().add(RESULT_ERROR, error).add(RESULT_ERROR_CODE, responseCode);
    }
    private AddTipsRequest getRequest(Transaction transaction, BigDecimal tipsAmount) {
        String transactionNumber = transaction.serviceTransactionNumber;
        String amount = RequestsUtil.centsFormat(CalculationUtil.getInCents(tipsAmount));

        AddTipsRequest request = new AddTipsRequest(transactionNumber, amount);
        Logger.d("PaxBlackstoneAddTipsCommand.getResponse(): request: " + request);
        return request;
    }

    private AddTipsResponse getResponse(PaxWebApi api, AddTipsRequest request) {
        SaleActionResponse reloadResponse = (SaleActionResponse) getArgs().getSerializable(ARG_RELOAD_RESPONSE);
        if (reloadResponse != null) {
            Logger.d("PaxBlackstoneAddTipsCommand.getResponse(): reloadResponse: " + reloadResponse);
            int responseCode = reloadResponse.getResponse();
            Details details = reloadResponse.getDetails();
            Sale sale = details == null ? null : details.getSale();
            AddTipsResponse response = new AddTipsResponse(responseCode, sale);
            Logger.d("PaxAddTipsCommand.getResponse(): response: " + response);
            return response;
        }

        AddTipsResponse response = api.addTips(request);
        Logger.d("PaxAddTipsCommand.getResponse(): response: " + response);
        return response;
    }


    public static abstract class PaxTipsCommandBaseCallback {

        @OnSuccess(PaxBlackstoneAddTipsCommand.class)
        public final void onSuccess(@Param(PaxBlackstoneAddTipsCommand.RESULT_DATA) TransactionStatusCode responseCode) {
            handleSuccess(responseCode);
        }

        protected abstract void handleSuccess(TransactionStatusCode responseCode);

        @OnFailure(PaxBlackstoneAddTipsCommand.class)
        public final void onFailure(@Param(PaxBlackstoneAddTipsCommand.RESULT_ERROR) PaxGateway.Error error, @Param(PaxBlackstoneAddTipsCommand.RESULT_ERROR_CODE) TransactionStatusCode errorCode) {
            handleError(error, errorCode);
        }

        protected abstract void handleError(PaxGateway.Error error, TransactionStatusCode errorCode);
    }

}
