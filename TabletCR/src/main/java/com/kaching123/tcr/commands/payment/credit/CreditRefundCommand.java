package com.kaching123.tcr.commands.payment.credit;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.payment.AddFakePaymentCommand;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand.AddReturnOrderResult;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.wireless.VoidOrderCommand;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.model.CreditReceiptModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.credit.CreditTransactionFactory;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by gdubina on 24/02/14.
 */
public class CreditRefundCommand extends AsyncCommand {

    private static final Uri URI_PAYMENT = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);

    protected static final String ARG_DATA = "ARG_DATA";
    protected static final String ARG_AMOUNT = "ARG_AMOUNT";
    protected static final String ARG_RECEIPT = "ARG_RECEIPT";
    protected static final String ARG_NEED_TO_CANCEL = "ARG_NEED_TO_CANCEL";
    protected static final String ARG_IS_MANUAL_RETURN = "ARG_IS_MANUAL_RETURN";

    public static final String EXTRA_CHILD_TRANSACTION = "EXTRA_CHILD_TRANSACTION";
    public static final String EXTRA_REFUNDED_AMOUNT = "EXTRA_REFUNDED_AMOUNT";


    private boolean needToCancel;

    private SaleOrderModel returnOrder;
    private CreditReceiptModel creditReceiptModel;
    protected PaymentTransactionModel childTransactionModel;
    protected PaymentTransactionModel transactionModel;
    private boolean isManualReturn;

    private SyncResult prepaidVoidResult;
    private AddReturnOrderResult addReturnOrderResult;
    private SyncResult addFakePaymentResult;

    @Override
    protected TaskResult doCommand() {
        needToCancel = getBooleanArg(ARG_NEED_TO_CANCEL);

        BigDecimal refundAmount = (BigDecimal) getArgs().getSerializable(ARG_AMOUNT);
        creditReceiptModel = (CreditReceiptModel) getArgs().getSerializable(ARG_RECEIPT);
        transactionModel = (PaymentTransactionModel) getArgs().getSerializable(ARG_DATA);
        isManualReturn = getBooleanArg(ARG_IS_MANUAL_RETURN);
        assert transactionModel != null;

        if (isManualReturn) {
            addFakePaymentResult = new AddFakePaymentCommand().sync(getContext(), transactionModel, getAppCommandContext());
            if (addFakePaymentResult == null)
                return failed();
        }

        if (!needToCancel) {
            prepaidVoidResult = new VoidOrderCommand().sync(getContext(), transactionModel.orderGuid, getAppCommandContext());

            if (prepaidVoidResult == null)
                return failed();
        }

        addReturnOrderResult = new AddReturnOrderCommand().sync(getContext(), getArgs(), transactionModel.orderGuid, getAppCommandContext());
        returnOrder = addReturnOrderResult.getOrderModel();
        if (!addReturnOrderResult.isSuccessful())
            return failed();

        childTransactionModel = createReturnTransaction(transactionModel, refundAmount);

        return succeeded()
                .add(EXTRA_REFUNDED_AMOUNT, childTransactionModel.amount)
                .add(EXTRA_CHILD_TRANSACTION, childTransactionModel)
                .add(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL, returnOrder);
    }

    private PaymentTransactionModel createReturnTransaction(PaymentTransactionModel transactionModel, BigDecimal amount) {
        Transaction transaction = CreditTransactionFactory.createChild(getAppCommandContext().getEmployeeGuid(),
                amount,
                returnOrder.guid,
                transactionModel.guid,
                creditReceiptModel.guid,
                transactionModel.isPreauth);

        PaymentTransactionModel childTransactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), transaction);
        if(needToCancel){
            childTransactionModel.status = PaymentStatus.CANCELED;
            transactionModel.status = PaymentStatus.CANCELED;
        }

        return childTransactionModel;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        if (addFakePaymentResult != null && addFakePaymentResult.getLocalDbOperations() != null)
            operations.addAll(addFakePaymentResult.getLocalDbOperations());

        if (addReturnOrderResult.getLocalDbOperations() != null)
            operations.addAll(addReturnOrderResult.getLocalDbOperations());

        if (prepaidVoidResult != null && prepaidVoidResult.getLocalDbOperations() != null)
            operations.addAll(prepaidVoidResult.getLocalDbOperations());

        if (needToCancel) {
            operations.add(ContentProviderOperation.newUpdate(URI_PAYMENT)
                    .withValues(transactionModel.getUpdatePaymentStatus())
                    .withSelection(PaymentTransactionTable.GUID + " = ?", new String[]{transactionModel.guid})
                    .build());
        }

        operations.add(ContentProviderOperation.newInsert(URI_PAYMENT)
                .withValues(childTransactionModel.toValues())
                .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchInsert(PaymentTransactionModel.class);

        if (addFakePaymentResult != null)
            batch.add(addFakePaymentResult.getSqlCmd());

        batch.add(addReturnOrderResult.getSqlCmd());

        if (prepaidVoidResult != null)
            batch.add(prepaidVoidResult.getSqlCmd());

        PaymentTransactionJdbcConverter jdbcConverter = (PaymentTransactionJdbcConverter) JdbcFactory.getConverter(PaymentTransactionTable.TABLE_NAME);
        if (needToCancel) {
            batch.add(jdbcConverter.updateStatus(transactionModel, getAppCommandContext()));
        }
        batch.add(jdbcConverter.insertSQL(childTransactionModel, getAppCommandContext()));

        return batch;
    }

    public static TaskHandler start(Context context,
                                    CreditRefundCommandBaseCallback callback,
                                    PaymentTransactionModel transaction,
                                    BigDecimal amount,
                                    CreditReceiptModel receiptModel,
                                    SaleOrderModel returnOrder,
                                    boolean needToCancel,
                                    boolean isManualReturn) {
        return create(CreditRefundCommand.class)
                .arg(ARG_DATA, transaction)
                .arg(ARG_AMOUNT, amount)
                .arg(ARG_RECEIPT, receiptModel)
                .arg(ARG_NEED_TO_CANCEL, needToCancel)
                .arg(ARG_IS_MANUAL_RETURN, isManualReturn)
                .arg(AddReturnOrderCommand.ARG_ORDER_MODEL_CHILD, returnOrder)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class CreditRefundCommandBaseCallback {

        @OnSuccess(CreditRefundCommand.class)
        public final void onSuccess(@Param(EXTRA_CHILD_TRANSACTION) PaymentTransactionModel childTransaction,
                                    @Param(EXTRA_REFUNDED_AMOUNT) BigDecimal refundedAmount,
                                    @Param(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL) SaleOrderModel returnOrder) {
            handleOnSuccess(childTransaction, refundedAmount, returnOrder);
        }

        @OnFailure(CreditRefundCommand.class)
        public final void onFailure() {
            handleOnFailure();
        }

        protected abstract void handleOnSuccess(PaymentTransactionModel childTransaction, BigDecimal refundedAmount, SaleOrderModel returnOrder);

        protected abstract void handleOnFailure();
    }
}
