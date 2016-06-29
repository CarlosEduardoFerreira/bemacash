package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.LoyaltyIncentiveTable;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by vkompaniets on 28.06.2016.
 */
public class LoyaltyIncentiveModel implements IValueModel, Serializable {

    public String guid;
    public String name;
    public LoyaltyType type;
    public LoyaltyRewardType rewardType;
    public int birthdayOffset;
    public BigDecimal pointThreshold;
    public BigDecimal rewardValue;
    public DiscountType rewardValuetype;

    public LoyaltyIncentiveModel(String guid, String name, LoyaltyType type, LoyaltyRewardType rewardType, int birthdayOffset, BigDecimal pointThreshold, BigDecimal rewardValue, DiscountType rewardValuetype) {
        this.guid = guid;
        this.name = name;
        this.type = type;
        this.rewardType = rewardType;
        this.birthdayOffset = birthdayOffset;
        this.pointThreshold = pointThreshold;
        this.rewardValue = rewardValue;
        this.rewardValuetype = rewardValuetype;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(LoyaltyIncentiveTable.GUID, guid);
        values.put(LoyaltyIncentiveTable.NAME, name);
        values.put(LoyaltyIncentiveTable.TYPE, type.ordinal());
        values.put(LoyaltyIncentiveTable.REWARD_TYPE, rewardType.ordinal());
        values.put(LoyaltyIncentiveTable.BIRTHDAY_OFFSET, birthdayOffset);
        values.put(LoyaltyIncentiveTable.POINT_THRESHOLD, _decimal(pointThreshold));
        values.put(LoyaltyIncentiveTable.REWARD_VALUE, _decimal(rewardValue));
        values.put(LoyaltyIncentiveTable.REWARD_VALUE, rewardValuetype == null ? null : rewardValuetype.ordinal());
        return values;
    }
}
