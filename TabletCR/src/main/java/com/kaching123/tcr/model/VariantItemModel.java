package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore;

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
        this(cursor.getString(cursor.getColumnIndex(ShopStore.VariantItemTable.GUID)),
                cursor.getString(cursor.getColumnIndex(ShopStore.VariantItemTable.NAME)),
                cursor.getString(cursor.getColumnIndex(ShopStore.VariantItemTable.ITEM_GUID)), cursor.getLong(cursor.getColumnIndex(ShopStore.VariantItemTable.SHOP_ID)));

    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = new ContentValues(4);
        contentValues.put(ShopStore.VariantItemTable.GUID, guid);
        contentValues.put(ShopStore.VariantItemTable.NAME, name);
        contentValues.put(ShopStore.VariantItemTable.ITEM_GUID, parentGuid);
        contentValues.put(ShopStore.VariantItemTable.SHOP_ID, shopId);
        return contentValues;
    }

}
