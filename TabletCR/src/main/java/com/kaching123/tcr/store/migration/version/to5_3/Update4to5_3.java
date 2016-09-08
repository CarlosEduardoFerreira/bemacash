package com.kaching123.tcr.store.migration.version.to5_3;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;
import com.kaching123.tcr.store.migration.version.to5.Update4to5;
import com.kaching123.tcr.store.migration.version.to5_1.Update4to5_1;
import com.kaching123.tcr.store.migration.version.to5_1.Update5to5_1;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update4to5_3 extends Update4to5_1 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        Update4to5.update4To5(db);
        Update5to5_1.update5to5_1(db);
        UpdateBlock.update5_2to5_3(db);
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_3;
    }
}
