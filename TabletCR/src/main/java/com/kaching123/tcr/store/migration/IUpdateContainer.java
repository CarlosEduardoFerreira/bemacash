package com.kaching123.tcr.store.migration;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dot on 24.06.2014.
 */
public interface IUpdateContainer {

    int VERSION1    = 214;
    int VERSION2    = 237;
    int VERSION3    = 240;
    int VERSION4    = 251;
    int VERSION4_1  = 252;
    int VERSION4_2  = 253;
    int VERSION5    = 290;
    int VERSION5_1  = 291;
    int VERSION5_2  = 292;
    int VERSION5_3  = 293;
    int VERSION5_4  = 294;
    int VERSION5_5  = 295;
    int VERSION5_6  = 296;
    int VERSION5_7  = 297;
    int VERSION5_8  = 298;
    int VERSION5_9  = 299; // build 160+
    int VERSION6_1  = 300; // build 167+
    int VERSION6_2  = 301; // build 170+
    int VERSION6_3  = 302; // build 184+
    int VERSION6_4  = 303; // build 250+
    int VERSION6_5  = 304; // VERSION CODE 22
    int VERSION6_6  = 305; // VERSION CODE 23
    int VERSION6_7  = 306; // VERSION CODE 31
    int VERSION7    = 307;
    int VERSION8    = 308;
    int VERSION9    = 309; // Version Code 54
    int VERSION10   = 400; // Version Code 55
    int VERSION10_1   = 401;

    void onUpdate(final SQLiteDatabase db);

    int getSqlOldVersion();

    int getSqlNewVersion();
}
