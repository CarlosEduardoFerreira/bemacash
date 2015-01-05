package com.kaching123.tcr.service.response;

import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.store.SyncOpenHelper;

/**
 * Created by hamsterksu on 11.09.2014.
 */
public class SyncSingleResponseHandler extends SyncResponseHandler{

    public SyncSingleResponseHandler(SyncOpenHelper syncOpenHelper, JdbcConverter converter, String localTableName, long serverLastTimestamp) {
        super(syncOpenHelper, converter, localTableName, serverLastTimestamp);
    }
}
