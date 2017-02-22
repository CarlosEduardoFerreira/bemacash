package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;

import java.io.Serializable;
import java.util.List;

import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ShopModuleTable;


/**
 * Created by Rodrigo Busata on 10/20/16.
 */
public class ShopModuleModel implements IValueModel, Serializable {

    public Integer id;
    public Integer shopId;
    public Integer moduleId;
    public Boolean enabled;

    private List<String> mIgnoreFields;

    public ShopModuleModel(Cursor c) {
        this.id = c.getInt(c.getColumnIndex(ShopModuleTable.ID));
        this.shopId = c.getInt(c.getColumnIndex(ShopModuleTable.SHOP_ID));
        this.moduleId = c.getInt(c.getColumnIndex(ShopModuleTable.MODULE_ID));
    }

    public ShopModuleModel(Integer id,
                           Integer shopId,
                           Integer moduleId,
                           Boolean enabled,
                           List<String> ignoreFields) {
        this.id = id;
        this.shopId = shopId;
        this.moduleId = moduleId;
        this.enabled = enabled;

        this.mIgnoreFields = ignoreFields;
    }

    @Override
    public String getGuid() {
        return ShopModuleTable.ID;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopModuleTable.ID)) v.put(ShopModuleTable.ID, id);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopModuleTable.SHOP_ID)) v.put(ShopModuleTable.SHOP_ID, shopId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopModuleTable.MODULE_ID)) v.put(ShopModuleTable.MODULE_ID, moduleId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopModuleTable.ENABLED)) v.put(ShopModuleTable.ENABLED, enabled);

        return v;
    }

    @Override
    public String getIdColumn() {
        return null;
    }

    public static boolean hasModule(Context context, Module module) {
        try (
                Cursor c = ProviderAction.query(ShopProvider.contentUri(ShopModuleTable.URI_CONTENT))
                        .projection(ShopModuleTable.ENABLED)
                        .where(ShopModuleTable.MODULE_ID + " = ?", module.ordinal())
                        .perform(context)
        ) {

            return c != null && c.moveToFirst() && c.getInt(0) == 1;
        }
    }

    public enum Module {
        NULL, TABLE
    }
}