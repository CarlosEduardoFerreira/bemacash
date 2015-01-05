package com.kaching123.tcr.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;

import com.kaching123.tcr.model.ApplicationVersion;

import java.util.Calendar;
import java.util.Date;

public class Util {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public static int toInt(String text, int def) {
        try {
            return Integer.parseInt(text);
        } catch (Exception e) {
            return def;
        }
    }

    public static Date cropSeconds(Date time) {
        Calendar c = Calendar.getInstance();
        c.setTime(time);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static ApplicationVersion getApplicationVersion(Context context) {
        String versionName;
        int versionCode;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "";
            versionCode = 0;
        }
        return new ApplicationVersion(versionCode, versionName);
    }

}
