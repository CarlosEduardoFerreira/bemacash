package com.kaching123.tcr.store.migration.version.to6_4;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;
import com.kaching123.tcr.store.migration.version.to5_1.Update5to5_1;
import com.kaching123.tcr.store.migration.version.to5_2.Update5_1to5_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update6_1to6_2;
import com.kaching123.tcr.store.migration.version.to6_3.Update6_2to6_3;

import static com.kaching123.tcr.store.migration.UpdateBlock.update6_3to6_4;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5to6_4 implements IUpdateContainer {
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
        Update6_1to6_2.update6_1to6_2(db);
        Update6_2to6_3.update6_2to6_3(db);
        update6_3to6_4(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_4;
    }
}
