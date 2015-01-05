package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

import static com.mayer.sql.update.version.Update4to5.update4To5;
import static com.mayer.sql.update.version.Update5to5_1.update5to5_1;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update3to5_3 extends Update3to5_1 implements IUpdateContainer {
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update3to4(db);
        update4To5(db);
        update5to5_1(db);
        UpdateBlock.update5_2to5_3(db);
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_3;
    }
}
