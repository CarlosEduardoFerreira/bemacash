package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._putDiscount;
import static com.kaching123.tcr.model.ContentValuesUtil._putEnum;
import static com.kaching123.tcr.util.ContentValuesUtilBase._enum;

/**
 * Created by gdubina on 06/11/13.
 */
public class SaleOrderItemModel implements IValueModel, Serializable {

    private static final Uri URI_ORDER_ITEM = ShopProvider.contentUri(ShopStore.SaleItemTable.URI_CONTENT);

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
    public BigDecimal tmpEBTpayed;

    public BigDecimal finalGrossPrice;
    public BigDecimal finalTax;
    public BigDecimal finalDiscount;

    public String notes;
    public boolean hasNotes;
    public boolean isPrepaidItem;
    public boolean isGiftCard;
    public BigDecimal loyaltyPoints;
    public boolean pointsForDollarAmount;
    public String discountBundleId;

    public boolean isEbtEligible;

    private List<String> mIgnoreFields;

    public SaleOrderItemModel(String saleItemGuid) {
        this.saleItemGuid = saleItemGuid;
    }

    public SaleOrderItemModel(String saleItemGuid,
                              String orderGuid,
                              String itemGuid,
                              BigDecimal qty,
                              BigDecimal kitchenPrintedQty,
                              PriceType priceType,
                              BigDecimal price,
                              boolean discountable,
                              BigDecimal discount,
                              DiscountType discountType,
                              boolean isTaxable,
                              BigDecimal tax,
                              BigDecimal tax2,
                              long sequence,
                              String parentGuid,
                              BigDecimal finalGrossPrice,
                              BigDecimal finalTax,
                              BigDecimal finalDiscount,
                              BigDecimal tmpRefundQty,
                              String notes,
                              boolean hasNotes,
                              boolean isPrepaidItem,
                              boolean isGiftCard,
                              BigDecimal loyaltyPoints,
                              boolean pointsForDollarAmount,
                              String discountBundleId,
                              boolean isEbtEligible,
                              BigDecimal tmpEBTpayed,
                              List<String> ignoreFields) {
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
        this.isGiftCard = isGiftCard;
        this.loyaltyPoints = loyaltyPoints;
        this.pointsForDollarAmount = pointsForDollarAmount;
        this.discountBundleId = discountBundleId;
        this.isEbtEligible = isEbtEligible;
        this.tmpEBTpayed = tmpEBTpayed;

        this.mIgnoreFields = ignoreFields;

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
                              boolean isGiftCard,
                              BigDecimal loyaltyPoints,
                              boolean pointsForDollarAmount,
                              String discountBundleId,
                              boolean isEbtEligible,
                              BigDecimal tmpEBTpayed) {
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
        this.isGiftCard = isGiftCard;
        this.loyaltyPoints = loyaltyPoints;
        this.pointsForDollarAmount = pointsForDollarAmount;
        this.discountBundleId = discountBundleId;
        this.isEbtEligible = isEbtEligible;
        this.tmpEBTpayed = tmpEBTpayed;
    }

