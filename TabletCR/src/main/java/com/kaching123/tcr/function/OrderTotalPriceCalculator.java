package com.kaching123.tcr.function;

import android.util.Log;

import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;

/**
 * Created by gdubina on 17/12/13.
 */
public final class OrderTotalPriceCalculator {

    private static final BigDecimal ONE_CENT = new BigDecimal("0.01");
    private static final BigDecimal ONE_CENT_NEGATIVE = new BigDecimal("-0.01");

    OrderTotalPriceCalculator() {
    }

    public static SaleOrderInfo toOrderInfo(List<SaleOrderItemViewModel> items) {
        SaleOrderInfo orderInfo = null;
        for (SaleOrderItemViewModel i : items) {
            if (orderInfo == null) {
                orderInfo = new SaleOrderInfo(i.isTaxableOrder, i.orderDiscount, i.orderDiscountType, i.orderTransactionFee);
            }

            orderInfo.map.put(i.getSaleItemGuid(),
                    new SaleItemInfo(
                            i.getSaleItemGuid(),
                            i.itemModel.itemGuid,
                            i.description,
                            i.getQty(), i.getPrice(),
                            i.isDiscountable(), i.getDiscount(), i.getDiscountType(),
                            i.isTaxable(), i.getTax(), i.getTax2(),
                            i.unitsLabel, i.itemModel.priceType,
                            i,
                            i.isEbtEligible(),
                            i.itemModel.tmpEBTpayed
                    )
            );
        }
        return orderInfo;
    }

    public static void calculate(List<SaleOrderItemViewModel> items, BigDecimal tips, final Handler handler) {
        SaleOrderInfo orderInfo = toOrderInfo(items);
        if (orderInfo == null)
            return;
        SaleOrderCostInfo result = calculate(orderInfo, new Handler2() {

            @Override
            public void splitItem(SaleItemInfo item) {

            }

            @Override
            public void handleItem(SaleItemInfo i, BigDecimal itemFinalPrice, BigDecimal itemFinalDiscount, BigDecimal itemFinalTax) {
                BigDecimal itemSubTotal = getSubTotal(i.qty, i.totalPrice);
                BigDecimal itemTotal = getSubTotal(i.qty, i.totalPrice, i.discount, i.discountType);
                BigDecimal itemEbtTotal = i.isEbtEligible ? itemSubTotal: BigDecimal.ZERO;

                handler.handleItem(i.saleItemGuid, i.description, i.qty, i.totalPrice,
                        itemSubTotal, itemTotal, itemEbtTotal, itemFinalPrice, itemFinalDiscount, itemFinalTax);

            }
        });
        handler.handleTotal(result.totalItemDiscount.add(result.tmpOderDiscountVal), result.subTotalItemTotal, result.totalTaxVatValue, result.totalOrderPrice, tips);
    }

    public static void calculate(List<SaleOrderItemViewModel> items, BigDecimal tips, final Handler2 handler) {
        SaleOrderInfo orderInfo = toOrderInfo(items);
        if (orderInfo == null)
            return;
        calculate(orderInfo, handler);
    }

    public static SaleOrderCostInfo calculate(SaleOrderInfo info) {
        return calculate(info, null);
    }

    static BigDecimal zeroDecimals = new BigDecimal(0.00000);

