package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.MultipleDiscountTable;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.util.ContentValuesUtilBase._bool;

/**
 * Created by vkompaniets on 23.08.2016.
 */
public class MultipleDiscountModel implements IValueModel, Serializable{

    public final String id;
    public final String bundleId;
    public final String itemId;
    public final BigDecimal qty;
    public final BigDecimal discount;
    public final boolean isActive;

    public MultipleDiscountModel(String id, String bundleId, String itemId, BigDecimal qty, BigDecimal discount, boolean isActive) {
        this.id = id;
        this.bundleId = bundleId;
        this.itemId = itemId;
        this.qty = qty;
        this.discount = discount;
        this.isActive = isActive;
    }

    public MultipleDiscountModel(Cursor c){
        this(
                c.getString(c.getColumnIndex(MultipleDiscountTable.ID)),
                c.getString(c.getColumnIndex(MultipleDiscountTable.BUNDLE_ID)),
                c.getString(c.getColumnIndex(MultipleDiscountTable.ITEM_ID)),
                _decimalQty(c, c.getColumnIndex(MultipleDiscountTable.QTY), BigDecimal.ZERO),
                _decimal(c, c.getColumnIndex(MultipleDiscountTable.DISCOUNT), BigDecimal.ZERO),
                _bool(c, c.getColumnIndex(MultipleDiscountTable.IS_ACTIVE))
        );
    }

    @Override
    public String getGuid() {
        return id;
    }

    @Override
    public ContentValues toValues() {
        ContentValues cv = new ContentValues();
        cv.put(MultipleDiscountTable.ID, id);
        cv.put(MultipleDiscountTable.BUNDLE_ID, bundleId);
        cv.put(MultipleDiscountTable.ITEM_ID, itemId);
        cv.put(MultipleDiscountTable.QTY, _decimalQty(qty));
        cv.put(MultipleDiscountTable.DISCOUNT, _decimal(discount));
        cv.put(MultipleDiscountTable.IS_ACTIVE, isActive);
        return cv;
    }
}
