package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.LoyaltyIncentiveTable;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;

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
    public DiscountType rewardValueType;

    public LoyaltyIncentiveModel(String guid, String name, LoyaltyType type, LoyaltyRewardType rewardType, int birthdayOffset, BigDecimal pointThreshold, BigDecimal rewardValue, DiscountType rewardValueType) {
        this.guid = guid;
        this.name = name;
        this.type = type;
        this.rewardType = rewardType;
        this.birthdayOffset = birthdayOffset;
        this.pointThreshold = pointThreshold;
        this.rewardValue = rewardValue;
        this.rewardValueType = rewardValueType;
    }

    public LoyaltyIncentiveModel(Cursor c){
        this(
                c.getString(c.getColumnIndex(LoyaltyIncentiveTable.GUID)),
                c.getString(c.getColumnIndex(LoyaltyIncentiveTable.NAME)),
                LoyaltyType.valueOf(c.getInt(c.getColumnIndex(LoyaltyIncentiveTable.TYPE))),
                LoyaltyRewardType.valueOf(c.getInt(c.getColumnIndex(LoyaltyIncentiveTable.REWARD_TYPE))),
                c.getInt(c.getColumnIndex(LoyaltyIncentiveTable.BIRTHDAY_OFFSET)),
                _decimal(c, c.getColumnIndex(LoyaltyIncentiveTable.POINT_THRESHOLD), BigDecimal.ZERO),
                _decimal(c, c.getColumnIndex(LoyaltyIncentiveTable.REWARD_VALUE), BigDecimal.ZERO),
                _discountType(c, c.getColumnIndex(LoyaltyIncentiveTable.REWARD_VALUE_TYPE))
        );
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
        values.put(LoyaltyIncentiveTable.REWARD_VALUE_TYPE, rewardValueType == null ? null : rewardValueType.ordinal());
        return values;
    }

    @Override
    public String getIdColumn() {
        return null;
    }
}
