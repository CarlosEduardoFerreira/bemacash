package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.SaleItemTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._putDiscount;
import static com.kaching123.tcr.model.ContentValuesUtil._putEnum;

/**
 * Created by gdubina on 06/11/13.
 */
public class SaleOrderItemModel implements IValueModel, Serializable {

    public String saleItemGuid;
    public ArrayList<Unit> tmpUnit = new ArrayList<Unit>();
    public String orderGuid;
    public String itemGuid;
    public BigDecimal qty;
    public BigDecimal kitchenPrintedQty;
    public BigDecimal price;
    public PriceType priceType;

    public BigDecimal discount;
    public DiscountType discountType;
    public boolean isTaxable;
    public BigDecimal tax;
    public boolean discountable;
    public long sequence;
    public String parentGuid;

    public BigDecimal tmpRefundQty;

    public BigDecimal finalGrossPrice;
    public BigDecimal finalTax;
    public BigDecimal finalDiscount;

    public String notes;
    public boolean hasNotes;

    public SaleOrderItemModel(String saleItemGuid) {
        this.saleItemGuid = saleItemGuid;
    }

    public SaleOrderItemModel(String saleItemGuid, String orderGuid, String itemGuid, BigDecimal qty, BigDecimal kitchenPrintedQty, PriceType priceType, BigDecimal price, boolean discountable, BigDecimal discount, DiscountType discountType, boolean isTaxable, BigDecimal tax, long sequence, String parentGuid,
                              BigDecimal finalGrossPrice,
                              BigDecimal finalTax,
                              BigDecimal finalDiscount,
                              BigDecimal tmpRefundQty,
                              String notes,
                              boolean hasNotes) {
        this.saleItemGuid = saleItemGuid;
        this.orderGuid = orderGuid;
        this.itemGuid = itemGuid;
        this.qty = qty;
        this.price = price;
        this.priceType = priceType;
        this.discount = discount;
        this.discountType = discountType;
        this.isTaxable = isTaxable;
        this.discountable = discountable;
        this.sequence = sequence;
        this.tmpRefundQty = tmpRefundQty;
        this.parentGuid = parentGuid;
        this.tax = tax;
        this.finalGrossPrice = finalGrossPrice;
        this.finalTax = finalTax;
        this.finalDiscount = finalDiscount;
        this.notes = notes;
        this.hasNotes = hasNotes;
    }

    public SaleOrderItemModel setUnitItemGuid( ArrayList<Unit>  tmpUnit) {
        this.tmpUnit = tmpUnit;
        return this;
    }

    @Override
    public String getGuid() {
        return saleItemGuid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(SaleItemTable.SALE_ITEM_GUID, saleItemGuid);
        values.put(SaleItemTable.ORDER_GUID, orderGuid);
        values.put(SaleItemTable.ITEM_GUID, itemGuid);
        values.put(SaleItemTable.QUANTITY, _decimalQty(qty));

        values.put(SaleItemTable.PRICE, _decimal(price));
        _putEnum(values, SaleItemTable.PRICE_TYPE, priceType);

        values.put(SaleItemTable.DISCOUNTABLE, discountable);
        values.put(SaleItemTable.DISCOUNT, _decimal(discount));
        _putDiscount(values, SaleItemTable.DISCOUNT_TYPE, discountType);
        values.put(SaleItemTable.TAXABLE, isTaxable);
        values.put(SaleItemTable.TAX, _decimal(tax));
        values.put(SaleItemTable.SEQUENCE, sequence);
        values.put(SaleItemTable.PARENT_GUID, parentGuid);

        values.put(SaleItemTable.FINAL_GROSS_PRICE, _decimal(finalGrossPrice));
        values.put(SaleItemTable.FINAL_TAX, _decimal(finalTax));
        values.put(SaleItemTable.FINAL_DISCOUNT, _decimal(finalDiscount));
        values.put(SaleItemTable.NOTES, notes);
        values.put(SaleItemTable.HAS_NOTES, hasNotes);

        return values;
    }

    public ContentValues updateFinalFields() {
        ContentValues values = new ContentValues(3);

        values.put(SaleItemTable.FINAL_GROSS_PRICE, _decimal(finalGrossPrice));
        values.put(SaleItemTable.FINAL_TAX, _decimal(finalTax));
        values.put(SaleItemTable.FINAL_DISCOUNT, _decimal(finalDiscount));

        return values;
    }

    public BigDecimal getFinalPrice(){
        return finalGrossPrice.add(finalTax).subtract(finalDiscount);
    }
}