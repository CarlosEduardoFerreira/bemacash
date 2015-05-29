package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_7to5_9 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update5_7to5_8(db);
        UpdateBlock.update5_8to5_9(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_7;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_9;
    }
}
