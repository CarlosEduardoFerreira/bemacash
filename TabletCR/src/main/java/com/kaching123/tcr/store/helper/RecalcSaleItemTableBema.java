package com.kaching123.tcr.store.helper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.function.FastOrderTotalQuery;
import com.kaching123.tcr.function.FastOrderTotalQuery.OrderInfoCursorWrapper;
import com.kaching123.tcr.function.FastOrderTotalQuery.SaleOrderCostInfo;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.RecalcSaleItemTableView;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by hamsterksu on 19.09.2014.
 */
public class RecalcSaleItemTableBema extends ProviderHelper {

    private static final Uri SALE_ITEM_URI = ShopProvider.contentUri(SaleItemTable.URI_CONTENT);
    private static final Uri SALE_ORDER_URI = ShopProvider.contentUri(SaleOrderTable.URI_CONTENT);

    private static final Uri SALE_ITEM_SYNCED_URI = ShopProvider.contentUri(RecalcSaleItemTableView.URI_CONTENT);

    private static final int BULK_UPDATE_SIZE = 1000;

    private String TAG = "BemaCarl15";

    public RecalcSaleItemTableBema(Context context, SQLiteOpenHelper dbHelper) {
        super(context, dbHelper);
    }

    public void bulkRecalcSaleItemTableAfterSync() {
        Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTableAfterSync1");
        bulkRecalcSaleItemTable();
        recalculateSaleItemsAvailableQty();
    }

    private void recalculateSaleItemsAvailableQty() {
        Log.d(TAG, "RecalcSaleItemTable.recalculateSaleItemsAvailableQty");
        Cursor c = getContext().getContentResolver()
                .query(SALE_ITEM_SYNCED_URI,
                        null,
                        null,
                        null,
                        null
                );
        if (c.getCount() == 0) {
            c.close();
            return;
        }
        handleCursor(c, true);
    }

    public void bulkRecalcSaleItemTable(ContentValues[] saleItems) {
        Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable");
        if (saleItems == null)
            return;
        HashSet<String> orders = new HashSet<String>();
        HashSet<String> items = new HashSet<String>(saleItems.length);
        for (ContentValues i : saleItems) {
            if (i.containsKey(SaleItemTable.IS_DELETED) && i.getAsBoolean(SaleItemTable.IS_DELETED))
                continue;
            if (i.containsKey(SaleItemTable.UPDATE_IS_DRAFT) && i.getAsBoolean(SaleItemTable.UPDATE_IS_DRAFT))
                continue;
            String orderGuid = i.getAsString(SaleItemTable.ORDER_GUID);
            String parentId = i.getAsString(SaleItemTable.PARENT_GUID);
            if (!TextUtils.isEmpty(parentId)) {
                items.add(parentId);
            }
            if (TextUtils.isEmpty(orderGuid)) {
                Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable.isEmpty: " + i.getAsString(SaleItemTable.SALE_ITEM_GUID));
                continue;
            }
            orders.add(orderGuid);
        }
        Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable.orders.size: " + orders.size());
        bulkRecalcSaleItemTable(orders);
        Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable.items.size: " + items.size());
        recalculateSaleItemsAvailableQty(items);
    }

    void bulkRecalcSaleItemTable(HashSet<String> orders) {
        Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable2");
        if (orders == null || orders.isEmpty())
            return;
        HashMap<String, SaleOrderCostInfo> result = FastOrderTotalQuery.calc(getContext(), orders);
        ArrayList<ContentValues> values = new ArrayList<ContentValues>(result.size());
        for (Entry<String, SaleOrderCostInfo> e : result.entrySet()) {
            FastOrderTotalQuery.SaleOrderCostInfo cost = e.getValue();
            ContentValues v = new ContentValues(4);
            v.put(SaleOrderTable.GUID, e.getKey());
            v.put(SaleOrderTable.TML_TOTAL_PRICE, _decimal(cost.totalPrice));
            v.put(SaleOrderTable.TML_TOTAL_TAX, _decimal(cost.totalTax));
            v.put(SaleOrderTable.TML_TOTAL_DISCOUNT, _decimal(cost.totalDiscount));
            Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable2.orderInfo.totalPrice: " + cost.totalPrice);
            Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable2.orderInfo.totalTax: " + cost.totalTax);
            Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable2.orderInfo.totalDiscount: " + cost.totalDiscount);
            values.add(v);
        }
        Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable2.values.size: " + values.size());
        bulkUpdate(SaleOrderTable.TABLE_NAME, values, SaleOrderTable.GUID);
    }

