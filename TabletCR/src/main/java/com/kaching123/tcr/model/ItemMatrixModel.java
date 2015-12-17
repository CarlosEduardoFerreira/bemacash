package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore;

import java.io.Serializable;


/**
 * Created by aakimov on 23/04/15.
 */
public class ItemMatrixModel implements Serializable, IValueModel {

    public String guid;
    public String name;
    public String parentItemGuid;
    public String childItemGuid;

    public ItemMatrixModel(String guid, String name, String parentItemGuid, String childItemGuid) {
        this.guid = guid;
        this.name = name;
        this.parentItemGuid = parentItemGuid;
        this.childItemGuid = childItemGuid;
    }

    public ItemMatrixModel(Cursor cursor) {
        this(cursor.getString(cursor.getColumnIndex(ShopStore.ItemMatrixTable.GUID)),
                cursor.getString(cursor.getColumnIndex(ShopStore.ItemMatrixTable.NAME)),
                cursor.getString(cursor.getColumnIndex(ShopStore.ItemMatrixTable.PARENT_GUID)),
                cursor.isNull(cursor.getColumnIndex(ShopStore.ItemMatrixTable.CHILD_GUID)) ? null : cursor.getString(cursor.getColumnIndex(ShopStore.ItemMatrixTable.CHILD_GUID)));
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShopStore.ItemMatrixTable.GUID, guid);
        contentValues.put(ShopStore.ItemMatrixTable.NAME, name);
        contentValues.put(ShopStore.ItemMatrixTable.PARENT_GUID, parentItemGuid);
        contentValues.put(ShopStore.ItemMatrixTable.CHILD_GUID, childItemGuid);

        return contentValues;
    }
}