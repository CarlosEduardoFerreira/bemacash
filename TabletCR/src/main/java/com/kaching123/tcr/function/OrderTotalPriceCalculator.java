package com.kaching123.tcr.function;

import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.Unit;
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
                            i.isTaxable(), i.getTax(),
                            i.unitsLabel, i.itemModel.priceType,
                            i
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

//                handler.handleItem(i.saleItemGuid, i.description,
// i.qty, i.totalPrice, i.unitLabel, i.priceType, itemSubTotal,
// itemTotal, itemFinalPrice, itemFinalDiscount, itemFinalTax);
                handler.handleItem(i.saleItemGuid, i.description, i.qty, i.totalPrice,
                        itemSubTotal, itemTotal, itemFinalPrice, itemFinalDiscount, itemFinalTax);

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

    public static SaleOrderCostInfo calculate(SaleOrderInfo info, Handler2 handler) {
        BigDecimal orderDiscount = info.orderDiscount;
        DiscountType orderDiscountType = info.orderDiscountType;

        HashMap<String, SaleItemInfo> map = info.map;

        BigDecimal subTotalItemTotal = BigDecimal.ZERO;
        BigDecimal totalItemDiscount = BigDecimal.ZERO;
        BigDecimal totalDiscountableItemTotal = BigDecimal.ZERO;

        Logger.d("TotalCost: -------- start -------- ");
        //calculate discountable value
        for (SaleItemInfo i : map.values()) {

            BigDecimal itemSubTotal = getSubTotal(i.qty, i.totalPrice);
            subTotalItemTotal = subTotalItemTotal.add(itemSubTotal);
            if (i.discountable) {
                BigDecimal itemDiscount = CalculationUtil.getItemDiscountValue(i.totalPrice, i.discount, i.discountType);
                BigDecimal itemSubDiscount = getSubTotal(i.qty, itemDiscount);
                BigDecimal itemTotal = itemSubTotal.subtract(itemSubDiscount);
                Logger.d("TotalCost: %s = %s; * %s = %s", i.description, itemDiscount, i.qty, itemSubDiscount);
                totalItemDiscount = totalItemDiscount.add(itemSubDiscount);
                totalDiscountableItemTotal = totalDiscountableItemTotal.add(itemTotal);
            }
        }

        Logger.d("TotalCost: total item discount = %s", totalItemDiscount);
        Logger.d("TotalCost: discountable = %s", totalDiscountableItemTotal);
        BigDecimal tmpOderDiscountVal = CalculationUtil.getDiscountValue(totalDiscountableItemTotal, orderDiscount, orderDiscountType);
        BigDecimal tmpOderDiscountPercent = CalculationUtil.getDiscountValueInPercent(totalDiscountableItemTotal, orderDiscount, orderDiscountType);

        /* calculate item order discount */
        Logger.d("TotalCost: --------------------- ");
        ArrayList<CalcItemInfo> calcItems = calcItemOrderDiscount(map, tmpOderDiscountVal, tmpOderDiscountPercent, handler);

        BigDecimal totalOrderPrice = BigDecimal.ZERO;
        BigDecimal totalItemTaxVatValue = BigDecimal.ZERO;
        for (CalcItemInfo ci : calcItems) {
            SaleItemInfo i = ci.itemInfo;
            BigDecimal itemFinalPrice = ci.finalItemPrice;//CalculationUtil.getSubTotal(BigDecimal.ONE, i.totalPrice, i.discount, i.discountType);
            BigDecimal itemFinalDiscount = ci.getFinalDiscount();//CalculationUtil.getDiscountValue(i.totalPrice, i.discount, i.discountType);
            BigDecimal itemTotal = itemFinalPrice;

            BigDecimal itemFinalTax = BigDecimal.ZERO;
            if (i.isTaxable && info.isTaxableOrder) {
                itemFinalTax = CalculationUtil.getTaxVatValue(itemFinalPrice, i.tax);
                Logger.d("TotalCost: item tax: %s; from %s = %s", i.description, itemFinalPrice, itemFinalTax);
                itemFinalPrice = itemFinalPrice.add(itemFinalTax);
            }

            itemFinalPrice = CalculationUtil.value(itemFinalPrice);

            BigDecimal itemFinalPrice2 = getSubTotal(i.qty, itemFinalPrice);

            if (BuildConfig.DEBUG) {
                BigDecimal tax2 = getSubTotal(i.qty, itemFinalTax);
                totalItemTaxVatValue = totalItemTaxVatValue.add(tax2);
                Logger.d("TotalCost: %s [%s * %s] = %s;\tdiscount = %s (%s)\tafter discount = %s (%s);\ttax = %s/%s (%s%%);\t final price = %s/%s",
                        i.description, i.qty, i.totalPrice, getSubTotal(i.qty, i.totalPrice),
                        itemFinalDiscount, getSubTotal(i.qty, itemFinalDiscount),
                        itemTotal, getSubTotal(i.qty, itemTotal), itemFinalTax, tax2, i.tax, itemFinalPrice, itemFinalPrice2);
            }
            if (handler != null) {
                handler.handleItem(i, itemFinalPrice, itemFinalDiscount, itemFinalTax);
            }
            //totalTaxVatValue = totalTaxVatValue.add(tax2);
            totalOrderPrice = totalOrderPrice.add(itemFinalPrice2);
        }
        BigDecimal totalTaxVatValue = totalOrderPrice.subtract(subTotalItemTotal).add(totalItemDiscount).add(tmpOderDiscountVal);
        /*totalTaxVatValue = CalculationUtil.value(totalTaxVatValue);
        totalOrderPrice = CalculationUtil.value(totalOrderPrice);*/

        Logger.d("TotalCost: --------------------- ");
        Logger.d("TotalCost: itemSubTotal:\t%s", subTotalItemTotal);
        Logger.d("TotalCost: oderDiscountVal:\t%s", tmpOderDiscountVal);
        Logger.d("TotalCost: oderDiscountPercent:\t%s", tmpOderDiscountPercent);
        Logger.d("TotalCost: totalTaxVatValue:\t%s", totalTaxVatValue);
        Logger.d("TotalCost: totalItemTaxVatValue:\t%s", totalItemTaxVatValue);
        Logger.d("TotalCost: totalOrderPrice:\t%s", totalOrderPrice);
        Logger.d("TotalCost: -------- end -------- ");


        return new SaleOrderCostInfo(info.isTaxableOrder, orderDiscount, orderDiscountType, tmpOderDiscountVal, subTotalItemTotal, totalTaxVatValue, totalItemDiscount, totalOrderPrice, totalDiscountableItemTotal);
    }

    private static ArrayList<CalcItemInfo> calcItemOrderDiscount(HashMap<String, SaleItemInfo> map, BigDecimal tmpOderDiscountVal, BigDecimal tmpOderDiscountPercent, Handler2 handler2) {
        Logger.d("TotalCost: check discount");

        Logger.d("TotalCost: tmpOderDiscountVal = %s", tmpOderDiscountVal);
        Logger.d("TotalCost: tmpOderDiscountPercent = %s", tmpOderDiscountPercent);

        Collection<SaleItemInfo> saleItemsInfo = map.values();
        ArrayList<CalcItemInfo> calcItems = new ArrayList<CalcItemInfo>(saleItemsInfo.size());
        if (saleItemsInfo.size() == 0) {
            return calcItems;
        }

        BigDecimal calcDiscountVal = BigDecimal.ZERO;
        for (SaleItemInfo i : saleItemsInfo) {
            BigDecimal itemDiscount = CalculationUtil.getDiscountValue(i.totalPrice, i.discount, i.discountType);
            BigDecimal itemFinalPrice = i.totalPrice.subtract(itemDiscount);
            BigDecimal itemOrderDiscount = BigDecimal.ZERO;
            if (BigDecimal.ZERO.compareTo(tmpOderDiscountVal) != 0 && i.discountable) {
                itemOrderDiscount = CalculationUtil.getDiscountValue(itemFinalPrice, tmpOderDiscountPercent, DiscountType.PERCENT);
                BigDecimal qtyOrderDiscount = getSubTotal(i.qty, itemOrderDiscount);
                calcDiscountVal = calcDiscountVal.add(qtyOrderDiscount);
                Logger.d("TotalCost: item order discount %s %s(%s) * %s = %s; * %s = %s", i.description, itemFinalPrice, itemDiscount, tmpOderDiscountPercent, itemOrderDiscount, i.qty, qtyOrderDiscount);
            }
            calcItems.add(new CalcItemInfo(i, itemFinalPrice, itemDiscount, itemOrderDiscount));
        }
        Logger.d("TotalCost: order discount by items = %s", calcDiscountVal);
        Collections.sort(calcItems, new Comparator<CalcItemInfo>() {
            @Override
            public int compare(CalcItemInfo l, CalcItemInfo r) {
                return -1 * (l.itemOderDiscount == null ? -1 : r.itemOderDiscount == null ? 1 : l.itemOderDiscount.compareTo(r.itemOderDiscount));
            }
        });
        CalcItemInfo biggestOrderDiscount = calcItems.get(0);

        BigDecimal discountDiff = tmpOderDiscountVal.subtract(calcDiscountVal);
        Logger.d("TotalCost: need adjust %s", discountDiff);

        BigDecimal qty = biggestOrderDiscount.itemInfo.qty;
        BigDecimal qtyInt = qty.setScale(0, RoundingMode.DOWN);
        if(discountDiff.compareTo(BigDecimal.ZERO) == 0)
            return calcItems;

        BigDecimal cents = discountDiff.multiply(CalculationUtil.ONE_HUNDRED).setScale(0,RoundingMode.HALF_UP);
        BigDecimal centsAbs = cents.abs();
        boolean negative = cents.compareTo(BigDecimal.ZERO) == -1;

        //qty is integer:  > 0 and doesn't have decimal part; cents != qty
        if (centsAbs.compareTo(qty) != 0 && qty.compareTo(BigDecimal.ONE) == 1 && qtyInt.compareTo(qty) == 0) {
            BigDecimal oneCent = negative ? ONE_CENT_NEGATIVE : ONE_CENT;
            CalcItemInfo splitedItem;
            Logger.d("TotalCost: cents and qty  = %s %s", centsAbs, qty);
            //cents > qty
            if(centsAbs.compareTo(qty) == 1){
                int d = centsAbs.intValue()/qty.intValue();
                BigDecimal firstAdjust = d > 0 ? oneCent.multiply(new BigDecimal(d)) : oneCent;
                Logger.d("TotalCost: cents > qty row will be splitted");
                updateDiscount(biggestOrderDiscount, firstAdjust);
                splitedItem = biggestOrderDiscount.split(BigDecimal.ONE);
                Logger.d("TotalCost: spletted to %s and %s", splitedItem.itemInfo.qty, biggestOrderDiscount.itemInfo.qty);

                BigDecimal alreadyAdjusted = getSubTotal(qty, firstAdjust);
                BigDecimal adjust = discountDiff.subtract(alreadyAdjusted);
                Logger.d("TotalCost: add to splitted item %s", adjust);
                updateDiscount(splitedItem, adjust);
            }else{
                Logger.d("TotalCost: cents < qty row will be splitted to %s and %s", centsAbs, biggestOrderDiscount.itemInfo.qty);
                splitedItem = biggestOrderDiscount.split(centsAbs);
                updateDiscount(splitedItem, oneCent);
            }

            calcItems.add(splitedItem);
            if (handler2 != null) {
                handler2.splitItem(splitedItem.itemInfo);
            }

        } else {
            Logger.d("TotalCost: adjust default");
            BigDecimal diff = CalculationUtil.divide(discountDiff, biggestOrderDiscount.itemInfo.qty, 3);
            Logger.d("TotalCost: adjust diff %s/%s = %s", discountDiff, biggestOrderDiscount.itemInfo.qty, diff);
            updateDiscount(biggestOrderDiscount, diff);
        }
        Logger.d("TotalCost:\t");
        return calcItems;
    }

    private static void updateDiscount(CalcItemInfo biggestOrderDiscount, BigDecimal diff) {
        biggestOrderDiscount.adjustItemOrderDiscount(diff);
        Logger.d("TotalCost: adjust item order discount for %s (%s): %s", biggestOrderDiscount.itemInfo.description, biggestOrderDiscount.itemInfo.qty, diff);
        Logger.d("TotalCost: after adjust item for %s = %s [%s]", biggestOrderDiscount.itemInfo.description, biggestOrderDiscount.itemOderDiscount, getSubTotal(biggestOrderDiscount.itemInfo.qty, biggestOrderDiscount.itemOderDiscount));
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
            this.map = new HashMap<String, SaleItemInfo>();
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
        public final BigDecimal tax;
        public final String unitLabel;
        public final PriceType priceType;
        public SaleOrderItemViewModel itemViewModel;

        public BigDecimal totalPrice;

        public SaleItemInfo(String saleItemGiud, String itemGiud, String description, BigDecimal qty,
                            BigDecimal totalPrice, boolean discountable, BigDecimal discount, DiscountType discountType, boolean isTaxable,
                            BigDecimal tax, String unitLabel, PriceType priceType, SaleOrderItemViewModel itemViewModel) {
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
            this.unitLabel = unitLabel;
            this.priceType = priceType;
            this.itemViewModel = itemViewModel;
        }

        public SaleItemInfo(String saleItemGiud, String itemGiud, String description, BigDecimal qty, BigDecimal totalPrice,
                            boolean discountable, BigDecimal discount, DiscountType discountType, boolean isTaxable, BigDecimal tax) {
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
            this.unitLabel = null;
            this.priceType = null;
        }

        public SaleItemInfo copy(BigDecimal qty) {
            return new SaleItemInfo(saleItemGuid, itemGiud, description, qty, totalPrice, discountable, discount, discountType, isTaxable, tax, unitLabel, priceType, itemViewModel);
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
        public final BigDecimal totalDiscountableItemTotal;

        public SaleOrderCostInfo(boolean isTaxableOrder, BigDecimal orderDiscount, DiscountType orderDiscountType, BigDecimal tmpOderDiscountVal, BigDecimal subTotalItemTotal, BigDecimal totalTaxVatValue, BigDecimal totalItemDiscount, BigDecimal totalOrderPrice, BigDecimal totalDiscountableItemTotal) {
            this.isTaxableOrder = isTaxableOrder;
            this.orderDiscount = orderDiscount;
            this.orderDiscountType = orderDiscountType;
            this.tmpOderDiscountVal = tmpOderDiscountVal;
            this.subTotalItemTotal = subTotalItemTotal;
            this.totalTaxVatValue = totalTaxVatValue;
            this.totalItemDiscount = totalItemDiscount;
            this.totalOrderPrice = totalOrderPrice;
            this.totalDiscountableItemTotal = totalDiscountableItemTotal;
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

        boolean isDiscountable();

        boolean isSerializable();

        BigDecimal getTax();

        BigDecimal getDiscount();

        DiscountType getDiscountType();

        BigDecimal getOrderDiscount();

        DiscountType getOrderDiscountType();

        String getNotes();
    }
}
