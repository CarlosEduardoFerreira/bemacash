package com.kaching123.tcr.commands.payment.cash;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 */
public class CashSaleCommand extends AsyncCommand {

    private static final Uri URI_PAYMENT = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);
    protected static final String ARG_DATA = "ARG_data";

    private PaymentTransactionModel transactionModel;

    @Override
    protected TaskResult doCommand() {
        Transaction data = getArgs().getParcelable(ARG_DATA);

        transactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), data);
        Logger.d("CashSaleCommand.doCommand 35: " + transactionModel.toDebugString());

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newInsert(URI_PAYMENT)
                .withValues(transactionModel.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(transactionModel).insertSQL(transactionModel, getAppCommandContext());
    }

    public final static TaskHandler start(Context context, Object callback, Transaction data) {
        return create(CashSaleCommand.class).arg(ARG_DATA, data).callback(callback).queueUsing(context);
    }
}
