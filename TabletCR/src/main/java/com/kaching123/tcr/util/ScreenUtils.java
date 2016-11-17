package com.kaching123.tcr.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;

import com.kaching123.tcr.activity.DashboardActivity;
import com.kaching123.tcr.fragment.dialog.WriteSettingsPermissionDialogFragment;

import java.util.concurrent.TimeUnit;

/**
 * Created by pkabakov on 12.12.13.
 */
public class ScreenUtils {

    private static final long DEFAULT_SCREEN_OFF_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

    public static void setScreenOffTimeout(Context context) {
        setScreenOffTimeout(context, DEFAULT_SCREEN_OFF_TIMEOUT);
    }

    public static void setScreenOffTimeout(Context context, long timeout) {
        Settings.System.putLong(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeout);
    }

    public static long geScreenOffTimeout(Context context) {
        return Settings.System.getLong(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0L);
    }

    public static boolean isGrantedWriteSettingsPermission(Context context) {
        return !((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && (!Settings.System.canWrite(context)));
    }

    public static void getPermission(FragmentActivity activity) {
        WriteSettingsPermissionDialogFragment.show(activity);
    }

}
