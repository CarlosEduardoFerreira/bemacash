package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.CustomerLoyaltyPointsTable;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by vkompaniets on 01.07.2016.
 */
public class CustomerLoyaltyPointsModel implements IValueModel, Serializable{

    public String guid;
    public String customerId;
    public BigDecimal loyaltyPoints;

    public CustomerLoyaltyPointsModel(String guid, String customerId, BigDecimal loyaltyPoints) {
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
        values.put(CustomerLoyaltyPointsTable.GUID, guid);
        values.put(CustomerLoyaltyPointsTable.CUSTOMER_ID, customerId);
        values.put(CustomerLoyaltyPointsTable.LOYALTY_POINTS, _decimal(loyaltyPoints));
        return values;
    }
}
