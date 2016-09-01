package com.kaching123.tcr.function;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsViewFast2.SaleItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsViewFast2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsViewFast;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsViewFastSynced;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;
import static com.kaching123.tcr.util.CursorUtil._wrap;
import static com.kaching123.tcr.util.CursorUtil._wrapOrNull;

/**
 * Created by gdubina on 14.02.14.
 */
public final class FastOrderTotalQuery {

    private FastOrderTotalQuery() {
    }

    private static final Uri URI_ORDER_ITEMS = ShopProvider.contentUri(SaleOrderItemsViewFast.URI_CONTENT);
    private static final Uri URI_ORDER_ITEMS_SYNCED = ShopProvider.contentUri(SaleOrderItemsViewFastSynced.URI_CONTENT);

    private static final int SaleItemTable_ORDER_GUID = 0;
    private static final int SaleItemTable_SALE_ITEM_GUID = 1;
    private static final int SaleItemTable_ITEM_GUID = 2;
    private static final int SaleItemTable_QUANTITY = 3;
    private static final int SaleItemTable_PRICE = 4;
    private static final int SaleItemTable_DISCOUNTABLE = 5;
    private static final int SaleItemTable_DISCOUNT = 6;
    private static final int SaleItemTable_DISCOUNT_TYPE = 7;
    private static final int SaleItemTable_TAXABLE = 8;
    private static final int SaleItemTable_TAX = 9;
    private static final int SaleItemTable_FINAL_GROSS_PRICE = 10;
    private static final int SaleItemTable_FINAL_DISCOUNT = 11;
    private static final int SaleItemTable_FINAL_TAX = 12;
    private static final int SaleItemTable_EBT_ELIGIBLE = 13;
    private static final int SaleOrderTable_TAXABLE = 14;
    private static final int SaleOrderTable_DISCOUNT = 15;
    private static final int SaleOrderTable_DISCOUNT_TYPE = 16;


    //be careful to modify it check SaleOrderItemsViewFastSynced too
    private static final String[] PROJECTION = new String[]{
            SaleItemTable.ORDER_GUID,
            SaleItemTable.SALE_ITEM_GUID,
            SaleItemTable.ITEM_GUID,
            SaleItemTable.QUANTITY,
            SaleItemTable.PRICE,
            SaleItemTable.DISCOUNTABLE,
            SaleItemTable.DISCOUNT,
            SaleItemTable.DISCOUNT_TYPE,
            SaleItemTable.TAXABLE,
            SaleItemTable.TAX,
            SaleItemTable.FINAL_GROSS_PRICE,
            SaleItemTable.FINAL_DISCOUNT,
            SaleItemTable.FINAL_TAX,
            SaleItemTable.EBT_ELIGIBLE,
            SaleOrderTable.TAXABLE,
            SaleOrderTable.DISCOUNT,
            SaleOrderTable.DISCOUNT_TYPE};

    public static SaleOrderCostInfo calc(Context context, String order) {
        SaleOrderInfo data = loadData(context, order);
        if (data == null)
            return null;
        return calculate(data);
    }

    public static HashMap<String, SaleOrderCostInfo> calc(Context context, Collection<String> orders) {
        HashMap<String, SaleOrderInfo> data = loadData(context, orders);
        HashMap<String, SaleOrderCostInfo> result = new HashMap<String, SaleOrderCostInfo>();
        for (Entry<String, SaleOrderInfo> e : data.entrySet()) {
            SaleOrderCostInfo r = calculate(e.getValue());
            result.put(e.getKey(), r);
        }

        return result;
    }

    public static OrderInfoCursorWrapper calcSynced(Context context) {
        OrderInfoCursorWrapper data = loadSyncedData(context);
        /*HashMap<String, SaleOrderCostInfo> result = new HashMap<String, SaleOrderCostInfo>();
        SaleOrderInfo orderInfo;
        while ((orderInfo = data.getNextOrderInfo()) != null) {
            SaleOrderCostInfo r = calculate(orderInfo);
            result.put(orderInfo.guid, r);
        }
        data.close();
        return result;*/
        return data;
    }

    private static OrderInfoCursorWrapper loadSyncedData(Context context) {
        Logger.d("Fast.loadSyncedData");
        Cursor c = ProviderAction
                .query(URI_ORDER_ITEMS_SYNCED)
                .projection(PROJECTION)
                .perform(context);
        return new OrderInfoCursorWrapper(c);
    }

