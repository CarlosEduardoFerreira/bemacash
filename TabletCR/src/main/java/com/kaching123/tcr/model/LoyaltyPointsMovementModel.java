package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.LoyaltyPointsMovementTable;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by vkompaniets on 01.07.2016.
 */
public class LoyaltyPointsMovementModel implements IValueModel, Serializable{

    public String guid;
    public String customerId;
    public BigDecimal loyaltyPoints;

    public LoyaltyPointsMovementModel(String guid, String customerId, BigDecimal loyaltyPoints) {
        this.guid = guid;
        this.customerId = customerId;
        this.loyaltyPoints = loyaltyPoints;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        values.put(LoyaltyPointsMovementTable.GUID, guid);
        values.put(LoyaltyPointsMovementTable.CUSTOMER_ID, customerId);
        values.put(LoyaltyPointsMovementTable.LOYALTY_POINTS, _decimal(loyaltyPoints));
        return values;
    }

    @Override
    public String getIdColumn() {
        return LoyaltyPointsMovementTable.GUID;
    }
}
