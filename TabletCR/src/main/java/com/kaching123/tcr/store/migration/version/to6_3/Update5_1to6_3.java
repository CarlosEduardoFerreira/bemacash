package com.kaching123.tcr.store.migration.version.to6_3;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;
import com.kaching123.tcr.store.migration.version.to6_2.Update6_1to6_2;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_1to6_3 implements IUpdateContainer {

    private static final String SQL_DELETE_PRINTER_SAME_MAC_ALIAS =
            "delete from printer_able " +
                    "where rowid not in (" +
                    "select  " +
                    "max(rowid) " +
                    "from " +
                    "printer_able " +
                    "group by " +
                    "mac, alias_guid" +
                    ")";

    private static final String SQL_DELETE_PRINTER_SAME_IP_ALIAS =
            "delete from printer_able " +
                    "where rowid not in (" +
                    "select  " +
                    "max(rowid) " +
                    "from " +
                    "printer_able " +
                    "group by " +
                    "ip, alias_guid" +
                    ")";

    private static final String SQl_DELETE_PAX_EXCEPT_LAST =
            "delete from pax_table " +
                    "where rowid < (" +
                    "select " +
                    "max(rowid) " +
                    "from " +
                    "pax_table" +
                    ")";


    static void update5_1to5_2(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_PRINTER_SAME_MAC_ALIAS);
        db.execSQL(SQL_DELETE_PRINTER_SAME_IP_ALIAS);
        db.execSQL(SQl_DELETE_PAX_EXCEPT_LAST);
    }

    @Override
    public void onUpdate(SQLiteDatabase db) {
        update5_1to5_2(db);
        UpdateBlock.update5_2to5_3(db);
        UpdateBlock.update5_3to5_4(db);
        UpdateBlock.update5_4to5_5(db);
        UpdateBlock.update5_5to5_6(db);
        UpdateBlock.update5_6to5_7(db);
        UpdateBlock.update5_7to5_8(db);
        UpdateBlock.update5_8to5_9(db);
        Update6_1to6_2.update6_1to6_2(db);
        Update6_2to6_3.update6_2to6_3(db);
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION6_3;
    }
}
