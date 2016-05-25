package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

import static com.mayer.sql.update.version.UpdateBlock.update6_4to6_5;
import static com.mayer.sql.update.version.UpdateBlock.update6_5to6_6;
import static com.mayer.sql.update.version.UpdateBlock.update6_6to6_7;

/**
 * Created by Long on 5/25/2016.
 */
public class Update6_6to6_7 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update6_6to6_7(db);
    }



    @Override
    public int getSqlOldVersion() {
        return VERSION6_6;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_7;
    }
}
