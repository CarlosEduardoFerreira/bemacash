package com.kaching123.tcr.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.fragment.dialog.SyncWaitDialogFragment;
import com.kaching123.tcr.notification.NotificationHelper;
import com.kaching123.tcr.service.broadcast.WifiSocketService;
import com.kaching123.tcr.service.broadcast.messages.RequestCommandsMsg;
import com.kaching123.tcr.service.v2.UploadTaskV2;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OfflineCommandsService extends Service {


    private static final int REQUEST_CODE = 0x111;

    public static final String ACTION_SYNC = "OfflineCommandsService.ACTION_SYNC";
    public static final String ACTION_UPLOAD = "OfflineCommandsService.ACTION_UPLOAD";
    public static final String ACTION_EMPLOYEE_UPLOAD = "OfflineCommandsService.ACTION_EMPLOYEE_UPLOAD";
    public static final String ACTION_UPLOAD_AND_SYNC = "OfflineCommandsService.ACTION_UPLOAD_AND_SYNC";

    protected UploadTaskV2 uploadTaskV2Adapter;
    protected static final Uri URI_SQL_COMMAND_NO_NOTIFY = ShopProvider.getNoNotifyContentUri(ShopStore.SqlCommandTable.URI_CONTENT);

    protected static final Uri URI_SQL_COMMAND = ShopProvider.contentUri(ShopStore.SqlCommandTable.URI_CONTENT);

    public static final String EXTRA_IS_MANUAL = "OfflineCommandsService.EXTRA_IS_MANUAL";
    public static final String EXTRA_IS_FROM = "OfflineCommandsService.EXTRA_IS_FROM";
    public static final String EXTRA_IS_OLD = "OfflineCommandsService.EXTRA_IS_OLD";
    public static final String EXTRA_ONLY_UPLOAD = "OfflineCommandsService.EXTRA_ONLY_UPLOAD";
    public static final String EXTRA_UPLOAD_SQL_HOST = "OfflineCommandsService.EXTRA_UPLOAD_SQL_HOST";

    public static final String ACTION_SYNC_PROGRESS = "com.kaching123.tcr.service.ACTION_SYNC_PROGRESS";
    public static final String ACTION_SYNC_COMPLETED = "com.kaching123.tcr.service.ACTION_SYNC_COMPLETED";
    public static final String EXTRA_TABLE = "table";
    public static final String EXTRA_PAGES = "pages";
    public static final String EXTRA_PROGRESS = "progress";
    public static final String EXTRA_DATA_LABEL = "data_label";
    public static final String EXTRA_SUCCESS = "success";
    public static final String EXTRA_SYNC_LOCKED = "sync_locked";
    public static final String EXTRA_SYNC_CANCELED = "EXTRA_SYNC_CANCELED";

    private static Handler handler = new Handler();

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public static boolean forceCancelDownloadUpload;
    public static boolean ignoreCommandObserver;


    private ContentObserver commandObserver = new ContentObserver(handler) {
        public void onChange(boolean selfChange) {
            Logger.d("[OfflineService] onChange");
            if (ignoreCommandObserver){
                Logger.d("ignoreCommandObserver");
                return;
            }
            doUploadAndMaybeDownload(true, false, null, false, false);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        uploadTaskV2Adapter = new UploadTaskV2();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_STICKY;

        startService(new Intent(this, WifiSocketService.class));
        doUploadAndMaybeDownload(
                intent.getBooleanExtra(EXTRA_ONLY_UPLOAD, false),
                intent.getBooleanExtra(EXTRA_IS_MANUAL, false),
                intent.getStringExtra(EXTRA_IS_FROM),
                intent.getBooleanExtra(EXTRA_UPLOAD_SQL_HOST, false),
                intent.getBooleanExtra(EXTRA_IS_OLD, false));
        /*
        if (ACTION_SYNC.equals(intent.getAction())) {
            boolean isManual = intent.getBooleanExtra(EXTRA_IS_MANUAL, false);
            doDownload(isManual);
        } else if (ACTION_UPLOAD.equals(intent.getAction())) {
            boolean isManual = intent.getBooleanExtra(EXTRA_IS_MANUAL, false);
            doUpload(isManual);
        } else if (ACTION_UPLOAD_AND_SYNC.equals(intent.getAction())) {
            doUpload(false);
            doDownload(false);
        } else if (ACTION_EMPLOYEE_UPLOAD.equals(intent.getAction())) {
            doEmployeeUpload();
        }
        /**/

        return START_STICKY;
    }

    /**
     * can be executed by
     * - During login - Sync now
     * - Manual from the settings - Sync now
     * - Network state is changed
     * - Scheduler
     */
    private void doDownload(boolean isManual) {
        Logger.d("[OfflineService] doDownload: isManual = " + isManual);
        executor.submit(new SyncCommand(this, isManual));
    }

    /**
     * can be executed by
     * - After login
     * - Manual from the settings
     * - Network state is changed
     * - Scheduler
     * - Sale finished
     */
    /*
    private void doUpload(boolean isManual) {
        Logger.d("[OfflineService] doUpload: isManual = " + isManual);
        executor.submit(new UploadTask(this, isManual));
    }
    /**/

    private void doEmployeeUpload() {
        Logger.d("[OfflineService] doUpload: isManual = false");
//        executor.submit(new UploadTask(this, false, true));
        final ContentResolver cr = this.getContentResolver();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (uploadTaskV2Adapter.employeeUpload(cr, OfflineCommandsService.this))
                        cr.delete(URI_SQL_COMMAND_NO_NOTIFY, ShopStore.SqlCommandTable.IS_SENT + " = ?", new String[]{"1"});
                } catch (SyncCommand.SyncLockedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public static void scheduleSyncAction(Context context) {
        int mins = getSyncPeriod(context);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, mins);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Logger.d("Sync schedule to %s with periodSpinner %d", c.getTime().toString(), mins);
        alarmManager.setRepeating(AlarmManager.RTC, c.getTimeInMillis(), TimeUnit.MINUTES.toMillis(mins), createPendingIntent(context));

    }

    private static int getSyncPeriod(Context context) {
        TcrApplication app = ((TcrApplication) context.getApplicationContext());
        assert app != null;
        int mins = app.getShopPref().syncPeriod().get();
        if (mins <= 0) {
            mins = context.getResources().getInteger(R.integer.sync_time_entries_def_val);
        }
        return mins;
    }


    /**
     * can be executed by
     * 1. after login
     * 2. Network state is changed
     * 3. content observer for SqlCommandTable
     * 4. Manual from the settings - Sync now
     */
    private void doUploadAndMaybeDownload(final boolean onlyUpload, final boolean isManual, final String from, boolean fromSQLHost, final boolean isOldSync) {
        if (from != null){
            doDownloadAfterUpload(isManual, from, isOldSync);
            return;
        }

        Logger.d("[OfflineService] doUpload");

        LocalSyncHelper.notifyChanges();

        if (isOldSync){
            executor.submit(new UploadTask(this, isManual));

        } else {
            executor.submit(new UploadTask(this, isManual));
        }
    }


    private void doDownloadAfterUpload(final boolean isManual, final String from, final boolean isOldSync) {
        Logger.d("[OfflineService] doDownload: isManual = " + isManual);

        //LOCAL SYNC
        new Thread(new Runnable() {
            @Override
            public void run() {
                doDownloadLocal(OfflineCommandsService.this, from);
                if (from != null) return;

                executor.submit(
                        isOldSync ? (Runnable) new SyncCommand(OfflineCommandsService.this, isManual)
                                : new SyncCommand(OfflineCommandsService.this, isManual));
            }
        }).start();
    }

    public static void doDownloadLocal(Context context, String from){
        if (!TcrApplication.get().getShopPref().enabledLocalSync().get()) return;

        RequestCommandsMsg request = new RequestCommandsMsg(TcrApplication.get().getRegisterSerial());
        Logger.d(LocalSyncHelper.TAG_HEIGHT + ": Requesting new commands from: " + from);

        try {
            WifiSocketService.getInstance().makeMsgAndSend(request.toJson(), true, from);

        } catch (Exception e) {
            LocalSyncHelper.localSyncError(context, context.getString(R.string.local_sync_failed), "");
            Logger.e(LocalSyncHelper.TAG_HEIGHT, e);
        }

        Logger.d(LocalSyncHelper.TAG + ": END new commands from: " + from);
        LocalSyncHelper.clearSqlHostTable(context.getContentResolver());
    }

    private static PendingIntent createPendingIntent(Context context) {
        return PendingIntent.getService(context, REQUEST_CODE, getScheduleIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Intent getSyncIntent(Context context, boolean isManual) {
        Intent intent = new Intent(context, OfflineCommandsService.class);
        intent.setAction(ACTION_SYNC);
        intent.putExtra(EXTRA_IS_MANUAL, isManual);
        return intent;
    }

    private static Intent getUploadIntent(Context context, boolean isManual) {
        Intent intent = new Intent(context, OfflineCommandsService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(EXTRA_IS_MANUAL, isManual);
        return intent;
    }

    private static Intent getEmployeeUploadIntent(Context context, boolean isManual) {
        Intent intent = new Intent(context, OfflineCommandsService.class);
        intent.setAction(ACTION_EMPLOYEE_UPLOAD);
        intent.putExtra(EXTRA_IS_MANUAL, isManual);
        return intent;
    }

    private static Intent getScheduleIntent(Context context) {
        Intent intent = new Intent(context, OfflineCommandsService.class);
        intent.setAction(ACTION_UPLOAD_AND_SYNC);
        return intent;
    }

    public static void startDownload(Context context) {
        startDownload(context, false);
    }

    public static void startDownload(Context context, boolean isManual) {
        Logger.d("[OfflineService] startDownload");
        Intent intent = getSyncIntent(context, isManual);
        context.startService(intent);
    }

    public static void startUpload(Context context) {
        startUpload(context, false);
    }

    public static void startUpload(Context context, boolean isManual) {
        Logger.d("[OfflineService] startUpload");
        Intent intent = getUploadIntent(context, isManual);
        context.startService(intent);
    }

    public static void startemployeeTableUpload(Context context) {
        Logger.d("[OfflineService] startUpload");
        Intent intent = getEmployeeUploadIntent(context, false);
        context.startService(intent);
    }


    public static void startUploadAndDownload(Context context, boolean isManual, String from) {
        Logger.d("[OfflineService] startUploadAndDownload");
        Intent intent = new Intent(context, OfflineCommandsService.class);
        intent.putExtra(EXTRA_IS_MANUAL, isManual);
        intent.putExtra(EXTRA_IS_FROM, from);
        context.startService(intent);
    }

    public static void startOldUploadAndOldDownload(Context context, boolean isManual, String from) {
        Logger.d("[OfflineService] startOldUploadAndOldDownload");
        Intent intent = new Intent(context, OfflineCommandsService.class);
        intent.putExtra(EXTRA_IS_MANUAL, isManual);
        intent.putExtra(EXTRA_IS_FROM, from);
        intent.putExtra(EXTRA_IS_OLD, true);
        context.startService(intent);
    }

    public  static void fireEvent(int message, Context context){
        fireEvent(context.getString(message), context);
    }

    public  static  void fireEvent(String message, Context context){
        fireEvent(context, null, message, 0, 0);
    }

    public  static  void fireEvent(Context context, String table) {
        fireEvent(context, table, null, 0, 0);
    }

    public  static  void fireEvent(Context context, String table, int pages, int progress) {
        fireEvent(context, table, null, pages, progress);
    }

    public  static  void fireEvent(Context context, String table, String dataLabel, int pages, int progress) {
        Intent intent = new Intent(ACTION_SYNC_PROGRESS);
        intent.putExtra(EXTRA_TABLE, table);
        intent.putExtra(EXTRA_DATA_LABEL, dataLabel);
        intent.putExtra(EXTRA_PAGES, pages);
        intent.putExtra(EXTRA_PROGRESS, progress);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        NotificationHelper.setMessageNotification(context, SyncWaitDialogFragment.processMessage(context, dataLabel, table, pages, progress));
    }

    public  static  void fireCompleteEvent(Context context, boolean success, boolean isSyncLocked, boolean isManual, boolean isCanceled) {
        if (!isManual)
            return;

        Intent intent = new Intent(ACTION_SYNC_COMPLETED);
        intent.putExtra(EXTRA_SUCCESS, success);
        intent.putExtra(EXTRA_SYNC_LOCKED, isSyncLocked);
        intent.putExtra(EXTRA_SYNC_CANCELED, isCanceled);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
