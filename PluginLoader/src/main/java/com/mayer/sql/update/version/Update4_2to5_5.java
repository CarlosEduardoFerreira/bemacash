package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

import static com.mayer.sql.update.version.Update4to5.update4To5;
import static com.mayer.sql.update.version.Update5to5_1.update5to5_1;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update4_2to5_5 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        update4To5(db);
        update5to5_1(db);
        UpdateBlock.update5_2to5_3(db);
        UpdateBlock.update5_3to5_4(db);
        UpdateBlock.update5_4to5_5(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION4_2;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_5;
    }
}
