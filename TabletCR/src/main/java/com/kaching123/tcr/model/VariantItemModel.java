package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.VariantItemTable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by aakimov on 23/04/15.
 */
public class VariantItemModel implements Serializable, IValueModel {

    public String guid;
    public String name;
    public String parentGuid;
    public long shopId;

    private List<String> mIgnoreFields;

    public VariantItemModel(String guid) {
        this.guid = guid;
    }

    public VariantItemModel(String guid, String name, String parentGuid, long shopId, List<String> ignoreFields) {
        this.guid = guid;
        this.name = name;
        this.parentGuid = parentGuid;
        this.shopId = shopId;
        this.mIgnoreFields = ignoreFields;

    }

    public VariantItemModel(Cursor cursor) {
        this(cursor.getString(cursor.getColumnIndex(VariantItemTable.GUID)),
                cursor.getString(cursor.getColumnIndex(VariantItemTable.NAME)),
                cursor.getString(cursor.getColumnIndex(VariantItemTable.ITEM_GUID)),
                cursor.getLong(cursor.getColumnIndex(VariantItemTable.SHOP_ID)),
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

        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.VariantItemTable.GUID)) contentValues.put(ShopStore.VariantItemTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.VariantItemTable.NAME)) contentValues.put(ShopStore.VariantItemTable.NAME, name);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.VariantItemTable.ITEM_GUID)) contentValues.put(ShopStore.VariantItemTable.ITEM_GUID, parentGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.VariantItemTable.SHOP_ID)) contentValues.put(ShopStore.VariantItemTable.SHOP_ID, shopId);

        return contentValues;
    }

    @Override
    public String getIdColumn() {
        return ShopStore.VariantItemTable.GUID;
    }

}
