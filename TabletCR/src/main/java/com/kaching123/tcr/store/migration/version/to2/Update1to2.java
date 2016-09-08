package com.kaching123.tcr.store.migration.version.to2;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;

/**
 * Created by dot on 24.06.2014.
 */
public class Update1to2 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase database) {
        UpdateBlock.update1to2(database);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION2;
    }
}
