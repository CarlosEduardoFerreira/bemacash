package com.kaching123.tcr.store.migration.version.to5;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

/**
 * Created by pkabakov on 08.09.2014.
 */
public class Update4_2to5 implements IUpdateContainer {
    
    @Override
    public void onUpdate(SQLiteDatabase db) {
        Update4to5.update4To5(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION4_2;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5;
    }
}
