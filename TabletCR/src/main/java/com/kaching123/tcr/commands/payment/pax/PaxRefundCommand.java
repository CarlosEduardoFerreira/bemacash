package com.kaching123.tcr.commands.payment.pax;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.payment.RefundCommand;
import com.kaching123.tcr.commands.payment.RefundCommand.RefundCommandResult;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.pax.PaxTransaction;
import com.kaching123.tcr.model.payment.blackstone.pax.PaxTransactionFactory;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.SaleActionRequest;
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
 * Created by hamsterksu on 24.04.2014.
 */
public class PaxRefundCommand extends PaxBaseCommand {

    private static final String ARG_AMOUNT = "ARG_AMOUNT";
    private static final String ARG_AMOUNT_OLD = "ARG_AMOUNT_OLD";
    private static final String ARG_PURPOSE = "ARG_PURPOSE";
    private static final String ARG_TRANSACTION = "ARG_TRANSACTION";
    private static final String ARG_TRANSACTION_MODEL = "ARG_TRANSACTION_MODEL";
    private static final String RESULT_ERROR_REASON = "RESULT_ERROR_REASON";
    private static final String ARG_SALE_ACTION_RESPONSE= "ARG_SALE_ACTION_RESPONSE";
    private static final String ARG_REFUND_TIPS = "ARG_REFUND_TIPS";
    public static final String ARG_IS_MANUAL_RETURN = "ARG_IS_MANUAL_RETURN";

    private SaleOrderModel returnOrder;
    private PaymentTransactionModel childTransactionModel;
    private boolean isManualReturn;

    public static final TaskHandler startReturn(Context context,
                                                PaxModel paxTerminal,
                                                PaymentTransactionModel transaction,
                                                BigDecimal amount,
                                                SaleOrderModel childOrderModel,
                                                int saleId,
                                                boolean refundTips,
                                                boolean isManualReturn,
                                                PaxREFUNDCommandBaseCallback callback) {
        return  create(PaxRefundCommand.class)
                .arg(ARG_DATA_PAX, paxTerminal)
                .arg(ARG_AMOUNT, transaction)
                .arg(ARG_AMOUNT_OLD, amount)
                .arg(ARG_PURPOSE, saleId)
                .arg(AddReturnOrderCommand.ARG_ORDER_MODEL_CHILD, childOrderModel)
                .arg(ARG_REFUND_TIPS, refundTips)
                .arg(ARG_IS_MANUAL_RETURN, isManualReturn)
                .callback(callback)
                .queueUsing(context);
    }

