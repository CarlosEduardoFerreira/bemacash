package com.kaching123.tcr.store.migration.version.to10_1;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;

import static com.kaching123.tcr.store.migration.UpdateBlock.update10to10_1;
import static com.kaching123.tcr.store.migration.UpdateBlock.update8to9;
import static com.kaching123.tcr.store.migration.UpdateBlock.update9to10;

/**
 * Created by mboychenko on 2/8/2017.
 */

public class Update8to10_1 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db){
        update8to9(db);
        update9to10(db);
        update10to10_1(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION8;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION10_1;
    }

}
