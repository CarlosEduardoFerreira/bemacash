package com.kaching123.tcr.store;

import android.content.ContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.store.ShopOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BemaCarl on 04/05/2017.
 */

public class DatabaseUtil extends ShopOpenHelper {

    SQLiteDatabase db;

    public DatabaseUtil(Context context) {
        super(context);
    }

    public boolean clearDataOnTables(){
        db = getWritableDatabase();
        db.beginTransaction();
        try {
            ShopSchemaEx.onDrop(db, false);
            ShopSchemaEx.onCreate(db, false);
            /*
            Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            List<String> tables = new ArrayList<>();

            while (c.moveToNext()) {
                tables.add(c.getString(0));
            }

            for (String table : tables) {
                Log.d("BemaCarl16","DatabaseUtil.clearDataOnTables: cleaning table " + table);
                String dropQuery = "DELETE FROM " + table;
                db.execSQL(dropQuery);
            }
            /**/
            db.setTransactionSuccessful();

            return true;
        } catch (Exception e) {
            Logger.e("ShopOpenHelper.clearDatabaseKeepSync(): failed", e);
        } finally {
            db.endTransaction();
        }
        return false;
    }
}