    public static final TaskHandler startReturn(Context context,
                                                PaxModel paxTerminal,
                                                PaymentTransactionModel transaction,
                                                BigDecimal amount,
                                                SaleOrderModel childOrderModel,
                                                int saleId,
                                                SaleActionResponse resp,
                                                boolean refundTips,
                                                boolean isManualReturn,
                                                PaxREFUNDCommandBaseCallback callback) {
        return  create(PaxRefundCommand.class)
                .arg(ARG_DATA_PAX, paxTerminal)
                .arg(ARG_AMOUNT, transaction)
                .arg(ARG_AMOUNT_OLD, amount)
                .arg(ARG_PURPOSE, saleId)
                .arg(ARG_SALE_ACTION_RESPONSE, resp)
                .arg(AddReturnOrderCommand.ARG_ORDER_MODEL_CHILD, childOrderModel)
                .arg(ARG_REFUND_TIPS, refundTips)
                .arg(ARG_IS_MANUAL_RETURN, isManualReturn)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected TaskResult doCommand(PaxWebApi api) {
        int transactionId = getIntArg(ARG_PURPOSE);
        PaymentTransactionModel transactionModel = (PaymentTransactionModel) getArgs().getSerializable(ARG_AMOUNT);
        BigDecimal amount = (BigDecimal) getArgs().getSerializable(ARG_AMOUNT_OLD);
        boolean refundTips = getBooleanArg(ARG_REFUND_TIPS);
        isManualReturn = getBooleanArg(ARG_IS_MANUAL_RETURN);

        BigDecimal cents = amount.multiply(CalculationUtil.ONE_HUNDRED).setScale(0);
        String sAmount = String.valueOf(cents.intValue());
        Logger.d("PaxRefundCommand %d - %s", transactionId, sAmount);

        String errorReason = "";
        try {
            Object possibleData = getArgs().getSerializable(ARG_SALE_ACTION_RESPONSE);
            SaleActionResponse response;
            if (possibleData == null) {
                SaleActionRequest request = new SaleActionRequest(transactionId, sAmount, transactionModel.guid);
                Logger.d("PaxRefundCommand request: " + request);
                response = api.refund(request);
            } else {
                response = (SaleActionResponse) possibleData;
            }
            Logger.d("PaxRefundCommand response: " + response);

            Details details = response == null ? null : response.getDetails();
            Sale sale = details == null ? null : details.getSale();
            TransactionStatusCode responseCode = sale == null ? null : TransactionStatusCode.valueOf(sale.getResponseCode());

            boolean paxSuccess = response != null && response.getResponse() == HttpStatus.SC_OK;
            boolean apiSuccess = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY == responseCode;
            boolean success = paxSuccess && apiSuccess;

            if (success) {
                PaxTransaction childTransaction = PaxTransactionFactory.createChild(getAppCommandContext().getEmployeeGuid(),
                        new BigDecimal(details.getAmount()),
                        null,
                        details.getTransactionNumber(),
                        sale.getType(), transactionModel.gateway, transactionModel.isPreauth);
                childTransactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), childTransaction);
                RefundCommandResult refundCommandResult = new RefundCommand().sync(getContext(), getArgs(), transactionModel, childTransactionModel, refundTips, isManualReturn, getAppCommandContext());
                returnOrder = refundCommandResult.returnOrder;
                if (!refundCommandResult.success) {
                    Logger.e("PaxRefundCommand.doCommand(): error, failed to insert data in the database!");
                    childTransactionModel = null;
                    errorReason = "Rare SQL exception caught, data was not updated";
                    transactionModel.allowReload = true;
                }
            }

            if (!success) {
                if (sale != null) {
                    if (sale.getMessage() != null)
                        for (String msg : sale.getMessage()) {
                            if (errorReason.length() > 0) {
                                errorReason = errorReason.concat(". ");
                            }
                            errorReason = errorReason.concat(msg);
                        }
                    if (sale.getVerbiage() != null) {
                        errorReason = errorReason.concat(" ").concat(sale.getVerbiage());
                    }
                } else {
                    errorReason = "The payment action failed. Please try again.";
                    transactionModel.allowReload = true;
                }
            }
        } catch (Pax404Exception e) {
            Logger.e("Pax 404", e);
            errorReason = "Payment cancelled or connection problem.";
            transactionModel.allowReload = true;
        } catch (RetrofitError e) {
            Logger.e("PaxError", e);
            errorReason = getContext().getString(R.string.pax_timeout);
            transactionModel.allowReload = true;
        } catch (Exception e) {
            // Though it should not happen, as Gena confirms we only care about local DB and data will sync after,
            // I put this check due to possibilty on DB corruption, DB access failure and many other ugly rare stuff
            Logger.e("Rare SQL exception caught, data was not updated", e);
            errorReason = "Rare SQL exception caught, data was not updated";
            transactionModel.allowReload = true;
        }
        return succeeded()
                .add(ARG_TRANSACTION_MODEL, childTransactionModel)
                .add(ARG_TRANSACTION, transactionModel == null ? null : transactionModel.toTransaction())
                .add(RESULT_ERROR_REASON, errorReason)
                .add(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL, returnOrder);
    }

    public static abstract class PaxREFUNDCommandBaseCallback {

        @OnSuccess(PaxRefundCommand.class)
        public final void onSuccess(@Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel,
                                    @Param(PaxRefundCommand.ARG_TRANSACTION_MODEL) PaymentTransactionModel childTransactionModel,
                                    @Param(PaxRefundCommand.RESULT_ERROR_REASON) String errorMessage,
                                    @Param(PaxRefundCommand.ARG_TRANSACTION) Transaction transaction) {
            handleSuccess(childOrderModel, childTransactionModel, transaction, errorMessage);
        }

        protected abstract void handleSuccess(SaleOrderModel childOrderModel,
                                              PaymentTransactionModel childTransactionModel,
                                              Transaction transaction,
                                              String errorMessage);

        @OnFailure(PaxRefundCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }

}
