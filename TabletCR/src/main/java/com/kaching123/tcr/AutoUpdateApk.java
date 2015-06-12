package com.kaching123.tcr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.kaching123.tcr.APKupdaterJDBCConverter.APKUpdater;
import com.kaching123.tcr.AutoUpdateService.Task;
import com.kaching123.tcr.commands.rest.sync.GetArrayResponse;
import com.kaching123.tcr.commands.rest.sync.SyncApi2;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Observable;

public class AutoUpdateApk extends Observable {

    public AutoUpdateApk(Context ctx, long timer, Task task) {
        setupVariables(ctx);
        this.customTimer = timer;
        this.task = task;
    }

    public static final String AUTOUPDATE_CHECKING = "autoupdate_checking";
    public static final String AUTOUPDATE_NO_UPDATE = "autoupdate_no_update";
    public static final String AUTOUPDATE_GOT_UPDATE = "autoupdate_got_update";
    public static final String AUTOUPDATE_HAVE_UPDATE = "autoupdate_have_update";

    protected static Context context = null;
    private static long last_update = 0;

    private static String registerSerial;

    private long customTimer;

    private Runnable task;

    private APKUpdater result;
    private int buildNumber;

    public boolean isAprove() {
        return aprove;
    }

    public void setAprove(boolean aprove) {
        this.aprove = aprove;
    }

    private boolean aprove;

    private void setAPKupdateREsult(APKUpdater result) {
        this.result = result;
    }

    private APKUpdater getResult() {
        return result;
    }

    protected static final Uri URI_APK_UPDATE = ShopProvider.getNoNotifyContentUri(ShopStore.ApkUpdate.URI_CONTENT);

    public int getUpdateBuildNumber() {
        return updateBuildNumber;
    }

    public void setUpdateBuildNumber(int updateBuildNumber) {
        this.updateBuildNumber = updateBuildNumber;
    }

    private int updateBuildNumber;

    private class ScheduleEntry {
        public int start;
        public int end;

        public ScheduleEntry(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static ArrayList<ScheduleEntry> schedule = new ArrayList<ScheduleEntry>();




    private void setupVariables(Context ctx) {
        context = ctx;
        try {
            buildNumber = getApp().getPackageManager().getPackageInfo(getApp().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        setRegisterSerial();
        last_update = getApp().getLastUpdateTime();


    }

    private void setRegisterSerial() {
        registerSerial = !Build.UNKNOWN.equals(Build.SERIAL) ? Build.SERIAL : Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        registerSerial += cut4Symbols(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        registerSerial = formatByBlocksString(registerSerial);
    }

    private String cut4Symbols(String s) {
        if (s == null)
            return "";
        if (s.length() < 4) {
            return s;
        }
        return s.substring(0, 3);
    }

    private static final int BLOCK_SIZE = 6;

    private String formatByBlocksString(String str) {
        int blocks = str.length() / BLOCK_SIZE;
        StringBuilder builder = new StringBuilder(str);
        for (int i = 0; i < blocks - 1; i++) {
            builder.insert((i + 1) * BLOCK_SIZE + i, '-');
        }
        return builder.toString();
    }

    private boolean checkSchedule() {
        if (schedule.size() == 0) return true;    // empty schedule always fits

        int now = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        for (ScheduleEntry e : schedule) {
            if (now >= e.start && now < e.end) return true;
        }
        return false;
    }

    private static final int PAGE_ROWS = 1;

    private class checkUpdateTask extends AsyncTask<Void, Void, APKupdaterJDBCConverter.APKUpdater> {
        protected APKupdaterJDBCConverter.APKUpdater doInBackground(Void... v) {
            SyncApi2 api2 = getApp().getRestAdapter().create(SyncApi2.class);
            try {
                if (getCurrentUser() == null)
                    return null;
                GetArrayResponse resp = api2.download(getApp().emailApiKey, SyncUploadRequestBuilder.getReqCredentials(getCurrentUser(), getApp()),
                        getAPKUpdateRequest(APKupdaterJDBCConverter.getTableName(), PAGE_ROWS));

                if (resp == null) {
                    Logger.e("Auto update error: can't get response");
                    return null;
                }
                if (!resp.isSuccess()) {
                    return null;
                }
                APKUpdater info = APKupdaterJDBCConverter.read(resp.getEntity().getJSONObject(0));
                setUpdateBuildNumber((Integer.parseInt(info.getUpdateBuildNumber())));
                setAprove(info.aprove);
                if (info == null) {
                    Logger.e("Auto update error: response is empty");
                    return null;
                }
                if (Integer.parseInt(info.getUpdateBuildNumber()) > buildNumber) {
                    context.getContentResolver().insert(ShopProvider.contentUri(ShopStore.ApkUpdate.URI_CONTENT), info.toValues());
                } else {
                    String update_file = getApp().getUpdateFilePath();
                    if (new File(update_file).delete()) {
                        // delete the old used udpated apk file from tablet cache.
                        clearUpdateInfo();
                    }
                    Logger.d("current build is the latest one");

                    return null;
                }
                return info;
            } catch (Exception e) {
                Logger.e("Auto update FAILED!", e);
            }
            return null;
        }

        protected void onPreExecute() {
            Logger.d("checking if there's update on the server");
        }

        protected void onPostExecute(APKUpdater result) {
            if (result != null) {
                setAPKupdateREsult(result);
                storeUpdateInfo(result);
                setChanged();
                notifyObservers(AUTOUPDATE_GOT_UPDATE);
            } else {
                setChanged();
                notifyObservers(AUTOUPDATE_NO_UPDATE);
                Logger.d("no new apk updates return from server");
            }
        }
    }

    private EmployeeModel getCurrentUser() {
        return getApp().getOperator();
    }

    private boolean isFileExisted() {
        String update_file = getApp().getUpdateFilePath();
        if (update_file != null && update_file != "" && new File(update_file).exists())
            return true;
        return false;
    }

    public JSONObject getAPKUpdateRequest(String table, int limit) throws JSONException {

        JSONObject request = new JSONObject();
        request.put("table", table);

        JSONObject from = new JSONObject();
        from.put("date", JSONObject.NULL);
        from.put("id", JSONObject.NULL);

        request.put("from", from);

        request.put("where", JSONObject.NULL);

        request.put("limit", limit);
        return request;
    }

    private void clearUpdateInfo() {
        getApp().getShopPref().updateUrl().remove();
        getApp().getShopPref().updateRequire().remove();
        getApp().getShopPref().updateTime().remove();
        getApp().getShopPref().updateFilePath().remove();
    }

    private void storeUpdateInfo(APKUpdater result) {
        getApp().setUpdateTime(result.getUpdateTime().getTime());
        getApp().setUpdateURL(result.getUpdateURL());
        getApp().setUpdateRequire(result.getApkUpdateRequire().name());
        getApp().setUpdateApprove(result.isAprove());
    }

    public void checkUpdates(boolean forced) {
        long now = System.currentTimeMillis();
        if (forced || (last_update + customTimer) < now && checkSchedule()) {
            new checkUpdateTask().execute();
            last_update = System.currentTimeMillis();
            getApp().setLastUpdateTime(last_update);

            this.setChanged();
            this.notifyObservers(AUTOUPDATE_CHECKING);
        }
    }

    private TcrApplication getApp() {
        return (TcrApplication) context.getApplicationContext();
    }
}
