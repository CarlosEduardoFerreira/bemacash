package com.kaching123.tcr.commands.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.service.UploadTask;
import com.kaching123.tcr.service.v2.UploadTaskV2;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SqlCommandTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

/**
 * Created by pkabakov on 24.09.2014.
 */
public class EndUncompletedTransactionsCommand extends PublicGroundyTask {

    private static final Uri URI_SQL_COMMAND = ShopProvider.contentUriNoNotify(SqlCommandTable.URI_CONTENT);

    private boolean checkUploadTransactions = true;

    @Override
    protected TaskResult doInBackground() {
        if (getApp().isTrainingMode())
            return succeeded();

        if (checkUploadTransactions = checkUploadTransactions())
            return succeeded();

        fixUploadTransactions();
        return succeeded();
    }

    private boolean checkUploadTransactions() {
        Cursor c = ProviderAction.query(URI_SQL_COMMAND)
                .projection(SqlCommandTable.SQL_COMMAND)
                .where(SqlCommandTable.IS_SENT + " = ?", "0")
                .where("(" + SqlCommandTable.SQL_COMMAND + " = ? OR " + SqlCommandTable.SQL_COMMAND + " = ?)", new String[]{UploadTask.CMD_START_TRANSACTION, UploadTask.CMD_END_TRANSACTION})
                .orderBy(SqlCommandTable.ID)
                .perform(getContext());
        try {
            boolean lastTransactionWasClosed = true;

            while(c.moveToNext()) {
                String jsonString = c.getString(0);

                if (UploadTaskV2.CMD_START_TRANSACTION.equals(jsonString)) {
                    if (!lastTransactionWasClosed) {
                        Logger.e("EndUncompletedTransactionsCommand: check failed - two starts in a row!");
                        return false;
                    }
                    lastTransactionWasClosed = false;
                } else {
                    if (lastTransactionWasClosed) {
                        if (c.isFirst())
                            Logger.e("EndUncompletedTransactionsCommand: check failed - end without a start(or end before it)!");
                        else
                            Logger.e("EndUncompletedTransactionsCommand: check failed - two ends in a row!");
                        return false;
                    }
                    lastTransactionWasClosed = true;
                }
            }

            if (!lastTransactionWasClosed) {
                Logger.e("EndUncompletedTransactionsCommand: check failed - last transaction is not closed!");
                return false;
            }

            return true;
        } finally {
            c.close();
        }
    }

    private void fixUploadTransactions() {
        Cursor c = ProviderAction.query(URI_SQL_COMMAND)
                .projection(SqlCommandTable.ID, SqlCommandTable.SQL_COMMAND)
                .where(SqlCommandTable.IS_SENT + " = ?", "0")
                .orderBy(SqlCommandTable.ID)
                .perform(getContext());

        try {
            boolean lastTransactionWasClosed = true;

            String prevJsonString = null;
            String firstJsonString = null;
            long lastEndTransactionId = -1L;
            while(c.moveToNext()) {
                long id = c.getLong(0);
                String jsonString = c.getString(1);

                if (UploadTaskV2.CMD_START_TRANSACTION.equals(jsonString)) {
                    if (!lastTransactionWasClosed) {
                        getContext().getContentResolver().delete(URI_SQL_COMMAND, SqlCommandTable.ID + " = ?", new String[]{String.valueOf(id)});
                        Logger.e("EndUncompletedTransactionsCommand: transaction fixed in the middle, start after start removed, first action: " + firstJsonString);
                        continue;
                    }
                    lastTransactionWasClosed = false;
                    firstJsonString = null;
                } else if (UploadTaskV2.CMD_END_TRANSACTION.equals(jsonString)) {
                    if (lastTransactionWasClosed) {
                        if (lastEndTransactionId > 0L) {
                            getContext().getContentResolver().delete(URI_SQL_COMMAND, SqlCommandTable.ID + " = ?", new String[]{String.valueOf(lastEndTransactionId)});
                            Logger.e("EndUncompletedTransactionsCommand: transaction fixed in the middle, end before end removed, last action: " + prevJsonString);
                        } else {
                            getContext().getContentResolver().delete(URI_SQL_COMMAND, SqlCommandTable.ID + " = ?", new String[]{String.valueOf(id)});
                            Logger.e("EndUncompletedTransactionsCommand: transaction fixed at the start, end without start removed, last action: " + prevJsonString);
                            continue;
                        }
                    }
                    lastTransactionWasClosed = true;
                    firstJsonString = null;
                    lastEndTransactionId = id;
                } else if (!lastTransactionWasClosed && firstJsonString == null) {
                    firstJsonString = jsonString;
                }

                prevJsonString = jsonString;
            }

            if (!lastTransactionWasClosed) {
                ContentValues values = new ContentValues();
                values.put(SqlCommandTable.CREATE_TIME, System.currentTimeMillis());
                values.put(SqlCommandTable.SQL_COMMAND, UploadTask.CMD_END_TRANSACTION);
                getContext().getContentResolver().insert(URI_SQL_COMMAND, values);
                Logger.e("EndUncompletedTransactionsCommand: transaction fixed at the end, first action: " + firstJsonString);
            }
        } finally {
            c.close();
        }
    }

    public EndUncompletedTransactionsResult sync(Context context) {
        //no need in creds
        super.sync(context, null, null);
        return new EndUncompletedTransactionsResult(!checkUploadTransactions);
    }

    public static class EndUncompletedTransactionsResult {
        public final boolean hadInvalidUploadTransaction;

        public EndUncompletedTransactionsResult(boolean hadInvalidUploadTransaction) {
            this.hadInvalidUploadTransaction = hadInvalidUploadTransaction;
        }
    }

}
