package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemMatrixTable;

import java.io.Serializable;
import java.util.List;


/**
 * Created by aakimov on 23/04/15.
 */
public class ItemMatrixModel implements Serializable, IValueModel {

    public String guid;
    public String name;
    public String parentItemGuid;
    public String childItemGuid;

    private List<String> mIgnoreFields;

    public ItemMatrixModel(String guid, String name, String parentItemGuid, String childItemGuid, List<String> ignoreFields) {
        this.guid = guid;
        this.name = name;
        this.parentItemGuid = parentItemGuid;
        this.childItemGuid = childItemGuid;

        this.mIgnoreFields = ignoreFields;
    }

    public ItemMatrixModel(Cursor cursor) {
        this(cursor.getString(cursor.getColumnIndex(ItemMatrixTable.GUID)),
                cursor.getString(cursor.getColumnIndex(ItemMatrixTable.NAME)),
                cursor.getString(cursor.getColumnIndex(ItemMatrixTable.PARENT_GUID)),
                cursor.isNull(cursor.getColumnIndex(ItemMatrixTable.CHILD_GUID)) ? null : cursor.getString(cursor.getColumnIndex(ItemMatrixTable.CHILD_GUID)),
                null);
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.ItemMatrixTable.GUID)) contentValues.put(ShopStore.ItemMatrixTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.ItemMatrixTable.NAME)) contentValues.put(ShopStore.ItemMatrixTable.NAME, name);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.ItemMatrixTable.PARENT_GUID)) contentValues.put(ShopStore.ItemMatrixTable.PARENT_GUID, parentItemGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.ItemMatrixTable.CHILD_GUID)) contentValues.put(ShopStore.ItemMatrixTable.CHILD_GUID, childItemGuid);

        return contentValues;
    }

    @Override
    public String getIdColumn() {
        return ShopStore.ItemMatrixTable.GUID;
    }
}
