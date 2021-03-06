package com.kaching123.tcr.store.migration.version.to5_4;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_2to5_4 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update5_2to5_3(db);
        UpdateBlock.update5_3to5_4(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_2;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_4;
    }
}
