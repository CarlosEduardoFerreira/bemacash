package com.kaching123.tcr.commands.local;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.local.EndUncompletedTransactionsCommand.EndUncompletedTransactionsResult;
import com.kaching123.tcr.service.UploadTask;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SqlCommandTable;
import com.telly.groundy.Groundy;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.lang.ref.WeakReference;

/**
 * Created by hamsterksu on 28.08.2014.
 */
public class StartTransactionCommand extends PublicGroundyTask {

    private static final String ARG_SALE_ORDER_GUID = "arg_sale_order_guid";

    private static final String EXTRA_UPLOAD_TRANSACTION_INVALID = "extra_upload_transaction_invalid";

    private boolean hadInvalidUploadTransaction;

    @Override
    protected TaskResult doInBackground() {
        if (getApp().isTrainingMode())
            return succeeded();

        EndUncompletedTransactionsResult result = new EndUncompletedTransactionsCommand().sync(getContext());
        if (hadInvalidUploadTransaction = result.hadInvalidUploadTransaction) {
            Logger.e("StartTransactionCommand: had invalid upload transactions");
        }

        String saleOrderGuid = getStringArg(ARG_SALE_ORDER_GUID);
        if (!TextUtils.isEmpty(saleOrderGuid))
            getApp().getShopPref().lastUncompletedSaleOrderGuid().put(saleOrderGuid);

        ContentValues values = new ContentValues();
        values.put(SqlCommandTable.CREATE_TIME, System.currentTimeMillis());
        values.put(SqlCommandTable.SQL_COMMAND, UploadTask.CMD_START_TRANSACTION);

        getContext().getContentResolver().insert(ShopProvider.contentUri(SqlCommandTable.URI_CONTENT), values);
        return succeeded().add(EXTRA_UPLOAD_TRANSACTION_INVALID, hadInvalidUploadTransaction);
    }

    public static void start(Context context) {
        start(context, null, new BaseStartTransactionCallback(context));
    }

    public static void start(Context context, String saleOrderGuid) {
        start(context, saleOrderGuid, new BaseStartTransactionCallback(context));
    }

    private static void start(Context context, String saleOrderGuid, BaseStartTransactionCallback callback) {
        Groundy.create(StartTransactionCommand.class).arg(ARG_SALE_ORDER_GUID, saleOrderGuid).callback(callback).queueUsing(context);
    }

    private static class BaseStartTransactionCallback {

        private WeakReference<Context> contextReference;

        public BaseStartTransactionCallback(Context context) {
            contextReference = new WeakReference<Context>(context);
        }

        protected Context getContext() {
            return contextReference.get();
        }

        @OnSuccess(StartTransactionCommand.class)
        public void handleSuccess(@Param(EXTRA_UPLOAD_TRANSACTION_INVALID) boolean hadInvalidUploadTransaction) {
            if (!hadInvalidUploadTransaction)
                return;

            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, context.getString(R.string.warning_message_incomplete_order), Toast.LENGTH_SHORT).show();
            }
        }

    }
}
