package com.kaching123.tcr.store.migration.version.to10;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

import static com.kaching123.tcr.store.migration.UpdateBlock.update6_3to6_4;
import static com.kaching123.tcr.store.migration.UpdateBlock.update6_4to6_5;
import static com.kaching123.tcr.store.migration.UpdateBlock.update6_5to6_6;
import static com.kaching123.tcr.store.migration.UpdateBlock.update6_6to6_7;
import static com.kaching123.tcr.store.migration.UpdateBlock.update6_7to7;
import static com.kaching123.tcr.store.migration.UpdateBlock.update7to8;
import static com.kaching123.tcr.store.migration.UpdateBlock.update8to9;
import static com.kaching123.tcr.store.migration.UpdateBlock.update9to10;
import static com.kaching123.tcr.store.migration.version.to6_2.Update6_1to6_2.update6_1to6_2;
import static com.kaching123.tcr.store.migration.version.to6_3.Update6_2to6_3.update6_2to6_3;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update6_1to10 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update6_1to6_2(db);
        update6_2to6_3(db);
        update6_3to6_4(db);
        update6_4to6_5(db);
        update6_5to6_6(db);
        update6_6to6_7(db);
        update6_7to7(db);
        update7to8(db);
        update8to9(db);
        update9to10(db);
    }



    @Override
    public int getSqlOldVersion() {
        return VERSION6_1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION10;
    }
}
