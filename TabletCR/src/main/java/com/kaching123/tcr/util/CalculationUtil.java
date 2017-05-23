package com.kaching123.tcr.util;

import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.PaymentTransactionModel;

import junit.framework.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by gdubina on 07.11.13.
 */
public final class CalculationUtil {

    public static final BigDecimal NEGATIVE = new BigDecimal(-1);
    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00000");
    public static final BigDecimal ONE_HOUR = new BigDecimal(60);
    private final static int PERCENT_SCALE = 8;
    private final static int RESULT_SCALE = 8;
    private final static int QUANTITY_SCALE = 8;
    private final static RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private CalculationUtil(){}

    public static BigDecimal getTotalDue(BigDecimal hRate, BigDecimal totalMins) {
        return hRate.multiply(totalMins).divide(ONE_HOUR, RESULT_SCALE, BigDecimal.ROUND_HALF_EVEN);
    }

    public static BigDecimal getSubTotal(BigDecimal qty, BigDecimal price){
        if (qty == null || price == null)
            return BigDecimal.ZERO;

        return qty.multiply(price).setScale(RESULT_SCALE, MONEY_ROUNDING);
    }

    public static BigDecimal getSubTotalNoScale(BigDecimal qty, BigDecimal price){
        return qty.multiply(price);
    }

    public static BigDecimal getSubTotal(BigDecimal qty, BigDecimal price, BigDecimal discount, DiscountType discountType){
        BigDecimal total = getSubTotal(qty, price);
        if(discount != null && discountType != null){
            BigDecimal itemDiscount = getItemDiscountValue(price, discount, discountType);
            BigDecimal itemSubDiscount = getSubTotal(qty, itemDiscount);
            total = total.subtract(itemSubDiscount);
        }
        return total;
    }

    public static BigDecimal getSubTotalNoScale2(BigDecimal qty, BigDecimal price, BigDecimal discount, DiscountType discountType){
        BigDecimal total = qty.multiply(price);
        if(discount != null && discountType != null){
            BigDecimal discountValue = discount;
            if(discountType == DiscountType.PERCENT){
                discountValue = price.divide(ONE_HUNDRED, PERCENT_SCALE, MONEY_ROUNDING).multiply(discount);
            }
            total = total.subtract(discountValue.multiply(qty));
        }
        return total;
    }

    public static BigDecimal getItemDiscountValue(/*BigDecimal qty, */BigDecimal price, BigDecimal discount, DiscountType discountType){
        return getDiscountValue(price/*qty.multiply(price)*/, discount, discountType);
    }

    public static BigDecimal getDiscountValue(BigDecimal total, BigDecimal discount, DiscountType discountType){
        BigDecimal cem = new BigDecimal("100");
        if(discountType == DiscountType.PERCENT) {
            return total.divide(cem, 8, BigDecimal.ROUND_HALF_UP).multiply(discount);
        }
        return getDiscountValueNoScale(total, discount, discountType).setScale(RESULT_SCALE, MONEY_ROUNDING);
    }

    public static BigDecimal getTipValue(BigDecimal total, BigDecimal discount, DiscountType tipType){
        return getDiscountValueNoScale(total, discount, tipType).setScale(RESULT_SCALE, MONEY_ROUNDING);
    }

    public static BigDecimal getDiscountValueNoScale(BigDecimal total, BigDecimal discount, DiscountType discountType){
        BigDecimal result = BigDecimal.ZERO;
        if(discount != null && discountType != null){
            result = discount;
            if(discountType == DiscountType.PERCENT){
                result = total.divide(ONE_HUNDRED, PERCENT_SCALE, MONEY_ROUNDING).multiply(discount);
            }
        }
        return result;
    }

    public static BigDecimal getDiscountValueInPercent(BigDecimal total, BigDecimal discount, DiscountType discountType){
        BigDecimal result = BigDecimal.ZERO;
        if(total != null && BigDecimal.ZERO.compareTo(total) != 0 && discount != null && discountType != null){
            result = discount;
            if(discountType == DiscountType.VALUE){
                result = discount.divide(total, PERCENT_SCALE, MONEY_ROUNDING).multiply(ONE_HUNDRED);
            }
        }
        return result;
        //return result.setScale(RESULT_SCALE, MONEY_ROUNDING);
    }

    public static BigDecimal getTaxVatValue(BigDecimal itemSubTotal, BigDecimal taxVATPercent) {
        return getTaxVatValueNoScale(itemSubTotal, taxVATPercent).setScale(8, MONEY_ROUNDING);
    }

    public static BigDecimal getTaxVatValueNoScale(BigDecimal itemSubTotal, BigDecimal taxVATPercent) {
        if(taxVATPercent == null)
            return BigDecimal.ZERO;
        BigDecimal result = itemSubTotal.multiply(taxVATPercent.divide(ONE_HUNDRED, 8, MONEY_ROUNDING));
        return result;
    }

    public static BigDecimal getAmountFromList(List<PaymentTransactionModel> list) {
        Assert.assertNotNull(list);
        BigDecimal result = BigDecimal.ZERO;
        for (PaymentTransactionModel item : list) {
            result = result.add(item.availableAmount);
        }
        return result;
    }

    public static BigDecimal getPercentInDecimal(BigDecimal percent) {
        return percent.divide(ONE_HUNDRED, RESULT_SCALE, MONEY_ROUNDING);
    }

    public static BigDecimal getInCents(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        return value.multiply(CalculationUtil.ONE_HUNDRED).setScale(0);
    }

    public static BigDecimal splitAmount(BigDecimal amount, int count) {
        return amount.divide(new BigDecimal(count), RESULT_SCALE, RoundingMode.DOWN);
    }

    public static BigDecimal value(BigDecimal value) {
        return value.setScale(RESULT_SCALE, MONEY_ROUNDING);
    }

    public static BigDecimal negative(BigDecimal add) {
        if(add == null)
            return null;
        return add.multiply(NEGATIVE).setScale(RESULT_SCALE, MONEY_ROUNDING);
    }

    public static BigDecimal negativeQty(BigDecimal add) {
        if(add == null)
            return null;
        return add.multiply(NEGATIVE).setScale(QUANTITY_SCALE, MONEY_ROUNDING);
    }

    public static BigDecimal divide(BigDecimal v1, BigDecimal v2){
        if(BigDecimal.ZERO.compareTo(v2) == 0)
            return BigDecimal.ZERO;
        return v1.divide(v2, RESULT_SCALE, MONEY_ROUNDING);
    }

    public static BigDecimal divide(BigDecimal v1, BigDecimal v2, int scale){
        if(BigDecimal.ZERO.compareTo(v2) == 0)
            return BigDecimal.ZERO;
        return v1.divide(v2, scale, MONEY_ROUNDING);
    }
}
