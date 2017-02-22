package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;

import java.io.Serializable;
import java.util.List;

/**
 * Created by gdubina on 06/11/13.
 */
public class DepartmentModel implements IValueModel, Serializable {

    public final String guid;
    public String title;

    private List<String> mIgnoreFields;

    public DepartmentModel(String guid, String title, List<String> ignoreFields) {
        this.guid = guid;
        this.title = title;

        this.mIgnoreFields = ignoreFields;
    }

    public DepartmentModel(Cursor c) {
        this(c.getString(c.getColumnIndex(ShopStore.DepartmentTable.GUID)),
                c.getString(c.getColumnIndex(ShopStore.DepartmentTable.TITLE)),
                null);
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.DepartmentTable.GUID)) values.put(ShopStore.DepartmentTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.DepartmentTable.TITLE)) values.put(ShopStore.DepartmentTable.TITLE, title);
        return values;
    }

    @Override
    public String getIdColumn() {
        return ShopStore.DepartmentTable.GUID;
    }
}