    void bulkRecalcSaleItemTable() {
        Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable3");
        OrderInfoCursorWrapper result = FastOrderTotalQuery.calcSynced(getContext());
        if (result.isEmpty()) {
            return;
        }
        ArrayList<ContentValues> values = new ArrayList<ContentValues>();
        SaleOrderCostInfo orderInfo;
        while((orderInfo = result.getNextOrderInfo()) != null) {
            ContentValues v = new ContentValues(4);
            v.put(SaleOrderTable.GUID, orderInfo.guid);
            v.put(SaleOrderTable.TML_TOTAL_PRICE, _decimal(orderInfo.totalPrice));
            v.put(SaleOrderTable.TML_TOTAL_TAX, _decimal(orderInfo.totalTax));
            v.put(SaleOrderTable.TML_TOTAL_DISCOUNT, _decimal(orderInfo.totalDiscount));
            Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable3.orderInfo.totalPrice: " + orderInfo.totalPrice);
            Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable3.orderInfo.totalTax: " + orderInfo.totalTax);
            Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable3.orderInfo.totalDiscount: " + orderInfo.totalDiscount);
            values.add(v);
            if (values.size() == BULK_UPDATE_SIZE) {
                bulkUpdate(SaleOrderTable.TABLE_NAME, values, SaleOrderTable.GUID);
                values.clear();
            }
        }
        result.close();
        Log.d(TAG, "RecalcSaleItemTable.bulkRecalcSaleItemTable3.values.size: " + values.size());
        if (!values.isEmpty())
            bulkUpdate(SaleOrderTable.TABLE_NAME, values, SaleOrderTable.GUID);
    }

    public void recalculateOrderTotalPrice(final String orderGuid) {
        Log.d(TAG, "RecalcSaleItemTable.recalculateOrderTotalPrice.orderGuid: " + orderGuid);
        FastOrderTotalQuery.SaleOrderCostInfo info = FastOrderTotalQuery.calc(getContext(), orderGuid);
        if (info != null) {
            Log.d(TAG, "RecalcSaleItemTable.recalculateOrderTotalPrice.info.totalPrice: " + info.totalPrice);

            ContentValues v = new ContentValues(4);
            v.put(SaleOrderTable.TML_TOTAL_PRICE, _decimal(info.totalPrice));
            v.put(SaleOrderTable.TML_TOTAL_TAX, _decimal(info.totalTax));
            v.put(SaleOrderTable.TML_TOTAL_DISCOUNT, _decimal(info.totalDiscount));
            Log.d(TAG, "RecalcSaleItemTable.recalculateOrderTotalPrice.orderInfo.totalPrice: " + info.totalPrice);
            Log.d(TAG, "RecalcSaleItemTable.recalculateOrderTotalPrice.orderInfo.totalTax: " + info.totalTax);
            Log.d(TAG, "RecalcSaleItemTable.recalculateOrderTotalPrice.orderInfo.totalDiscount: " + info.totalDiscount);

            ContentResolver cr = getContext().getContentResolver();
            cr.update(SALE_ORDER_URI, v, SaleOrderTable.GUID + " = ?", new String[]{orderGuid});
        }
    }

    public void bulkRecalculateOrderTotalPriceAfterSync(){
        bulkRecalcSaleItemTable();
    }

    public void recalculateOrderTotalPriceByItem(String saleItemGuid) {
        Log.d(TAG, "RecalcSaleItemTable.recalculateOrderTotalPriceByItem");
        ContentResolver cr = getContext().getContentResolver();

        Cursor c = ProviderAction.query(SALE_ITEM_URI)
                .projection(SaleItemTable.ORDER_GUID, SaleItemTable.ITEM_GUID)
                .where(SaleItemTable.SALE_ITEM_GUID + " = ?", saleItemGuid)
                .perform(cr);

        String orderGuid = null;
        if (c.moveToFirst()) {
            orderGuid = c.getString(0);
        }
        c.close();
        if (TextUtils.isEmpty(orderGuid)) {
            return;
        }
        recalculateOrderTotalPrice(orderGuid);
    }

    void recalculateSaleItemsAvailableQty(HashSet<String> saleItemsGuids) {
        if (saleItemsGuids == null || saleItemsGuids.isEmpty())
            return;
        Cursor c = ProviderAction.query(SALE_ITEM_URI)
                .projection(SaleItemTable.PARENT_GUID, SaleItemTable.QUANTITY)
                .whereIn(SaleItemTable.PARENT_GUID, saleItemsGuids)
                .orderBy(SaleItemTable.PARENT_GUID)
                .perform(getContext());
        if (c.getCount() == 0) {
            c.close();
            return;
        }
        handleCursor(c, false);
    }

