package com.kaching123.tcr.store.migration.version.to5_6;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_5to5_6 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update5_5to5_6(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_5;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_6;
    }
}
