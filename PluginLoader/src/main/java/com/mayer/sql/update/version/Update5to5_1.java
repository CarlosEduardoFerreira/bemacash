package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vkompaniets on 10.11.2014.
 */
public class Update5to5_1 implements IUpdateContainer {

    private static final String SQL_RENAME_WIRELESS_ITEM = "ALTER TABLE wireless_item RENAME TO wireless_item_tmp;";
    private static final String SQL_CREATE_WIRELESS_ITEM = "create table wireless_item( _id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT, name TEXT, carrierName TEXT, countryCode TEXT, countryName TEXT, type TEXT, url TEXT, useFixedDenominations INTEGER, denominations TEXT, minDenomination TEXT, maxDenomination TEXT, dialCountryCode TEXT)";
    private static final String SQL_COPY_WIRELESS_ITEM = "INSERT INTO wireless_item(_id, code, name, carrierName, countryCode, countryName, type, url, useFixedDenominations, denominations, minDenomination, maxDenomination, dialCountryCode) SELECT _id, code, name, carrierName, countryCode, countryName, type, url, useFixedDenominations, denominations, minDenomination, maxDenomination, dialCountryCode FROM wireless_item_tmp;";
    private static final String SQL_DROP_TEMP_WIRELESS_ITEM = "DROP TABLE wireless_item_tmp;";

    static void update5to5_1(SQLiteDatabase db){
        db.execSQL(SQL_RENAME_WIRELESS_ITEM);
        db.execSQL(SQL_CREATE_WIRELESS_ITEM);
        db.execSQL(SQL_COPY_WIRELESS_ITEM);
        db.execSQL(SQL_DROP_TEMP_WIRELESS_ITEM);
    }

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update5to5_1(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_1;
    }
}
