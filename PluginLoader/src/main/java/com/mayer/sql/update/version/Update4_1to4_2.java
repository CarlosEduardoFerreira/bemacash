package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by pkabakov on 08.09.2014.
 */
public class Update4_1to4_2 implements IUpdateContainer {
    
    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update4_1to4_2(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION4_1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION4_2;
    }
}
