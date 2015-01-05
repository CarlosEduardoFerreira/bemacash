package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vkompaniets on 13.08.2014.
 */
public class Update1to4_2 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update1to2(db);
        UpdateBlock.update2to3(db);
        UpdateBlock.update3to4(db);
        UpdateBlock.update4to4_1(db);
        UpdateBlock.update4_1to4_2(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION4_2;
    }
}