    private void handleCursor(Cursor c, boolean fromSync) {
        Logger.d("RecalcSaleItemTable. handleCursor: from sync: " + fromSync + "; size = %d", c.getCount());

        RefundQuantityCursorWrapper returnedQtys = new RefundQuantityCursorWrapper(c);

        ArrayList<ContentValues> values = new ArrayList<ContentValues>(c.getCount());
        RefundQuantity availableQuantity;
        while ((availableQuantity = returnedQtys.getNextRefundQuantity()) != null) {
            ContentValues v = new ContentValues(2);
            v.put(SaleItemTable.SALE_ITEM_GUID, availableQuantity.saleItemGuid);
            v.put(SaleItemTable.TMP_REFUND_QUANTITY, _decimalQty(availableQuantity.refundQty));
            values.add(v);
            if (fromSync && values.size() == BULK_UPDATE_SIZE) {
                Logger.d("RecalcSaleItemTable. handleCursor.bulkUpdate: from sync: " + fromSync + ";  size = %d", values.size());
                bulkUpdate(SaleItemTable.TABLE_NAME, values, SaleItemTable.SALE_ITEM_GUID);
                values.clear();
            }
        }
        returnedQtys.close();

        if (!values.isEmpty()) {
            Logger.d("RecalcSaleItemTable. handleCursor.bulkUpdate: from sync: " + fromSync + ";  size = %d", values.size());
            bulkUpdate(SaleItemTable.TABLE_NAME, values, SaleItemTable.SALE_ITEM_GUID);
        }
    }

    public void recalculateSaleItemsAvailableQty(String saleItemsGuid) {
        if (TextUtils.isEmpty(saleItemsGuid))
            return;
        Cursor c = ProviderAction.query(SALE_ITEM_URI)
                .projection(SaleItemTable.PARENT_GUID, SaleItemTable.QUANTITY)
                .where(SaleItemTable.PARENT_GUID + " = ?", saleItemsGuid)
                .perform(getContext());

        BigDecimal returnedQty = BigDecimal.ZERO;
        while (c.moveToNext()) {
            String saleItemGuid = c.getString(0);
            BigDecimal retQty = _decimalQty(c, 1, BigDecimal.ZERO);//should be negative, it's returned qty
            returnedQty = returnedQty.add(retQty);
        }
        c.close();

        if (BigDecimal.ZERO.compareTo(returnedQty) == 0) {
            return;
        }

        c = ProviderAction.query(SALE_ITEM_URI)
                .projection(SaleItemTable.SALE_ITEM_GUID, SaleItemTable.QUANTITY)
                .where(SaleItemTable.SALE_ITEM_GUID + " = ?", saleItemsGuid)
                .perform(getContext());

        ArrayList<ContentValues> values = new ArrayList<ContentValues>(c.getCount());
        if (c.moveToNext()) {
            String saleItemGuid = c.getString(0);
            BigDecimal qty = _decimalQty(c, 1, BigDecimal.ZERO);

            ContentValues v = new ContentValues(2);
            v.put(SaleItemTable.SALE_ITEM_GUID, saleItemGuid);
            v.put(SaleItemTable.TMP_REFUND_QUANTITY, _decimalQty(returnedQty));
            values.add(v);
        }
        c.close();

        if (!values.isEmpty()) {
            bulkUpdate(SaleItemTable.TABLE_NAME, values, SaleItemTable.SALE_ITEM_GUID);
        }
    }

    public String getSaleOrderGuidByItem(String item) {
        if (TextUtils.isEmpty(item))
            return null;
        Cursor c = ProviderAction.query(SALE_ITEM_URI)
                .projection(SaleItemTable.ORDER_GUID)
                .where(SaleItemTable.SALE_ITEM_GUID + " = ?", item)
                .perform(getContext());

        String order = null;
        if (c.moveToFirst()) {
            order = c.getString(0);
        }
        c.close();
        return order;
    }

    public static class RefundQuantity {

        public final String saleItemGuid;
        public final BigDecimal refundQty;

        public RefundQuantity(String saleItemGuid, BigDecimal refundQty) {
            this.saleItemGuid = saleItemGuid;
            this.refundQty = refundQty;
        }
    }

    public static class RefundQuantityCursorWrapper {

        public final Cursor c;

        public RefundQuantityCursorWrapper(Cursor c) {
            this.c = c;
        }

        public void close() {
            if (!c.isClosed())
                c.close();
        }

        public RefundQuantity getNextRefundQuantity() {
            if (c.getCount() == 0)
                return null;

            if (c.isBeforeFirst())
                c.moveToFirst();

            if (c.isAfterLast())
                return null;

            String saleItemGuid = c.getString(0);
            BigDecimal returnedQty = _decimalQty(c, 1, BigDecimal.ZERO);//should be negative, it's returned qty

            String nextSaleItemGuid;
            while (c.moveToNext()) {
                nextSaleItemGuid = c.getString(0);
                if (!nextSaleItemGuid.equals(saleItemGuid))
                    break;

                returnedQty = returnedQty.add(_decimalQty(c, 1, BigDecimal.ZERO));
            }

            return new RefundQuantity(saleItemGuid, returnedQty);
        }
    }
}
