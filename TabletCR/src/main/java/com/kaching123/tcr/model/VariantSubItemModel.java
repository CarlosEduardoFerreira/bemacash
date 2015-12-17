package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;

import com.kaching123.tcr.store.ShopStore;

/**
 * Created by aakimov on 23/04/15.
 */
public class VariantSubItemModel implements Serializable, IValueModel {

    public String guid;
    public String name;
    public String parentVariantItemGuid;
    public String itemGuid;

    public VariantSubItemModel(String guid) {
        this.guid = guid;
    }

    public VariantSubItemModel(String guid, String name, String parentVariantItemGuid, String itemGuid) {
        this.guid = guid;
        this.name = name;
        this.parentVariantItemGuid = parentVariantItemGuid;
        this.itemGuid = itemGuid;
    }

    public VariantSubItemModel(Cursor cursor) {
        this(cursor.getString(cursor.getColumnIndex(ShopStore.VariantSubItemTable.GUID)),
                cursor.getString(cursor.getColumnIndex(ShopStore.VariantSubItemTable.NAME)),
                cursor.getString(cursor.getColumnIndex(ShopStore.VariantSubItemTable.VARIANT_ITEM_GUID)), cursor.getString(cursor.getColumnIndex(ShopStore.VariantSubItemTable.ITEM_GUID)));
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = new ContentValues(4);
        contentValues.put(ShopStore.VariantSubItemTable.GUID, guid);
        contentValues.put(ShopStore.VariantSubItemTable.NAME, name);
        contentValues.put(ShopStore.VariantSubItemTable.VARIANT_ITEM_GUID, parentVariantItemGuid);
        contentValues.put(ShopStore.VariantSubItemTable.ITEM_GUID, itemGuid);
        return contentValues;
    }
}
