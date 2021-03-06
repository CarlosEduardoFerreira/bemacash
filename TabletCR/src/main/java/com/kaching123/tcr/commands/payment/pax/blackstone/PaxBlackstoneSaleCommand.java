package com.kaching123.tcr.commands.payment.pax.blackstone;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.WebCommand.ErrorReason;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.pax.PaxTransaction;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.SaleActionRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;

import retrofit.RetrofitError;

/**
 * Created by hamsterksu on 24.04.2014.
 */
public class PaxBlackstoneSaleCommand extends PaxBlackstoneBaseCommand {

    public static final int TRANSACTION_ID_CREDIT_SALE = 1;
    public static final int TRANSACTION_ID_DEBIT_SALE = 0;
    public static final int TRANSACTION_ID_EBT_FOODSTAMP_SALE = 5;
    public static final int TRANSACTION_ID_EBT_CASH_SALE = 7;
    public static final int TRANSACTION_ID_CREDIT_REFUND = 20;
    public static final int TRANSACTION_ID_DEBIT_REFUND = 21;
    public static final int TRANSACTION_ID_EBT_REFUND = 6;

    private static final String ARG_AMOUNT = "ARG_AMOUNT";
    private static final String RESULT_TRANSACTION = "RES_TRANSACTION";
    private static final String RESULT_ERROR_REASON = "ERROR";
    private static final String ARG_SALEACTIONRESPONSE = "SaleActionResponse";
    private static final String ARG_PURPOSE = "ARG_AMOUNT_1";

    private PaxTransaction transaction;
    private String errorReason;

    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sqlCommand;

    public static final TaskHandler startSale(Context context,
                                              PaxModel paxTerminal,
                                              Transaction transaction,
                                              int saleId,
                                              PaxSaleCommandBaseCallback callback) {
        return create(PaxBlackstoneSaleCommand.class)
                .arg(ARG_DATA_PAX, paxTerminal)
                .arg(ARG_AMOUNT, transaction)
                .arg(ARG_PURPOSE, saleId)
                .callback(callback)
                .queueUsing(context);
    }

    public static final TaskHandler startSaleFromData(Context context,
                                                      PaxModel paxTerminal,
                                                      Transaction transaction,
                                                      int saleId,
                                                      SaleActionResponse response,
                                                      PaxSaleCommandBaseCallback callback) {
        return create(PaxBlackstoneSaleCommand.class)
                .arg(ARG_DATA_PAX, paxTerminal)
                .arg(ARG_AMOUNT, transaction)
                .arg(ARG_SALEACTIONRESPONSE, response)
                .arg(ARG_PURPOSE, saleId)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected TaskResult doInBackground() {
        TaskResult result = failed();
        try {
            result = super.doInBackground();
        } catch (Exception e) {
            Logger.e("PaxSaleCommand: doInBackground(): failed", e);
        }

        if (isFailed(result)) {
            if (transaction != null)
                transaction.allowReload = true;
            if (TextUtils.isEmpty(errorReason))
                errorReason = ErrorReason.UNKNOWN.getDescription();
        }

        return succeeded()
                .add(RESULT_TRANSACTION, transaction)
                .add(RESULT_ERROR_REASON, errorReason);
    }

    @Override
    protected TaskResult doCommand(PaxWebApi api) {
        PaymentTransactionJdbcConverter jdbcConverter = (PaymentTransactionJdbcConverter) JdbcFactory.getConverter(PaymentTransactionTable.TABLE_NAME);
        operations = new ArrayList<ContentProviderOperation>();
        sqlCommand = batchInsert(PaymentTransactionModel.class);

        int transactionId = getIntArg(ARG_PURPOSE);
        transaction = getArgs().getParcelable(ARG_AMOUNT);
        BigDecimal amount = transaction.getAmount();

        BigDecimal cents = amount.multiply(CalculationUtil.ONE_HUNDRED).setScale(0);
        String sAmount = String.valueOf(cents.intValue());
        Logger.d("PaxSaleCommand %d - %s", transactionId, sAmount);
        try {
            Object possibleData = getArgs().getSerializable(ARG_SALEACTIONRESPONSE);
            SaleActionResponse response;
            if (possibleData == null) {
                SaleActionRequest request = new SaleActionRequest(transactionId, sAmount, null);
                Logger.d("PaxSaleCommand request:" + request);
                response = api.sale(request);
                Logger.d("response trace, amount:" + response.getDetails().getAmount() + ", cashback: " + response.getDetails().getCashBackAmount());
            } else {
                response = (SaleActionResponse) possibleData;
            }
            Logger.d("PaxSaleCommand response:" + response);
            if (200 == response.getResponse()) {
                transaction.updateWith(response);
                if (response.getDetails().getDigits() != null)
                    transaction.lastFour = response.getDetails().getDigits();
                if (response.getDetails().getCashBackAmount() != null)
                    transaction.cashBack = new BigDecimal(response.getDetails().getCashBackAmount()).negate();
                if (response.getDetails().getTransactionNumber() != null)
                    transaction.authorizationNumber = response.getDetails().getSale().getAuthNumber();
                PaymentTransactionModel transactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), transaction);
                operations.add(ContentProviderOperation.newInsert(ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT))
                        .withValues(transactionModel.toValues())
                        .build());
                sqlCommand.add(jdbcConverter.insertSQL(transactionModel, getAppCommandContext()));
            } else {
                errorReason = "";
                for (String msg : response.getDetails().getSale().getMessage()) {
                    if (errorReason.length() > 0) {
                        errorReason = errorReason.concat(". ");
                    }
                    errorReason = errorReason.concat(msg);
                }
                if (errorReason.length() > 0 && response.getDetails().getVerbiage() != null) {
                    errorReason = errorReason.concat(": ").concat(response.getDetails().getVerbiage());
                }
            }
        } catch (Pax404Exception e) {
            transaction.allowReload = true;
            errorReason = "Payment cancelled or connection problem.";
            Logger.e("Pax 404", e);
        } catch (RetrofitError e) {
            transaction.allowReload = true;
            errorReason = getContext().getString(R.string.pax_timeout);
            Logger.e("PaxError", e);
        } catch (Exception e) {
            transaction.allowReload = true;
            // Though it should not happen, as Gena confirms we only care about local DB and data will sync after,
            // I put this check due to possibilty on DB corruption, DB access failure and many other ugly rare stuff
            Logger.e("Rare SQL exception caught, data was not updated", e);
            errorReason = "Rare SQL exception caught, data was not updated";
        }
        return succeeded();
    }

    @Override
    protected boolean validateAppCommandContext() {
        return true;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sqlCommand;
    }

    public static abstract class PaxSaleCommandBaseCallback {

        @OnSuccess(PaxBlackstoneSaleCommand.class)
        public final void onSuccess(@Param(PaxBlackstoneSaleCommand.RESULT_TRANSACTION) Transaction result,
                                    @Param(PaxBlackstoneSaleCommand.RESULT_ERROR_REASON) String errorReason) {
            handleSuccess(result, errorReason);
        }

        protected abstract void handleSuccess(Transaction result, String errorReason);

        @OnFailure(PaxBlackstoneSaleCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }

}
