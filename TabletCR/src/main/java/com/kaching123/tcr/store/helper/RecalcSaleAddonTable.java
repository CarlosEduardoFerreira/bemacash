package com.kaching123.tcr.store.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;

import java.util.HashSet;

/**
 * Created by hamsterksu on 19.09.2014.
 */
public class RecalcSaleAddonTable extends ProviderHelper {

    private static final Uri SALE_ITEM_URI = ShopProvider.contentUri(SaleItemTable.URI_CONTENT);

    private RecalcSaleItemTable saleItemHelper;

    public RecalcSaleAddonTable(Context context, SQLiteOpenHelper dbHelper) {
        super(context, dbHelper);
        saleItemHelper = new RecalcSaleItemTable(context, dbHelper);
    }

    public void bulkRecalcSaleAddonTableAfterSync() {
        Logger.d("RecalcSaleAddonTable. after sync");
        saleItemHelper.bulkRecalcSaleItemTableAfterSync();
    }

    public void bulkRecalcSaleAddonTable(ContentValues[] addons) {
        if (addons == null)
            return;
        HashSet<String> items = new HashSet<String>();
        for (ContentValues a : addons) {
            if (a.containsKey(SaleAddonTable.IS_DELETED) && a.getAsBoolean(SaleAddonTable.IS_DELETED))
                continue;
            if (a.containsKey(SaleAddonTable.UPDATE_IS_DRAFT) && a.getAsBoolean(SaleAddonTable.UPDATE_IS_DRAFT))
                continue;
            items.add(a.getAsString(SaleAddonTable.ITEM_GUID));
        }

        Logger.d("RecalculateOrderPrice: bulkRecalcSaleAddonTable SaleAddonTable: %d", items.size());
        if (items.isEmpty())
            return;
        HashSet<String> orders = getSaleOrderGuidByItems(items);
        saleItemHelper.bulkRecalcSaleItemTable(orders);
    }

    private HashSet<String> getSaleOrderGuidByItems(HashSet<String> items) {
        if (items == null || items.isEmpty())
            return new HashSet<String>(0);
        Cursor c = ProviderAction.query(SALE_ITEM_URI)
                .projection(SaleItemTable.ORDER_GUID)
                .whereIn(SaleItemTable.SALE_ITEM_GUID, items)
                .perform(getContext());

        HashSet<String> orders = new HashSet<String>();
        while (c.moveToNext()) {
            String order = c.getString(0);
            if (TextUtils.isEmpty(order)) {
                Logger.d("RecalculateOrderPrice: getSaleOrderGuidByItems EMPTY");
                continue;
            }
            orders.add(order);

        }
        c.close();
        return orders;
    }

}
