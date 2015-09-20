package com.kaching123.tcr.commands.payment.pax.processor;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.payment.RefundCommand;
import com.kaching123.tcr.commands.payment.RefundCommand.RefundCommandResult;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.pax.PaxTransaction;
import com.kaching123.tcr.model.payment.blackstone.pax.PaxTransactionFactory;
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
 * Created by hamsterksu on 24.04.2014.
 */
public class PaxProcessorRefundCommand extends PaxProcessorBaseCommand {

    private static final String ARG_AMOUNT = "ARG_AMOUNT";
    private static final String ARG_AMOUNT_OLD = "ARG_AMOUNT_OLD";
    private static final String ARG_PURPOSE = "ARG_PURPOSE";
    private static final String ARG_TRANSACTION = "ARG_TRANSACTION";
    private static final String ARG_TRANSACTION_MODEL = "ARG_TRANSACTION_MODEL";
    private static final String RESULT_ERROR_REASON = "RESULT_ERROR_REASON";
    private static final String ARG_SALE_ACTION_RESPONSE = "ARG_SALE_ACTION_RESPONSE";
    private static final String ARG_REFUND_TIPS = "ARG_REFUND_TIPS";
    public static final String ARG_IS_MANUAL_RETURN = "ARG_IS_MANUAL_RETURN";

    private SaleOrderModel returnOrder;
    private PaymentTransactionModel childTransactionModel;
    private boolean isManualReturn;
    private PaymentTransactionModel transaction;

    public static final TaskHandler startReturn(Context context,
                                                PaxModel paxTerminal,
                                                PaymentTransactionModel transaction,
                                                BigDecimal amount,
                                                SaleOrderModel childOrderModel,
                                                int saleId,
                                                boolean refundTips,
                                                boolean isManualReturn,
                                                PaxREFUNDCommandBaseCallback callback) {
        return create(PaxProcessorRefundCommand.class)
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
        return create(PaxProcessorRefundCommand.class)
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
    protected PaxModel getPaxModel() {
        return (PaxModel) getArgs().getParcelable(ARG_DATA_PAX);
    }

    @Override
    protected TaskResult doCommand() {
        int transactionId = getIntArg(ARG_PURPOSE);
        PaymentTransactionModel transactionModel = (PaymentTransactionModel) getArgs().getSerializable(ARG_AMOUNT);
        BigDecimal amount = (BigDecimal) getArgs().getSerializable(ARG_AMOUNT_OLD);
        boolean refundTips = getBooleanArg(ARG_REFUND_TIPS);
        isManualReturn = getBooleanArg(ARG_IS_MANUAL_RETURN);
        transaction = (PaymentTransactionModel) getArgs().getSerializable(ARG_AMOUNT);

        BigDecimal cents = amount.multiply(CalculationUtil.ONE_HUNDRED).setScale(0);
        String sAmount = String.valueOf(cents.intValue());
        Logger.d("PaxProcessorRefundCommand %d - %s", transactionId, sAmount);

        String errorReason = "";
        try {
            PaymentRequest request = new PaymentRequest();

            request.Amount = sAmount;
            request.TransType = TRANS_TYPE_RETURN;
            request.ECRRefNum = ECRREFNUM_DEFAULT;

            request.TenderType = 0;
            switch (transactionModel.gateway) {
                case PAX:
                    request.TenderType = TRANSACTION_ID_CREDIT_SALE;
                    break;
                case PAX_EBT_CASH:
                    request.TenderType = TRANSACTION_ID_EBT_CASH_SALE;
                    break;
                case PAX_EBT_FOODSTAMP:
                    request.TenderType = TRANSACTION_ID_EBT_FOODSTAMP_SALE;
                    break;
                case PAX_DEBIT:
                    request.TenderType = TRANSACTION_ID_DEBIT_SALE;
                    break;
                default:
                    break;

            }

            PosLink posLink = createPosLink();

            posLink.PaymentRequest = request;
            ProcessTransResult ptr = posLink.ProcessTrans();

            Logger.d("PaxProcessorRefundCommand response:" + ptr.Msg);

            if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {

                PaymentResponse response = posLink.PaymentResponse;
                if (response.ResultCode.compareTo(RESULT_CODE_SUCCESS) == 0) {
                    PaxTransaction childTransaction = PaxTransactionFactory.createChild(getAppCommandContext().getEmployeeGuid(),
                            new BigDecimal(response.ApprovedAmount).divide(CalculationUtil.ONE_HUNDRED),
                            null,
                            transaction.getGuid(),
                            response.CardType, transactionModel.gateway, transactionModel.isPreauth);
                    childTransactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), childTransaction);
                    RefundCommandResult refundCommandResult = new RefundCommand().sync(getContext(), getArgs(), transactionModel, childTransactionModel, refundTips, isManualReturn, getAppCommandContext());
                    returnOrder = refundCommandResult.returnOrder;
                    if (!refundCommandResult.success) {
                        Logger.e("PaxProcessorRefundCommand.doCommand(): error, failed to insert data in the database!");
                        childTransactionModel = null;
                        errorReason = "Rare SQL exception caught, data was not updated";
                        transactionModel.allowReload = true;
                    }

                } else {
                    Logger.e(response.Message + ":" + response.ResultTxt);
                    errorReason = response.ResultTxt;
                    if (response.ResultCode.compareTo(RESULT_CODE_ABORTED) == 0) {
                        transactionModel.allowReload = true;
                    }
                }
            } else {

                errorReason = "The payment action failed. Please try again.";
                transactionModel.allowReload = true;
            }

        } catch (PaxProcessorException e) {
            Logger.e("Pax Processor exc", e);
            errorReason = "Refund cancelled or connection problems.";
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

        @OnSuccess(PaxProcessorRefundCommand.class)
        public final void onSuccess(@Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel childOrderModel,
                                    @Param(PaxProcessorRefundCommand.ARG_TRANSACTION_MODEL) PaymentTransactionModel childTransactionModel,
                                    @Param(PaxProcessorRefundCommand.RESULT_ERROR_REASON) String errorMessage,
                                    @Param(PaxProcessorRefundCommand.ARG_TRANSACTION) Transaction transaction) {
            handleSuccess(childOrderModel, childTransactionModel, transaction, errorMessage);
        }

        protected abstract void handleSuccess(SaleOrderModel childOrderModel,
                                              PaymentTransactionModel childTransactionModel,
                                              Transaction transaction,
                                              String errorMessage);

        @OnFailure(PaxProcessorRefundCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }

}
