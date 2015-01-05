package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

import static com.mayer.sql.update.version.Update4to5.update4To5;

/**
 * Created by pkabakov on 08.09.2014.
 */
public class Update4_2to5 implements IUpdateContainer {
    
    @Override
    public void onUpdate(SQLiteDatabase db) {
        update4To5(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION4_2;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5;
    }
}
