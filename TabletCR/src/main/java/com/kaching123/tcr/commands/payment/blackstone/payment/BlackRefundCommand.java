package com.kaching123.tcr.commands.payment.blackstone.payment;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.payment.RESTWebCommand;
import com.kaching123.tcr.commands.payment.RefundCommand;
import com.kaching123.tcr.commands.payment.RefundCommand.RefundCommandResult;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.request.RefundRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.response.RefundResponse;
import com.kaching123.tcr.model.payment.general.transaction.BlackStoneTransactionFactory;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.RefundResult;
import com.kaching123.tcr.websvc.service.BlackStoneWebService;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         REFUND REQUEST
 *         URL Template
 *         /api/Transactions/DoRefund
 *         Process a refund. The request will fail if the transaction was previously refunded or voided.
 */
public class BlackRefundCommand extends RESTWebCommand<RefundResponse, RefundResult> {

    public static final String ARG_TRANSACTION = "Transaction";
    public static final String ARG_TRANSACTION_MODEL = "Transaction model";
    public static final String ARG_AMOUNT = "ARG_AMOUNT";
    public static final String ARG_REFUND_TIPS = "ARG_REFUND_TIPS";
    public static final String ARG_IS_MANUAL_RETURN = "ARG_IS_MANUAL_RETURN";

    public static final String RESULT_DATA = "RESULT_DATA";

    private Transaction transaction;
    private PaymentTransactionModel childTransactionModel;
    private SaleOrderModel returnOrder;
    private boolean isManualReturn;

    @Override
    protected final TaskResult doInBackground() {

        return super.doInBackground()
                .add(ARG_TRANSACTION, transaction)
                .add(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL, returnOrder)
                .add(ARG_TRANSACTION_MODEL, childTransactionModel);
    }

    @Override
    protected boolean doCommand(RefundResult result) throws IOException {

        RefundRequest data = getArgs().getParcelable(ARG_DATA);
        PaymentTransactionModel transactionModel = data.getTransactionModel();
        boolean refundTips = getBooleanArg(ARG_REFUND_TIPS);
        isManualReturn = getBooleanArg(ARG_IS_MANUAL_RETURN);

        this.transaction = transactionModel.toTransaction().setAmount(data.getAmount());
        Transaction childTransaction = BlackStoneTransactionFactory.createRefundChild(getAppCommandContext().getEmployeeGuid(),
                (BigDecimal) getArgs().getSerializable(ARG_AMOUNT),
                null,
                this.transaction.getGuid(),
                transaction.getCardName(),
                transaction.isPreauth);
        childTransaction.serviceTransactionNumber = this.transaction.serviceTransactionNumber;

        data.setTransaction(childTransaction);
        BlackStoneWebService.refund(data, result);

        //mockSuccessResponse(result);

        childTransaction.amount = CalculationUtil.negative(childTransaction.amount);

        boolean success = isResultSuccessful();
        if (!success) {
            Logger.e("BlackRefundCommand.doCommand(): error result: " + (result.getData() == null ? null : result.getData().toDebugString()));
        }
        Logger.d(transactionModel.toDebugString());

        if (success) {
            childTransactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), childTransaction);

            RefundCommandResult refundCommandResult = new RefundCommand().sync(getContext(), getArgs(), transactionModel, childTransactionModel, refundTips, isManualReturn, getAppCommandContext());
            returnOrder = refundCommandResult.returnOrder;
            if (!refundCommandResult.success) {
                Logger.e("BlackRefundCommand.doCommand(): error, failed to insert data in the database!");
                return false;
            }
        }

        return result.isValid();
    }

    private void mockSuccessResponse(RefundResult result) {
        result.setResultCode(200);
        RefundResponse response = new RefundResponse(null, TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);
        result.setData(response);
    }

    /*package*/
    final static TaskHandler start(Context context,
                                   Object callback,
                                   RefundRequest data,
                                   BigDecimal amountToRefund,
                                   SaleOrderModel childOrderModel,
                                   boolean refundTips,
                                   boolean isManualReturn) {
        return create(BlackRefundCommand.class)
                .arg(ARG_DATA, data)
                .arg(AddReturnOrderCommand.ARG_ORDER_MODEL_CHILD, childOrderModel)
                .arg(ARG_AMOUNT, amountToRefund)
                .arg(ARG_REFUND_TIPS, refundTips)
                .arg(ARG_IS_MANUAL_RETURN, isManualReturn)
                .callback(callback)
                .queueUsing(context);
    }


    @Override
    protected RefundResult getEmptyResult() {
        return new RefundResult();
    }
}
