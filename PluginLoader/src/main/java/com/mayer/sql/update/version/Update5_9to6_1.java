package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

import static com.mayer.sql.update.version.Update5_1to5_2.update5_1to5_2;
import static com.mayer.sql.update.version.Update5to5_1.update5to5_1;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_9to6_1 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update5_9to6_1(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_9;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_1;
    }
}
