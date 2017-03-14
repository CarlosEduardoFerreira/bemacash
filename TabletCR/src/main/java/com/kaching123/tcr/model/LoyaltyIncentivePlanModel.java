package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.LoyaltyIncentivePlanTable;

import java.io.Serializable;

/**
 * Created by vkompaniets on 28.06.2016.
 */
public class LoyaltyIncentivePlanModel implements IValueModel, Serializable{

    public String guid;
    public String incentiveGuid;
    public String planGuid;

    public LoyaltyIncentivePlanModel(String guid, String incentiveGuid, String planGuid) {
        this.guid = guid;
        this.incentiveGuid = incentiveGuid;
        this.planGuid = planGuid;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        values.put(LoyaltyIncentivePlanTable.GUID, guid);
        values.put(LoyaltyIncentivePlanTable.INCENTIVE_GUID, incentiveGuid);
        values.put(LoyaltyIncentivePlanTable.PLAN_GUID, planGuid);
        return values;
    }

    @Override
    public String getIdColumn() {
        return LoyaltyIncentivePlanTable.GUID;
    }
}