    private static HashMap<String, SaleOrderInfo> loadData(Context context, Collection<String> orders) {
        return _wrap(ProviderAction
                        .query(URI_ORDER_ITEMS)
                        .projection(PROJECTION)
                        .whereIn(SaleItemTable.ORDER_GUID, orders)
                        .perform(context),
                new Function<Cursor, HashMap<String, SaleOrderInfo>>() {
                    @Override
                    public HashMap<String, SaleOrderInfo> apply(Cursor cursor) {
                        return parseCursorMultiple(cursor);
                    }
                }
        );

    }

    private static SaleOrderInfo loadData(Context context, String order) {
        return _wrapOrNull(ProviderAction
                        .query(URI_ORDER_ITEMS)
                        .projection(PROJECTION)
                        .where(SaleItemTable.ORDER_GUID + " = ?", order)
                        .perform(context),
                new Function<Cursor, SaleOrderInfo>() {
                    @Override
                    public SaleOrderInfo apply(Cursor cursor) {
                        return parseCursorSimple(cursor);
                    }
                }
        );
    }

    public static SaleOrderCostInfo calculate(SaleOrderInfo orderInfo) {
        if (orderInfo == null)
            return null;
        Logger.d("[FAST_CALC] === start ===");
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalEbtPrice = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        int index = 1;
        for (SaleItemInfo i : orderInfo.items) {
            BigDecimal total = i.finalGrossPrice.subtract(i.finalDiscount).add(i.finalTax);
            BigDecimal sub = getSubTotal(i.qty, total);
            totalPrice = totalPrice.add(sub);
            if(i.isEligible) {
                totalEbtPrice = totalEbtPrice.add(i.finalGrossPrice);
            }

            totalDiscount = totalDiscount.add(getSubTotal(i.qty, i.finalDiscount));
            totalTax = totalTax.add(getSubTotal(i.qty, i.finalTax));

            Logger.d("[FAST_CALC] %d. %s: %s - %s + %s = %s; *%s = %s", index, i.itemGiud, i.finalGrossPrice, i.finalDiscount, i.finalTax, total, i.qty, sub);
            index++;
        }
        Logger.d("[FAST_CALC] === end ===");
        return new SaleOrderCostInfo(orderInfo.guid, totalPrice, totalEbtPrice, totalDiscount, totalTax);
    }

    public static SaleOrderInfo parseCursorSimple(Cursor c) {
        if (c == null)
            return null;

        SaleOrderInfo result = null;

        if (c.moveToFirst()) {
            do {
                if (result == null) {
                    result = new SaleOrderInfo(
                            null,
                            _bool(c, SaleOrderTable_TAXABLE),
                            _decimal(c, SaleOrderTable_DISCOUNT, BigDecimal.ZERO),
                            _discountType(c, SaleOrderTable_DISCOUNT_TYPE)
                    );
                }
                readCursorRow(c, result);
            } while (c.moveToNext());
        }
        return result == null ? new SaleOrderInfo(null, false, BigDecimal.ZERO, DiscountType.VALUE) : result;
    }

    public static HashMap<String, SaleOrderInfo> parseCursorMultiple(Cursor c) {
        if (c == null || c.getCount() == 0)
            return new HashMap<>(0);
        //printCursor(c);
        Logger.d("Fast.parseCursorMultiple: %d", c.getCount());
        HashMap<String, SaleOrderInfo> result = new HashMap<>();
        if (c.moveToFirst()) {
            do {
                String orderGuid = c.getString(SaleItemTable_ORDER_GUID);
                SaleOrderInfo order = result.get(orderGuid);
                if (order == null) {
                    order = new SaleOrderInfo(
                            orderGuid,
                            _bool(c, SaleOrderTable_TAXABLE),
                            _decimal(c, SaleOrderTable_DISCOUNT, BigDecimal.ZERO),
                            _discountType(c, SaleOrderTable_DISCOUNT_TYPE));
                    result.put(orderGuid, order);
                }
                readCursorRow(c, order);
            } while (c.moveToNext());
        }
        return result;
    }

/*
    private static void printCursor(Cursor c){
        if (c.moveToFirst()) {
            do {
                Logger.d("\nFast.cursor *** ");
                for (int i = 0; i < c.getColumnCount(); i++) {
                    Logger.d("Fast.cursor %d. %s : %s", i, c.getColumnName(i), c.getString(i));
                }
            }while (c.moveToNext());
        }

    }
*/

