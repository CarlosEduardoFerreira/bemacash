package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_4to5_7 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update5_4to5_5(db);
        UpdateBlock.update5_5to5_6(db);
        UpdateBlock.update5_6to5_7(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_4;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_7;
    }
}
