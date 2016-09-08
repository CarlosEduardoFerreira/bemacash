package com.kaching123.tcr.store.migration.version.to5_4;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;
import com.kaching123.tcr.store.migration.version.to5_1.Update1to5_1;
import com.kaching123.tcr.store.migration.version.to5_1.Update5to5_1;

import static com.kaching123.tcr.store.migration.version.to5.Update4to5.update4To5;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update1to5_4 extends Update1to5_1 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update1to2(db);
        UpdateBlock.update2to3(db);
        UpdateBlock.update3to4(db);
        update4To5(db);
        Update5to5_1.update5to5_1(db);
        UpdateBlock.update5_2to5_3(db);
        UpdateBlock.update5_3to5_4(db);
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_3;
    }
}
