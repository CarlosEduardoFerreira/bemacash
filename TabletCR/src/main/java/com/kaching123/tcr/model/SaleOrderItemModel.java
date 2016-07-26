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

    public String description;
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
    public BigDecimal tax2;
    public boolean discountable;
    public long sequence;
    public String parentGuid;

    public BigDecimal tmpRefundQty;

    public BigDecimal finalGrossPrice;
    public BigDecimal finalTax;
    public BigDecimal finalDiscount;

    public String notes;
    public boolean hasNotes;
    public boolean isPrepaidItem;
    public BigDecimal loyaltyPoints;
    public boolean useLoyaltyPoints;

    public SaleOrderItemModel(String saleItemGuid) {
        this.saleItemGuid = saleItemGuid;
    }

    public SaleOrderItemModel(String saleItemGuid, String orderGuid, String itemGuid, BigDecimal qty, BigDecimal kitchenPrintedQty,
                              PriceType priceType, BigDecimal price, boolean discountable, BigDecimal discount, DiscountType discountType,
                              boolean isTaxable, BigDecimal tax, BigDecimal tax2, long sequence, String parentGuid,
                              BigDecimal finalGrossPrice,
                              BigDecimal finalTax,
                              BigDecimal finalDiscount,
                              BigDecimal tmpRefundQty,
                              String notes,
                              boolean hasNotes,
                              boolean isPrepaidItem,
                              BigDecimal loyaltyPoints,
                              boolean useLoyaltyPoints) {
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
        this.tax2 = tax2;
        this.finalGrossPrice = finalGrossPrice;
        this.finalTax = finalTax;
        this.finalDiscount = finalDiscount;
        this.notes = notes;
        this.hasNotes = hasNotes;
        this.kitchenPrintedQty = kitchenPrintedQty;
        this.isPrepaidItem = isPrepaidItem;
        this.loyaltyPoints = loyaltyPoints;
        this.useLoyaltyPoints = useLoyaltyPoints;
    }

    public SaleOrderItemModel(String saleItemGuid, String orderGuid, String itemGuid, String description,
                              BigDecimal qty, BigDecimal kitchenPrintedQty, PriceType priceType,
                              BigDecimal price, boolean discountable, BigDecimal discount, DiscountType discountType,
                              boolean isTaxable, BigDecimal tax, BigDecimal tax2, long sequence, String parentGuid,
                              BigDecimal finalGrossPrice,
                              BigDecimal finalTax,
                              BigDecimal finalDiscount,
                              BigDecimal tmpRefundQty,
                              String notes,
                              boolean hasNotes,
                              boolean isPrepaidItem,
                              BigDecimal loyaltyPoints,
                              boolean useLoyaltyPoints) {
        this.saleItemGuid = saleItemGuid;
        this.orderGuid = orderGuid;
        this.itemGuid = itemGuid;
        this.description = description;
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
        this.tax2 = tax2;
        this.finalGrossPrice = finalGrossPrice;
        this.finalTax = finalTax;
        this.finalDiscount = finalDiscount;
        this.notes = notes;
        this.hasNotes = hasNotes;
        this.kitchenPrintedQty = kitchenPrintedQty;
        this.isPrepaidItem = isPrepaidItem;
        this.loyaltyPoints = loyaltyPoints;
        this.useLoyaltyPoints = useLoyaltyPoints;
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
        values.put(SaleItemTable.KITCHEN_PRINTED_QTY, _decimalQty(kitchenPrintedQty));
        _putDiscount(values, SaleItemTable.DISCOUNT_TYPE, discountType);
        values.put(SaleItemTable.TAXABLE, isTaxable);
        values.put(SaleItemTable.TAX, _decimal(tax));
        values.put(SaleItemTable.TAX2, _decimal(tax2));
        values.put(SaleItemTable.SEQUENCE, sequence);
        values.put(SaleItemTable.PARENT_GUID, parentGuid);

        values.put(SaleItemTable.FINAL_GROSS_PRICE, _decimal(finalGrossPrice));
        values.put(SaleItemTable.FINAL_TAX, _decimal(finalTax));
        values.put(SaleItemTable.FINAL_DISCOUNT, _decimal(finalDiscount));
        values.put(SaleItemTable.NOTES, notes);
        values.put(SaleItemTable.HAS_NOTES, hasNotes);
        values.put(SaleItemTable.IS_PREPAID_ITEM, isPrepaidItem);
        values.put(SaleItemTable.LOYALTY_POINTS, _decimal(loyaltyPoints));
        values.put(SaleItemTable.USE_LOYALTY_POINTS, useLoyaltyPoints);

        return values;
    }

    public ContentValues updateFinalFields() {
        ContentValues values = new ContentValues(3);

        values.put(SaleItemTable.FINAL_GROSS_PRICE, _decimal(finalGrossPrice));
        values.put(SaleItemTable.FINAL_TAX, _decimal(finalTax));
        values.put(SaleItemTable.FINAL_DISCOUNT, _decimal(finalDiscount));

        return values;
    }

    public BigDecimal getFinalPrice() {
        return finalGrossPrice.add(finalTax).subtract(finalDiscount);
    }
}
