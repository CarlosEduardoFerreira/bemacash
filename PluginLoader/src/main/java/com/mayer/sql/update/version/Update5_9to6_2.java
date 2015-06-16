package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_9to6_2 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update5_9to6_1(db);
        Update6_1to6_2.update6_1to6_2(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_9;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_2;
    }
}
