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

    private static final String DB_NAME_PREFIX = "sync_";

    private static final String SQL_CLEAR_TABLE = "DELETE FROM %s;";

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

    public synchronized void beginTransaction() {
        getWritableDatabase().beginTransaction();
    }

    public synchronized void setTransactionSuccessful() {
        getWritableDatabase().setTransactionSuccessful();
    }

    public synchronized void endTransaction() {
        getWritableDatabase().endTransaction();
    }

    public synchronized boolean insert(String tableName, ContentValues[] valuesArray) {
        return insert(tableName, valuesArray, true);
    }

    public synchronized boolean insert(String tableName, ContentValues[] valuesArray, boolean inTransaction) {
        if (TextUtils.isEmpty(tableName) || valuesArray == null || valuesArray.length == 0)
            return false;

        SQLiteDatabase db = getWritableDatabase();
        if (inTransaction)
            db.beginTransaction();
        try {
            for (ContentValues values : valuesArray) {
                long id = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (id == -1L) {
                    Logger.e("SyncOpenHelper.insert(): can't insert values: " + values + "; tableName: " + tableName);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Logger.e("SyncOpenHelper.insert(): tableName: " + tableName + " error", e);
        } finally {
            if (inTransaction) {
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        }
        return false;
    }

    public synchronized void clearTable(String tableName) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(String.format(SQL_CLEAR_TABLE, tableName));
    }

    public synchronized Cursor getMaxUpdateTime(String[] selectionArgs) {
        return ProviderQueryHelper.getMaxUpdateTime(this, selectionArgs);
    }

    public synchronized Cursor getMaxUpdateParentTime(String[] selectionArgs) {
        return ProviderQueryHelper.getMaxUpdateParentTime(this, selectionArgs);
    }
}
