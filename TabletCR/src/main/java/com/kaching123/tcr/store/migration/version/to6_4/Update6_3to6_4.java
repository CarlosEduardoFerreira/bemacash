package com.kaching123.tcr.store.migration.version.to6_4;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

import static com.kaching123.tcr.store.migration.UpdateBlock.update6_3to6_4;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update6_3to6_4 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update6_3to6_4(db);
    }



    @Override
    public int getSqlOldVersion() {
        return VERSION6_3;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_4;
    }
}
