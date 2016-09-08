package com.kaching123.tcr.store.migration.version.to7;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

import static com.kaching123.tcr.store.migration.UpdateBlock.update6_7to7;

/**
 * Created by vkompaniets on 08.09.2016.
 */
public class Update6_7to7 implements IUpdateContainer{

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update6_7to7(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION6_7;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION7;
    }
}
