package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.LoyaltyIncentiveItemTable;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by vkompaniets on 28.06.2016.
 */
public class LoyaltyIncentiveItemModel implements IValueModel, Serializable {

    public String guid;
    public String incentiveGuid;
    public String itemGuid;
    public BigDecimal price;
    public BigDecimal qty;

    public LoyaltyIncentiveItemModel(String guid, String incentiveGuid, String itemGuid, BigDecimal price, BigDecimal qty) {
        this.guid = guid;
        this.incentiveGuid = incentiveGuid;
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
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        values.put(LoyaltyIncentiveItemTable.GUID, guid);
        values.put(LoyaltyIncentiveItemTable.INCENTIVE_GUID, incentiveGuid);
        values.put(LoyaltyIncentiveItemTable.ITEM_GUID, itemGuid);
        values.put(LoyaltyIncentiveItemTable.PRICE, _decimal(price));
        values.put(LoyaltyIncentiveItemTable.QTY, _decimalQty(qty));
        return values;
    }

    @Override
    public String getIdColumn() {
        return LoyaltyIncentiveItemTable.GUID;
    }
}
