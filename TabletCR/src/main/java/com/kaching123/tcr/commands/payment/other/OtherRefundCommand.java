package com.kaching123.tcr.commands.payment.other;

import android.content.ContentProviderOperation;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.AddFakePaymentCommand;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand.AddReturnOrderResult;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;

public abstract class OtherRefundCommand extends AsyncCommand {

    public static final String RESULT_DATA = "RESULT_DATA";
    protected static final String ARG_DATA = "ARG_DATA";
    protected static final String ARG_AMOUNT = "ARG_AMOUNT";

    public static final String ARG_TRANSACTION = "ARG_TRANSACTION";
    public static final String ARG_TRANSACTION_MODEL = "ARG_TRANSACTION_MODEL";
    protected static final String ARG_IS_MANUAL_RETURN = "ARG_IS_MANUAL_RETURN";

    private PaymentTransactionModel transactionModel;
    private Transaction transaction;
    private PaymentTransactionModel childTransactionModel;
    private boolean isManualReturn;

    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sqlCommand;

    private SaleOrderModel returnOrder;

    private AddReturnOrderResult addReturnOrderResult;
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

        Transaction transaction = getChildTransaction((BigDecimal) getArgs().getSerializable(ARG_AMOUNT),
                returnOrder.guid,
                transactionModel.guid,
                transactionModel.cardName);

        childTransactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), transaction);

        operations.add(ContentProviderOperation.newInsert(ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT))
                .withValues(childTransactionModel.toValues())
                .build());
        sqlCommand.add(jdbcConverter.insertSQL(childTransactionModel, getAppCommandContext()));

        return succeeded().add(RESULT_DATA, childTransactionModel);
    }

    protected abstract Transaction getChildTransaction(BigDecimal amount, String orderGuid, String parentGuid, String cardname);

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sqlCommand;
    }


}