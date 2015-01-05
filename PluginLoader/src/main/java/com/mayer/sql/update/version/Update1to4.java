package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by pkabakov on 21.07.2014.
 */
public class Update1to4 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update1to2(db);
        UpdateBlock.update2to3(db);
        UpdateBlock.update3to4(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION4;
    }
}
