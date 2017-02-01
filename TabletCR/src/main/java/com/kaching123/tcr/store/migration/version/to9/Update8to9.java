package com.kaching123.tcr.store.migration.version.to9;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

import static com.kaching123.tcr.store.migration.UpdateBlock.update8to9;

/**
 * Created by alboyko on 13.10.2016.
 */

public class Update8to9 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update8to9(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION8;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION9;
    }
}
