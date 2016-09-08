package com.kaching123.tcr.store.migration.version.to5_7;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;
import com.kaching123.tcr.store.migration.version.to5_1.Update4to5_1;
import com.kaching123.tcr.store.migration.version.to5_1.Update5to5_1;

import static com.kaching123.tcr.store.migration.version.to5.Update4to5.update4To5;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update4to5_7 extends Update4to5_1 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        update4To5(db);
        Update5to5_1.update5to5_1(db);
        UpdateBlock.update5_2to5_3(db);
        UpdateBlock.update5_3to5_4(db);
        UpdateBlock.update5_4to5_5(db);
        UpdateBlock.update5_5to5_6(db);
        UpdateBlock.update5_6to5_7(db);
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_7;
    }
}
