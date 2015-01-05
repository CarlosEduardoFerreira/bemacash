package com.kaching123.tcr.service.response;

import android.content.ContentValues;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.rest.sync.GetArrayResponse;
import com.kaching123.tcr.commands.rest.sync.GetResponse;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.SyncOpenHelper;
import com.kaching123.tcr.util.JdbcJSONArray;
import com.kaching123.tcr.util.JdbcJSONObject;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;


/**
 * Created by hamsterksu on 11.09.2014.
 */
public abstract class SyncResponseHandler {

    private SyncOpenHelper syncOpenHelper;
    private JdbcConverter converter;
    private String localTableName;
    private long serverLastTimestamp;
    private boolean serverHasBeenUpdated;

    protected SyncResponseHandler(SyncOpenHelper syncOpenHelper, JdbcConverter converter, String localTableName, long serverLastTimestamp) {
        this.syncOpenHelper = syncOpenHelper;
        this.converter = converter;
        this.localTableName = localTableName;
        this.serverLastTimestamp = serverLastTimestamp;
    }

    public HandlerResult handleResponse(GetResponse response) throws JSONException {
        serverHasBeenUpdated = false;
        JdbcJSONObject data = response.getEntity();
        if(data == null){
            Logger.d("Empty response");
            return new HandlerResult(false, false);
        }

        ArrayList<ContentValues> values = parseResponseArray(data);
        if (values.size() != 0) {
            saveResult(values);
        }
        return new HandlerResult(values.size() > 0, serverHasBeenUpdated);
    }

    public HandlerResult handleResponse(GetArrayResponse response) throws JSONException {
        serverHasBeenUpdated = false;
        JdbcJSONArray data = response.getEntity();
        if(data == null){
            Logger.d("Empty response");
            return new HandlerResult(false, false);
        }

        ArrayList<ContentValues> values = parseResponseArray(data);
        if (values.size() != 0) {
            saveResult(values);
        }
        return new HandlerResult(values.size() > 0, serverHasBeenUpdated);
    }

    public ArrayList<ContentValues> parseResponse(GetArrayResponse response) throws JSONException {
        serverHasBeenUpdated = false;
        JdbcJSONArray data = response.getEntity();
        if(data == null){
            Logger.d("Empty response");
            return null;
        }

        return parseResponseArray(data);
    }

    protected ArrayList<ContentValues> parseResponseArray(JdbcJSONObject data) throws JSONException {
        ArrayList<ContentValues> values = new ArrayList<ContentValues>();
        JSONArray names = data.names();
        for (int i = 0; i < names.length(); i++) {
            String guid = names.getString(i);
            JdbcJSONObject rs = data.getJSONObject(guid);
            values.add(parseItem(rs));
        }
        return values;
    }

    protected ArrayList<ContentValues> parseResponseArray(JdbcJSONArray data) throws JSONException {
        ArrayList<ContentValues> values = new ArrayList<ContentValues>();
        for (int i = 0; i < data.length(); i++) {
            JdbcJSONObject rs = data.getJSONObject(i);
            values.add(parseItem(rs));
        }
        return values;
    }

    protected ContentValues parseItem(JdbcJSONObject rs) throws JSONException {
        Long updateTime = null;
        if(converter.supportUpdateTimeFlag()) {
            updateTime = rs.getTimestamp(JdbcBuilder.FIELD_UPDATE_TIME).getTime();

            if (updateTime > serverLastTimestamp) {
                Logger.w("SyncResponseHandler.parseItem(): server data has been updated; table: " + localTableName + "; updateTime: " + new Date(updateTime));
                serverHasBeenUpdated = true;
            }
        }
        IValueModel valuesModel = converter.toValues(rs);
        ContentValues v = valuesModel.toValues();
        if(converter.supportUpdateTimeFlag()) {
            v.put(ShopStore.DEFAULT_UPDATE_TIME, updateTime);
        }
        if (converter.supportDeleteFlag()) {
            v.put(ShopStore.DEFAULT_IS_DELETED, rs.getBoolean(JdbcBuilder.FIELD_IS_DELETED));
        }
        if(converter.supportDraftFlag()) {
            v.put(ShopStore.DEFAULT_IS_DRAFT, 1);
        }
        return v;
    }

    protected void saveResult(ArrayList<ContentValues> result) {
        boolean success = syncOpenHelper.insert(localTableName, result.toArray(new ContentValues[result.size()]));
        if(!success){
            throw new RuntimeException("some data was not saved");
        }
    }

    public static class HandlerResult {

        public final boolean hasData;
        public final boolean serverHasBeenUpdated;

        public HandlerResult(boolean hasData, boolean serverHasBeenUpdated) {
            this.hasData = hasData;
            this.serverHasBeenUpdated = serverHasBeenUpdated;
        }

    }

}
