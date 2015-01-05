package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

import static com.mayer.sql.update.version.Update4to5.update4To5;

/**
 * Created by pkabakov on 07.08.2014.
 */
public class Update2to5 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update2to3(db);
        UpdateBlock.update3to4(db);
        update4To5(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION2;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5;
    }
}