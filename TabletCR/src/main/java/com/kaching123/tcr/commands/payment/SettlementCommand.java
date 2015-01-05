package com.kaching123.tcr.commands.payment;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.rest.RestCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by pkabakov on 21.06.2014.
 */
public class SettlementCommand extends AsyncCommand {

    private static final Uri URI_PAYMENT = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);

    private PaymentTransactionModel transactionModel;
    private boolean isPreauthTransactionsClosed;

    public boolean sync(Context context, IAppCommandContext appCommandContext) {
        transactionModel = null;
        syncStandalone(context, appCommandContext);
        return isPreauthTransactionsClosed;
    }

    @Override
    protected TaskResult doCommand() {
        //TODO: set paymentId = preauthPaymentId?
        isPreauthTransactionsClosed = closePreauthTransactions(getApi());
        if (!isPreauthTransactionsClosed)
            return failed();

        transactionModel = new PaymentTransactionModel();
        transactionModel.status = PaymentStatus.SUCCESS;

        return succeeded();
    }

    private boolean closePreauthTransactions(SyncApi api) {
        if (getApp().isTrainingMode()) {
            return true;
        }

        try {
            RestCommand.Response response = api.closePreauthTransactions(getApp().emailApiKey, SyncUploadRequestBuilder.getReqCredentials(getApp().getOperator(), getApp()));
            Logger.d("SettlementCommand.closePreauthTransactions() response: " + response);
            boolean success = response != null && response.isSuccess();
            if (!success) {
                Logger.e("SettlementCommand.closePreauthTransactions() failed, response: " + response);
            }
            return success;
        } catch (Exception e) {
            Logger.e("SettlementCommand.closePreauthTransactions()", e);
        }
        return false;
    }

    private SyncApi getApi() {
        return getApp().getRestAdapter().create(SyncApi.class);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newUpdate(URI_PAYMENT)
                .withValues(transactionModel.getUpdatePaymentStatus())
                .withSelection(PaymentTransactionTable.STATUS + " = ?", new String[]{String.valueOf(_enum(PaymentStatus.PRE_AUTHORIZED))})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }
}
