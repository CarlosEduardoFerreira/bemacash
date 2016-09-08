package com.kaching123.tcr.store.migration.version.to6_5;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.version.to6_2.Update6_1to6_2;
import com.kaching123.tcr.store.migration.version.to6_3.Update6_2to6_3;

import static com.kaching123.tcr.store.migration.UpdateBlock.update6_3to6_4;
import static com.kaching123.tcr.store.migration.UpdateBlock.update6_4to6_5;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update6_1to6_5 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        Update6_1to6_2.update6_1to6_2(db);
        Update6_2to6_3.update6_2to6_3(db);
        update6_3to6_4(db);
        update6_4to6_5(db);
    }



    @Override
    public int getSqlOldVersion() {
        return VERSION6_1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_5;
    }
}
