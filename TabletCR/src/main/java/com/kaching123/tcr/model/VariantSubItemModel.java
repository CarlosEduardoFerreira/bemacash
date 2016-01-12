package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.VariantSubItemTable;

import java.io.Serializable;


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
        this(cursor.getString(cursor.getColumnIndex(VariantSubItemTable.GUID)),
                cursor.getString(cursor.getColumnIndex(VariantSubItemTable.NAME)),
                cursor.getString(cursor.getColumnIndex(VariantSubItemTable.VARIANT_ITEM_GUID)),
                cursor.getString(cursor.getColumnIndex(VariantSubItemTable.ITEM_GUID)));
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = new ContentValues(4);
        contentValues.put(VariantSubItemTable.GUID, guid);
        contentValues.put(VariantSubItemTable.NAME, name);
        contentValues.put(VariantSubItemTable.VARIANT_ITEM_GUID, parentVariantItemGuid);
        contentValues.put(VariantSubItemTable.ITEM_GUID, itemGuid);
        return contentValues;
    }
}
