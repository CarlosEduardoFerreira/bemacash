package com.kaching123.tcr.service;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.local.EndUncompletedTransactionsCommand;
import com.kaching123.tcr.commands.local.EndUncompletedTransactionsCommand.EndUncompletedTransactionsResult;
import com.kaching123.tcr.commands.rest.sync.DBVersionCheckCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder;
import com.kaching123.tcr.commands.support.SendLogCommand;
import com.kaching123.tcr.notification.NotificationHelper;
import com.kaching123.tcr.pref.ShopPref_;
import com.kaching123.tcr.service.v1.UploadTaskV1;
import com.kaching123.tcr.service.v2.UploadTaskV2;
import com.kaching123.tcr.service.v2.UploadTaskV2.TransactionNotFinalizedException;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SqlCommandTable;
import com.kaching123.tcr.util.Util;

public class UploadTask implements Runnable {

    public static String ACTION_UPLOAD_STARTED = "com.kaching123.tcr.service.ACTION_UPLOAD_STARTED";
    public static String ACTION_UPLOAD_COMPLETED = "com.kaching123.tcr.service.ACTION_UPLOAD_COMPLETED";
    public static String ACTION_INVALID_UPLOAD_TRANSACTION = "com.kaching123.tcr.service.ACTION_INVALID_UPLOAD_TRANSACTION";
    public static final String CMD_START_EMPLOYEE = "start_employee";
    public static final String CMD_END_EMPLOYEE = "end_employee";
    public static String EXTRA_SUCCESS = "success";

    protected static final Uri URI_SQL_COMMAND_NO_NOTIFY = ShopProvider.getNoNotifyContentUri(SqlCommandTable.URI_CONTENT);

    public static final String CMD_START_TRANSACTION = "start_transaction";
    public static final String CMD_END_TRANSACTION = "end_transaction";

    private UploadTaskV2 uploadTaskV2Adapter;
    private UploadTaskV1 uploadTaskV1Adapter;

    private OfflineCommandsService service;

    private final boolean isManual;

    private boolean uploadEmployee;

    public UploadTask(OfflineCommandsService service, boolean isManual, boolean uploadEmployee) {
        this.service = service;
        this.isManual = isManual;
        uploadTaskV1Adapter = new UploadTaskV1(service);
        uploadTaskV2Adapter = new UploadTaskV2(service);
        this.uploadEmployee = uploadEmployee;
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

    private void fireInvalidUploadTransactionEvent(Context context) {
        if (!isManual)
            return;

        Intent intent = new Intent(ACTION_INVALID_UPLOAD_TRANSACTION);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void run() {
        Logger.d("UploadTask START");
        if (!TcrApplication.get().isUserLogin()) {
            if (isManual)
                Logger.e("UploadTask skip task: NO USER");
            else
            {

                if (uploadEmployee)
                    try {
                        ContentResolver cr = service.getContentResolver();
                        uploadTaskV2Adapter.employeeUpload(cr);
                        cr.delete(URI_SQL_COMMAND_NO_NOTIFY, SqlCommandTable.IS_SENT + " = ?", new String[]{"1"});
                    } catch (TransactionNotFinalizedException e) {
                        e.printStackTrace();
                        Logger.e("UploadTask uploadEmployee error", e);
                    }

            }
            return;
        }

        fireStartEvent(service);

        if (isManual) {
            EndUncompletedTransactionsResult result = new EndUncompletedTransactionsCommand().sync(service);
            if (result.hadInvalidUploadTransaction) {
                Logger.e("UploadTask: manual upload, had invalid upload transactions");
                fireInvalidUploadTransactionEvent(service);
            }
        }

        if (!Util.isNetworkAvailable(service)) {
            Logger.e("UploadTask skip task: NO CONNECTION");
            onUploadFailure();
            return;
        }

        NotificationHelper.addUploadNotification(service);

        ShopPref_ pref = TcrApplication.get().getShopPref();
        boolean errorsOccurred = false;
        boolean isV1CommandsSent = pref.isV1CommandsSent().getOr(false);
        TcrApplication.get().lockOnTrainingMode();
        try {
            ContentResolver cr = service.getContentResolver();
            if (!isV1CommandsSent) {
                Logger.d("Send V1 commands");
                errorsOccurred = !uploadTaskV1Adapter.webApiUpload(cr);
                isV1CommandsSent = !errorsOccurred && uploadTaskV1Adapter.getV1CommandsCount() == 0;
                pref.isV1CommandsSent().put(isV1CommandsSent);
            }
            if (isV1CommandsSent) {
                errorsOccurred = !uploadTaskV2Adapter.webApiUpload(cr);
            }
            cr.delete(URI_SQL_COMMAND_NO_NOTIFY, SqlCommandTable.IS_SENT + " = ?", new String[]{"1"});
        } catch (TransactionNotFinalizedException e) {
            if (isManual)
                Logger.e("UploadTask: transaction not finalized!", e);
            else
                Logger.w("UploadTask: transaction not finalized yet", e);
            NotificationHelper.removeUploadNotification(service);
            fireCompleteEvent(service, false);
            return;
        } catch (Exception e) {
            Logger.e("UploadTask error", e);
            errorsOccurred = true;
        } finally {
            TcrApplication.get().unlockOnTrainingMode();
        }

        if (errorsOccurred) {
            NotificationHelper.showUploadErrorNotification(service);
            onUploadFailure();
            SendLogCommand.start(service);
        } else {
            NotificationHelper.removeUploadNotification(service);
            onUploadSuccess();
            sendSyncSuccessful();
        }

        Logger.d("UploadTask END");
    }

    private void sendSyncSuccessful() {
        TcrApplication app = TcrApplication.get();
        SyncApi api = app.getRestAdapter().create(SyncApi.class);
        try {
            api.setRegisterLastUpdate(app.emailApiKey, SyncUploadRequestBuilder.getReqCredentials(app.getOperator(), app));
        } catch (Exception e) {
            Logger.e("UploadTask.sendSyncSuccessful(): failed", e);
        }
    }

    private void onUploadSuccess() {
        setOfflineMode(false);
        fireCompleteEvent(service, true);
    }

    private void onUploadFailure() {
        setOfflineMode(true);
        fireCompleteEvent(service, false);
    }

    private void setOfflineMode(boolean isOfflineMode) {
        if (!TcrApplication.get().isUserLogin())
            return;
        if (TcrApplication.get().isTrainingMode())
            return;

        TcrApplication.get().lockOnOfflineMode();
        try {
            if (!isOfflineMode && !Util.isNetworkAvailable(service)) {
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
        return service.getString(errorMessage);
    }

}