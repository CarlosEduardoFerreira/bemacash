package com.kaching123.tcr.service.response;

import android.content.ContentValues;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.rest.sync.GetArrayResponse;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.store.ShopOpenHelper;
import com.kaching123.tcr.store.SyncOpenHelper;
import com.kaching123.tcr.util.JdbcJSONArray;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by pkabakov on 19.01.2015.
 */
public class SyncOldActiveOrdersResponseHandler extends BaseOrdersResponseHandler {

    private final SyncOpenHelper syncOpenHelper;

    public SyncOldActiveOrdersResponseHandler(SyncOpenHelper syncOpenHelper) {
        this.syncOpenHelper = syncOpenHelper;
    }

    @Override
    public boolean handleResponse(GetArrayResponse response) throws JSONException {
        JdbcJSONArray data = response.getEntity();
        if(data == null){
            Logger.w("SyncOldActiveOrdersResponseHandler: empty response");
            return false;
        }

        syncOpenHelper.beginTransaction();
        try {
            //TODO: improve?
            boolean hasData = parseResponse(data);

            syncOpenHelper.setTransactionSuccessful();

            return hasData;
        } finally {
            syncOpenHelper.endTransaction();
        }
    }

    @Override
    protected void saveResult(String localTableName, String idColumn, ArrayList<ContentValues> result) {
        JdbcConverter converter = JdbcFactory.getConverter(localTableName);
        if (syncOpenHelper.insert(localTableName, result.toArray(new ContentValues[result.size()]), converter.getLocalGuidColumn(), converter.supportUpdateTimeLocalFlag()) != null) {
            throw new RuntimeException("some data was not saved");
        }
    }

    @Override
    protected void saveResult(String localTableName, String idColumn, ContentValues result) {
        JdbcConverter converter = JdbcFactory.getConverter(localTableName);
        if (syncOpenHelper.insert(localTableName, new ContentValues[]{result}, converter.getLocalGuidColumn(), converter.supportUpdateTimeLocalFlag()) != null) {
            throw new RuntimeException("some data was not saved");
        }
    }
}
