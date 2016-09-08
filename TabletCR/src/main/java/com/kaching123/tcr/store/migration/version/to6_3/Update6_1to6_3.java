package com.kaching123.tcr.store.migration.version.to6_3;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.version.to6_2.Update6_1to6_2;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update6_1to6_3 implements IUpdateContainer {


    static void update6_1to6_3(SQLiteDatabase db) {
        Update6_1to6_2.update6_1to6_2(db);

        Update6_2to6_3.update6_2to6_3(db);
    }

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update6_1to6_3(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION6_1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_3;
    }
}
