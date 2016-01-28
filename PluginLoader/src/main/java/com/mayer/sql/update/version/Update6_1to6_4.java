package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

import static com.mayer.sql.update.version.UpdateBlock.update6_3to6_4;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update6_1to6_4 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        Update6_1to6_2.update6_1to6_2(db);
        Update6_2to6_3.update6_2to6_3(db);
        update6_3to6_4(db);
    }



    @Override
    public int getSqlOldVersion() {
        return VERSION6_1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_4;
    }
}
