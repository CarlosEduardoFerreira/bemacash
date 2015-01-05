package com.kaching123.tcr.commands.payment.blackstone.payment;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand.AddReturnOrderResult;
import com.kaching123.tcr.commands.payment.RESTWebCommand;
import com.kaching123.tcr.commands.payment.blackstone.prepaid.wireless.VoidOrderCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.request.DoFullRefundRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.response.DoFullRefundResponse;
import com.kaching123.tcr.model.payment.general.transaction.BlackStoneTransactionFactory;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.util.CalculationUtil;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.DoFullRefundResult;
import com.kaching123.tcr.websvc.service.BlackStoneWebService;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 *         VOID TRANSACTION REQUEST
 *         URL Template
 *         /api/Transactions/DoVoid
 *         Void a transaction. The request will fail if the transaction was previously voided or refunded.
 */
public class BlackVoidCommand extends RESTWebCommand<DoFullRefundResponse, DoFullRefundResult> {

    private static final Uri PAYMENT_URI = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);

    private static final String ARG_NEED_TO_CANCEL = "ARG_NEED_TO_CANCEL";
    public static final String ARG_TRANSACTION = "ARG_TRANSACTION";
    public static final String ARG_CHILD_TRANSACTION_MODEL = "Transaction model child";

    private PaymentTransactionModel transactionModel;
    private PaymentTransactionModel childTransactionModel;
    private SaleOrderModel returnOrder;
    private boolean needToCancel;

    private SyncResult prepaidVoidResult;
    private AddReturnOrderResult addReturnOrderResult;
    private Transaction transaction;

    /*package*/
    static TaskHandler start(Context context, Object callback, DoFullRefundRequest data, SaleOrderModel childOrderModel, boolean needToCancel) {
        return create(BlackVoidCommand.class)
                .arg(ARG_DATA, data)
                .arg(ARG_NEED_TO_CANCEL, needToCancel)
                .arg(AddReturnOrderCommand.ARG_ORDER_MODEL_CHILD, childOrderModel)
                .callback(callback).queueUsing(context);
    }

    @Override
    protected final TaskResult doInBackground() {
        TaskResult result = super.doInBackground();
        boolean success = !isFailed(result);

        SaleOrderModel returnOrder = null;
        if (success || (addReturnOrderResult != null && addReturnOrderResult.isOrderWasCreatedPreviously())) {
            returnOrder = this.returnOrder;
        }
        return result.add(ARG_TRANSACTION, transactionModel)
                .add(ARG_CHILD_TRANSACTION_MODEL, childTransactionModel)
                .add(AddReturnOrderCommand.RESULT_CHILD_ORDER_MODEL, returnOrder);
    }

    @Override
    protected DoFullRefundResult getEmptyResult() {
        return new DoFullRefundResult();
    }

    @Override
    protected boolean doCommand(DoFullRefundResult result) throws IOException {
        needToCancel = getBooleanArg(ARG_NEED_TO_CANCEL);
        DoFullRefundRequest data = getArgs().getParcelable(ARG_DATA);
        transactionModel = data.getTransactionModel();

        if (!needToCancel) {
            prepaidVoidResult = new VoidOrderCommand().sync(getContext(), transactionModel.orderGuid, getAppCommandContext());

            if (prepaidVoidResult == null)
                return false;
        }

        /***************************************** child ***********************************************/
        addReturnOrderResult = new AddReturnOrderCommand().sync(getContext(), getArgs(), transactionModel.orderGuid, getAppCommandContext());
		returnOrder = addReturnOrderResult.getOrderModel();
        if (!addReturnOrderResult.isSuccessful())
            return false;
        /***********************************************************************************************/

        transaction = BlackStoneTransactionFactory.createRefundChild(getAppCommandContext().getEmployeeGuid(),
                transactionModel.availableAmount,
                returnOrder.guid,
                transactionModel.guid,
                transactionModel.cardName,
                transactionModel.isPreauth);
        transaction.serviceTransactionNumber = (TextUtils.isEmpty(transactionModel.paymentId) ? transactionModel.preauthPaymentId : transactionModel.paymentId);
        data.setTransaction(transaction);
        BlackStoneWebService.doFullRefund(data, result);

        //mockSuccessResponse(result);

        boolean success = isResultSuccessful();
        if (!success) {
            Logger.e("BlackVoidCommand.doCommand(): error result: " + (result.getData() == null ? null : result.getData().toDebugString()));
        }

        transaction.amount = CalculationUtil.negative(transaction.amount);

        return result.isValid();
    }

    private void mockSuccessResponse(DoFullRefundResult result) {
        result.setResultCode(200);
        DoFullRefundResponse response = new DoFullRefundResponse(null, TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);
        result.setData(response);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        childTransactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), transaction);

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
    protected boolean validateAppCommandContext() {
        return true;
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
