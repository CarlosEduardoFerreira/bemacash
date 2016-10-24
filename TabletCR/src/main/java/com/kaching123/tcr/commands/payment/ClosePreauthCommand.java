package com.kaching123.tcr.commands.payment;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.user.AddTipsCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.TipsModel;
import com.kaching123.tcr.model.TipsModel.PaymentType;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by pkabakov on 23.06.2014.
 */
public class ClosePreauthCommand extends AsyncCommand {

    private static final Uri URI_PAYMENT = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);

    private PaymentTransactionModel transactionModel;

    private TransactionStatusCode responseCode;
    private BigDecimal tipsAmount;
    private String tipsComments;
    private String tippedEmployeeId;

    private boolean isResponseSuccessful;
    private boolean isPreauthCompletedBefore;
    private SyncResult addTipsResult;
    private String registerId;

    public boolean sync(Context context, PaymentTransactionModel transactionModel, TransactionStatusCode responseCode, BigDecimal tipsAmount, String tipsComments, String tippedEmployeeId, IAppCommandContext appCommandContext) {
        this.transactionModel = transactionModel;
        this.responseCode = responseCode;
        this.tipsAmount = tipsAmount;
        this.tipsComments = tipsComments;
        this.tippedEmployeeId = tippedEmployeeId;
        this.isPreauthCompletedBefore = false;
        this.addTipsResult = null;
        return !isFailed(syncStandalone(context, null, appCommandContext));
    }

    @Override
    protected TaskResult doCommand() {

        isResponseSuccessful = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY == responseCode;

        registerId = String.valueOf(((TcrApplication) getContext().getApplicationContext()).getRegisterId());

        if (isResponseSuccessful) {
            if (!addTips(tipsAmount, tipsComments, tippedEmployeeId))
                return failed();

            return succeeded();
        }

        if (isPreauthCompletedBefore = isPreauthCompletedBefore(responseCode)){
            closePreauthTransaction(transactionModel);
            return succeeded();
        }

        return succeeded();
    }

    private boolean isPreauthCompletedBefore (TransactionStatusCode responseCode){
        return TransactionStatusCode.REFERENCED_PREAUTHORIZATION_COMPLETED == responseCode;
    }

    private void closePreauthTransaction(PaymentTransactionModel transactionModel) {
        transactionModel.status = PaymentStatus.SUCCESS;
    }

    private boolean addTips(BigDecimal tipsAmount, String tipsComments, String tippedEmployeeId) {
        //TODO: add tips amount field to transaction?
        if (tipsAmount == null || tipsAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }

        addTipsResult = new AddTipsCommand().sync(getContext(), new TipsModel(
                UUID.randomUUID().toString(),
                null,
                tippedEmployeeId,
                getAppCommandContext().getShiftGuid(),
                transactionModel.orderGuid,
                transactionModel.guid,
                new Date(),
                tipsAmount,
                tipsComments,
                PaymentType.CREDIT,
                registerId
        ), getAppCommandContext());
        return addTipsResult != null;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        if (isResponseSuccessful) {
            operations.add(ContentProviderOperation.newUpdate(URI_PAYMENT)
                    .withValues(transactionModel.toUpdateValues())
                    .withSelection(PaymentTransactionTable.GUID + " = ?", new String[]{transactionModel.guid})
                    .build());

            if (addTipsResult != null && addTipsResult.getLocalDbOperations() != null)
                operations.addAll(addTipsResult.getLocalDbOperations());
        } else if (isPreauthCompletedBefore) {
            operations.add(ContentProviderOperation.newUpdate(URI_PAYMENT)
                    .withValues(transactionModel.getUpdatePaymentStatus())
                    .withSelection(PaymentTransactionTable.GUID + " = ?", new String[]{transactionModel.guid})
                    .build());
        }

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand sqlCommand = batchUpdate(transactionModel);

        if (isResponseSuccessful) {
            sqlCommand.add(JdbcFactory.getConverter(transactionModel).updateSQL(transactionModel, getAppCommandContext()));

            if (addTipsResult != null)
                sqlCommand.add(addTipsResult.getSqlCmd());
        } else if (isPreauthCompletedBefore) {
            sqlCommand.add(((PaymentTransactionJdbcConverter) JdbcFactory.getConverter(transactionModel)).updateStatus(transactionModel, getAppCommandContext()));
        }

        return sqlCommand;
    }
}