    public static SaleOrderCostInfo calculate(SaleOrderInfo info, Handler2 handler) {

        Log.d("BemaCarl20","OrderTotalPriceCalculator ------------------  start calculate  ----------------------");

        HashMap<String, SaleItemInfo> map       = info.map;

        BigDecimal      itemSubTotal        = BigDecimal.ZERO;
        BigDecimal      itemDiscountTotal   = BigDecimal.ZERO;

        BigDecimal      orderDiscount       = info.orderDiscount;
        DiscountType    orderDiscountType   = info.orderDiscountType;
        BigDecimal      orderTax            = BigDecimal.ZERO;
        BigDecimal      orderTotal          = BigDecimal.ZERO;

        BigDecimal      totalEbtOrderPrice  = BigDecimal.ZERO;

        for (SaleItemInfo i1 : map.values()) {

            BigDecimal itemValue    = i1.totalPrice;
            BigDecimal itemDiscount = BigDecimal.ZERO;

            itemSubTotal = itemSubTotal.add(itemValue.multiply(i1.qty));

            if (i1.discountable) {
                itemDiscount        = CalculationUtil.getItemDiscountValue(itemValue, i1.discount, i1.discountType);
                itemDiscountTotal = itemDiscountTotal.add(itemDiscount.multiply(i1.qty));
            }

            if (i1.isEbtEligible){
                totalEbtOrderPrice = totalEbtOrderPrice.add(itemValue.multiply(i1.qty));
            }

        }

        BigDecimal subTotalDiscontable = itemSubTotal.subtract(itemDiscountTotal);

        for (SaleItemInfo i2 : map.values()) {

            BigDecimal itemFinalPrice               = i2.totalPrice;
            BigDecimal itemFinalPriceDiscount       = BigDecimal.ZERO;
            BigDecimal itemFinalPriceTax            = BigDecimal.ZERO;

            if(i2.discountable) {

                itemFinalPriceDiscount    = CalculationUtil.getItemDiscountValue(itemFinalPrice, i2.discount, i2.discountType);

                Log.d("BemaCarl20", "OrderTotalPriceCalculator.itemFinalPrice           2: " + itemFinalPrice);
                Log.d("BemaCarl20", "OrderTotalPriceCalculator.itemFinalPriceDiscount   2: " + itemFinalPriceDiscount);

                if (orderDiscount.compareTo(zeroDecimals) == 1) {
                    BigDecimal discountByItemPercent    = orderDiscount;

                    if (orderDiscountType == DiscountType.VALUE) {
                        discountByItemPercent       = CalculationUtil.getDiscountValueInPercent(subTotalDiscontable, orderDiscount, orderDiscountType);
                    }
                    BigDecimal discountByItemValue  = CalculationUtil.getItemDiscountValue(itemFinalPrice.subtract(itemFinalPriceDiscount), discountByItemPercent, DiscountType.PERCENT);

                    itemFinalPriceDiscount          = itemFinalPriceDiscount.add(discountByItemValue);

                    Log.d("BemaCarl20", "OrderTotalPriceCalculator.discountByItemPercent      3: " + discountByItemPercent);
                    Log.d("BemaCarl20", "OrderTotalPriceCalculator.discountByItemValue        3: " + discountByItemValue);
                    Log.d("BemaCarl20", "OrderTotalPriceCalculator.itemFinalPriceDiscount     3: " + itemFinalPriceDiscount);
                }

                itemFinalPrice              = itemFinalPrice.subtract(itemFinalPriceDiscount);

            }

            Log.d("BemaCarl20", "OrderTotalPriceCalculator.itemFinalPrice         5.1: " + itemFinalPrice);

            if (i2.isTaxable && info.isTaxableOrder) {
                BigDecimal itemFinalPriceTaxable = BigDecimal.ONE.subtract(i2.ebtPayed).multiply(itemFinalPrice);

                itemFinalPriceTax = CalculationUtil.getTaxVatValue(itemFinalPriceTaxable, i2.tax);
                itemFinalPrice = itemFinalPrice.add(itemFinalPriceTax);
                if (TcrApplication.getCountryFunctionality().isMultiTaxGroup()) {
                    BigDecimal itemFinalPriceTax2 = CalculationUtil.getTaxVatValue(itemFinalPriceTaxable, i2.tax2);
                    itemFinalPrice = itemFinalPrice.add(itemFinalPriceTax2);
                    itemFinalPriceTax = itemFinalPriceTax.add(itemFinalPriceTax2);
                }
                Log.d("BemaCarl20", "OrderTotalPriceCalculator.itemFinalPriceTaxable     5.3: " + itemFinalPriceTaxable);
                Log.d("BemaCarl20", "OrderTotalPriceCalculator.itemFinalPriceTax         5.3: " + itemFinalPriceTax);
                Log.d("BemaCarl20", "OrderTotalPriceCalculator.itemFinalPrice            5.3: " + itemFinalPrice);
                Log.d("BemaCarl20", "OrderTotalPriceCalculator.orderTax                  5.3: " + orderTax);
                orderTax = orderTax.add(itemFinalPriceTax.multiply(i2.qty));
                Log.d("BemaCarl20", "OrderTotalPriceCalculator.orderTax                  5.4: " + orderTax);
            }

            orderTotal = orderTotal.add(itemFinalPrice.multiply(i2.qty));

            Log.d("BemaCarl20", "OrderTotalPriceCalculator.handler                  6: " + handler);
            if (handler != null) {
                handler.handleItem(i2, itemFinalPrice, itemFinalPriceDiscount, itemFinalPriceTax);
            }

        }

        BigDecimal tmpOderDiscountVal = BigDecimal.ZERO;
        if (orderDiscount.compareTo(zeroDecimals) == 1) {
            tmpOderDiscountVal = CalculationUtil.getDiscountValue(subTotalDiscontable, orderDiscount, orderDiscountType);
        }

        //orderTotal                 = orderTotal.add(itemSubTotal);
        //orderTotal                 = orderTotal.subtract(itemDiscountTotal);
        //orderTotal                 = orderTotal.subtract(tmpOderDiscountVal);
        //orderTotal                 = orderTotal.add(orderTax);

        Log.d("BemaCarl20", "OrderTotalPriceCalculator.orderDiscount            7: " + orderDiscount);
        Log.d("BemaCarl20", "OrderTotalPriceCalculator.orderDiscountType        7: " + orderDiscountType);
        Log.d("BemaCarl20", "OrderTotalPriceCalculator.tmpOderDiscountVal       7: " + tmpOderDiscountVal);
        Log.d("BemaCarl20", "OrderTotalPriceCalculator.itemSubTotal             7: " + itemSubTotal);
        Log.d("BemaCarl20", "OrderTotalPriceCalculator.orderTax                 7: " + orderTax);
        Log.d("BemaCarl20", "OrderTotalPriceCalculator.itemDiscountTotal        7: " + itemDiscountTotal);
        Log.d("BemaCarl20", "OrderTotalPriceCalculator.orderTotal               7: " + orderTotal);
        Log.d("BemaCarl20", "OrderTotalPriceCalculator.subTotalDiscontable      7: " + subTotalDiscontable);
        Log.d("BemaCarl20", "OrderTotalPriceCalculator.totalEbtOrderPrice       7: " + totalEbtOrderPrice);

        Log.d("BemaCarl20","OrderTotalPriceCalculator ------------------ finish calculate ----------------------");

        return new SaleOrderCostInfo(info.isTaxableOrder, orderDiscount, orderDiscountType, tmpOderDiscountVal,
                itemSubTotal, orderTax, itemDiscountTotal, orderTotal, subTotalDiscontable, totalEbtOrderPrice);

    }


