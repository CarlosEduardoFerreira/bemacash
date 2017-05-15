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

    public static SaleOrderCostInfo calculate3(SaleOrderInfo info, Handler2 handler) {
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
        Log.d("BemaCarl15", "OrderTotalPriceCalculator.calculate.tmpOderDiscountVal1: " + tmpOderDiscountVal);
        ArrayList<CalcItemInfo> calcItems = calcItemOrderDiscount(map, tmpOderDiscountVal, tmpOderDiscountPercent, handler);

        BigDecimal totalOrderPrice = BigDecimal.ZERO;
        BigDecimal totalItemTaxVatValue = BigDecimal.ZERO;
        BigDecimal totalEbtOrderPrice = BigDecimal.ZERO;

        for (CalcItemInfo ci : calcItems) {
            SaleItemInfo i = ci.itemInfo;
            BigDecimal itemFinalPrice = ci.finalItemPrice;//CalculationUtil.getSubTotal(BigDecimal.ONE, i.totalPrice, i.discount, i.discountType);
            BigDecimal itemFinalDiscount = ci.getFinalDiscount();//CalculationUtil.getDiscountValue(i.totalPrice, i.discount, i.discountType);
            BigDecimal itemTotal = itemFinalPrice;

            Log.d("BemaCarl17","OrderTotalPriceCalculator.totalOrderPrice        1: " + totalOrderPrice);
            Log.d("BemaCarl17","OrderTotalPriceCalculator.totalItemTaxVatValue   1: " + totalItemTaxVatValue);
            Log.d("BemaCarl17","OrderTotalPriceCalculator.totalEbtOrderPrice     1: " + totalEbtOrderPrice);
            Log.d("BemaCarl17","OrderTotalPriceCalculator.itemFinalPrice         1: " + itemFinalPrice);
            Log.d("BemaCarl17","OrderTotalPriceCalculator.itemFinalDiscount      1: " + itemFinalDiscount);
            Log.d("BemaCarl17","OrderTotalPriceCalculator.itemTotal              1: " + itemTotal);

            if (i.isEbtEligible){
                totalEbtOrderPrice = totalEbtOrderPrice.add(getSubTotal(i.qty, itemFinalPrice));
            }

            BigDecimal itemFinalTax = BigDecimal.ZERO;
            if (i.isTaxable && info.isTaxableOrder) {
                BigDecimal taxableFinalPrice = BigDecimal.ONE.subtract(i.ebtPayed).multiply(itemFinalPrice);
                itemFinalTax = CalculationUtil.getTaxVatValue(taxableFinalPrice, i.tax);
                if (TcrApplication.getCountryFunctionality().isMultiTaxGroup()) {//.isCurrentCountryUsesMultiTax()) {//.isEcuadorVersion()) {
                    BigDecimal tax2 = CalculationUtil.getTaxVatValue(taxableFinalPrice, i.tax2);
                    Logger.d("TotalCost: [ecuador] tax2: %s;", tax2);
                    itemFinalTax = itemFinalTax.add(tax2);
                }
                Logger.d("TotalCost: item tax: %s; from %s = %s", i.description, itemFinalPrice, itemFinalTax);
                itemFinalPrice = itemFinalPrice.add(itemFinalTax);
            }

            BigDecimal itemFinalPrice2 = i.qty.multiply(itemFinalPrice);
            itemFinalPrice2 = itemFinalPrice2.setScale(5, RoundingMode.UP);

            BigDecimal tax2 = i.qty == null || itemFinalTax == null ? BigDecimal.ZERO : i.qty.multiply(itemFinalTax).setScale(8, RoundingMode.HALF_UP);
            totalItemTaxVatValue = totalItemTaxVatValue.add(tax2);
            if (BuildConfig.DEBUG) {
                Logger.d("TotalCost: %s [%s * %s] = %s;\tdiscount = %s (%s)\tafter discount = %s (%s);\ttax = %s/%s (%s%%);\t final price = %s/%s",
                        i.description, i.qty, i.totalPrice, getSubTotal(i.qty, i.totalPrice),
                        itemFinalDiscount, getSubTotal(i.qty, itemFinalDiscount),
                        itemTotal, getSubTotal(i.qty, itemTotal), itemFinalTax, tax2, i.tax, itemFinalPrice, itemFinalPrice2);
            }
            if (handler != null) {
                handler.handleItem(i, itemFinalPrice, itemFinalDiscount, itemFinalTax);
            }

            totalOrderPrice = totalOrderPrice.add(itemFinalPrice2);

            Log.d("BemaCarl17","OrderTotalPriceCalculator.totalOrderPrice       2.1: " + totalOrderPrice);
            Log.d("BemaCarl17","OrderTotalPriceCalculator.itemFinalPrice2       2.1: " + itemFinalPrice2);

        }

        BigDecimal totalTaxVatValue = totalOrderPrice.subtract(subTotalItemTotal).add(totalItemDiscount).add(tmpOderDiscountVal);

        Log.d("BemaCarl17","OrderTotalPriceCalculator.totalOrderPrice    4: " + totalOrderPrice);
        Log.d("BemaCarl17","OrderTotalPriceCalculator.subTotalItemTotal  4: " + subTotalItemTotal);
        Log.d("BemaCarl17","OrderTotalPriceCalculator.totalItemDiscount  4: " + totalItemDiscount);
        Log.d("BemaCarl17","OrderTotalPriceCalculator.tmpOderDiscountVal 4: " + tmpOderDiscountVal);
        Log.d("BemaCarl17","OrderTotalPriceCalculator.totalTaxVatValue   4: " + totalTaxVatValue);


        /********************************************************************************
         *   Start - Temporary fix for tax negative issue
         */
        BigDecimal zeroDecimals = new BigDecimal(0.00000);
        /**
         int res = totalTaxVatValue.compareTo(zeroDecimals)
         if( res == 0 )
            Both values are equal
         else if( res == 1 )
            First Value is greater
         else if( res == -1 )
            Second value is greater
         */
        // When tax is negative
        if(totalTaxVatValue.compareTo(zeroDecimals) == -1){
            Log.d("BemaCarl17","OrderTotalPriceCalculator.totalOrderPrice  5.1: " + totalOrderPrice);
            totalOrderPrice  = totalOrderPrice.add(totalTaxVatValue.abs());
            Log.d("BemaCarl17","OrderTotalPriceCalculator.totalOrderPrice  5.2: " + totalOrderPrice);
            totalTaxVatValue = BigDecimal.ZERO;
        //}else if(tmpOderDiscountVal.compareTo(zeroDecimals) != 0){
        }else if(totalTaxVatValue.compareTo(zeroDecimals) == 1){
            Log.d("BemaCarl17","OrderTotalPriceCalculator.totalOrderPrice  5.3: " + totalOrderPrice);
            //totalOrderPrice = totalOrderPrice.setScale(2, RoundingMode.DOWN);
            Log.d("BemaCarl17","OrderTotalPriceCalculator.totalOrderPrice  5.4: " + totalOrderPrice);
        }
        /**
         *    Finish - Temporary fix for tax negative issue
         *******************************************************************************/

        /*
                16.35 x 11 = 179.85....................
                179.85 - 11% (discount 19.7835) = 160.0665 (160.01)       desconto sobre items
                179.85 + 10% (tax 17.985) = 197.835.......................
                160.0665 + 10% (tax 17.985) = 197.835.....................
        */

        Log.d("BemaCarl17","OrderTotalPriceCalculator.totalOrderPrice    6: " + totalOrderPrice);
        Log.d("BemaCarl17","OrderTotalPriceCalculator.subTotalItemTotal  6: " + subTotalItemTotal);
        Log.d("BemaCarl17","OrderTotalPriceCalculator.totalItemDiscount  6: " + totalItemDiscount);
        Log.d("BemaCarl17","OrderTotalPriceCalculator.tmpOderDiscountVal 6: " + tmpOderDiscountVal);
        Log.d("BemaCarl17","OrderTotalPriceCalculator.totalTaxVatValue   6: " + totalTaxVatValue);

        return new SaleOrderCostInfo(info.isTaxableOrder, orderDiscount, orderDiscountType, tmpOderDiscountVal,
                subTotalItemTotal, totalTaxVatValue, totalItemDiscount, totalOrderPrice, totalDiscountableItemTotal, totalEbtOrderPrice);
    }

    static BigDecimal zeroDecimals = new BigDecimal(0.00000);

    public static SaleOrderCostInfo calculate(SaleOrderInfo info, Handler2 handler) {

        Log.d("BemaCarl20","OrderTotalPriceCalculator ------------------  start calculate  ----------------------");

        BigDecimal orderDiscount                = info.orderDiscount;
        DiscountType orderDiscountType          = info.orderDiscountType;

        HashMap<String, SaleItemInfo> map       = info.map;

        BigDecimal subTotalItemTotal            = BigDecimal.ZERO;
        BigDecimal totalItemDiscount            = BigDecimal.ZERO;
        BigDecimal totalDiscountableItemTotal   = BigDecimal.ZERO;
        BigDecimal totalItemTaxVatValue         = BigDecimal.ZERO;
        BigDecimal totalEbtOrderPrice           = BigDecimal.ZERO;
        BigDecimal totalOrderPrice              = BigDecimal.ZERO;

        for (SaleItemInfo i : map.values()) {

            // item group
            BigDecimal itemSubTotal    = getSubTotal(i.qty, i.totalPrice);

            if (i.discountable) {
                BigDecimal itemGroupDiscount    = CalculationUtil.getItemDiscountValue(itemSubTotal, i.discount, i.discountType);
                itemSubTotal                    = itemSubTotal.subtract(itemGroupDiscount);
                totalItemDiscount               = totalItemDiscount.add(itemGroupDiscount);
                totalDiscountableItemTotal      = totalDiscountableItemTotal.add(itemSubTotal);
                //totalDiscountableItemTotal      = totalDiscountableItemTotal.setScale(2, RoundingMode.DOWN);
            }

            if (i.isEbtEligible){
                totalEbtOrderPrice = totalEbtOrderPrice.add(itemSubTotal);
            }

            BigDecimal itemGroupTax = BigDecimal.ZERO;
            if (i.isTaxable && info.isTaxableOrder) {

                Log.d("BemaCarl20","OrderTotalPriceCalculator -------------------  start tax  ------------------");
                Log.d("BemaCarl20","OrderTotalPriceCalculator.tax.itemSubTotal                          1: " + itemSubTotal);
                BigDecimal taxableFinalPrice = BigDecimal.ONE.subtract(i.ebtPayed).multiply(itemSubTotal);
                Log.d("BemaCarl20","OrderTotalPriceCalculator.tax.taxableFinalPrice                     1: " + taxableFinalPrice);
                itemGroupTax = CalculationUtil.getTaxVatValue(taxableFinalPrice, i.tax);
                Log.d("BemaCarl20","OrderTotalPriceCalculator.tax.itemGroupTax                          1: " + itemGroupTax);

                if (TcrApplication.getCountryFunctionality().isMultiTaxGroup()) {
                    BigDecimal tax2 = CalculationUtil.getTaxVatValue(taxableFinalPrice, i.tax2);
                    itemGroupTax = itemGroupTax.add(tax2);
                }

                Log.d("BemaCarl20","OrderTotalPriceCalculator.tax.itemGroupTax                          2: " + itemGroupTax);
                //itemSubTotal = itemSubTotal.add(itemGroupTax);
                Log.d("BemaCarl20","OrderTotalPriceCalculator.tax.itemSubTotal                          2: " + itemSubTotal);
                totalItemTaxVatValue = totalItemTaxVatValue.add(itemGroupTax);
                Log.d("BemaCarl20","OrderTotalPriceCalculator.tax.totalItemTaxVatValue                  2: " + totalItemTaxVatValue);
                Log.d("BemaCarl20","OrderTotalPriceCalculator ------------------- finish tax ------------------");

            }

            subTotalItemTotal = subTotalItemTotal.add(itemSubTotal);
            //subTotalItemTotal = subTotalItemTotal.add(itemGroupTax);
            //subTotalItemTotal = subTotalItemTotal.setScale(2, RoundingMode.DOWN);

            Log.d("BemaCarl20","OrderTotalPriceCalculator.itemSubTotal                  1: " + itemSubTotal);
            Log.d("BemaCarl20","OrderTotalPriceCalculator.totalItemDiscount             1: " + totalItemDiscount);
            Log.d("BemaCarl20","OrderTotalPriceCalculator.totalDiscountableItemTotal    1: " + totalDiscountableItemTotal);
            Log.d("BemaCarl20","OrderTotalPriceCalculator.subTotalItemTotal             1: " + subTotalItemTotal);

        }


        BigDecimal totalTaxVatValue     = BigDecimal.ZERO;


        for (SaleItemInfo i2 : map.values()) {
            BigDecimal itemFinalPrice               = i2.totalPrice;
            BigDecimal itemFinalPriceDiscount       = BigDecimal.ZERO;
            BigDecimal itemFinalPriceTax            = BigDecimal.ZERO;
            Log.d("BemaCarl20","OrderTotalPriceCalculator...........handler         3.1: " + handler);

            if(i2.discountable) {

                itemFinalPriceDiscount    = CalculationUtil.getItemDiscountValue(itemFinalPrice, i2.discount, i2.discountType);
                //itemFinalPrice = itemFinalPrice.subtract(itemFinalPriceDiscount);

                Log.d("BemaCarl20", "OrderTotalPriceCalculator.itemFinalPrice           1: " + itemFinalPrice);
                Log.d("BemaCarl20", "OrderTotalPriceCalculator.itemFinalPriceDiscount   1: " + itemFinalPriceDiscount);
                Log.d("BemaCarl20", "OrderTotalPriceCalculator.itemFinalPriceTax        1: " + itemFinalPriceTax);

                if (orderDiscount.compareTo(zeroDecimals) == 1) {
                    BigDecimal orderDiscountValue       = orderDiscount;
                    BigDecimal discountByItemPercent    = orderDiscount;
                    if (orderDiscountType == DiscountType.PERCENT) {
                        orderDiscountValue = CalculationUtil.getDiscountValue(subTotalItemTotal, orderDiscount, orderDiscountType);
                    }else{
                        discountByItemPercent = CalculationUtil.getDiscountValueInPercent(itemFinalPrice, orderDiscountValue, DiscountType.VALUE);
                    }
                    BigDecimal discountByItemValue = CalculationUtil.getItemDiscountValue(itemFinalPrice, discountByItemPercent, DiscountType.PERCENT);
                    itemFinalPriceDiscount         = itemFinalPriceDiscount.add(discountByItemValue);

                    Log.d("BemaCarl20", "OrderTotalPriceCalculator.orderDiscountValue         3: " + orderDiscountValue);
                    Log.d("BemaCarl20", "OrderTotalPriceCalculator.discountByItemPercent      3: " + discountByItemPercent);
                    Log.d("BemaCarl20", "OrderTotalPriceCalculator.discountByItemValue        3: " + discountByItemValue);
                    Log.d("BemaCarl20", "OrderTotalPriceCalculator.itemFinalPriceDiscount     3: " + itemFinalPriceDiscount);
                }

            }

            itemFinalPrice = itemFinalPrice.subtract(itemFinalPriceDiscount);

            if (i2.isTaxable && info.isTaxableOrder) {
                BigDecimal itemFinalPriceTaxable = BigDecimal.ONE.subtract(i2.ebtPayed).multiply(itemFinalPrice);
                itemFinalPriceTax = CalculationUtil.getTaxVatValue(itemFinalPriceTaxable, i2.tax);
                itemFinalPrice = itemFinalPrice.add(itemFinalPriceTax);
                if (TcrApplication.getCountryFunctionality().isMultiTaxGroup()) {
                    BigDecimal itemFinalPriceTax2 = CalculationUtil.getTaxVatValue(itemFinalPriceTaxable, i2.tax2);
                    itemFinalPrice = itemFinalPrice.add(itemFinalPriceTax2);
                    itemFinalPriceTax = itemFinalPriceTax.add(itemFinalPriceTax2);
                }
                totalTaxVatValue = totalTaxVatValue.add(itemFinalPriceTax.multiply(i2.qty));
            }
            Log.d("BemaCarl20","OrderTotalPriceCalculator.i2.isTaxable              3.1: " + i2.isTaxable);
            Log.d("BemaCarl20","OrderTotalPriceCalculator.info.isTaxableOrder       3.1: " + info.isTaxableOrder);
            Log.d("BemaCarl20","OrderTotalPriceCalculator.itemFinalPrice            3.1: " + itemFinalPrice);
            Log.d("BemaCarl20","OrderTotalPriceCalculator.itemFinalPriceDiscount    3.1: " + itemFinalPriceDiscount);
            Log.d("BemaCarl20","OrderTotalPriceCalculator.itemFinalPriceTax         3.1: " + itemFinalPriceTax);

            if (handler != null) {
                handler.handleItem(i2, itemFinalPrice, itemFinalPriceDiscount, itemFinalPriceTax);
            }

        }

        BigDecimal tmpOderDiscountVal   = CalculationUtil.getDiscountValue(totalDiscountableItemTotal, orderDiscount, orderDiscountType);
        //BigDecimal totalTaxVatValue   = totalOrderPrice.subtract(subTotalItemTotal).add(totalItemDiscount).add(tmpOderDiscountVal);

        Log.d("BemaCarl20","OrderTotalPriceCalculator.subTotalItemTotal    4: " + subTotalItemTotal);
        Log.d("BemaCarl20","OrderTotalPriceCalculator.tmpOderDiscountVal   4: " + tmpOderDiscountVal);
        Log.d("BemaCarl20","OrderTotalPriceCalculator.totalItemDiscount    4: " + totalItemDiscount);
        Log.d("BemaCarl20","OrderTotalPriceCalculator.totalTaxVatValue     4: " + totalTaxVatValue);
        Log.d("BemaCarl20","OrderTotalPriceCalculator.totalOrderPrice      4: " + totalOrderPrice);

        totalOrderPrice                 = totalOrderPrice.add(subTotalItemTotal);
        totalOrderPrice                 = totalOrderPrice.subtract(tmpOderDiscountVal);
        totalOrderPrice                 = totalOrderPrice.subtract(totalItemDiscount);
        totalOrderPrice                 = totalOrderPrice.add(totalTaxVatValue);

        Log.d("BemaCarl20","OrderTotalPriceCalculator.totalOrderPrice      5: " + totalOrderPrice);

        /*
                qty 16.35 x 11 = 179.85................................
                dis 179.85 - 11% (discount 19.7835) = 160.0665 (160.07)
                tax 179.85 + 10% (tax 17.985) = 197.835................
                tax 160.0665 + 10% (tax 17.985) = 176.07315............
        */
        /********************************************************************************
         *   Start - Temporary fix for tax negative issue
         */
        /**
         int res = totalTaxVatValue.compareTo(zeroDecimals)
         if( res == 0 )
         Both values are equal
         else if( res == 1 )
         First Value is greater
         else if( res == -1 )
         Second value is greater
         */
        // When tax is negative
        if(totalTaxVatValue.compareTo(zeroDecimals) == -1){
            Log.d("BemaCarl20","OrderTotalPriceCalculator.totalOrderPrice  5.1: " + totalOrderPrice);
            //totalOrderPrice  = totalOrderPrice.add(totalTaxVatValue.abs());
            Log.d("BemaCarl20","OrderTotalPriceCalculator.totalOrderPrice  5.2: " + totalOrderPrice);
            //totalTaxVatValue = BigDecimal.ZERO;
            //}else if(tmpOderDiscountVal.compareTo(zeroDecimals) != 0){
        }else if(totalTaxVatValue.compareTo(zeroDecimals) == 1){
            Log.d("BemaCarl20","OrderTotalPriceCalculator.totalOrderPrice  5.3: " + totalOrderPrice);
            //totalOrderPrice = totalOrderPrice.setScale(2, RoundingMode.DOWN);
            Log.d("BemaCarl20","OrderTotalPriceCalculator.totalOrderPrice  5.4: " + totalOrderPrice);
        }
        /**
         *    Finish - Temporary fix for tax negative issue
         *******************************************************************************/

        Log.d("BemaCarl20","OrderTotalPriceCalculator.info.isTaxableOrder           6: " + info.isTaxableOrder);
        Log.d("BemaCarl20","OrderTotalPriceCalculator.orderDiscount                 6: " + orderDiscount);
        Log.d("BemaCarl20","OrderTotalPriceCalculator.orderDiscountType             6: " + orderDiscountType);
        Log.d("BemaCarl20","OrderTotalPriceCalculator.tmpOderDiscountVal            6: " + tmpOderDiscountVal);
        Log.d("BemaCarl20","OrderTotalPriceCalculator.subTotalItemTotal             6: " + subTotalItemTotal);
        Log.d("BemaCarl20","OrderTotalPriceCalculator.totalTaxVatValue              6: " + totalTaxVatValue);
        Log.d("BemaCarl20","OrderTotalPriceCalculator.totalItemDiscount             6: " + totalItemDiscount);
        Log.d("BemaCarl20","OrderTotalPriceCalculator.totalOrderPrice               6: " + totalOrderPrice);
        Log.d("BemaCarl20","OrderTotalPriceCalculator.totalDiscountableItemTotal    6: " + totalDiscountableItemTotal);
        Log.d("BemaCarl20","OrderTotalPriceCalculator.totalEbtOrderPrice            6: " + totalEbtOrderPrice);

        Log.d("BemaCarl20","OrderTotalPriceCalculator ------------------ finish calculate ----------------------");

        return new SaleOrderCostInfo(info.isTaxableOrder, orderDiscount, orderDiscountType, tmpOderDiscountVal,
                subTotalItemTotal, totalTaxVatValue, totalItemDiscount, totalOrderPrice, totalDiscountableItemTotal, totalEbtOrderPrice);

    }


    private static ArrayList<CalcItemInfo> calcItemOrderDiscount(HashMap<String, SaleItemInfo> map, BigDecimal tmpOderDiscountVal, BigDecimal tmpOderDiscountPercent, Handler2 handler2) {
        Logger.d("TotalCost: check discount");

        Logger.d("TotalCost: tmpOderDiscountVal = %s", tmpOderDiscountVal);
        Logger.d("TotalCost: tmpOderDiscountPercent = %s", tmpOderDiscountPercent);

        Collection<SaleItemInfo> saleItemsInfo = map.values();
        ArrayList<CalcItemInfo> calcItems = new ArrayList<>(saleItemsInfo.size());
        if (saleItemsInfo.size() == 0) {
            return calcItems;
        }
        Log.d("BemaCarl15", "OrderTotalPriceCalculator.calculate.tmpOderDiscountVal2: " + tmpOderDiscountVal);
        BigDecimal calcDiscountVal = BigDecimal.ZERO;

        for (SaleItemInfo i : saleItemsInfo) {
            BigDecimal itemDiscount = CalculationUtil.getDiscountValue(i.totalPrice, i.discount, i.discountType);
            BigDecimal itemFinalPrice = i.totalPrice.subtract(itemDiscount);
            BigDecimal itemOrderDiscount = BigDecimal.ZERO;
            if (BigDecimal.ZERO.compareTo(tmpOderDiscountVal) != 0 && i.discountable) {
                itemOrderDiscount = CalculationUtil.getDiscountValue(itemFinalPrice, tmpOderDiscountPercent, DiscountType.PERCENT);
                Log.d("BemaCarl15", "OrderTotalPriceCalculator.calcItemOrderDiscount.itemOrderDiscount3: " + itemOrderDiscount);
                BigDecimal qtyOrderDiscount = getSubTotal(i.qty, itemOrderDiscount);
                calcDiscountVal = calcDiscountVal.add(qtyOrderDiscount);
                Logger.d("TotalCost: item order discount %s %s(%s) * %s = %s; * %s = %s", i.description, itemFinalPrice, itemDiscount, tmpOderDiscountPercent, itemOrderDiscount, i.qty, qtyOrderDiscount);
            }
            calcItems.add(new CalcItemInfo(i, itemFinalPrice, itemDiscount, itemOrderDiscount));
        }
        /*
        Logger.d("TotalCost: order discount by items = %s", calcDiscountVal);
        Collections.sort(calcItems, new Comparator<CalcItemInfo>() {
            @Override
            public int compare(CalcItemInfo l, CalcItemInfo r) {
                return -1 * (l.itemOderDiscount == null ? -1 : r.itemOderDiscount == null ? 1 : l.itemOderDiscount.compareTo(r.itemOderDiscount));
            }
        });
        /**/
        //CalcItemInfo biggestOrderDiscount = calcItems.get(0);

        //BigDecimal discountDiff = tmpOderDiscountVal.subtract(calcDiscountVal);
        //Logger.d("TotalCost: need adjust %s", discountDiff);

        //BigDecimal qty = biggestOrderDiscount.itemInfo.qty;
        //BigDecimal qtyInt = qty.setScale(0, RoundingMode.DOWN);
        //if (discountDiff.compareTo(BigDecimal.ZERO) == 0)
            //return calcItems;

        //BigDecimal cents = discountDiff.multiply(CalculationUtil.ONE_HUNDRED).setScale(0, RoundingMode.HALF_UP);
        //BigDecimal centsAbs = cents.abs();
        //boolean negative = cents.compareTo(BigDecimal.ZERO) == -1;
        //Log.d("BemaCarl15", "OrderTotalPriceCalculator.calcItemOrderDiscount.centsAbs: " + centsAbs);
        //Log.d("BemaCarl15", "OrderTotalPriceCalculator.calcItemOrderDiscount.qty: " + qty);
        //Log.d("BemaCarl15", "OrderTotalPriceCalculator.calcItemOrderDiscount.qtyInt: " + qtyInt);
        //qty is integer:  > 0 and doesn't have decimal part; cents != qty
        /*if (centsAbs.compareTo(qty) != 0 && qty.compareTo(BigDecimal.ONE) == 1 && qtyInt.compareTo(qty) == 0 || false) {
            BigDecimal oneCent = negative ? ONE_CENT_NEGATIVE : ONE_CENT;
            CalcItemInfo splitedItem;
            Logger.d("TotalCost: cents and qty  = %s %s", centsAbs, qty);
            //cents > qty

            if (centsAbs.compareTo(qty) == 1) {
                int d = centsAbs.intValue() / qty.intValue();
                BigDecimal firstAdjust = d > 0 ? oneCent.multiply(new BigDecimal(d)) : oneCent;
                Logger.d("TotalCost: cents > qty row will be splitted");
                updateDiscount(biggestOrderDiscount, firstAdjust);
                splitedItem = biggestOrderDiscount.split(BigDecimal.ONE);
                Logger.d("TotalCost: spletted to %s and %s", splitedItem.itemInfo.qty, biggestOrderDiscount.itemInfo.qty);

                BigDecimal alreadyAdjusted = getSubTotal(qty, firstAdjust);
                BigDecimal adjust = discountDiff.subtract(alreadyAdjusted);
                Logger.d("TotalCost: add to splitted item %s", adjust);
                updateDiscount(splitedItem, adjust);
            } else {
                Logger.d("TotalCost: cents < qty row will be splitted to %s and %s", centsAbs, biggestOrderDiscount.itemInfo.qty);
                splitedItem = biggestOrderDiscount.split(centsAbs);
                updateDiscount(splitedItem, oneCent);
            }

            calcItems.add(splitedItem);
            if (handler2 != null) {
                handler2.splitItem(splitedItem.itemInfo);
            }

        /**///} else {
            //Logger.d("TotalCost: adjust default");
            //BigDecimal diff = CalculationUtil.divide(discountDiff, biggestOrderDiscount.itemInfo.qty, 3);
            //Logger.d("TotalCost: adjust diff %s/%s = %s", discountDiff, biggestOrderDiscount.itemInfo.qty, diff);
            //updateDiscount(biggestOrderDiscount, diff);
        //}/**/
        //Log.d("BemaCarl15", "OrderTotalPriceCalculator.calcItemOrderDiscount.diff: " + diff);
        //Log.d("BemaCarl15", "OrderTotalPriceCalculator.calcItemOrderDiscount.discountDiff: " + discountDiff);
        //Log.d("BemaCarl15", "OrderTotalPriceCalculator.calcItemOrderDiscount.biggestOrderDiscount: " + biggestOrderDiscount.getFinalDiscount());
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
