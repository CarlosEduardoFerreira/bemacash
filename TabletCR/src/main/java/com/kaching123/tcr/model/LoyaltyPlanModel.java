package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.LoyaltyPlanTable;

import java.io.Serializable;

/**
 * Created by vkompaniets on 28.06.2016.
 */
public class LoyaltyPlanModel implements IValueModel, Serializable{

    public String guid;
    public String name;

    public LoyaltyPlanModel(String guid, String name) {
        this.guid = guid;
        this.name = name;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(LoyaltyPlanTable.GUID, guid);
        values.put(LoyaltyPlanTable.NAME, name);
        return values;
    }
}
