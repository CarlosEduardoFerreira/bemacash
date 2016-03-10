package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

import static com.mayer.sql.update.version.UpdateBlock.update6_3to6_4;
import static com.mayer.sql.update.version.UpdateBlock.update6_4to6_5;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update6_2to6_5 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        Update6_2to6_3.update6_2to6_3(db);
        UpdateBlock.update6_3to6_4(db);
        update6_4to6_5(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION6_2;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_5;
    }
}
