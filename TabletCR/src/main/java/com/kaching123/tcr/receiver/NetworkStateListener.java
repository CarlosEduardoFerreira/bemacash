package com.kaching123.tcr.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.util.Util;

/**
 * Created by gdubina on 30.01.14.
 */
public class NetworkStateListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() == null)
            return;

        boolean hasConnection = true;
        if (intent.getExtras().containsKey(ConnectivityManager.EXTRA_NO_CONNECTIVITY)) {
            hasConnection = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        }

        if (TcrApplication.get().isNetworkConnected() != null && TcrApplication.get().isNetworkConnected() == hasConnection) {
            Logger.d("[NETWORK] change state is the same - action ignored");
            return;
        }
        TcrApplication.get().setNetworkConnected(hasConnection);

        if (!TcrApplication.get().isUserLogin()) {
            Logger.d("[NETWORK] not logged in - action ignored");
            return;
        }

        if (hasConnection) {
            Logger.d("[NETWORK] change state to CONNECTED");
            OfflineCommandsService.startUpload(context);
            OfflineCommandsService.startDownload(context);
        } else {
            Logger.d("[NETWORK] change state to DISCONNECTED");
            setOfflineMode();
        }
    }

    public static void checkConnectivity(Context context) {
        setOfflineModeManual(context);
    }

    private static void setOfflineModeManual(Context context) {
        if (TcrApplication.get().isTrainingMode())
            return;

        TcrApplication.get().lockOnOfflineMode();
        try {
            if (Util.isNetworkAvailable(context)) {
                return;
            }
            if (!TcrApplication.get().isOfflineMode())
                TcrApplication.get().setOfflineMode(System.currentTimeMillis());
        } finally {
            TcrApplication.get().unlockOnOfflineMode();
        }
    }

    private static void setOfflineMode() {
        if (TcrApplication.get().isTrainingMode())
            return;

        TcrApplication.get().lockOnOfflineMode();
        try {
            if (!TcrApplication.get().isOfflineMode())
                TcrApplication.get().setOfflineMode(System.currentTimeMillis());
        } finally {
            TcrApplication.get().unlockOnOfflineMode();
        }
    }

}