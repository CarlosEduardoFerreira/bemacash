package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.LoyaltyXitemTable;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by vkompaniets on 28.06.2016.
 */
public class LoyaltyXitemModel implements IValueModel, Serializable {

    public String guid;
    public String loyaltyGuid;
    public String itemGuid;
    public BigDecimal price;
    public BigDecimal qty;

    public LoyaltyXitemModel(String guid, String loyaltyGuid, String itemGuid, BigDecimal price, BigDecimal qty) {
        this.guid = guid;
        this.loyaltyGuid = loyaltyGuid;
        this.itemGuid = itemGuid;
        this.price = price;
        this.qty = qty;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(LoyaltyXitemTable.GUID, guid);
        values.put(LoyaltyXitemTable.LOYALTY_GUID, loyaltyGuid);
        values.put(LoyaltyXitemTable.ITEM_GUID, itemGuid);
        values.put(LoyaltyXitemTable.PRICE, _decimal(price));
        values.put(LoyaltyXitemTable.QTY, _decimalQty(qty));
        return values;
    }
}
