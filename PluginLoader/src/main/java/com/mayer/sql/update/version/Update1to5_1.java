package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

import static com.mayer.sql.update.version.Update4to5.update4To5;
import static com.mayer.sql.update.version.Update5to5_1.update5to5_1;

/**
 * Created by vkompaniets on 10.11.2014.
 */
public class Update1to5_1 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update1to2(db);
        UpdateBlock.update2to3(db);
        UpdateBlock.update3to4(db);
        update4To5(db);
        update5to5_1(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_1;
    }
}
