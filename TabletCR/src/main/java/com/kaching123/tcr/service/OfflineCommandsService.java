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
import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
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

    private static Handler handler = new Handler();

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public static boolean ignoreCommandObserver;

    public static boolean localSync = false;

    private ContentObserver commandObserver = new ContentObserver(handler) {
        public void onChange(boolean selfChange) {
            Logger.d("[OfflineService] onChange");
            if (ignoreCommandObserver){
                Logger.d("ignoreCommandObserver");
                return;
            }
            LocalSyncHelper.notifyChanges();
        }
    };

    public void onCreate() {
        super.onCreate();
        uploadTaskV2Adapter = new UploadTaskV2();
        getContentResolver().registerContentObserver(URI_SQL_COMMAND, false, commandObserver);
    }

    @Override
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(commandObserver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_STICKY;

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
    private void doUpload(boolean isManual) {
        Logger.d("[OfflineService] doUpload: isManual = " + isManual);
        executor.submit(new UploadTask(this, isManual));
    }

    private void doEmployeeUpload() {
        Logger.d("[OfflineService] doEmployeeUpload: isManual = false");
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
        mins = localSync ? context.getResources().getInteger(R.integer.local_sync_time_def_val) : mins;
        if (mins <= 0) {
            mins = context.getResources().getInteger(R.integer.sync_time_entries_def_val);
        }
        return mins;
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
        Logger.d("[OfflineService] startemployeeTableUpload");
        Intent intent = getEmployeeUploadIntent(context, false);
        context.startService(intent);
    }

}