    private static class CalcItemInfo {
        SaleItemInfo itemInfo;
        BigDecimal itemPrice;
        BigDecimal finalItemPrice;
        BigDecimal itemDiscount;
        BigDecimal itemOderDiscount;

        private CalcItemInfo(SaleItemInfo itemInfo, BigDecimal itemPrice, BigDecimal itemDiscount, BigDecimal itemOderDiscount) {
            this.itemInfo = itemInfo;
            this.itemPrice = itemPrice;
            this.itemDiscount = itemDiscount;
            setItemOderDiscount(itemOderDiscount);
        }

        private void setItemOderDiscount(BigDecimal value) {
            finalItemPrice = itemPrice;
            this.itemOderDiscount = value;
            if (itemOderDiscount != null) {
                finalItemPrice = finalItemPrice.subtract(itemOderDiscount);
            }
        }

        public void adjustItemOrderDiscount(BigDecimal value) {
            if (this.itemOderDiscount == null)
                return;
            setItemOderDiscount(CalculationUtil.value(this.itemOderDiscount.add(value)));
        }

        public BigDecimal getFinalDiscount() {
            return (itemDiscount == null ? BigDecimal.ZERO : itemDiscount).add(itemOderDiscount == null ? BigDecimal.ZERO : itemOderDiscount);
        }

