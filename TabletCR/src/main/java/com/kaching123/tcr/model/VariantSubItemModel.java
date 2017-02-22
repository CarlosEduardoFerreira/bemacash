package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.VariantSubItemTable;

import java.io.Serializable;
import java.util.List;


/**
 * Created by aakimov on 23/04/15.
 */
public class VariantSubItemModel implements Serializable, IValueModel {

    public String guid;
    public String name;
    public String parentVariantItemGuid;
    public String itemGuid;

    private List<String> mIgnoreFields;

    public VariantSubItemModel(String guid) {
        this.guid = guid;
    }

    public VariantSubItemModel(String guid, String name, String parentVariantItemGuid, String itemGuid, List<String> ignoreFields) {
        this.guid = guid;
        this.name = name;
        this.parentVariantItemGuid = parentVariantItemGuid;
        this.itemGuid = itemGuid;

        this.mIgnoreFields = ignoreFields;
    }

    public VariantSubItemModel(Cursor cursor) {
        this(cursor.getString(cursor.getColumnIndex(VariantSubItemTable.GUID)),
                cursor.getString(cursor.getColumnIndex(VariantSubItemTable.NAME)),
                cursor.getString(cursor.getColumnIndex(VariantSubItemTable.VARIANT_ITEM_GUID)),
                cursor.getString(cursor.getColumnIndex(VariantSubItemTable.ITEM_GUID)),
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

        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.VariantSubItemTable.GUID)) contentValues.put(ShopStore.VariantSubItemTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.VariantSubItemTable.NAME)) contentValues.put(ShopStore.VariantSubItemTable.NAME, name);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.VariantSubItemTable.VARIANT_ITEM_GUID)) contentValues.put(ShopStore.VariantSubItemTable.VARIANT_ITEM_GUID, parentVariantItemGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.VariantSubItemTable.ITEM_GUID)) contentValues.put(ShopStore.VariantSubItemTable.ITEM_GUID, itemGuid);

        return contentValues;
    }

    @Override
    public String getIdColumn() {
        return ShopStore.VariantSubItemTable.GUID;
    }
}
