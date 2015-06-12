package com.mayer.sql.update;

import com.mayer.sql.update.version.*;

public class SqlUpdateVersionMatcher {

    private SqlUpdateVersionMatcher() {

    }

    public static SqlUpdateVersionMatcher get() {
        return new SqlUpdateVersionMatcher();
    }

    public IUpdateContainer getUpdater(int oldVersion, int newVersion) {
        for (VERSION version : VERSION.values()) {
            if (version.container.getSqlOldVersion() == oldVersion && version.container.getSqlNewVersion() == newVersion) {
                return version.container;
            }
        }

//        return null;
        throw new UnsupportedOperationException("This update not supported. oldVersion: " + oldVersion + " newVersion: " + newVersion);
    }

    private enum VERSION {

        UPDATE1TO2(new Update1to2()),
        UPDATE1TO3(new Update1to3()),
        UPDATE1TO4(new Update1to4()),
        UPDATE1TO4_1(new Update1to4_1()),
        UPDATE1TO4_2(new Update1to4_2()),
        UPDATE1TO5(new Update1to5()),
        UPDATE1TO5_1(new Update1to5_1()),
        UPDATE1TO5_2(new Update1to5_2()),
        UPDATE1TO5_3(new Update1to5_3()),
        UPDATE1TO5_4(new Update1to5_4()),
        UPDATE1TO5_5(new Update1to5_5()),
        UPDATE1TO5_6(new Update1to5_6()),
        UPDATE1TO5_7(new Update1to5_7()),
        UPDATE1TO5_8(new Update1to5_8()),
        UPDATE1TO5_9(new Update1to5_9()),
        UPDATE1TO6_1(new Update1to6_1()),

        UPDATE2TO3(new Update2to3()),
        UPDATE2TO4(new Update2to4()),
        UPDATE2TO4_1(new Update2to4_1()),
        UPDATE2TO4_2(new Update2to4_2()),
        UPDATE2TO5(new Update2to5()),
        UPDATE2TO5_1(new Update2to5_1()),
        UPDATE2TO5_2(new Update2to5_2()),
        UPDATE2TO5_3(new Update2to5_3()),
        UPDATE2TO5_4(new Update2to5_4()),
        UPDATE2TO5_5(new Update2to5_5()),
        UPDATE2TO5_6(new Update2to5_6()),
        UPDATE2TO5_7(new Update2to5_7()),
        UPDATE2TO5_8(new Update2to5_8()),
        UPDATE2TO5_9(new Update2to5_9()),
        UPDATE2TO6_1(new Update2to6_1()),

        UPDATE3TO4(new Update3to4()),
        UPDATE3TO4_1(new Update3to4_1()),
        UPDATE3TO4_2(new Update3to4_2()),
        UPDATE3TO5(new Update3to5()),
        UPDATE3TO5_1(new Update3to5_1()),
        UPDATE3TO5_2(new Update3to5_2()),
        UPDATE3TO5_3(new Update3to5_3()),
        UPDATE3TO5_4(new Update3to5_4()),
        UPDATE3TO5_5(new Update3to5_5()),
        UPDATE3TO5_6(new Update3to5_6()),
        UPDATE3TO5_7(new Update3to5_7()),
        UPDATE3TO5_8(new Update3to5_8()),
        UPDATE3TO5_9(new Update3to5_9()),
        UPDATE3TO6_1(new Update3to6_1()),

        UPDATE4TO4_1(new Update4to4_1()),
        UPDATE4TO4_2(new Update4to4_2()),
        UPDATE4TO5(new Update4to5()),
        UPDATE4TO5_1(new Update4to5_1()),
        UPDATE4TO5_2(new Update4to5_2()),
        UPDATE4TO5_3(new Update4to5_3()),
        UPDATE4TO5_4(new Update4to5_4()),
        UPDATE4TO5_5(new Update4to5_5()),
        UPDATE4TO5_6(new Update4to5_6()),
        UPDATE4TO5_7(new Update4to5_7()),
        UPDATE4TO5_8(new Update4to5_8()),
        UPDATE4TO5_9(new Update4to5_9()),
        UPDATE4TO6_1(new Update4to6_1()),

        UPDATE4_1TO4_2(new Update4_1to4_2()),
        UPDATE4_1TO5(new Update4_1to5()),
        UPDATE4_1TO5_1(new Update4_1to5_1()),
        UPDATE4_1TO5_2(new Update4_1to5_2()),
        UPDATE4_1TO5_3(new Update4_1to5_3()),
        UPDATE4_1TO5_4(new Update4_1to5_4()),
        UPDATE4_1TO5_5(new Update4_1to5_5()),
        UPDATE4_1TO5_6(new Update4_1to5_6()),
        UPDATE4_1TO5_7(new Update4_1to5_7()),
        UPDATE4_1TO5_8(new Update4_1to5_8()),
        UPDATE4_1TO5_9(new Update4_1to5_9()),
        UPDATE4_1TO6_1(new Update4_1to6_1()),