        /**
         * split one to 2 rows - qty in returned row should newQty
         *
         * @param newQty
         * @return
         */
        public CalcItemInfo split(BigDecimal newQty) {
            itemInfo = itemInfo.copy(itemInfo.qty.subtract(newQty));
            return new CalcItemInfo(itemInfo.copy(newQty), itemPrice, itemDiscount, itemOderDiscount);
        }
    }

    public interface Handler {

        ///void handleItem(String saleItemGuid, String description, BigDecimal qty, BigDecimal itemPriceWithAddons,String unitLabel, PriceType priceType, BigDecimal itemSubTotal, BigDecimal itemTotal, BigDecimal itemFinalPrice, BigDecimal itemFinalDiscount, BigDecimal itemFinalTax);
        void handleItem(String saleItemGuid,
                        String description, BigDecimal qty,
                        BigDecimal itemPriceWithAddons,
                        BigDecimal itemSubTotal,
                        BigDecimal itemTotal,
                        BigDecimal itemEbtTotal,
                        BigDecimal itemFinalPrice, BigDecimal itemFinalDiscount, BigDecimal itemFinalTax);

        void handleTotal(BigDecimal totalDiscount, BigDecimal subTotalItemTotal, BigDecimal totalTaxVatValue, BigDecimal totalOrderPrice, BigDecimal tipsValue);

    }

    public static interface Handler2 {
        void splitItem(SaleItemInfo item);

        void handleItem(SaleItemInfo i, BigDecimal itemFinalPrice, BigDecimal itemFinalDiscount, BigDecimal itemFinalTax);
    }

    public static class SaleOrderInfo {

        public final boolean isTaxableOrder;
        public final BigDecimal orderDiscount;
        public final DiscountType orderDiscountType;
        public final BigDecimal transactionFee;
        public final HashMap<String, SaleItemInfo> map;

        public SaleOrderInfo(boolean isTaxableOrder, BigDecimal orderDiscount, DiscountType orderDiscountType, BigDecimal transactionFee, HashMap<String, SaleItemInfo> map) {
            this.isTaxableOrder = isTaxableOrder;
            this.orderDiscount = orderDiscount;
            this.orderDiscountType = orderDiscountType;
            this.transactionFee = transactionFee;
            this.map = map;
        }

        public SaleOrderInfo(boolean isTaxableOrder, BigDecimal orderDiscount, DiscountType orderDiscountType, BigDecimal transactionFee) {
            this.isTaxableOrder = isTaxableOrder;
            this.orderDiscount = orderDiscount;
            this.orderDiscountType = orderDiscountType;
            this.transactionFee = transactionFee;
            this.map = new HashMap<>();
        }
    }

    public static class SaleItemInfo {
        public String saleItemGuid;
        public final String itemGiud;
        public final String description;
        public final BigDecimal qty;
        public final boolean discountable;
        public final BigDecimal discount;
        public final DiscountType discountType;
        public final boolean isTaxable;
        public final boolean isEbtEligible;
        public final BigDecimal ebtPayed;
        public final BigDecimal tax;
        public final BigDecimal tax2;
        public final String unitLabel;
        public final PriceType priceType;
        public SaleOrderItemViewModel itemViewModel;

        public BigDecimal totalPrice;

        public SaleItemInfo(String saleItemGiud, String itemGiud, String description, BigDecimal qty,
                            BigDecimal totalPrice, boolean discountable, BigDecimal discount, DiscountType discountType, boolean isTaxable,
                            BigDecimal tax, BigDecimal tax2, String unitLabel, PriceType priceType,
                            SaleOrderItemViewModel itemViewModel, boolean isEbtEligible, BigDecimal ebtPayed) {
            this.saleItemGuid = saleItemGiud;
            this.itemGiud = itemGiud;
            this.description = description;
            this.qty = qty;
            this.totalPrice = totalPrice;
            this.discountable = discountable;
            this.discount = discount;
            this.discountType = discountType;
            this.isTaxable = isTaxable;
            this.isEbtEligible = isEbtEligible;
            this.tax = tax;
            this.tax2 = tax2;
            this.unitLabel = unitLabel;
            this.priceType = priceType;
            this.itemViewModel = itemViewModel;
            this.ebtPayed = ebtPayed;
        }

