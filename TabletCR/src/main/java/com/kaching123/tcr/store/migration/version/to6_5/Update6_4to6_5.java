package com.kaching123.tcr.store.migration.version.to6_5;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

import static com.kaching123.tcr.store.migration.UpdateBlock.update6_4to6_5;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update6_4to6_5 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update6_4to6_5(db);
    }



    @Override
    public int getSqlOldVersion() {
        return VERSION6_4;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_5;
    }
}
