package com.kaching123.tcr.store.migration.version.to8;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

import static com.kaching123.tcr.store.migration.UpdateBlock.update7to8;

/**
 * Created by alboyko on 13.10.2016.
 */

public class Update7to8 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update7to8(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION7;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION8;
    }

}
