package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.KDSTable;

import java.io.Serializable;

/**
 * Created by long.jiao on 06.21.16.
 */
public class KDSModel implements IValueModel, Serializable{

    public String guid;
    public String ip;
    public int port;

    public String aliasGuid;

    public KDSModel(String guid, String ip, int port, String aliasGuid) {
        this.guid = guid;
        this.ip = ip;
        this.port = port;
        this.aliasGuid = aliasGuid;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(KDSTable.GUID, guid);
        values.put(KDSTable.IP, ip);
        values.put(KDSTable.PORT, port);
        values.put(KDSTable.ALIAS_GUID, aliasGuid);
        return values;
    }
}
