package com.kaching123.tcr.util;

import android.content.Context;
import android.provider.Settings;

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

}
