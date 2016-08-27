package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.ModifierGroupTable;

import java.io.Serializable;
/**
 * Created by alboyko 02.12.2015
 */
public class ModifierGroupModel implements IValueModel, Serializable {

    public String guid;
    public String itemGuid;
    public String title;
    public int orderNum;

    public ModifierGroupModel(String guid, String itemGuid, String title, int orderNum) {
        this.guid = guid;
        this.itemGuid = itemGuid;
        this.title = title;
        this.orderNum = orderNum;
    }

    public ModifierGroupModel() {
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ModifierGroupTable.GUID, guid);
        values.put(ModifierGroupTable.ITEM_GUID, itemGuid);
        values.put(ModifierGroupTable.TITLE, title);
        values.put(ModifierGroupTable.ORDER_NUM, orderNum);

        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ModifierGroupModel) {
            return guid.equals(((ModifierGroupModel)o).guid);
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        if (guid == null)
            return 0;

        return guid.hashCode();
    }
}