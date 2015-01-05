package com.kaching123.tcr.commands.payment.cash;

import android.content.ContentProviderOperation;
import android.content.Context;
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
import com.kaching123.tcr.model.payment.general.transaction.CashTransactionFactory;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class CashVoidCommand extends AsyncCommand {

    private static final Uri PAYMENT_URI = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);

    private static final String ARG_DATA = "ARG_data";
    private static final String ARG_NEED_TO_CANCEL = "ARG_NEED_TO_CANCEL";

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

        Logger.d(transactionModel.toDebugString());

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

        Transaction transaction = CashTransactionFactory.createChild(getAppCommandContext().getEmployeeGuid(),
                transactionModel.availableAmount,
                returnOrder.guid,
                transactionModel.guid,
                transactionModel.cardName,
                false);
        childTransactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), transaction);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        if (addReturnOrderResult.getLocalDbOperations() != null)
            operations.addAll(addReturnOrderResult.getLocalDbOperations());

        if (prepaidVoidResult != null && prepaidVoidResult.getLocalDbOperations() != null)
            operations.addAll(prepaidVoidResult.getLocalDbOperations());

        if (needToCancel) {
            childTransactionModel.status = PaymentStatus.CANCELED;
            transactionModel.status = PaymentStatus.CANCELED;
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

    public static TaskHandler start(Context context,
                                    Object callback,
                                    PaymentTransactionModel data,
                                    SaleOrderModel childOrderModel,
                                    boolean needToCancel) {
        return create(CashVoidCommand.class)
                .arg(AddReturnOrderCommand.ARG_ORDER_MODEL_CHILD, childOrderModel)
                .arg(ARG_DATA, data)
                .arg(ARG_NEED_TO_CANCEL, needToCancel)
                .callback(callback)
                .queueUsing(context);
    }
}