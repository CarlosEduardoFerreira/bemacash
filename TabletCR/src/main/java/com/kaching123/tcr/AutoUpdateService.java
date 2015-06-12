package com.kaching123.tcr;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by teli on 6/10/2015.
 */
public class AutoUpdateService extends Service implements UpdateObserver.UpdateObserverListener {

    public static String ACTION_APK_UPDATE = "com.kaching123.tcr.service.ACTION_APK_UPDATE";
    public static String ACTION_NO_UPDATE = "com.kaching123.tcr.service.ACTION_NO_UPDATE";
    private Task task;
    private long timer;
    private ExecutorService executor;
    private boolean force;
    public static final String ARG_TIMER = "ARG_TIMER";
    public static final String ARG_MANUAL_CHECK = "ARG_MANUAL_CHECK";
    public static final String ARG_BUILD_NUMBER = "ARG_BUILD_NUMBER";
    private Intent intent;
    private AutoUpdateApk autoUpdateApk;
    private UpdateObserver observer;
    private final static Handler updateHandler = new Handler();

    public static final long MINUTES = 60 * 1000;
    public static final long HOURS = 60 * MINUTES;
    public static final long DAYS = 24 * HOURS;

    private static long UPDATE_INTERVAL = 1 * MINUTES;    // how often to check

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadExecutor();
        AutoUpdateService.this.registerReceiver(connectivity_receiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer = intent.getLongExtra(ARG_TIMER, 0);
        force = intent.getBooleanExtra(ARG_MANUAL_CHECK, false);
        autoUpdateApk = new AutoUpdateApk(AutoUpdateService.this, timer == 0 ? UPDATE_INTERVAL : timer * MINUTES, task);
        observer = new UpdateObserver(autoUpdateApk);
        observer.setListener(this);
        if (task != null)
            task = null;
        task = new Task(force);

        if (force)
            executor.execute(task);
        return super.onStartCommand(intent, flags, startId);
    }

    public class Task implements Runnable {
        private boolean force;

        public Task(boolean force) {
            this.force = force;
        }

        @Override
        public void run() {
            callUpdateCheck(force);
        }
    }

    @Override
    public void onDestroy() {
        stopReceiver();
        super.onDestroy();
    }

    @Override
    public void onUpdate(Observable observable, Object o) {
        String update_status = (String) o;
        if (update_status == AutoUpdateApk.AUTOUPDATE_GOT_UPDATE) {
            intent = new Intent(ACTION_APK_UPDATE);
            intent.putExtra(ARG_BUILD_NUMBER, ((AutoUpdateApk) observable).getUpdateBuildNumber());
            LocalBroadcastManager.getInstance(AutoUpdateService.this).sendBroadcast(intent);
        } else if (update_status == AutoUpdateApk.AUTOUPDATE_NO_UPDATE) {
            intent = new Intent(ACTION_NO_UPDATE);
            LocalBroadcastManager.getInstance(AutoUpdateService.this).sendBroadcast(intent);
        }
    }

    public void callUpdateCheck(boolean force) {
        if (force)
            updateHandler.removeCallbacks(task);
        autoUpdateApk.checkUpdates(force);
        updateHandler.postDelayed(task, timer == 0 ? UPDATE_INTERVAL : timer * MINUTES);
    }

    private static boolean mobile_updates = false;
    private BroadcastReceiver connectivity_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

            // do application-specific task(s) based on the current network state, such
            // as enabling queuing of HTTP requests when currentNetworkInfo is connected etc.
            boolean not_mobile = currentNetworkInfo.getTypeName().equalsIgnoreCase("MOBILE") ? false : true;
            if (currentNetworkInfo.isConnected() && (mobile_updates || not_mobile)) {
                autoUpdateApk.checkUpdates(false);
                updateHandler.postDelayed(task, timer == 0 ? UPDATE_INTERVAL : timer * MINUTES);
            } else {
                updateHandler.removeCallbacks(task);    // no network anyway
            }
        }
    };

    public void stopReceiver() {
        AutoUpdateService.this.unregisterReceiver(connectivity_receiver);
        updateHandler.removeCallbacks(task);
    }
}
