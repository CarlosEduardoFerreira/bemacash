package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore;

import java.io.Serializable;

/**
 * Created by gdubina on 06/11/13.
 */
public class DepartmentModel implements IValueModel, Serializable {

    public final String guid;
    public String title;

    public DepartmentModel(String guid, String title) {
        this.guid = guid;
        this.title = title;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DepartmentTable.GUID, guid);
        values.put(ShopStore.DepartmentTable.TITLE, title);
        return values;
    }
}