    public SaleOrderItemModel(Cursor c) {
        this(
                c.getString(c.getColumnIndex(SaleItemTable.SALE_ITEM_GUID)),
                c.getString(c.getColumnIndex(SaleItemTable.ORDER_GUID)),
                c.getString(c.getColumnIndex(SaleItemTable.ITEM_GUID)),
                new BigDecimal(c.getDouble(c.getColumnIndex(SaleItemTable.QUANTITY))),
                new BigDecimal(c.getDouble(c.getColumnIndex(SaleItemTable.KITCHEN_PRINTED_QTY))),
                _enum(PriceType.class, c.getString(c.getColumnIndex(SaleItemTable.PRICE_TYPE)), PriceType.FIXED),
                new BigDecimal(c.getDouble(c.getColumnIndex(SaleItemTable.PRICE))),
                c.getInt(c.getColumnIndex(SaleItemTable.DISCOUNTABLE)) == 1,
                new BigDecimal(c.getDouble(c.getColumnIndex(SaleItemTable.DISCOUNT))),
                _enum(DiscountType.class, c.getString(c.getColumnIndex(SaleItemTable.DISCOUNT_TYPE)), DiscountType.PERCENT),
                c.getInt(c.getColumnIndex(SaleItemTable.TAXABLE)) == 1,
                new BigDecimal(c.getDouble(c.getColumnIndex(SaleItemTable.TAX))),
                new BigDecimal(c.getDouble(c.getColumnIndex(SaleItemTable.TAX2))),
                c.getLong(c.getColumnIndex(SaleItemTable.SEQUENCE)),
                c.getString(c.getColumnIndex(SaleItemTable.PARENT_GUID)),
                new BigDecimal(c.getDouble(c.getColumnIndex(SaleItemTable.FINAL_GROSS_PRICE))),
                new BigDecimal(c.getDouble(c.getColumnIndex(SaleItemTable.FINAL_TAX))),
                new BigDecimal(c.getDouble(c.getColumnIndex(SaleItemTable.FINAL_DISCOUNT))),
                new BigDecimal(c.getDouble(c.getColumnIndex(SaleItemTable.TMP_REFUND_QUANTITY))),
                c.getString(c.getColumnIndex(SaleItemTable.NOTES)),
                c.getInt(c.getColumnIndex(SaleItemTable.HAS_NOTES)) == 1,
                c.getInt(c.getColumnIndex(SaleItemTable.IS_PREPAID_ITEM)) == 1,
                c.getInt(c.getColumnIndex(SaleItemTable.IS_GIFT_CARD)) == 1,
                new BigDecimal(c.getDouble(c.getColumnIndex(SaleItemTable.LOYALTY_POINTS))),
                c.getInt(c.getColumnIndex(SaleItemTable.POINTS_FOR_DOLLAR_AMOUNT)) == 1,
                c.getString(c.getColumnIndex(SaleItemTable.DISCOUNT_BUNDLE_ID)),
                c.getInt(c.getColumnIndex(SaleItemTable.EBT_ELIGIBLE)) == 1,
                new BigDecimal(c.getDouble(c.getColumnIndex(SaleItemTable.TMP_EBT_PAYED))),
                null
        );
    }

    public SaleOrderItemModel(SaleOrderItemModel originalItem){
        this(
                originalItem.saleItemGuid,
                originalItem.orderGuid,
                originalItem.itemGuid,
                originalItem.qty,
                originalItem.kitchenPrintedQty,
                originalItem.priceType,
                originalItem.price,
                originalItem.discountable,
                originalItem.discount,
                originalItem.discountType,
                originalItem.isTaxable,
                originalItem.tax,
                originalItem.tax2,
                originalItem.sequence,
                originalItem.parentGuid,
                originalItem.finalGrossPrice,
                originalItem.finalTax,
                originalItem.finalDiscount,
                originalItem.tmpRefundQty,
                originalItem.notes,
                originalItem.hasNotes,
                originalItem.isPrepaidItem,
                originalItem.isGiftCard,
                originalItem.loyaltyPoints,
                originalItem.pointsForDollarAmount,
                originalItem.discountBundleId,
                originalItem.isEbtEligible,
                originalItem.tmpEBTpayed,
                originalItem.mIgnoreFields
        );
    }

