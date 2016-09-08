package com.kaching123.tcr.store.migration.version.to3;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;

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
