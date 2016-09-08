package com.kaching123.tcr.store.migration.version.to5_5;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_4to5_5 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update5_4to5_5(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_4;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_5;
    }
}
