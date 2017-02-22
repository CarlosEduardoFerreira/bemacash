package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.SaleIncentiveTable;

import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by vkompaniets on 18.07.2016.
 */
public class SaleIncentiveModel implements IValueModel {

    public String guid;
    public String incentiveId;
    public String customerId;
    public String orderId;
    public LoyaltyType type;
    public LoyaltyRewardType rewardType;
    public BigDecimal rewardValue;
    public DiscountType rewardValueType;
    public String saleItemId;
    public BigDecimal pointsThreshold;

    public SaleIncentiveModel(String guid){
        this.guid = guid;
    }

    public SaleIncentiveModel(String guid, String incentiveId, String customerId, String orderId, LoyaltyType type, LoyaltyRewardType rewardType, BigDecimal rewardValue, DiscountType rewardValueType, String saleItemId, BigDecimal pointsThreshold) {
        this.guid = guid;
        this.incentiveId = incentiveId;
        this.customerId = customerId;
        this.orderId = orderId;
        this.type = type;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.rewardValueType = rewardValueType;
        this.saleItemId = saleItemId;
        this.pointsThreshold = pointsThreshold;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues cv = new ContentValues();
        cv.put(SaleIncentiveTable.GUID, guid);
        cv.put(SaleIncentiveTable.INCENTIVE_ID, incentiveId);
        cv.put(SaleIncentiveTable.CUSTOMER_ID, customerId);
        cv.put(SaleIncentiveTable.ORDER_ID, orderId);
        cv.put(SaleIncentiveTable.TYPE, type.ordinal());
        cv.put(SaleIncentiveTable.REWARD_TYPE, rewardType.ordinal());
        cv.put(SaleIncentiveTable.REWARD_VALUE, _decimal(rewardValue));
        cv.put(SaleIncentiveTable.REWARD_VALUE_TYPE, rewardValueType == null ? null : rewardValueType.ordinal());
        cv.put(SaleIncentiveTable.SALE_ITEM_ID, saleItemId);
        cv.put(SaleIncentiveTable.POINT_THRESHOLD, _decimal(pointsThreshold));
        return cv;
    }

    @Override
    public String getIdColumn() {
        return null;
    }
}
