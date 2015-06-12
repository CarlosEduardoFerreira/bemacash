package com.kaching123.tcr;

import android.content.ContentValues;

import com.kaching123.tcr.model.APKUpdateRequire;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;

import org.json.JSONException;

import java.util.Date;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by teli on 6/2/2015.
 */
public class APKupdaterJDBCConverter {
    private static final String TABLE_NAME = "APK_UPDATE";

    private static final String GUID = "ID";
    private static final String UPDATE_SCHEDULE_TIME = "SCHEDULE_TIME";
    private static final String UPDATE_URL = "URL";
    private static final String UPDATE_PRIORITY = "PRIORITY";
    private static final String UPDATE_VERSION = "VERSION";
    private static final String UPDATE_APROVE = "APROVE";

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static APKUpdater read(JdbcJSONObject rs) throws JSONException {
        return new APKUpdater(
                rs.getString(GUID),
                rs.getDate(UPDATE_SCHEDULE_TIME),
                rs.getString(UPDATE_URL),
                _enum(APKUpdateRequire.class, rs.getString(UPDATE_PRIORITY), APKUpdateRequire.MINOR),
                rs.getString(UPDATE_VERSION),
                rs.getInt(UPDATE_APROVE) == 1
        );
    }

    public static String getGuidColumn() {
        return GUID;
    }

    public static class APKUpdater {
        public Date getUpdateTime() {
            return updateTime;
        }

        public String getUpdateURL() {
            return updateURL;
        }

        public APKUpdateRequire getApkUpdateRequire() {
            return apkUpdateRequire;
        }

        Date updateTime;

        public void setUpdateURL(String updateURL) {
            this.updateURL = updateURL;
        }

        String updateURL;
        APKUpdateRequire apkUpdateRequire;

        public String getUpdateBuildNumber() {
            return updateBuildNumber;
        }

        String updateBuildNumber;

        public String getGuid() {
            return guid;
        }

        public final String guid;

        public boolean isAprove() {
            return aprove;
        }

        public final boolean aprove;

        public APKUpdater(String guid, Date updateTime, String updateURL, APKUpdateRequire apkUpdateRequire, String updateBuildNumber, boolean aprove) {
            this.guid = guid;
            this.updateTime = updateTime;
            this.updateURL = updateURL;
            this.apkUpdateRequire = apkUpdateRequire;
            this.updateBuildNumber = updateBuildNumber;
            this.aprove = aprove;
        }

        public ContentValues toValues() {
            ContentValues values = new ContentValues();

            values.put(ShopStore.ApkUpdate.GUID, guid);
            values.put(ShopStore.ApkUpdate.URL, updateURL);
            values.put(ShopStore.ApkUpdate.APROVE, aprove);
            values.put(ShopStore.ApkUpdate.PRIORITY, apkUpdateRequire.name());
            values.put(ShopStore.ApkUpdate.VERSION, updateBuildNumber);
            values.put(ShopStore.ApkUpdate.SCHEDULE_TIME, updateTime.getTime());
            return values;
        }
    }
}