        UPDATE4_2TO5(new Update4_2to5()),
        UPDATE4_2TO5_1(new Update4_2to5_1()),
        UPDATE4_2TO5_2(new Update4_2to5_2()),
        UPDATE4_2TO5_3(new Update4_2to5_3()),
        UPDATE4_2TO5_4(new Update4_2to5_4()),
        UPDATE4_2TO5_5(new Update4_2to5_5()),
        UPDATE4_2TO5_6(new Update4_2to5_6()),
        UPDATE4_2TO5_7(new Update4_2to5_7()),
        UPDATE4_2TO5_8(new Update4_2to5_8()),
        UPDATE4_2TO5_9(new Update4_2to5_9()),
        UPDATE4_2TO6_1(new Update4_2to6_1()),

        UPDATE5TO5_1(new Update5to5_1()),
        UPDATE5TO5_2(new Update5to5_2()),
        UPDATE5TO5_3(new Update5to5_3()),
        UPDATE5TO5_4(new Update5to5_4()),
        UPDATE5TO5_5(new Update5to5_5()),
        UPDATE5TO5_6(new Update5to5_6()),
        UPDATE5TO5_7(new Update5to5_7()),
        UPDATE5TO5_8(new Update5to5_8()),
        UPDATE5TO5_9(new Update5to5_9()),
        UPDATE5TO6_1(new Update5to6_1()),
        UPDATE5_1TO5_2(new Update5_1to5_2()),
        UPDATE5_1TO5_3(new Update5_1to5_3()),
        UPDATE5_1TO5_4(new Update5_1to5_4()),
        UPDATE5_1TO5_5(new Update5_1to5_5()),
        UPDATE5_1TO5_6(new Update5_1to5_6()),
        UPDATE5_1TO5_7(new Update5_1to5_7()),
        UPDATE5_1TO5_8(new Update5_1to5_8()),
        UPDATE5_1TO5_9(new Update5_1to5_9()),
        UPDATE5_1TO6_1(new Update5_1to6_1()),

        UPDATE5_2TO5_3(new Update5_2to5_3()),
        UPDATE5_2TO5_4(new Update5_2to5_4()),
        UPDATE5_2TO5_5(new Update5_2to5_5()),
        UPDATE5_2TO5_6(new Update5_2to5_6()),
        UPDATE5_2TO5_7(new Update5_2to5_7()),
        UPDATE5_2TO5_8(new Update5_2to5_8()),
        UPDATE5_2TO5_9(new Update5_2to5_9()),
        UPDATE5_2TO6_1(new Update5_2to6_1()),

        UPDATE5_3TO5_4(new Update5_3to5_4()),
        UPDATE5_3TO5_5(new Update5_3to5_5()),
        UPDATE5_3TO5_6(new Update5_3to5_6()),
        UPDATE5_3TO5_7(new Update5_3to5_7()),
        UPDATE5_3TO5_8(new Update5_3to5_8()),
        UPDATE5_3TO5_9(new Update5_3to5_9()),
        UPDATE5_3TO6_1(new Update5_3to6_1()),

        UPDATE5_4TO5_5(new Update5_4to5_5()),
        UPDATE5_4TO5_6(new Update5_4to5_6()),
        UPDATE5_4TO5_7(new Update5_4to5_7()),
        UPDATE5_4TO5_8(new Update5_4to5_8()),
        UPDATE5_4TO5_9(new Update5_4to5_9()),
        UPDATE5_4TO6_1(new Update5_4to6_1()),

        UPDATE5_5TO5_6(new Update5_5to5_6()),
        UPDATE5_5TO5_7(new Update5_5to5_7()),
        UPDATE5_5TO5_8(new Update5_5to5_8()),
        UPDATE5_5TO5_9(new Update5_5to5_9()),
        UPDATE5_5TO6_1(new Update5_5to6_1()),

        UPDATE5_6TO5_7(new Update5_6to5_7()),
        UPDATE5_6TO5_8(new Update5_6to5_8()),
        UPDATE5_6TO5_9(new Update5_6to5_9()),
        UPDATE5_6TO6_1(new Update5_6to6_1()),

        UPDATE5_7TO5_8(new Update5_7to5_8()),
        UPDATE5_7TO5_9(new Update5_7to5_9()),
        UPDATE5_7TO6_1(new Update5_7to6_1()),

        UPDATE5_8TO5_9(new Update5_8to5_9()),
        UPDATE5_8TO6_1(new Update5_8to6_1()),

        UPDATE5_9TO6_1(new Update5_9to6_1());


        private IUpdateContainer container;

        private VERSION(IUpdateContainer container) {
            this.container = container;
        }
    }
}
