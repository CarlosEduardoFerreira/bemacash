package com.kaching123.tcr.store.migration.version.to5_3;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;
import com.kaching123.tcr.store.migration.version.to5_1.Update5to5_1;
import com.kaching123.tcr.store.migration.version.to5_2.Update5_1to5_2;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5to5_3 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        Update5to5_1.update5to5_1(db);
        Update5_1to5_2.update5_1to5_2(db);
        UpdateBlock.update5_2to5_3(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_3;
    }
}
