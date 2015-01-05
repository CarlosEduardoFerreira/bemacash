package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.ActivationCarrierTable;

import java.io.Serializable;

/**
 * Created by vkompaniets on 03.07.2014.
 */
public class ActivationCarrierModel implements IValueModel, Serializable {

    public final long id;
    public final String name;
    public final String url;
    public final boolean isActive;

    public ActivationCarrierModel(long id, String name, String url, boolean isActive) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.isActive = isActive;
    }

    public ActivationCarrierModel(Cursor c){
        this.id = c.getLong(c.getColumnIndex(ActivationCarrierTable.ID));
        this.name = c.getString(c.getColumnIndex(ActivationCarrierTable.NAME));
        this.url = c.getString(c.getColumnIndex(ActivationCarrierTable.URL));
        this.isActive = c.getInt(c.getColumnIndex(ActivationCarrierTable.IS_ACTIVE)) == 1;
    }

    @Override
    public String getGuid() {
        return null;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ActivationCarrierTable.ID, id);
        values.put(ActivationCarrierTable.NAME, name);
        values.put(ActivationCarrierTable.URL, url);
        values.put(ActivationCarrierTable.IS_ACTIVE, isActive);
        return values;
    }
}
