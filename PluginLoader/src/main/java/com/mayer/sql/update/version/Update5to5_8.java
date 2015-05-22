package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

import static com.mayer.sql.update.version.Update5_1to5_2.update5_1to5_2;
import static com.mayer.sql.update.version.Update5to5_1.update5to5_1;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5to5_8 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        update5to5_1(db);
        update5_1to5_2(db);
        UpdateBlock.update5_2to5_3(db);
        UpdateBlock.update5_3to5_4(db);
        UpdateBlock.update5_4to5_5(db);
        UpdateBlock.update5_5to5_6(db);
        UpdateBlock.update5_6to5_7(db);
        UpdateBlock.update5_7to5_8(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_8;
    }
}