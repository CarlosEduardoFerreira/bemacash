package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vkompaniets on 11/14/2014.
 */
public class Update5_1to5_2 implements IUpdateContainer {

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
    }

    @Override
    public int getSqlOldVersion() {
        return VERSION5_1;
    }

    @Override
    public int getSqlNewVersion() {
        return VERSION5_2;
    }
}
