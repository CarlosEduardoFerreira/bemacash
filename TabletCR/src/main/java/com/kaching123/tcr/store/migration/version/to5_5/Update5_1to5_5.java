package com.kaching123.tcr.store.migration.version.to5_5;

import android.database.sqlite.SQLiteDatabase;

import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.store.migration.UpdateBlock;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_1to5_5 implements IUpdateContainer {

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
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_5;
    }
}
