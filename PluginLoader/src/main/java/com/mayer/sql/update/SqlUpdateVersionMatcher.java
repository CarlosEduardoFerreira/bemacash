package com.mayer.sql.update;

import com.mayer.sql.update.version.IUpdateContainer;
import com.mayer.sql.update.version.Update1to2;
import com.mayer.sql.update.version.Update1to3;
import com.mayer.sql.update.version.Update1to4;
import com.mayer.sql.update.version.Update1to4_1;
import com.mayer.sql.update.version.Update1to4_2;
import com.mayer.sql.update.version.Update1to5;
import com.mayer.sql.update.version.Update1to5_1;
import com.mayer.sql.update.version.Update1to5_2;
import com.mayer.sql.update.version.Update1to5_3;
import com.mayer.sql.update.version.Update1to5_4;
import com.mayer.sql.update.version.Update1to5_5;
import com.mayer.sql.update.version.Update2to3;
import com.mayer.sql.update.version.Update2to4;
import com.mayer.sql.update.version.Update2to4_1;
import com.mayer.sql.update.version.Update2to4_2;
import com.mayer.sql.update.version.Update2to5;
import com.mayer.sql.update.version.Update2to5_1;
import com.mayer.sql.update.version.Update2to5_2;
import com.mayer.sql.update.version.Update2to5_3;
import com.mayer.sql.update.version.Update2to5_4;
import com.mayer.sql.update.version.Update2to5_5;
import com.mayer.sql.update.version.Update3to4;
import com.mayer.sql.update.version.Update3to4_1;
import com.mayer.sql.update.version.Update3to4_2;
import com.mayer.sql.update.version.Update3to5;
import com.mayer.sql.update.version.Update3to5_1;
import com.mayer.sql.update.version.Update3to5_2;
import com.mayer.sql.update.version.Update3to5_3;
import com.mayer.sql.update.version.Update3to5_4;
import com.mayer.sql.update.version.Update3to5_5;
import com.mayer.sql.update.version.Update4_1to4_2;
import com.mayer.sql.update.version.Update4_1to5;
import com.mayer.sql.update.version.Update4_1to5_1;
import com.mayer.sql.update.version.Update4_1to5_2;
import com.mayer.sql.update.version.Update4_1to5_3;
import com.mayer.sql.update.version.Update4_1to5_4;
import com.mayer.sql.update.version.Update4_1to5_5;
import com.mayer.sql.update.version.Update4_2to5;
import com.mayer.sql.update.version.Update4_2to5_1;
import com.mayer.sql.update.version.Update4_2to5_2;
import com.mayer.sql.update.version.Update4_2to5_3;
import com.mayer.sql.update.version.Update4_2to5_4;
import com.mayer.sql.update.version.Update4_2to5_5;
import com.mayer.sql.update.version.Update4to4_1;
import com.mayer.sql.update.version.Update4to4_2;
import com.mayer.sql.update.version.Update4to5;
import com.mayer.sql.update.version.Update4to5_1;
import com.mayer.sql.update.version.Update4to5_2;
import com.mayer.sql.update.version.Update4to5_3;
import com.mayer.sql.update.version.Update4to5_4;
import com.mayer.sql.update.version.Update4to5_5;
import com.mayer.sql.update.version.Update5_1to5_2;
import com.mayer.sql.update.version.Update5_1to5_3;
import com.mayer.sql.update.version.Update5_1to5_4;
import com.mayer.sql.update.version.Update5_1to5_5;
import com.mayer.sql.update.version.Update5_2to5_3;
import com.mayer.sql.update.version.Update5_2to5_4;
import com.mayer.sql.update.version.Update5_2to5_5;
import com.mayer.sql.update.version.Update5_3to5_4;
import com.mayer.sql.update.version.Update5_3to5_5;
import com.mayer.sql.update.version.Update5_4to5_5;
import com.mayer.sql.update.version.Update5to5_1;
import com.mayer.sql.update.version.Update5to5_2;
import com.mayer.sql.update.version.Update5to5_3;
import com.mayer.sql.update.version.Update5to5_4;
import com.mayer.sql.update.version.Update5to5_5;

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
        UPDATE3TO4(new Update3to4()),
        UPDATE3TO4_1(new Update3to4_1()),
        UPDATE3TO4_2(new Update3to4_2()),
        UPDATE3TO5(new Update3to5()),
        UPDATE3TO5_1(new Update3to5_1()),
        UPDATE3TO5_2(new Update3to5_2()),
        UPDATE3TO5_3(new Update3to5_3()),
        UPDATE3TO5_4(new Update3to5_4()),
        UPDATE3TO5_5(new Update3to5_5()),
        UPDATE4TO4_1(new Update4to4_1()),
        UPDATE4TO4_2(new Update4to4_2()),
        UPDATE4TO5(new Update4to5()),
        UPDATE4TO5_1(new Update4to5_1()),
        UPDATE4TO5_2(new Update4to5_2()),
        UPDATE4TO5_3(new Update4to5_3()),
        UPDATE4TO5_4(new Update4to5_4()),
        UPDATE4TO5_5(new Update4to5_5()),
        UPDATE4_1TO4_2(new Update4_1to4_2()),
        UPDATE4_1TO5(new Update4_1to5()),
        UPDATE4_1TO5_1(new Update4_1to5_1()),
        UPDATE4_1TO5_2(new Update4_1to5_2()),
        UPDATE4_1TO5_3(new Update4_1to5_3()),
        UPDATE4_1TO5_4(new Update4_1to5_4()),
        UPDATE4_1TO5_5(new Update4_1to5_5()),
        UPDATE4_2TO5(new Update4_2to5()),
        UPDATE4_2TO5_1(new Update4_2to5_1()),
        UPDATE4_2TO5_2(new Update4_2to5_2()),
        UPDATE4_2TO5_3(new Update4_2to5_3()),
        UPDATE4_2TO5_4(new Update4_2to5_4()),
        UPDATE4_2TO5_5(new Update4_2to5_5()),
        UPDATE5TO5_1(new Update5to5_1()),
        UPDATE5TO5_2(new Update5to5_2()),
        UPDATE5TO5_3(new Update5to5_3()),
        UPDATE5TO5_4(new Update5to5_4()),
        UPDATE5TO5_5(new Update5to5_5()),
        UPDATE5_1TO5_2(new Update5_1to5_2()),
        UPDATE5_1TO5_3(new Update5_1to5_3()),
        UPDATE5_1TO5_4(new Update5_1to5_4()),
        UPDATE5_1TO5_5(new Update5_1to5_5()),
        UPDATE5_2TO5_3(new Update5_2to5_3()),
        UPDATE5_2TO5_4(new Update5_2to5_4()),
        UPDATE5_2TO5_5(new Update5_2to5_5()),
        UPDATE5_3TO5_4(new Update5_3to5_4()),
        UPDATE5_3TO5_5(new Update5_3to5_5()),
        UPDATE5_4TO5_5(new Update5_4to5_5());


        private IUpdateContainer container;

        private VERSION(IUpdateContainer container) {
            this.container = container;
        }
    }
}
