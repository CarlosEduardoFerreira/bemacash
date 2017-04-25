package com.kaching123.tcr.store.migration.version.to10_1;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

import static com.kaching123.tcr.store.migration.UpdateBlock.update10to10_1;

/**
 * Created by mboychenko on 4/14/2017.
 */

public class Update10to10_1 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        update10to10_1(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION10;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION10_1;
    }
}
