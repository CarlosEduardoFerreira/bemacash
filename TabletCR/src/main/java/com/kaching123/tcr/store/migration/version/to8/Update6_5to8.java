package com.kaching123.tcr.store.migration.version.to8;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

import static com.kaching123.tcr.store.migration.UpdateBlock.update6_5to6_6;
import static com.kaching123.tcr.store.migration.UpdateBlock.update6_6to6_7;
import static com.kaching123.tcr.store.migration.UpdateBlock.update6_7to7;
import static com.kaching123.tcr.store.migration.UpdateBlock.update7to8;

/**
 * Created by vkompaniets on 08.09.2016.
 */
public class Update6_5to8 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        update6_5to6_6(db);
        update6_6to6_7(db);
        update6_7to7(db);
        update7to8(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION6_5;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION8;
    }
}
