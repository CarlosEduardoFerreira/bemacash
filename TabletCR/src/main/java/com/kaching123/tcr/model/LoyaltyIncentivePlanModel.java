package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.LoyaltyIncentivePlanTable;

import java.io.Serializable;

/**
 * Created by vkompaniets on 28.06.2016.
 */
public class LoyaltyIncentivePlanModel implements IValueModel, Serializable{

    public String guid;
    public String loyaltyGuid;
    public String planGuid;

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(LoyaltyIncentivePlanTable.GUID, guid);
        values.put(LoyaltyIncentivePlanTable.INCENTIVE_GUID, loyaltyGuid);
        values.put(LoyaltyIncentivePlanTable.PLAN_GUID, planGuid);
        return values;
    }
}
