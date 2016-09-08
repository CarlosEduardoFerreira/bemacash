package com.kaching123.tcr.store.migration.version.to6_1;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_9to6_1 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update5_9to6_1(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_9;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_1;
    }
}
