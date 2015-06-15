package com.mayer.sql.update.version;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dot on 24.06.2014.
 */
public interface IUpdateContainer {

    int VERSION1 = 214;
    int VERSION2 = 237;
    int VERSION3 = 240;
    int VERSION4 = 251;
    int VERSION4_1 = 252;
    int VERSION4_2 = 253;
    int VERSION5 = 290;
    int VERSION5_1 = 291;
    int VERSION5_2 = 292;
    int VERSION5_3 = 293;
    int VERSION5_4 = 294;
    int VERSION5_5 = 295;
    int VERSION5_6 = 296;
    int VERSION5_7 = 297;
    int VERSION5_8 = 298;
    int VERSION5_9 = 299; // build 160+
    int VERSION6_1 = 300; // build 167+
    int VERSION6_2 = 301; // build 170+

    void onUpdate(final SQLiteDatabase db);

    int getSqlOldVersion();

    int getSqlNewVersion();
}