        public SaleItemInfo(String saleItemGiud, String itemGiud, String description, BigDecimal qty, BigDecimal totalPrice,
                            boolean discountable, BigDecimal discount, DiscountType discountType,
                            boolean isTaxable, BigDecimal tax, BigDecimal tax2, boolean isEbtEligible, BigDecimal ebtPayed) {
            this.saleItemGuid = saleItemGiud;
            this.itemGiud = itemGiud;
            this.description = description;
            this.qty = qty;
            this.totalPrice = totalPrice;
            this.discountable = discountable;
            this.discount = discount;
            this.discountType = discountType;
            this.isTaxable = isTaxable;
            this.tax = tax;
            this.tax2 = tax2;
            this.unitLabel = null;
            this.priceType = null;
            this.isEbtEligible = isEbtEligible;
            this.ebtPayed = ebtPayed;

        }

        public SaleItemInfo copy(BigDecimal qty) {
            return new SaleItemInfo(saleItemGuid, itemGiud, description, qty, totalPrice, discountable,
                    discount, discountType, isTaxable, tax, tax2, unitLabel, priceType, itemViewModel, isEbtEligible, ebtPayed);
        }
    }

    public static class SaleOrderCostInfo {
        public final boolean isTaxableOrder;
        public final BigDecimal orderDiscount;
        public final DiscountType orderDiscountType;
        public final BigDecimal tmpOderDiscountVal;
        public final BigDecimal subTotalItemTotal;
        public final BigDecimal totalTaxVatValue;
        public final BigDecimal totalItemDiscount;
        public final BigDecimal totalOrderPrice;
        public final BigDecimal totalOrderEbtPrice;
        public final BigDecimal totalDiscountableItemTotal;

        public SaleOrderCostInfo(boolean isTaxableOrder, BigDecimal orderDiscount, DiscountType orderDiscountType,
                                 BigDecimal tmpOderDiscountVal, BigDecimal subTotalItemTotal, BigDecimal totalTaxVatValue,
                                 BigDecimal totalItemDiscount, BigDecimal totalOrderPrice, BigDecimal totalDiscountableItemTotal, BigDecimal totalOrderEbtPrice) {
            this.isTaxableOrder = isTaxableOrder;
            this.orderDiscount = orderDiscount;
            this.orderDiscountType = orderDiscountType;
            this.tmpOderDiscountVal = tmpOderDiscountVal;
            this.subTotalItemTotal = subTotalItemTotal;
            this.totalTaxVatValue = totalTaxVatValue;
            this.totalItemDiscount = totalItemDiscount;
            this.totalOrderPrice = totalOrderPrice;
            this.totalDiscountableItemTotal = totalDiscountableItemTotal;
            this.totalOrderEbtPrice = totalOrderEbtPrice;
        }

        public BigDecimal getOrderDiscount() {
            return totalItemDiscount.add(tmpOderDiscountVal);
        }
    }

    private static class DiscountCalcResult {
        ArrayList<CalcItemInfo> items;
        SaleItemInfo extraItem;

        private DiscountCalcResult(ArrayList<CalcItemInfo> items, SaleItemInfo extraItem) {
            this.items = items;
            this.extraItem = extraItem;
        }
    }

    public static interface IOrderItem {
        String getSaleItemGuid();

        BigDecimal getQty();

        BigDecimal getPrice();

        boolean isTaxable();

        boolean isTaxableOrder();

        boolean isEbtEligible();

        boolean isDiscountable();

        boolean isSerializable();

        BigDecimal getTax();

        BigDecimal getTax2();

        BigDecimal getDiscount();

        DiscountType getDiscountType();

        BigDecimal getOrderDiscount();

        DiscountType getOrderDiscountType();

        String getNotes();
    }
}
