package com.kaching123.tcr.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.fragment.dialog.WriteSettingsPermissionDialogFragment;

import java.util.concurrent.TimeUnit;

/**
 * Created by pkabakov on 12.12.13.
 */
public class ScreenUtils {

    private static final long DEFAULT_SCREEN_OFF_TIMEOUT = TimeUnit.MINUTES.toMillis(15);

    public static void setScreenOffTimeout(Context context) {
        setScreenOffTimeout(context, DEFAULT_SCREEN_OFF_TIMEOUT);
    }

    public static void setScreenOffTimeout(Context context, long timeout) {
        Settings.System.putLong(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeout);
    }

    public static long geScreenOffTimeout(Context context) {
        return Settings.System.getLong(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0L);
    }

    public static void getPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                intent.setData(Uri.parse("package:" + context.getPackageName()));
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
                ActivityCompat.requestPermissions((Activity) context, new String[]{Settings.ACTION_MANAGE_WRITE_SETTINGS}, new Integer(0));
            }
        }
    }

    public static boolean isGrantedWriteSettingsPermission(Context context) {
        return !((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && (!Settings.System.canWrite(context)));
    }

    public static void getPermission(FragmentActivity activity) {
        WriteSettingsPermissionDialogFragment.show(activity);
    }
}
