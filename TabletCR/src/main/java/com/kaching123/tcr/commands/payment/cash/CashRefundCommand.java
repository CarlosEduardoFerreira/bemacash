package com.kaching123.tcr.commands.payment.cash;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.AddFakePaymentCommand;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand.AddReturnOrderResult;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.history.AddRefundedTipsCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.general.transaction.CashTransactionFactory;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class CashRefundCommand extends AsyncCommand {

    public static final String RESULT_DATA = "RESULT_DATA";

    private static final String ARG_DATA = "ARG_data";
    private static final String ARG_AMOUNT = "ARG_AMOUNT";
    private static final String ARG_REFUND_TIPS = "ARG_REFUND_TIPS";
    private static final String ARG_TRANSACTION = "Transaction";
    private static final String ARG_TRANSACTION_MODEL = "Transaction model";
    private static final String ARG_IS_MANUAL_RETURN = "ARG_IS_MANUAL_RETURN";

    private PaymentTransactionModel transactionModel;
    private Transaction transaction;
    private PaymentTransactionModel childTransactionModel;
    private boolean isManualReturn;

    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sqlCommand;

    private SaleOrderModel returnOrder;
    private AddReturnOrderResult addReturnOrderResult;
    private SyncResult addRefundedTipsResult;
    private SyncResult addFakePaymentResult;

    @Override
    protected final TaskResult doInBackground() {
        TaskResult result = super.doInBackground();
        boolean success = !isFailed(result);

        SaleOrderModel returnOrder = null;
        if (success || (addReturnOrderResult != null && addReturnOrderResult.isOrderWasCreatedPreviously())) {
            returnOrder = this.returnOrder;
        }
        return result
                .add(ARG_TRANSACTION, transaction)
                .add(ARG_TRANSACTION_MODEL, childTransactionModel)
                .add(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL, returnOrder);
    }

    @Override
    protected TaskResult doCommand() {
        transactionModel = (PaymentTransactionModel) getArgs().getSerializable(ARG_DATA);
        isManualReturn = getBooleanArg(ARG_IS_MANUAL_RETURN);

        this.transaction = transactionModel.toTransaction();

        PaymentTransactionJdbcConverter jdbcConverter = (PaymentTransactionJdbcConverter) JdbcFactory.getConverter(PaymentTransactionTable.TABLE_NAME);
        operations = new ArrayList<ContentProviderOperation>();
        sqlCommand = batchInsert(PaymentTransactionModel.class);

        Logger.d(transactionModel.toDebugString());

        if (isManualReturn) {
            addFakePaymentResult = new AddFakePaymentCommand().sync(getContext(), transactionModel, getAppCommandContext());
            if (addFakePaymentResult == null)
                return failed();

            if (addFakePaymentResult.getLocalDbOperations() != null)
                operations.addAll(addFakePaymentResult.getLocalDbOperations());
            sqlCommand.add(addFakePaymentResult.getSqlCmd());
        }

        /***************************************** child ***********************************************/
        addReturnOrderResult = new AddReturnOrderCommand().sync(getContext(), getArgs(), transactionModel.orderGuid, getAppCommandContext());
        returnOrder = addReturnOrderResult.getOrderModel();
        if (!addReturnOrderResult.isSuccessful())
            return failed();

        if (addReturnOrderResult.getLocalDbOperations() != null)
            operations.addAll(addReturnOrderResult.getLocalDbOperations());
        sqlCommand.add(addReturnOrderResult.getSqlCmd());
        /***********************************************************************************************/

        Transaction transaction = CashTransactionFactory.createChild(getAppCommandContext().getEmployeeGuid(),
                (BigDecimal) getArgs().getSerializable(ARG_AMOUNT),
                returnOrder.guid,
                transactionModel.guid,
                transactionModel.cardName,
                transactionModel.isPreauth);


        childTransactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), transaction);

        operations.add(ContentProviderOperation.newInsert(ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT))
                .withValues(childTransactionModel.toValues())
                .build());
        sqlCommand.add(jdbcConverter.insertSQL(childTransactionModel, getAppCommandContext()));

        if (getBooleanArg(ARG_REFUND_TIPS)){
            addRefundedTipsResult = new AddRefundedTipsCommand().sync(getContext(), returnOrder, childTransactionModel, getAppCommandContext());
            if (addRefundedTipsResult == null)
                return failed();

            if (addRefundedTipsResult.getLocalDbOperations() != null)
                operations.addAll(addRefundedTipsResult.getLocalDbOperations());
            sqlCommand.add(addRefundedTipsResult.getSqlCmd());
        }

        return succeeded().add(RESULT_DATA, childTransactionModel);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sqlCommand;
    }

    public final static TaskHandler start(Context context,
                                          Object callback,
                                          PaymentTransactionModel data,
                                          BigDecimal refundAmount,
                                          SaleOrderModel childOrderModel,
                                          boolean refundTips,
                                          boolean isManualReturn) {
        return create(CashRefundCommand.class)
                    .arg(AddReturnOrderCommand.ARG_ORDER_MODEL_CHILD, childOrderModel)
                    .arg(ARG_DATA, data)
                    .arg(ARG_AMOUNT, refundAmount)
                    .arg(ARG_REFUND_TIPS, refundTips)
                    .arg(ARG_IS_MANUAL_RETURN, isManualReturn)
                    .callback(callback)
                    .queueUsing(context);
    }
}