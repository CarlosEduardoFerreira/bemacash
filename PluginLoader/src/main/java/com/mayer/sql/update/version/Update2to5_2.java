package com.mayer.sql.update.version;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update2to5_2 extends Update2to5_1 implements IUpdateContainer {

    @Override
    public int getSqlNewVersion() {
        return VERSION5_2;
    }
}
