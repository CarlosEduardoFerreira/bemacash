package com.kaching123.tcr.store.migration.version.to6_3;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;
import com.kaching123.tcr.store.migration.version.to6_2.Update6_1to6_2;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_6to6_3 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update5_5to5_6(db);
        UpdateBlock.update5_6to5_7(db);
        UpdateBlock.update5_7to5_8(db);
        UpdateBlock.update5_8to5_9(db);
        UpdateBlock.update5_9to6_1(db);
        Update6_1to6_2.update6_1to6_2(db);
        Update6_2to6_3.update6_2to6_3(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_6;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_3;
    }
}
