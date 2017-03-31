package com.kaching123.tcr.service;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.BackOfficeSyncCommand;
import com.kaching123.tcr.commands.local.EndUncompletedTransactionsCommand;
import com.kaching123.tcr.commands.rest.RestCommand;
import com.kaching123.tcr.commands.rest.sync.DBVersionCheckCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder;
import com.kaching123.tcr.notification.NotificationHelper;
import com.kaching123.tcr.service.v1.UploadTaskV1;
import com.kaching123.tcr.service.v2.UploadTaskV2;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SqlCommandTable;
import com.kaching123.tcr.util.Util;

public class UploadTask implements Runnable {

    public static String ACTION_UPLOAD_STARTED = "com.kaching123.tcr.service.ACTION_UPLOAD_STARTED";
    public static String ACTION_UPLOAD_COMPLETED = "com.kaching123.tcr.service.ACTION_UPLOAD_COMPLETED";
    public static String ACTION_EMPLOYEE_UPLOAD_COMPLETED = "com.kaching123.tcr.service.ACTION_EMPLOYEE_UPLOAD_COMPLETED";
    public static String ACTION_EMPLOYEE_UPLOAD_FAILED = "com.kaching123.tcr.service.ACTION_EMPLOYEE_UPLOAD_FAILED";
    public static String ACTION_INVALID_UPLOAD_TRANSACTION = "com.kaching123.tcr.service.ACTION_INVALID_UPLOAD_TRANSACTION";
    public static final String CMD_START_EMPLOYEE = "start_employee";
    public static final String CMD_END_EMPLOYEE = "end_employee";
    public static String EXTRA_SUCCESS = "success";

    protected static final Uri URI_SQL_COMMAND_NO_NOTIFY = ShopProvider.getNoNotifyContentUri(SqlCommandTable.URI_CONTENT);

    public static final String CMD_START_TRANSACTION = "start_transaction";
    public static final String CMD_END_TRANSACTION = "end_transaction";

    private UploadTaskV2 uploadTaskV2Adapter;
    private UploadTaskV1 uploadTaskV1Adapter;

    private Context context;

    private final boolean isManual;

    public UploadTask(Context context, boolean isManual) {
        this.context = context;
        this.isManual = isManual;
        uploadTaskV1Adapter = new UploadTaskV1();
        uploadTaskV2Adapter = new UploadTaskV2();
    }

