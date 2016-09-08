package com.kaching123.tcr.store.migration.version.to5_1;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

import static com.kaching123.tcr.store.migration.version.to5.Update4to5.update4To5;

/**
 * Created by vkompaniets on 10.11.2014.
 */
public class Update4_2to5_1 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        update4To5(db);
        Update5to5_1.update5to5_1(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION4_2;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_1;
    }
}
