package com.kaching123.tcr.store.migration.version.to6_1;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;
import com.kaching123.tcr.store.migration.version.to5_1.Update5to5_1;
import com.kaching123.tcr.store.migration.version.to5_2.Update5_1to5_2;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5to6_1 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        Update5to5_1.update5to5_1(db);
        Update5_1to5_2.update5_1to5_2(db);
        UpdateBlock.update5_2to5_3(db);
        UpdateBlock.update5_3to5_4(db);
        UpdateBlock.update5_4to5_5(db);
        UpdateBlock.update5_5to5_6(db);
        UpdateBlock.update5_6to5_7(db);
        UpdateBlock.update5_7to5_8(db);
        UpdateBlock.update5_8to5_9(db);
        UpdateBlock.update5_9to6_1(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_1;
    }
}
