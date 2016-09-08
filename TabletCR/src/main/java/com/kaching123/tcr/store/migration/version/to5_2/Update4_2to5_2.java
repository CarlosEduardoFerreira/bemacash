package com.kaching123.tcr.store.migration.version.to5_2;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.version.to5_1.Update4_2to5_1;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update4_2to5_2 extends Update4_2to5_1 implements IUpdateContainer {

    @Override
    public int getSqlNewVersion() {
        return VERSION5_2;
    }
}
