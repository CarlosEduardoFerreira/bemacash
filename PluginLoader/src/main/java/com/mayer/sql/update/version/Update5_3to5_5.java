package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_3to5_5 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update5_3to5_4(db);
        UpdateBlock.update5_4to5_5(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_3;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_5;
    }
}
