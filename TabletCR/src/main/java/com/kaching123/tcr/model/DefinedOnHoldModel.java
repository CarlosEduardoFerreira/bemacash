package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore;

import java.io.Serializable;

/**
 * Created by mboychenko on 2/3/2017.
 */

public class DefinedOnHoldModel implements IValueModel, Serializable{

    private final String id;
    private String name;

    public DefinedOnHoldModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public DefinedOnHoldModel(Cursor c) {
        this(c.getString(c.getColumnIndex(ShopStore.DefinedOnHoldTable.ID)),
             c.getString(c.getColumnIndex(ShopStore.DefinedOnHoldTable.NAME)));
    }

    @Override
    public String getGuid() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DefinedOnHoldTable.ID, id);
        values.put(ShopStore.DefinedOnHoldTable.NAME, name);
        return values;
    }
}
