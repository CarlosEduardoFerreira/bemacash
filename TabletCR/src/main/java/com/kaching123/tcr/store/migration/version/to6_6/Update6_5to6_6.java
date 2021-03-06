package com.kaching123.tcr.store.migration.version.to6_6;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

import static com.kaching123.tcr.store.migration.UpdateBlock.update6_4to6_5;
import static com.kaching123.tcr.store.migration.UpdateBlock.update6_5to6_6;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update6_5to6_6 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update6_4to6_5(db);
        update6_5to6_6(db);
    }



    @Override
    public int getSqlOldVersion() {
        return VERSION6_5;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_6;
    }
}
