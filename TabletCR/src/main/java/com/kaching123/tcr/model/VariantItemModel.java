package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.VariantItemTable;

import java.io.Serializable;

/**
 * Created by aakimov on 23/04/15.
 */
public class VariantItemModel implements Serializable, IValueModel {

    public String guid;
    public String name;
    public String parentGuid;
    public long shopId;

    public VariantItemModel(String guid) {
        this.guid = guid;
    }

    public VariantItemModel(String guid, String name, String parentGuid, long shopId) {
        this.guid = guid;
        this.name = name;
        this.parentGuid = parentGuid;
        this.shopId = shopId;
    }

    public VariantItemModel(Cursor cursor) {
        this(cursor.getString(cursor.getColumnIndex(VariantItemTable.GUID)),
                cursor.getString(cursor.getColumnIndex(VariantItemTable.NAME)),
                cursor.getString(cursor.getColumnIndex(VariantItemTable.ITEM_GUID)),
                cursor.getLong(cursor.getColumnIndex(VariantItemTable.SHOP_ID)));

    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = new ContentValues(4);
        contentValues.put(VariantItemTable.GUID, guid);
        contentValues.put(VariantItemTable.NAME, name);
        contentValues.put(VariantItemTable.ITEM_GUID, parentGuid);
        contentValues.put(VariantItemTable.SHOP_ID, shopId);
        return contentValues;
    }

}
