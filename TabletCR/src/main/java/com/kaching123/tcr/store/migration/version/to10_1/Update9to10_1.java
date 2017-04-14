package com.kaching123.tcr.store.migration.version.to10_1;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

import static com.kaching123.tcr.store.migration.UpdateBlock.update10to10_1;
import static com.kaching123.tcr.store.migration.UpdateBlock.update9to10;

/**
 * Created by mboychenko on 2/8/2017.
 */

public class Update9to10_1 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update9to10(db);
        update10to10_1(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION9;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION10_1;
    }

}