    private void fireStartEvent(Context context) {
        if (!isManual)
            return;

        Intent intent = new Intent(ACTION_UPLOAD_STARTED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void fireCompleteEvent(Context context, boolean success) {
        if (!isManual)
            return;

        Intent intent = new Intent(ACTION_UPLOAD_COMPLETED);
        intent.putExtra(EXTRA_SUCCESS, success);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

//    private void fireUploadEmployeeCompleteEvent(Context context, boolean success) {
//        Intent intent = new Intent(ACTION_EMPLOYEE_UPLOAD_COMPLETED);
//        intent.putExtra(EXTRA_SUCCESS, success);
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//    }

    private void fireInvalidUploadTransactionEvent(Context context) {
        if (!isManual)
            return;

        Intent intent = new Intent(ACTION_INVALID_UPLOAD_TRANSACTION);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void run() {
        Logger.d("UploadTask STARTED");
        if (!TcrApplication.get().isUserLogin()) {
            if (isManual)
                Logger.e("UploadTask skip task: NO USER");
            else
                Logger.w("UploadTask skip task: NO USER");
            return;
        }

        execute(true, true);
    }

    public ExecuteResult executeSync() {
        Logger.d("UploadTask EXECUTED SYNCHRONOUSLY");
        return execute(false, false);
    }

    private ExecuteResult execute(final boolean fireEvents, final boolean lockOnTrainingMode) {
        synchronized (UploadTask.class) {
            Log.d("BemaCarl10","UploadTask.execute --------------- Start --------------- ");
            Logger.d("UploadTask EXECUTE");

            if(!new AtomicUpload().hasInternetConnection()){
                new BackOfficeSyncCommand().adjustSyncTime();
                onUploadFailure(fireEvents);
                return new ExecuteResult(false, false);
            }
            /*
            if (!Util.isNetworkAvailable(context)) {
                Log.d("BemaCarl10","UploadTask.execute: if (!Util.isNetworkAvailable(context)) {");
                Logger.e("UploadTask error: NO CONNECTION");
                onUploadFailure(fireEvents);
                return new ExecuteResult(false, false);
            }
            /**/

            if (fireEvents)
                fireStartEvent(context);

            boolean hadInvalidUploadTransaction = false;
            if (isManual) {
                EndUncompletedTransactionsCommand.EndUncompletedTransactionsResult result = new EndUncompletedTransactionsCommand().sync(context);
                if (hadInvalidUploadTransaction = result.hadInvalidUploadTransaction) {
                    Logger.e("UploadTask: manual upload, had invalid upload transactions");
                    if (fireEvents)
                        fireInvalidUploadTransactionEvent(context);
                }
            }

            NotificationHelper.addUploadNotification(context);

            boolean errorsOccurred = false;
            String errorMessage = null;
            boolean isV1CommandsSent = TcrApplication.get().getShopPref().isV1CommandsSent().getOr(false);
            if (lockOnTrainingMode) {
                TcrApplication.get().lockOnTrainingMode();
            }
            try {
                ContentResolver cr = context.getContentResolver();
                if (!isV1CommandsSent) {
                    Logger.d("Send V1 commands");
                    errorsOccurred = !uploadTaskV1Adapter.webApiUpload(cr);
                    isV1CommandsSent = !errorsOccurred && uploadTaskV1Adapter.getV1CommandsCount(cr) == 0;
                    TcrApplication.get().getShopPref().isV1CommandsSent().put(isV1CommandsSent);
                }
                if (isV1CommandsSent) {
                    errorsOccurred = !uploadTaskV2Adapter.webApiUpload(cr, context);
                }
                cr.delete(URI_SQL_COMMAND_NO_NOTIFY, SqlCommandTable.IS_SENT + " = ?", new String[]{"1"});
            } catch (UploadTaskV2.TransactionNotFinalizedException e) {
                if (isManual)
                    Logger.e("UploadTask: transaction not finalized!", e);
                else
                    Logger.w("UploadTask: transaction not finalized yet", e);
                NotificationHelper.removeUploadNotification(context);
                if (fireEvents)
                    fireCompleteEvent(context, false);
                return new ExecuteResult(false, hadInvalidUploadTransaction);
            } catch (Exception e) {
                Logger.e("UploadTask error", e);
                errorsOccurred = true;

            } finally {
                if (lockOnTrainingMode) {
                    TcrApplication.get().unlockOnTrainingMode();
                }
            }

            if (errorsOccurred) {
                if (TextUtils.isEmpty(errorMessage)) {
                    NotificationHelper.showUploadErrorNotification(context);

                } else {
                    NotificationHelper.showUploadErrorNotification(context, errorMessage);
                }
                onUploadFailure(fireEvents);

            } else {
                NotificationHelper.removeUploadNotification(context);
                onUploadSuccess(fireEvents);
                sendSyncSuccessful();
            }

            Logger.d("UploadTask END");
            Log.d("BemaCarl10","UploadTask.execute --------------- End  --------------- ");
            return new ExecuteResult(!errorsOccurred, false);
        }
    }

    private void sendSyncSuccessful() {
        TcrApplication app = TcrApplication.get();
        SyncApi api = app.getRestAdapter().create(SyncApi.class);
        try {
            RestCommand.Response resp = api.setRegisterLastUpdate(app.emailApiKey, SyncUploadRequestBuilder.getReqCredentials(app.getOperator(), app));
            if (resp == null || !resp.isSuccess()) {
                Logger.e("UploadTask.sendSyncSuccessful(): failed, response: " + resp);
            }
        } catch (Exception e) {
            Logger.e("UploadTask.sendSyncSuccessful(): failed", e);
        }
    }

    private void onUploadSuccess(boolean fireEvents) {
        setOfflineMode(false);
        if (fireEvents)
            fireCompleteEvent(context, true);
    }

    private void onUploadFailure(boolean fireEvents) {
        setOfflineMode(true);
        if (fireEvents)
            fireCompleteEvent(context, false);
    }

    private void setOfflineMode(boolean isOfflineMode) {
        if (!TcrApplication.get().isUserLogin())
            return;
        if (TcrApplication.get().isTrainingMode())
            return;

        TcrApplication.get().lockOnOfflineMode();
        try {
            if (!isOfflineMode && !Util.isNetworkAvailable(context)) {
                return;
            }
            if (!isOfflineMode || !TcrApplication.get().isOfflineMode())
                TcrApplication.get().setOfflineMode(isOfflineMode ? System.currentTimeMillis() : null);
        } finally {
            TcrApplication.get().unlockOnOfflineMode();
        }
    }

    private DBVersionCheckCommand.DBVersionCheckError dbVersionCheck() {
        return new DBVersionCheckCommand().sync(TcrApplication.get());
    }

    private String getDBVersionErrorMessage(DBVersionCheckCommand.DBVersionCheckError dbVersionError) {
        int errorMessage;
        switch (dbVersionError) {
            case INVALID_VERSION:
                errorMessage = R.string.upload_notify_message_error_db_version_invalid;
                break;
            default:
                errorMessage = R.string.upload_notify_message_error_db_version_failed;
        }
        return context.getString(errorMessage);
    }

    public static class ExecuteResult {

        public final boolean isSuccessful;
        public final boolean hadInvalidUploadTransaction;

        public ExecuteResult(boolean isSuccessful, boolean hadInvalidUploadTransaction) {
            this.isSuccessful = isSuccessful;
            this.hadInvalidUploadTransaction = hadInvalidUploadTransaction;
        }
    }

}
