package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

import static com.mayer.sql.update.version.UpdateBlock.update6_3to6_4;
import static com.mayer.sql.update.version.UpdateBlock.update6_4to6_5;
import static com.mayer.sql.update.version.UpdateBlock.update6_5to6_6;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update6_1to6_6 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        Update6_1to6_2.update6_1to6_2(db);
        Update6_2to6_3.update6_2to6_3(db);
        update6_3to6_4(db);
        update6_4to6_5(db);
        update6_5to6_6(db);
    }



    @Override
    public int getSqlOldVersion() {
        return VERSION6_1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_6;
    }
}