    public static void readCursorRow(Cursor c, SaleOrderInfo result) {
        String saleItemId = c.getString(SaleItemTable_SALE_ITEM_GUID);
        SaleItemInfo value = new SaleItemInfo(
                saleItemId,
                c.getString(SaleItemTable_ITEM_GUID),
                _decimalQty(c, SaleItemTable_QUANTITY, BigDecimal.ZERO),
                _bool(c, SaleItemTable_DISCOUNTABLE),
                _decimal(c, SaleItemTable_DISCOUNT, BigDecimal.ZERO),
                _discountType(c, SaleItemTable_DISCOUNT_TYPE),
                _bool(c, SaleItemTable_TAXABLE),
                _decimal(c, SaleItemTable_TAX, BigDecimal.ZERO),
                _decimal(c, SaleItemTable_FINAL_GROSS_PRICE, BigDecimal.ZERO),
                _decimal(c, SaleItemTable_FINAL_DISCOUNT, BigDecimal.ZERO),
                _decimal(c, SaleItemTable_FINAL_TAX, BigDecimal.ZERO)
        );
        value.setEbtEligible(_bool(c,  SaleItemTable_EBT_ELIGIBLE));
        result.items.add(value);
    }

    public static class SaleOrderInfo {

        public final String guid;
        public final boolean isTaxableOrder;
        public final BigDecimal orderDiscount;
        public final DiscountType orderDiscountType;
        public final ArrayList<SaleItemInfo> items;

        public SaleOrderInfo(String guid, boolean isTaxableOrder, BigDecimal orderDiscount, DiscountType orderDiscountType) {
            this.guid = guid;
            this.isTaxableOrder = isTaxableOrder;
            this.orderDiscount = orderDiscount;
            this.orderDiscountType = orderDiscountType;
            this.items = new ArrayList<>();
        }
    }

    public static class SaleItemInfo {
        public final String saleItemGiud;
        public final String itemGiud;

        //public final String description;
        public final BigDecimal qty;

        public final boolean discountable;
        public final BigDecimal discount;
        public final DiscountType discountType;

        public final boolean isTaxable;
        public boolean isEligible;
        public final BigDecimal tax;

        public BigDecimal finalGrossPrice;
        public BigDecimal finalDiscount;
        public BigDecimal finalTax;

        public SaleItemInfo(String saleItemGiud,
                            String itemGiud,
                            //String description,
                            BigDecimal qty,

                            boolean discountable,
                            BigDecimal discount,
                            DiscountType discountType,

                            boolean isTaxable,
                            BigDecimal tax,

                            BigDecimal finalGrossPrice,
                            BigDecimal finalDiscount,
                            BigDecimal finalTax) {
            this.saleItemGiud = saleItemGiud;
            this.itemGiud = itemGiud;
            this.qty = qty;
            this.discountable = discountable;
            this.discount = discount;
            this.discountType = discountType;
            this.isTaxable = isTaxable;
            this.tax = tax;
            this.finalGrossPrice = finalGrossPrice;
            this.finalDiscount = finalDiscount;
            this.finalTax = finalTax;
        }

        public void setEbtEligible(boolean isEligible) {
            this.isEligible = isEligible;
        }
    }

    public static class SaleOrderCostInfo {

        public String guid;
        public BigDecimal totalPrice;
        public BigDecimal totalEbtPrice;
        public BigDecimal totalDiscount;
        public BigDecimal totalTax;

        public SaleOrderCostInfo(String guid, BigDecimal totalPrice, BigDecimal totalEbtPrice, BigDecimal totalDiscount, BigDecimal totalTax) {
            this.guid = guid;
            this.totalPrice = totalPrice;
            this.totalDiscount = totalDiscount;
            this.totalTax = totalTax;
            this.totalEbtPrice = totalEbtPrice;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "price = %s\tdiscount = %s\ttax = %s", totalPrice, totalDiscount, totalTax);
        }
    }

    public static class OrderInfoCursorWrapper {

        private final Cursor c;

        public OrderInfoCursorWrapper(Cursor c) {
            this.c = c;
        }

        public void close() {
            if (!c.isClosed())
                c.close();
        }

        public boolean isEmpty() {
            return c.getCount() == 0;
        }

        public SaleOrderCostInfo getNextOrderInfo() {
            if (c.getCount() == 0)
                return null;

            if (c.isBeforeFirst())
                c.moveToFirst();

            if (c.isAfterLast())
                return null;

            String orderGuid = c.getString(SaleItemTable_ORDER_GUID);
            SaleOrderInfo orderInfo = new SaleOrderInfo(
                    orderGuid,
                    _bool(c, SaleOrderTable_TAXABLE),
                    _decimal(c, SaleOrderTable_DISCOUNT, BigDecimal.ZERO),
                    _discountType(c, SaleOrderTable_DISCOUNT_TYPE));

            readCursorRow(c, orderInfo);

            String nextOrderGuid;
            while (c.moveToNext()) {
                nextOrderGuid = c.getString(SaleItemTable_ORDER_GUID);
                if (!nextOrderGuid.equals(orderGuid))
                    break;

                readCursorRow(c, orderInfo);
            }

            return calculate(orderInfo);
        }

    }

}
