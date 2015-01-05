package com.kaching123.tcr.commands.payment;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by pkabakov on 19.09.2014.
 */
public class AddFakePaymentCommand extends AsyncCommand {

    private PaymentTransactionModel transactionModel;

    @Override
    protected TaskResult doCommand() {
        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand sqlCommand = batchInsert(transactionModel);
        sqlCommand.add(((PaymentTransactionJdbcConverter) JdbcFactory.getConverter(transactionModel)).insertDeleted(transactionModel, getAppCommandContext()));
        return sqlCommand;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);

        ContentValues values = transactionModel.toValues();
        values.putAll(ShopStore.DELETE_VALUES);

        operations.add(ContentProviderOperation.newInsert(ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT))
                .withValues(values)
                .build());

        return operations;
    }

    public SyncResult sync(Context context, PaymentTransactionModel transactionModel, IAppCommandContext appCommandContext) {
        this.transactionModel = transactionModel;
        return syncDependent(context, appCommandContext);
    }
}
