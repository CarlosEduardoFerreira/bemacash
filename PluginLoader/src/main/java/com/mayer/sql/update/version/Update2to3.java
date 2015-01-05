package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dot on 24.06.2014.
 */
public class Update2to3 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase database) {
        UpdateBlock.update2to3(database);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION2;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION3;
    }
}
