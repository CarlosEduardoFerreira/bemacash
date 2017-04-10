package com.kaching123.tcr.store.migration;

import com.kaching123.tcr.store.migration.version.to10.Update6_4to10;
import com.kaching123.tcr.store.migration.version.to10.Update6_5to10;
import com.kaching123.tcr.store.migration.version.to10.Update6_6to10;
import com.kaching123.tcr.store.migration.version.to10.Update6_7to10;
import com.kaching123.tcr.store.migration.version.to10.Update7to10;
import com.kaching123.tcr.store.migration.version.to10.Update8to10;
import com.kaching123.tcr.store.migration.version.to10.Update9to10;
import com.kaching123.tcr.store.migration.version.to2.Update1to2;
import com.kaching123.tcr.store.migration.version.to3.Update1to3;
import com.kaching123.tcr.store.migration.version.to3.Update2to3;
import com.kaching123.tcr.store.migration.version.to4.Update1to4;
import com.kaching123.tcr.store.migration.version.to4.Update2to4;
import com.kaching123.tcr.store.migration.version.to4.Update3to4;
import com.kaching123.tcr.store.migration.version.to4_1.Update1to4_1;
import com.kaching123.tcr.store.migration.version.to4_1.Update2to4_1;
import com.kaching123.tcr.store.migration.version.to4_1.Update3to4_1;
import com.kaching123.tcr.store.migration.version.to4_1.Update4to4_1;
import com.kaching123.tcr.store.migration.version.to4_2.Update1to4_2;
import com.kaching123.tcr.store.migration.version.to4_2.Update2to4_2;
import com.kaching123.tcr.store.migration.version.to4_2.Update3to4_2;
import com.kaching123.tcr.store.migration.version.to4_2.Update4_1to4_2;
import com.kaching123.tcr.store.migration.version.to4_2.Update4to4_2;
import com.kaching123.tcr.store.migration.version.to5.Update1to5;
import com.kaching123.tcr.store.migration.version.to5.Update2to5;
import com.kaching123.tcr.store.migration.version.to5.Update3to5;
import com.kaching123.tcr.store.migration.version.to5.Update4_1to5;
import com.kaching123.tcr.store.migration.version.to5.Update4_2to5;
import com.kaching123.tcr.store.migration.version.to5.Update4to5;
import com.kaching123.tcr.store.migration.version.to5_1.Update1to5_1;
import com.kaching123.tcr.store.migration.version.to5_1.Update2to5_1;
import com.kaching123.tcr.store.migration.version.to5_1.Update3to5_1;
import com.kaching123.tcr.store.migration.version.to5_1.Update4_1to5_1;
import com.kaching123.tcr.store.migration.version.to5_1.Update4_2to5_1;
import com.kaching123.tcr.store.migration.version.to5_1.Update4to5_1;
import com.kaching123.tcr.store.migration.version.to5_1.Update5to5_1;
import com.kaching123.tcr.store.migration.version.to5_2.Update1to5_2;
import com.kaching123.tcr.store.migration.version.to5_2.Update2to5_2;
import com.kaching123.tcr.store.migration.version.to5_2.Update3to5_2;
import com.kaching123.tcr.store.migration.version.to5_2.Update4_1to5_2;
import com.kaching123.tcr.store.migration.version.to5_2.Update4_2to5_2;
import com.kaching123.tcr.store.migration.version.to5_2.Update4to5_2;
import com.kaching123.tcr.store.migration.version.to5_2.Update5_1to5_2;
import com.kaching123.tcr.store.migration.version.to5_2.Update5to5_2;
import com.kaching123.tcr.store.migration.version.to5_3.Update1to5_3;
import com.kaching123.tcr.store.migration.version.to5_3.Update2to5_3;
import com.kaching123.tcr.store.migration.version.to5_3.Update3to5_3;
import com.kaching123.tcr.store.migration.version.to5_3.Update4_1to5_3;
import com.kaching123.tcr.store.migration.version.to5_3.Update4_2to5_3;
import com.kaching123.tcr.store.migration.version.to5_3.Update4to5_3;
import com.kaching123.tcr.store.migration.version.to5_3.Update5_1to5_3;
import com.kaching123.tcr.store.migration.version.to5_3.Update5_2to5_3;
import com.kaching123.tcr.store.migration.version.to5_3.Update5to5_3;
import com.kaching123.tcr.store.migration.version.to5_4.Update1to5_4;
import com.kaching123.tcr.store.migration.version.to5_4.Update2to5_4;
import com.kaching123.tcr.store.migration.version.to5_4.Update3to5_4;
import com.kaching123.tcr.store.migration.version.to5_4.Update4_1to5_4;
import com.kaching123.tcr.store.migration.version.to5_4.Update4_2to5_4;
import com.kaching123.tcr.store.migration.version.to5_4.Update4to5_4;
import com.kaching123.tcr.store.migration.version.to5_4.Update5_1to5_4;
import com.kaching123.tcr.store.migration.version.to5_4.Update5_2to5_4;
import com.kaching123.tcr.store.migration.version.to5_4.Update5_3to5_4;
import com.kaching123.tcr.store.migration.version.to5_4.Update5to5_4;
import com.kaching123.tcr.store.migration.version.to5_5.Update1to5_5;
import com.kaching123.tcr.store.migration.version.to5_5.Update2to5_5;
import com.kaching123.tcr.store.migration.version.to5_5.Update3to5_5;
import com.kaching123.tcr.store.migration.version.to5_5.Update4_1to5_5;
import com.kaching123.tcr.store.migration.version.to5_5.Update4_2to5_5;
import com.kaching123.tcr.store.migration.version.to5_5.Update4to5_5;
import com.kaching123.tcr.store.migration.version.to5_5.Update5_1to5_5;
import com.kaching123.tcr.store.migration.version.to5_5.Update5_2to5_5;
import com.kaching123.tcr.store.migration.version.to5_5.Update5_3to5_5;
import com.kaching123.tcr.store.migration.version.to5_5.Update5_4to5_5;
import com.kaching123.tcr.store.migration.version.to5_5.Update5to5_5;
import com.kaching123.tcr.store.migration.version.to5_6.Update1to5_6;
import com.kaching123.tcr.store.migration.version.to5_6.Update2to5_6;
import com.kaching123.tcr.store.migration.version.to5_6.Update3to5_6;
import com.kaching123.tcr.store.migration.version.to5_6.Update4_1to5_6;
import com.kaching123.tcr.store.migration.version.to5_6.Update4_2to5_6;
import com.kaching123.tcr.store.migration.version.to5_6.Update4to5_6;
import com.kaching123.tcr.store.migration.version.to5_6.Update5_1to5_6;
import com.kaching123.tcr.store.migration.version.to5_6.Update5_2to5_6;
import com.kaching123.tcr.store.migration.version.to5_6.Update5_3to5_6;
import com.kaching123.tcr.store.migration.version.to5_6.Update5_4to5_6;
import com.kaching123.tcr.store.migration.version.to5_6.Update5_5to5_6;
import com.kaching123.tcr.store.migration.version.to5_6.Update5to5_6;
import com.kaching123.tcr.store.migration.version.to5_7.Update1to5_7;
import com.kaching123.tcr.store.migration.version.to5_7.Update2to5_7;
import com.kaching123.tcr.store.migration.version.to5_7.Update3to5_7;
import com.kaching123.tcr.store.migration.version.to5_7.Update4_1to5_7;
import com.kaching123.tcr.store.migration.version.to5_7.Update4_2to5_7;
import com.kaching123.tcr.store.migration.version.to5_7.Update4to5_7;
import com.kaching123.tcr.store.migration.version.to5_7.Update5_1to5_7;
import com.kaching123.tcr.store.migration.version.to5_7.Update5_2to5_7;
import com.kaching123.tcr.store.migration.version.to5_7.Update5_3to5_7;
import com.kaching123.tcr.store.migration.version.to5_7.Update5_4to5_7;
import com.kaching123.tcr.store.migration.version.to5_7.Update5_5to5_7;
import com.kaching123.tcr.store.migration.version.to5_7.Update5_6to5_7;
import com.kaching123.tcr.store.migration.version.to5_7.Update5to5_7;
import com.kaching123.tcr.store.migration.version.to5_8.Update1to5_8;
import com.kaching123.tcr.store.migration.version.to5_8.Update2to5_8;
import com.kaching123.tcr.store.migration.version.to5_8.Update3to5_8;
import com.kaching123.tcr.store.migration.version.to5_8.Update4_1to5_8;
import com.kaching123.tcr.store.migration.version.to5_8.Update4_2to5_8;
import com.kaching123.tcr.store.migration.version.to5_8.Update4to5_8;
import com.kaching123.tcr.store.migration.version.to5_8.Update5_1to5_8;
import com.kaching123.tcr.store.migration.version.to5_8.Update5_2to5_8;
import com.kaching123.tcr.store.migration.version.to5_8.Update5_3to5_8;
import com.kaching123.tcr.store.migration.version.to5_8.Update5_4to5_8;
import com.kaching123.tcr.store.migration.version.to5_8.Update5_5to5_8;
import com.kaching123.tcr.store.migration.version.to5_8.Update5_6to5_8;
import com.kaching123.tcr.store.migration.version.to5_8.Update5_7to5_8;
import com.kaching123.tcr.store.migration.version.to5_8.Update5to5_8;
import com.kaching123.tcr.store.migration.version.to5_9.Update1to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update2to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update3to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update4_1to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update4_2to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update4to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update5_1to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update5_2to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update5_3to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update5_4to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update5_5to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update5_6to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update5_7to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update5_8to5_9;
import com.kaching123.tcr.store.migration.version.to5_9.Update5to5_9;
import com.kaching123.tcr.store.migration.version.to6_1.Update1to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update2to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update3to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update4_1to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update4_2to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update4to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update5_1to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update5_2to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update5_3to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update5_4to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update5_5to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update5_6to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update5_7to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update5_8to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update5_9to6_1;
import com.kaching123.tcr.store.migration.version.to6_1.Update5to6_1;
import com.kaching123.tcr.store.migration.version.to6_2.Update1to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update2to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update3to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update4_1to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update4_2to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update4to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update5_1to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update5_2to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update5_3to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update5_4to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update5_5to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update5_6to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update5_7to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update5_8to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update5_9to6_2;
import com.kaching123.tcr.store.migration.version.to6_2.Update6_1to6_2;
import com.kaching123.tcr.store.migration.version.to6_3.Update1to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update2to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update3to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update4_1to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update4_2to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update4to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update5_1to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update5_2to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update5_3to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update5_4to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update5_5to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update5_6to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update5_7to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update5_8to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update5_9to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update6_1to6_3;
import com.kaching123.tcr.store.migration.version.to6_3.Update6_2to6_3;
import com.kaching123.tcr.store.migration.version.to6_4.Update5_9to6_4;
import com.kaching123.tcr.store.migration.version.to6_4.Update6_1to6_4;
import com.kaching123.tcr.store.migration.version.to6_4.Update6_2to6_4;
import com.kaching123.tcr.store.migration.version.to6_4.Update6_3to6_4;
import com.kaching123.tcr.store.migration.version.to6_5.Update5_7to6_5;
import com.kaching123.tcr.store.migration.version.to6_5.Update5_8to6_5;
import com.kaching123.tcr.store.migration.version.to6_5.Update5_9to6_5;
import com.kaching123.tcr.store.migration.version.to6_5.Update6_1to6_5;
import com.kaching123.tcr.store.migration.version.to6_5.Update6_2to6_5;
import com.kaching123.tcr.store.migration.version.to6_5.Update6_3to6_5;
import com.kaching123.tcr.store.migration.version.to6_5.Update6_4to6_5;
import com.kaching123.tcr.store.migration.version.to6_6.Update5_7to6_6;
import com.kaching123.tcr.store.migration.version.to6_6.Update5_8to6_6;
import com.kaching123.tcr.store.migration.version.to6_6.Update5_9to6_6;
import com.kaching123.tcr.store.migration.version.to6_6.Update6_1to6_6;
import com.kaching123.tcr.store.migration.version.to6_6.Update6_2to6_6;
import com.kaching123.tcr.store.migration.version.to6_6.Update6_3to6_6;
import com.kaching123.tcr.store.migration.version.to6_6.Update6_4to6_6;
import com.kaching123.tcr.store.migration.version.to6_6.Update6_5to6_6;
import com.kaching123.tcr.store.migration.version.to6_7.Update6_4to6_7;
import com.kaching123.tcr.store.migration.version.to6_7.Update6_6to6_7;
import com.kaching123.tcr.store.migration.version.to7.Update6_4to7;
import com.kaching123.tcr.store.migration.version.to7.Update6_5to7;
import com.kaching123.tcr.store.migration.version.to7.Update6_6to7;
import com.kaching123.tcr.store.migration.version.to7.Update6_7to7;
import com.kaching123.tcr.store.migration.version.to8.Update6_4to8;
import com.kaching123.tcr.store.migration.version.to8.Update6_5to8;
import com.kaching123.tcr.store.migration.version.to8.Update6_6to8;
import com.kaching123.tcr.store.migration.version.to8.Update6_7to8;
import com.kaching123.tcr.store.migration.version.to8.Update7to8;
import com.kaching123.tcr.store.migration.version.to9.Update6_4to9;
import com.kaching123.tcr.store.migration.version.to9.Update6_5to9;
import com.kaching123.tcr.store.migration.version.to9.Update6_6to9;
import com.kaching123.tcr.store.migration.version.to9.Update6_7to9;
import com.kaching123.tcr.store.migration.version.to9.Update7to9;
import com.kaching123.tcr.store.migration.version.to9.Update8to9;

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
        UPDATE1TO6_2(new Update1to6_2()),
        UPDATE1TO6_3(new Update1to6_3()),

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
        UPDATE2TO6_2(new Update2to6_2()),
        UPDATE2TO6_3(new Update2to6_3()),

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
        UPDATE3TO6_2(new Update3to6_2()),
        UPDATE3TO6_3(new Update3to6_3()),

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
        UPDATE4TO6_2(new Update4to6_2()),
        UPDATE4TO6_3(new Update4to6_3()),

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
        UPDATE4_1TO6_2(new Update4_1to6_2()),
        UPDATE4_1TO6_3(new Update4_1to6_3()),

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
        UPDATE4_2TO6_2(new Update4_2to6_2()),
        UPDATE4_2TO6_3(new Update4_2to6_3()),

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
        UPDATE5_1TO6_2(new Update5_1to6_2()),
        UPDATE5_1TO6_3(new Update5_1to6_3()),

        UPDATE5_2TO5_3(new Update5_2to5_3()),
        UPDATE5_2TO5_4(new Update5_2to5_4()),
        UPDATE5_2TO5_5(new Update5_2to5_5()),
        UPDATE5_2TO5_6(new Update5_2to5_6()),
        UPDATE5_2TO5_7(new Update5_2to5_7()),
        UPDATE5_2TO5_8(new Update5_2to5_8()),
        UPDATE5_2TO5_9(new Update5_2to5_9()),
        UPDATE5_2TO6_1(new Update5_2to6_1()),
        UPDATE5_2TO6_2(new Update5_2to6_2()),
        UPDATE5_2TO6_3(new Update5_2to6_3()),

        UPDATE5_3TO5_4(new Update5_3to5_4()),
        UPDATE5_3TO5_5(new Update5_3to5_5()),
        UPDATE5_3TO5_6(new Update5_3to5_6()),
        UPDATE5_3TO5_7(new Update5_3to5_7()),
        UPDATE5_3TO5_8(new Update5_3to5_8()),
        UPDATE5_3TO5_9(new Update5_3to5_9()),
        UPDATE5_3TO6_1(new Update5_3to6_1()),
        UPDATE5_3TO6_2(new Update5_3to6_2()),
        UPDATE5_3TO6_3(new Update5_3to6_3()),

        UPDATE5_4TO5_5(new Update5_4to5_5()),
        UPDATE5_4TO5_6(new Update5_4to5_6()),
        UPDATE5_4TO5_7(new Update5_4to5_7()),
        UPDATE5_4TO5_8(new Update5_4to5_8()),
        UPDATE5_4TO5_9(new Update5_4to5_9()),
        UPDATE5_4TO6_1(new Update5_4to6_1()),
        UPDATE5_4TO6_2(new Update5_4to6_2()),
        UPDATE5_4TO6_3(new Update5_4to6_3()),

        UPDATE5_5TO5_6(new Update5_5to5_6()),
        UPDATE5_5TO5_7(new Update5_5to5_7()),
        UPDATE5_5TO5_8(new Update5_5to5_8()),
        UPDATE5_5TO5_9(new Update5_5to5_9()),
        UPDATE5_5TO6_1(new Update5_5to6_1()),
        UPDATE5_5TO6_2(new Update5_5to6_2()),
        UPDATE5_5TO6_3(new Update5_5to6_3()),

        UPDATE5_6TO5_7(new Update5_6to5_7()),
        UPDATE5_6TO5_8(new Update5_6to5_8()),
        UPDATE5_6TO5_9(new Update5_6to5_9()),
        UPDATE5_6TO6_1(new Update5_6to6_1()),
        UPDATE5_6TO6_2(new Update5_6to6_2()),
        UPDATE5_6TO6_3(new Update5_6to6_3()),

        UPDATE5_7TO5_8(new Update5_7to5_8()),
        UPDATE5_7TO5_9(new Update5_7to5_9()),
        UPDATE5_7TO6_1(new Update5_7to6_1()),
        UPDATE5_7TO6_2(new Update5_7to6_2()),
        UPDATE5_7TO6_3(new Update5_7to6_3()),
        UPDATE5_7TO6_5(new Update5_7to6_5()),

        UPDATE5_8TO5_9(new Update5_8to5_9()),
        UPDATE5_8TO6_1(new Update5_8to6_1()),
        UPDATE5_8TO6_2(new Update5_8to6_2()),
        UPDATE5_8TO6_3(new Update5_8to6_3()),
        UPDATE5_8TO6_5(new Update5_8to6_5()),

        UPDATE5_9TO6_1(new Update5_9to6_1()),
        UPDATE5_9TO6_2(new Update5_9to6_2()),
        UPDATE5_9TO6_3(new Update5_9to6_3()),

        UPDATE6_1TO6_2(new Update6_1to6_2()),
        UPDATE6_1TO6_3(new Update6_1to6_3()),
        UPDATE6_2TO6_3(new Update6_2to6_3()),

        UPDATE5_9TO6_4(new Update5_9to6_4()),
        UPDATE5_9TO6_5(new Update5_9to6_5()),
        UPDATE6_1TO6_4(new Update6_1to6_4()),
        UPDATE6_1TO6_5(new Update6_1to6_5()),
        UPDATE6_2TO6_4(new Update6_2to6_4()),
        UPDATE6_2TO6_5(new Update6_2to6_5()),
        UPDATE6_3TO6_4(new Update6_3to6_4()),
        UPDATE6_3TO6_5(new Update6_3to6_5()),
        UPDATE6_4TO6_5(new Update6_4to6_5()),

        UPDATE5_7TO6_6(new Update5_7to6_6()),
        UPDATE5_8TO6_6(new Update5_8to6_6()),
        UPDATE5_9TO6_6(new Update5_9to6_6()),
        UPDATE6_1TO6_6(new Update6_1to6_6()),
        UPDATE6_2TO6_6(new Update6_2to6_6()),
        UPDATE6_3TO6_6(new Update6_3to6_6()),
        UPDATE6_4TO6_6(new Update6_4to6_6()),
        UPDATE6_5TO6_6(new Update6_5to6_6()),
        UPDATE6_6TO6_7(new Update6_6to6_7()),
        UPDATE6_5TO6_7(new Update6_4to6_7()),
        UPDATE6_4TO6_7(new Update6_4to6_7()),

        UPDATE6_4TO7(new Update6_4to7()),
        UPDATE6_5TO7(new Update6_5to7()),
        UPDATE6_6TO7(new Update6_6to7()),
        UPDATE6_7TO7(new Update6_7to7()),

        UPDATE6_4TO8(new Update6_4to8()),
        UPDATE6_5TO8(new Update6_5to8()),
        UPDATE6_6TO8(new Update6_6to8()),
        UPDATE6_7TO8(new Update6_7to8()),
        UPDATE7TO8(new Update7to8()),

        UPDATE6_4TO9(new Update6_4to9()),
        UPDATE6_5TO9(new Update6_5to9()),
        UPDATE6_6TO9(new Update6_6to9()),
        UPDATE6_7TO9(new Update6_7to9()),
        UPDATE7TO9(new Update7to9()),
        UPDATE8TO9(new Update8to9()),

        UPDATE6_4TO10(new Update6_4to10()),
        UPDATE6_5TO10(new Update6_5to10()),
        UPDATE6_6TO10(new Update6_6to10()),
        UPDATE6_7TO10(new Update6_7to10()),
        UPDATE7TO10(new Update7to10()),
        UPDATE8TO10(new Update8to10()),
        UPDATE9TO10(new Update9to10());

        private IUpdateContainer container;

        private VERSION(IUpdateContainer container) {
            this.container = container;
        }
    }
}
