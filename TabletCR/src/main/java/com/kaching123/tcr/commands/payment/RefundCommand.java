package com.kaching123.tcr.commands.payment;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.payment.AddReturnOrderCommand.AddReturnOrderResult;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.history.AddRefundedTipsCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;
import static com.kaching123.tcr.model.ContentValuesUtil._paymentGateway;

/**
 * Created by pkabakov on 24.06.2014.
 */
public class RefundCommand extends AsyncCommand {

    private static final Uri PAYMENT_TRANSACTIONS_URI = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);

    private PaymentTransactionModel transactionModel;
    private PaymentTransactionModel childTransactionModel;
    private boolean refundTips;
    private boolean isManualReturn;

    private SaleOrderModel returnOrder;
    private boolean closeTransaction;
    private AddReturnOrderResult addReturnOrderResult;
    private SyncResult addRefundedTipsResult;
    private SyncResult addFakePaymentResult;

    private ArrayList<ContentProviderOperation> operations;

    public RefundCommandResult sync(Context context, Bundle args, PaymentTransactionModel transactionModel, PaymentTransactionModel childTransactionModel, boolean refundTips, boolean isManualReturn, IAppCommandContext appCommandContext) {
        this.transactionModel = transactionModel;
        this.childTransactionModel = childTransactionModel;
        this.refundTips = refundTips;
        this.isManualReturn = isManualReturn;
        this.addRefundedTipsResult = null;
        boolean success = !isFailed(super.syncStandalone(context, args, appCommandContext));
        SaleOrderModel returnOrderTemp = null;
        if (success || (addReturnOrderResult != null && addReturnOrderResult.isOrderWasCreatedPreviously())) {
            returnOrderTemp = this.returnOrder;
        }
        return new RefundCommandResult(returnOrderTemp, success);
    }

    @Override
    protected TaskResult doCommand() {

        operations = new ArrayList<ContentProviderOperation>();

        if (isManualReturn) {
            addFakePaymentResult = new AddFakePaymentCommand().sync(getContext(), transactionModel, getAppCommandContext());
            if (addFakePaymentResult == null)
                return failed();

            if (addFakePaymentResult.getLocalDbOperations() != null)
                operations.addAll(addFakePaymentResult.getLocalDbOperations());
        }

        addReturnOrderResult = new AddReturnOrderCommand().sync(getContext(), getArgs(), transactionModel.orderGuid, getAppCommandContext());
        returnOrder = addReturnOrderResult.getOrderModel();

        if (!addReturnOrderResult.isSuccessful())
            return failed();

        if (addReturnOrderResult.getLocalDbOperations() != null)
            operations.addAll(addReturnOrderResult.getLocalDbOperations());

        childTransactionModel.orderGuid = returnOrder.guid;
        operations.add(ContentProviderOperation.newInsert(PAYMENT_TRANSACTIONS_URI)
                .withValues(childTransactionModel.toValues())
                .build());

        /*if preauth transaction was completely refunded with CC, change it's status to SUCCESS*/
        if (closeTransaction = transactionModel.isPreauth && isTransactionFullyRefunded(transactionModel, childTransactionModel)) {
            transactionModel.status = PaymentStatus.SUCCESS;
            operations.add(ContentProviderOperation.newUpdate(PAYMENT_TRANSACTIONS_URI)
                    .withSelection(PaymentTransactionTable.GUID + " = ?", new String[]{transactionModel.guid})
                    .withValue(PaymentTransactionTable.STATUS, _enum(transactionModel.status))
                    .build());
        }

        if (refundTips) {
            addRefundedTipsResult = new AddRefundedTipsCommand().sync(getContext(), returnOrder, childTransactionModel, getAppCommandContext());
            if (addRefundedTipsResult == null)
                return failed();

            if (addRefundedTipsResult.getLocalDbOperations() != null)
                operations.addAll(addRefundedTipsResult.getLocalDbOperations());
        }

        return succeeded();
    }

    private boolean isTransactionFullyRefunded(PaymentTransactionModel transactionModel, PaymentTransactionModel childTransaction) {
        return getCredidCardRefundsAmount(transactionModel).add(childTransaction.amount).add(transactionModel.amount).compareTo(BigDecimal.ZERO) <= 0;
    }

    private BigDecimal getCredidCardRefundsAmount(PaymentTransactionModel transactionModel) {
        Cursor c = ProviderAction.query(PAYMENT_TRANSACTIONS_URI)
                .projection(PaymentTransactionTable.GATEWAY, PaymentTransactionTable.AMOUNT)
                .where(PaymentTransactionTable.PARENT_GUID + " = ?", transactionModel.guid)
                .perform(getContext());

        BigDecimal result = BigDecimal.ZERO;
        while (c.moveToNext()) {
            if (_paymentGateway(c, 0).isCreditCard()) {
                result = result.add(_decimal(c, 1));
            }
        }
        c.close();
        return result;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand sqlCommand = batchInsert(PaymentTransactionModel.class);

        if (addFakePaymentResult != null)
            sqlCommand.add(addFakePaymentResult.getSqlCmd());

        sqlCommand.add(addReturnOrderResult.getSqlCmd());

        PaymentTransactionJdbcConverter jdbcConverter = (PaymentTransactionJdbcConverter) JdbcFactory.getConverter(PaymentTransactionTable.TABLE_NAME);
        sqlCommand.add(jdbcConverter.insertSQL(childTransactionModel, getAppCommandContext()));

        if (closeTransaction) {
            sqlCommand.add(jdbcConverter.updateStatus(transactionModel, getAppCommandContext()));
        }

        if (refundTips)
            sqlCommand.add(addRefundedTipsResult.getSqlCmd());

        return sqlCommand;
    }

    public static class RefundCommandResult {
        public final SaleOrderModel returnOrder;
        public final boolean success;

        public RefundCommandResult(SaleOrderModel returnOrder, boolean success) {
            this.returnOrder = returnOrder;
            this.success = success;
        }
    }
}
