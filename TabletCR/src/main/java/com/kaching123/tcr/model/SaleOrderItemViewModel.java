package com.kaching123.tcr.model;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.IOrderItem;
import com.kaching123.tcr.util.UnitUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gdubina on 07/11/13.
 */
public class SaleOrderItemViewModel implements IOrderItem, Serializable {
    private static final int NO_OPTIONS_COLOR = Color.rgb(210, 49, 64);

    public SaleOrderItemModel itemModel;
    public String description;
    public String eanCode;
    public String productCode;
    public String unitsLabel;
    public ArrayList<AddonInfo> modifiers;

    public ArrayList<Unit> tmpUnit = new ArrayList<>();

    public TaxGroupModel taxGroup1;
    public TaxGroupModel taxGroup2;

    public boolean isSerializable;
    public boolean isTaxableOrder;
    public BigDecimal orderDiscount;
    public DiscountType orderDiscountType;
    public BigDecimal orderTransactionFee;

    public boolean isPcsUnit;
    public Spannable subTitle;
    public BigDecimal finalPrice;
  //  public BigDecimal finalEbtPrice;

    public boolean isKitchenPrintable;
    public boolean isPrepaidItem;
    public boolean isGiftCard;

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
                                  boolean isKitchenPrintable,
                                  boolean isPrepaidItem,
                                  boolean isGiftCard,
                                  TaxGroupModel taxGroup1,
                                  TaxGroupModel taxGroup2) {
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
        this.isPrepaidItem = isPrepaidItem;
        this.isGiftCard = isGiftCard;
        this.taxGroup1 = taxGroup1;
        this.taxGroup2 = taxGroup2;
    }

    public static Comparator<SaleOrderItemViewModel> filterOrderItem= new Comparator<SaleOrderItemViewModel>(){
        @Override
        public int compare(SaleOrderItemViewModel lhs, SaleOrderItemViewModel rhs) {
            return lhs.description.compareTo(rhs.description);
        }
    };

    public SaleOrderItemViewModel setItemModel(SaleOrderItemModel itemModel) {
        this.itemModel = itemModel;
        return this;
    }

    public boolean hasModifiers() {
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

    public String getUniqSerialCodesString() {
        final ArrayList<Unit> units = tmpUnit;
        if (!units.isEmpty()) {
            final StringBuilder builder = new StringBuilder();
            for (Unit unit : units) {
                if (!TextUtils.isEmpty(unit.serialCode)) {
                    if (!builder.toString().contains(unit.serialCode + "\n")) {
                        builder.append(unit.serialCode).append("\n");
                    }
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

    public BigDecimal getPriceWithMods() {
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
    public boolean isEbtEligible() {
        return this.itemModel.isEbtEligible;
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

    public BigDecimal getTax2() {
        return this.itemModel.tax2;
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

    public static class AddonInfo implements Serializable{
        public SaleOrderItemAddonModel addon;
        public String addonTitle;
        public String groupName;
        public int orderNum;
        public int groupOrderNum;

        public AddonInfo(SaleOrderItemAddonModel addon, String addonTitle, String groupName, int groupOrderNum, int orderNum) {
            this.addon = addon;
            this.addonTitle = addonTitle;
            this.groupName = groupName;
            this.groupOrderNum = groupOrderNum;
            this.orderNum = orderNum;
        }
    }


    private static Comparator<AddonInfo> comparator = new Comparator<AddonInfo>() {
        @Override
        public int compare(AddonInfo lhs, AddonInfo rhs) {
            int diff;

            diff = lhs.addon.type.ordinal() - rhs.addon.type.ordinal();
            if (diff != 0)
                return diff;

            diff = lhs.groupOrderNum - rhs.groupOrderNum;
            if (diff != 0)
                return diff;

            diff = lhs.orderNum - rhs.orderNum;
            if (diff != 0)
                return diff;

            return  lhs.addonTitle.compareTo(rhs.addonTitle);
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

        for (AddonInfo a : mods) {
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
                builder.append(TcrApplication.get().getResources().getString(R.string.sale_item_noopition_prefix)).append("-").append(a.addonTitle);
                builder.setSpan(new ForegroundColorSpan(NO_OPTIONS_COLOR), start, builder.length(), 0);
            } else {
                builder.append(a.addonTitle);
            }
            needComa = true;
        }

        return builder;
    }
}
