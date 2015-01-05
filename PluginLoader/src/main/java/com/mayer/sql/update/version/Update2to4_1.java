package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vkompaniets on 13.08.2014.
 */
public class Update2to4_1 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update2to3(db);
        UpdateBlock.update3to4(db);
        UpdateBlock.update4to4_1(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION2;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION4_1;
    }
}