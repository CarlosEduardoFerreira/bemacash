package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dot on 24.06.2014.
 */
public class Update3to4 implements IUpdateContainer {


    @Override
    public void onUpdate(SQLiteDatabase database) {
        UpdateBlock.update3to4(database);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION3;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION4;
    }
}
