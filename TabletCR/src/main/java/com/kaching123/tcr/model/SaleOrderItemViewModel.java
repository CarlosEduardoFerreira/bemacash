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
    public AddonInfo modifier;

    public ArrayList<Unit> tmpUnit = new ArrayList<Unit>();

    private ArrayList<AddonInfo> addons;

    public int modifiersCount;
    public int addonsCount;
    public int optionalsCount;

    public boolean isSerializable;
    public boolean isTaxableOrder;
    public BigDecimal orderDiscount;
    public DiscountType orderDiscountType;
    public BigDecimal orderTransactionFee;

    public boolean isPcsUnit;
    public BigDecimal fullPrice;
    public Spannable subTitle;
    public BigDecimal finalPrice;

    public boolean isKitchenPrintable;

    public SaleOrderItemViewModel(SaleOrderItemModel itemModel,
                                  String description, String eanCode, String productCode, String unitsLabel,
                                  SaleOrderItemAddonModel modificator,
                                  String addonTitle,
                                  boolean isTaxableOrder,
                                  boolean isSerializable,
                                  BigDecimal orderDiscount,
                                  DiscountType orderDiscountType,
                                  BigDecimal orderTransactionFee,
                                  boolean isKitchenPrintable) {
        this.itemModel = itemModel;
        this.description = description;
        this.eanCode = eanCode;
        this.productCode = productCode;
        this.unitsLabel = unitsLabel;
        this.orderDiscount = orderDiscount;
        this.orderDiscountType = orderDiscountType;
        this.orderTransactionFee = orderTransactionFee;
        if (modificator != null) {
            this.modifier = new AddonInfo(modificator, addonTitle);
        }
        this.isPcsUnit = UnitUtil.isPcs(itemModel.priceType);
        updateFullPrice();
        updateAdditionalTitle();
        this.isTaxableOrder = isTaxableOrder;
        this.isKitchenPrintable = isKitchenPrintable;
        this.isSerializable = isSerializable;
    }

    public SaleOrderItemViewModel setItemModel(SaleOrderItemModel itemModel) {
        this.itemModel = itemModel;
        return this;
    }

    public void setModifier(AddonInfo modifier) {
        this.modifier = modifier;
        updateFullPrice();
        updateAdditionalTitle();
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

    public AddonInfo getModifier() {
        return modifier;
    }

    public void addAddon(AddonInfo addon) {
        if (addons == null)
            addons = new ArrayList<AddonInfo>();
        addons.add(addon);
        updateFullPrice();
        updateAdditionalTitle();
    }

    public ArrayList<AddonInfo> getAddons() {
        return addons;
    }

    private void updateAdditionalTitle() {
        this.subTitle = toAddonsString(this);
    }

    public void updateFullPrice() {
        if (itemModel == null || itemModel.price == null) {
            this.fullPrice = null;
        } else {
            this.fullPrice = itemModel.price.add(SaleOrderItemViewModel.getAddonsExtraCost(this));
        }
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
        return this.fullPrice;
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

    public static BigDecimal getAddonsExtraCost(SaleOrderItemViewModel item) {
        BigDecimal sum = BigDecimal.ZERO;
        if (item.modifier != null && item.modifier.addon.extraCost != null) {
            sum = sum.add(item.modifier.addon.extraCost);
        }
        if (item.addons != null) {
            for (AddonInfo a : item.addons) {
                if (a.addon.extraCost != null) {
                    sum = sum.add(a.addon.extraCost);
                }
            }
        }
        return sum;
    }

    public static Spannable toAddonsString(SaleOrderItemViewModel item) {

        SpannableStringBuilder builder = new SpannableStringBuilder(TextUtils.isEmpty(item.eanCode) ? (TextUtils.isEmpty(item.productCode) ? "" : item.productCode) : item.eanCode);
        if (builder.length() > 0)
            builder.append("\t");

        boolean needComa = false;
        if (item.getModifier() != null && item.getModifier().addonTitle != null) {
            builder.append(item.getModifier().addonTitle);
            needComa = true;
        }
        if (item.getAddons() != null) {
            for (AddonInfo a : item.getAddons()) {
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
                    builder.append("NO-").append(a.addonTitle);
                    builder.setSpan(new ForegroundColorSpan(NO_OPTIONS_COLOR), start, builder.length(), 0);
                } else {
                    builder.append(a.addonTitle);
                }
                needComa = true;
            }
        }
        return builder;//Joiner.on(", ").skipNulls().join(list);
    }
}
