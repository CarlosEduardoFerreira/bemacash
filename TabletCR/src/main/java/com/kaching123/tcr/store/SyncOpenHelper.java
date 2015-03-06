package com.kaching123.tcr.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;

/**
 * Created by pkabakov on 25.09.2014.
 */
public class SyncOpenHelper extends BaseOpenHelper {

    //TODO: check db lifecycle

    private static final String DB_NAME_PREFIX = "sync_";

    protected static String getDbName() {
        return DB_NAME_PREFIX + BaseOpenHelper.getDbName();
    }


    public SyncOpenHelper(Context context) {
        super(context, getDbName(), null, getDbVersion());
    }

    @Override
    protected boolean isWriteAheadLoggingEnabled() {
        return false;
    }

    @Override
    protected boolean isForeignKeysEnabled() {
        return false;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        upgradeDrop(db);
    }

    @Override
    protected void clearDbRelatedPreferences() {
        //do nothing
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ShopSchemaEx.onCreate(db, true, true);
    }

    public synchronized boolean insert(String tableName, ContentValues[] valuesArray) {
        if (TextUtils.isEmpty(tableName) || valuesArray == null || valuesArray.length == 0)
            return false;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (ContentValues values : valuesArray) {
                ContentValues contentValue = hotFixForTips(tableName, values);
                long id = db.insertWithOnConflict(tableName, null, contentValue, SQLiteDatabase.CONFLICT_REPLACE);
                if (id == -1L) {
                    Logger.e("SyncOpenHelper.insert(): can't insert values: " + contentValue + "; tableName: " + tableName);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Logger.e("SyncOpenHelper.insert(): tableName: " + tableName + " error", e);
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return false;
    }

    private ContentValues hotFixForTips(String tableName, ContentValues value) {
        ContentValues contentValue = value;
        if (tableName.equalsIgnoreCase("employee_tips"))
            if (value.containsKey("employee_id") && value.get("employee_id").toString().equalsIgnoreCase("0")) {
                String temp = null;
                contentValue.put("employee_id", temp);
            }
        return contentValue;
    }

    public synchronized Cursor getMaxUpdateTime(String[] selectionArgs) {
        return ProviderQueryHelper.getMaxUpdateTime(this, selectionArgs);
    }

    public synchronized Cursor getMaxUpdateParentTime(String[] selectionArgs) {
        return ProviderQueryHelper.getMaxUpdateParentTime(this, selectionArgs);
    }
}