    @Override
    public String getGuid() {
        return saleItemGuid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.SALE_ITEM_GUID))         values.put(SaleItemTable.SALE_ITEM_GUID, saleItemGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.ORDER_GUID))             values.put(SaleItemTable.ORDER_GUID, orderGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.ITEM_GUID))              values.put(SaleItemTable.ITEM_GUID, itemGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.QUANTITY))               values.put(SaleItemTable.QUANTITY, _decimalQty(qty));

        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.PRICE))                  values.put(SaleItemTable.PRICE, _decimal(price));
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.PRICE_TYPE))             _putEnum(values, SaleItemTable.PRICE_TYPE, priceType);

        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.DISCOUNTABLE))           values.put(SaleItemTable.DISCOUNTABLE, discountable);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.DISCOUNT))               values.put(SaleItemTable.DISCOUNT, _decimal(discount));
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.KITCHEN_PRINTED_QTY))    values.put(SaleItemTable.KITCHEN_PRINTED_QTY, _decimalQty(kitchenPrintedQty));
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.DISCOUNT_TYPE))          _putDiscount(values, SaleItemTable.DISCOUNT_TYPE, discountType);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.TAXABLE))                values.put(SaleItemTable.TAXABLE, isTaxable);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.TAX))                    values.put(SaleItemTable.TAX, _decimal(tax));
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.TAX2))                   values.put(SaleItemTable.TAX2, _decimal(tax2));
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.SEQUENCE))               values.put(SaleItemTable.SEQUENCE, sequence);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.PARENT_GUID))            values.put(SaleItemTable.PARENT_GUID, parentGuid);

        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.FINAL_GROSS_PRICE))      values.put(SaleItemTable.FINAL_GROSS_PRICE, _decimal(finalGrossPrice));
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.FINAL_TAX))              values.put(SaleItemTable.FINAL_TAX, _decimal(finalTax));
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.FINAL_DISCOUNT))         values.put(SaleItemTable.FINAL_DISCOUNT, _decimal(finalDiscount));
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.NOTES))                  values.put(SaleItemTable.NOTES, notes);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.HAS_NOTES))              values.put(SaleItemTable.HAS_NOTES, hasNotes);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.IS_PREPAID_ITEM))        values.put(SaleItemTable.IS_PREPAID_ITEM, isPrepaidItem);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.IS_GIFT_CARD))           values.put(SaleItemTable.IS_GIFT_CARD, isGiftCard);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.LOYALTY_POINTS))         values.put(SaleItemTable.LOYALTY_POINTS, _decimal(loyaltyPoints));
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.POINTS_FOR_DOLLAR_AMOUNT)) values.put(SaleItemTable.POINTS_FOR_DOLLAR_AMOUNT, pointsForDollarAmount);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.DISCOUNT_BUNDLE_ID))       values.put(SaleItemTable.DISCOUNT_BUNDLE_ID, discountBundleId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.EBT_ELIGIBLE))             values.put(SaleItemTable.EBT_ELIGIBLE, isEbtEligible);
        if (mIgnoreFields == null || !mIgnoreFields.contains(SaleItemTable.TMP_EBT_PAYED))            values.put(SaleItemTable.TMP_EBT_PAYED, _decimal(tmpEBTpayed, 6));

        return values;
    }


    public ContentValues updateFinalFields() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        values.put(SaleItemTable.FINAL_GROSS_PRICE, _decimal(finalGrossPrice));
        values.put(SaleItemTable.FINAL_TAX, _decimal(finalTax));
        values.put(SaleItemTable.FINAL_DISCOUNT, _decimal(finalDiscount));

        return values;
    }

    @Override
    public String getIdColumn() {
        return SaleItemTable.SALE_ITEM_GUID;
    }

    public static List<SaleOrderItemModel> getByOrderGuid(Context context, String orderGuid) {
        List<SaleOrderItemModel> items = new ArrayList<>();
        Cursor c = ProviderAction.query(URI_ORDER_ITEM)
                        .where(SaleItemTable.ORDER_GUID + " = ?", orderGuid)
                        .perform(context);

        while (c != null && c.moveToNext()) items.add(new SaleOrderItemModel(c));

        return items;
    }

    public static SaleOrderItemModel getByGuid(Context context, String guid) {

        Cursor c = ProviderAction.query(URI_ORDER_ITEM)
                        .where(SaleItemTable.SALE_ITEM_GUID + " = ?", guid)
                        .perform(context);

        if (c != null && c.moveToFirst()) return new SaleOrderItemModel(c);

        return null;
    }

}
