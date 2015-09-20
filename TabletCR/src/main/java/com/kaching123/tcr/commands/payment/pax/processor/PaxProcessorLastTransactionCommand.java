package com.kaching123.tcr.commands.payment.pax.processor;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.Details;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;

/**
 * Created by mayer
 */
public class PaxProcessorLastTransactionCommand extends PaxProcessorBaseCommand {

    private static final String ARG_TRANSACTION = "ARG_TRANSACTION";
    private static final String ARG_AMOUNT = "ARG_AMOUNT";

    public static final TaskHandler startSale(Context context,
                                              PaxModel paxTerminal,
                                              Transaction transaction,
                                              BigDecimal amount,
                                              PaxSaleCommandBaseCallback callback) {
        return  create(PaxProcessorLastTransactionCommand.class)
                .arg(ARG_DATA_PAX, paxTerminal)
                .arg(ARG_TRANSACTION, transaction)
                .arg(ARG_AMOUNT, amount)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected PaxModel getPaxModel() {
        return (PaxModel) getArgs().getParcelable(ARG_DATA_PAX);
    }

    @Override
    protected TaskResult doCommand() {
//        Transaction transaction = getArgs().getParcelable(ARG_TRANSACTION);
//        BigDecimal amount = (BigDecimal) getArgs().getSerializable(ARG_AMOUNT);
//        Logger.d("PaxLastTransactionCommand: required transaction: " + transaction);
//        if (amount != null)
//            Logger.d("PaxLastTransactionCommand: required amount: " + amount);
//
        PaxGateway.Error error = PaxGateway.Error.UNDEFINED;
//        try {
//            LastTrasnactionResponse response = api.last(new LastTransactionRequest());
//            Logger.d("PaxLastTransactionCommand: response: " + response);
//
//            Integer paxResponseCode = response == null ? null : response.getResponse();
//            SaleActionResponse saleActionResponse = response == null ? null : response.getDetails();
//            Details details = saleActionResponse == null ? null : saleActionResponse.getDetails();
//
//            boolean paxSuccess = paxResponseCode != null && paxResponseCode == HttpStatus.SC_OK;
//            boolean isRequiredTransaction = isRequiredTransaction(transaction, amount, details);
//            Logger.d("PaxLastTransactionCommand: isRequiredTransaction: " + isRequiredTransaction);
//            boolean success = paxSuccess && isRequiredTransaction;
//
//            if (success) {
//                return succeeded().add(RESULT_DATA, saleActionResponse);
//            }
//
//            if (paxResponseCode != null)
//                error = Error.PAX;
//            if (paxSuccess && !isRequiredTransaction)
//                error = Error.SERVICE;
//            Logger.e("PaxLastTransactionCommand failed, pax error code: " + paxResponseCode + ", error: " + error);
//            return failed().add(RESULT_ERROR, error);
//        } catch (Pax404Exception e) {
//            Logger.e("PaxLastTransactionCommand failed", e);
//            error = Error.PAX404;
//        } catch (RetrofitError e) {
//            Logger.e("PaxLastTransactionCommand failed", e);
//            error = Error.CONNECTIVITY;
//        }
//        Logger.e("PaxLastTransactionCommand failed, error: " + error);
        return failed().add(RESULT_ERROR, error);
    }

    private boolean isRequiredTransaction(Transaction transaction, BigDecimal amount, Details details) {
        if (details == null) {
            Logger.d("PaxLastTransactionCommand.isRequiredTransaction(): no details!");
            return false;
        }

        boolean validTransactionNumber = true;
        if (!TextUtils.isEmpty(transaction.serviceTransactionNumber)) {
            String lastTransactionNumber = details.getTransactionNumber();
            Logger.d("PaxLastTransactionCommand.isRequiredTransaction(): transaction number: " + transaction.serviceTransactionNumber + ",last transaction number: " + lastTransactionNumber);
            validTransactionNumber = lastTransactionNumber != null && lastTransactionNumber.equals(transaction.serviceTransactionNumber);
        }

        BigDecimal transactionAmount = amount == null ? transaction.getAmount() : amount;
        BigDecimal lastTransactionAmount = parseDecimal(details.getAmount());
        Logger.d("PaxLastTransactionCommand.isRequiredTransaction(): transaction amount: " + transactionAmount + ",last transaction amount: " + lastTransactionAmount);
        boolean validTransactionAmount = lastTransactionAmount != null && lastTransactionAmount.compareTo(transactionAmount) == 0;
        return validTransactionNumber && validTransactionAmount;
    }

    private BigDecimal parseDecimal(String value) {
        if (TextUtils.isEmpty(value))
            return null;
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static abstract class PaxSaleCommandBaseCallback {

        @OnSuccess(PaxProcessorLastTransactionCommand.class)
        public final void onSuccess(@Param(RESULT_DATA) SaleActionResponse response) {
            handleSuccess(response);
        }

        protected abstract void handleSuccess(SaleActionResponse response);

        @OnFailure(PaxProcessorLastTransactionCommand.class)
        public final void onFailure(@Param(RESULT_ERROR) Error error) {
            handleError(error);
        }

        protected abstract void handleError(Error error);
    }

}
