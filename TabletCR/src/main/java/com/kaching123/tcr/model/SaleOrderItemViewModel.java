package com.kaching123.tcr.model;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.kaching123.tcr.function.OrderTotalPriceCalculator.IOrderItem;
import com.kaching123.tcr.util.UnitUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gdubina on 07/11/13.
 */
public class SaleOrderItemViewModel implements IOrderItem {
    private static final int NO_OPTIONS_COLOR = Color.rgb(210, 49, 64);

    public SaleOrderItemModel itemModel;
    public String description;
    public String eanCode;
    public String productCode;
    public String unitsLabel;
    public ArrayList<AddonInfo> modifiers;

    public ArrayList<Unit> tmpUnit = new ArrayList<Unit>();


    public boolean isSerializable;
    public boolean isTaxableOrder;
    public BigDecimal orderDiscount;
    public DiscountType orderDiscountType;
    public BigDecimal orderTransactionFee;

    public boolean isPcsUnit;
    public Spannable subTitle;
    public BigDecimal finalPrice;

    public boolean isKitchenPrintable;

    public SaleOrderItemViewModel(SaleOrderItemModel itemModel,
                                  String description,
                                  ArrayList<AddonInfo> modifiers,
                                  String eanCode,
                                  String productCode, String unitsLabel,
                                  boolean isTaxableOrder,
                                  boolean isSerializable,
                                  BigDecimal orderDiscount,
                                  DiscountType orderDiscountType,
                                  BigDecimal orderTransactionFee,
                                  boolean isKitchenPrintable) {
        this.itemModel = itemModel;
        this.description = description;
        this.modifiers = modifiers;
        this.eanCode = eanCode;
        this.productCode = productCode;
        this.unitsLabel = unitsLabel;
        this.orderDiscount = orderDiscount;
        this.orderDiscountType = orderDiscountType;
        this.orderTransactionFee = orderTransactionFee;
        this.isPcsUnit = UnitUtil.isPcs(itemModel.priceType);
        this.isTaxableOrder = isTaxableOrder;
        this.isKitchenPrintable = isKitchenPrintable;
        this.isSerializable = isSerializable;
    }

    public SaleOrderItemViewModel setItemModel(SaleOrderItemModel itemModel) {
        this.itemModel = itemModel;
        return this;
    }

    public boolean hasModifiers(){
        return modifiers != null && !modifiers.isEmpty();
    }

    public String getSerialCodesString() {
        final ArrayList<Unit> units = tmpUnit;
        if (!units.isEmpty()) {
            final StringBuilder builder = new StringBuilder();
            for (Unit unit : units) {
                if (!TextUtils.isEmpty(unit.serialCode)) {
                    builder.append(unit.serialCode).append("\n");
                }
            }
            return builder.toString();
        }
        return "";
    }

    @Override
    public String getSaleItemGuid() {
        return this.itemModel.saleItemGuid;
    }

    @Override
    public BigDecimal getQty() {
        return this.itemModel.qty;
    }

    @Override
    public BigDecimal getPrice() {
        return getPriceWithMods();
    }

    public BigDecimal getPriceWithMods(){
        if (itemModel == null || itemModel.price == null)
            return null;

        return itemModel.price.add(getModsExtraCost());
    }

    private BigDecimal getModsExtraCost() {
        BigDecimal sum = BigDecimal.ZERO;
        if (modifiers != null) {
            for (AddonInfo a : modifiers) {
                if (a.addon.type == ModifierType.OPTIONAL)
                    continue;

                if (a.addon.extraCost != null) {
                    sum = sum.add(a.addon.extraCost);
                }
            }
        }
        return sum;
    }



    @Override
    public boolean isTaxable() {
        return this.itemModel.isTaxable;
    }

    @Override
    public boolean isTaxableOrder() {
        return this.isTaxableOrder;
    }

    @Override
    public boolean isDiscountable() {
        return this.itemModel.discountable;
    }

    @Override
    public boolean isSerializable() {
        return this.isSerializable;
    }

    @Override
    public BigDecimal getTax() {
        return this.itemModel.tax;
    }

    @Override
    public BigDecimal getDiscount() {
        return this.itemModel.discount;
    }

    @Override
    public DiscountType getDiscountType() {
        return this.itemModel.discountType;
    }

    @Override
    public BigDecimal getOrderDiscount() {
        return this.orderDiscount;
    }

    @Override
    public DiscountType getOrderDiscountType() {
        return this.orderDiscountType;
    }

    @Override
    public String getNotes() {
        return this.itemModel.notes;
    }

    public static class AddonInfo {
        public SaleOrderItemAddonModel addon;
        public String addonTitle;

        public AddonInfo(SaleOrderItemAddonModel addon, String addonTitle) {
            this.addon = addon;
            this.addonTitle = addonTitle;
        }
    }


    private static Comparator<AddonInfo> comparator = new Comparator<AddonInfo>() {
        @Override
        public int compare(AddonInfo lhs, AddonInfo rhs) {
            return lhs.addon.type.ordinal() - rhs.addon.type.ordinal();
        }
    };

    public Spannable toAddonsString() {

        SpannableStringBuilder builder =
                new SpannableStringBuilder(TextUtils.isEmpty(eanCode) ? (TextUtils.isEmpty(productCode) ? "" : productCode) : eanCode);
        if (builder.length() > 0)
            builder.append("\t");

        if (!hasModifiers())
            return builder;

        ArrayList<AddonInfo> mods = modifiers;
        Collections.sort(mods, comparator);

        boolean needComa = false;

        for (AddonInfo a : mods){
            if (a.addonTitle == null)
                continue;
            if (needComa) {
                builder.append(", ");
            }
            if (a.addon.type == ModifierType.OPTIONAL) {
                int start = 0;
                if (builder.length() > 0) {
                    start = builder.length() - 1;
                }
                builder.append("Sem-").append(a.addonTitle);
                builder.setSpan(new ForegroundColorSpan(NO_OPTIONS_COLOR), start, builder.length(), 0);
            } else {
                builder.append(a.addonTitle);
            }
            needComa = true;
        }

        return builder;
    }
}
