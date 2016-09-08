package com.kaching123.tcr.store.migration.version.to4_2;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;

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
