package com.kaching123.tcr.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OfflineCommandsService extends Service {


    private static final int REQUEST_CODE = 0x111;

    public static final String ACTION_SYNC = "OfflineCommandsService.ACTION_SYNC";
    public static final String ACTION_UPLOAD = "OfflineCommandsService.ACTION_UPLOAD";
    public static final String ACTION_UPLOAD_AND_SYNC = "OfflineCommandsService.ACTION_UPLOAD_AND_SYNC";

    public static final String EXTRA_IS_MANUAL = "OfflineCommandsService.EXTRA_IS_MANUAL";

    private ExecutorService executor = Executors.newSingleThreadExecutor();


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

}
