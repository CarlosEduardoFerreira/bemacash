package com.kaching123.tcr.commands.payment.other;

import android.content.ContentProviderOperation;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand.AddReturnOrderResult;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.wireless.VoidOrderCommand;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;

public abstract class OtherVoidCommand extends AsyncCommand {

    private static final Uri PAYMENT_URI = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);

    protected static final String ARG_DATA = "ARG_data";
    protected static final String ARG_NEED_TO_CANCEL = "ARG_NEED_TO_CANCEL";

    public static final String ARG_TRANSACTION = "ARG_TRANSACTION";
    public static final String ARG_CHILD_TRANSACTION_MODEL = "Transaction model child";

    private PaymentTransactionModel transactionModel;
    private PaymentTransactionModel childTransactionModel;

    private SaleOrderModel returnOrder;
    private boolean needToCancel;

    private SyncResult prepaidVoidResult;
    private AddReturnOrderResult addReturnOrderResult;

    @Override
    protected final TaskResult doInBackground() {
        TaskResult result = super.doInBackground();
        boolean success = !isFailed(result);

        SaleOrderModel returnOrder = null;
        if (success || (addReturnOrderResult != null && addReturnOrderResult.isOrderWasCreatedPreviously())) {
            returnOrder = this.returnOrder;
        }
        return result
                .add(ARG_TRANSACTION, transactionModel)
                .add(ARG_CHILD_TRANSACTION_MODEL, childTransactionModel)
                .add(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL, returnOrder);
    }

    @Override
    protected TaskResult doCommand() {
        transactionModel = (PaymentTransactionModel) getArgs().getSerializable(ARG_DATA);
        needToCancel = getBooleanArg(ARG_NEED_TO_CANCEL);

        Logger.d("OtherVoidCommand.doCommand 65: " + transactionModel.toDebugString());

        if (!needToCancel) {
            prepaidVoidResult = new VoidOrderCommand().sync(getContext(), transactionModel.orderGuid, getAppCommandContext());

            if (prepaidVoidResult == null)
                return failed();
        }

        /***************************************** child ***********************************************/
        addReturnOrderResult = new AddReturnOrderCommand().sync(getContext(), getArgs(), transactionModel.orderGuid, getAppCommandContext());
        returnOrder = addReturnOrderResult.getOrderModel();
        if (!addReturnOrderResult.isSuccessful())
            return failed();
        /***********************************************************************************************/

        Transaction transaction = getChildTransaction(transactionModel.availableAmount,
                returnOrder.guid,
                transactionModel.guid,
                transactionModel.cardName);
        childTransactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), transaction);

        if (needToCancel) {
            childTransactionModel.status = PaymentStatus.CANCELED;
            transactionModel.status = PaymentStatus.CANCELED;
        }

        return succeeded();
    }

    protected abstract Transaction getChildTransaction(BigDecimal amount, String orderGuid, String parentGuid, String cardname);

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        if (addReturnOrderResult.getLocalDbOperations() != null)
            operations.addAll(addReturnOrderResult.getLocalDbOperations());

        if (prepaidVoidResult != null && prepaidVoidResult.getLocalDbOperations() != null)
            operations.addAll(prepaidVoidResult.getLocalDbOperations());

        if (needToCancel) {
            operations.add(ContentProviderOperation.newUpdate(PAYMENT_URI)
                    .withValues(transactionModel.getUpdatePaymentStatus())
                    .withSelection(PaymentTransactionTable.GUID + " = ?", new String[]{transactionModel.guid})
                    .build());
        }
        operations.add(ContentProviderOperation.newInsert(PAYMENT_URI)
                .withValues(childTransactionModel.toValues())
                .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchInsert(PaymentTransactionModel.class);

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

}