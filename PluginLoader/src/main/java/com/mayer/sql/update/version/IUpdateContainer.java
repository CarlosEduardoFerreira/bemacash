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
    void onUpdate(final SQLiteDatabase db);
    int getSqlOldVersion();
    int getSqlNewVersion();
}
