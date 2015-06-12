package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_8to6_1 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update5_8to5_9(db);
        UpdateBlock.update5_9to6_1(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_8;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_1;
    }
}
