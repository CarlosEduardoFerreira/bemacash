package com.kaching123.tcr.store.migration.version.to5;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;

/**
 * Created by pkabakov on 07.08.2014.
 */
public class Update3to5 implements IUpdateContainer {

    @Override
    public void onUpdate(SQLiteDatabase db) {
        UpdateBlock.update3to4(db);
        Update4to5.update4To5(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION3;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5;
    }
}
