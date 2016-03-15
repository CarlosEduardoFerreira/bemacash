package com.kaching123.tcr;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;

public class InventoryHelper {

    private static final Uri ITEM_URI = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    public static long getLimit() {
        long limit = TcrApplication.get().getShopPref().inventoryLimit().get();
        Logger.d("[Inventory] limit = %d", limit);
        return limit;
    }

    public static boolean isLimited() {
        return TcrApplication.get().isFreemium() || PlanOptions.isInventoryLimited();
    }

    public static boolean isLimitReached(Context context) {
        long itemsCount = getLimitedItemsCount(context);
        Logger.d("[Inventory] itemsCount = %d inventoryLimit = %d isInventoryLimited = %s",
                itemsCount, InventoryHelper.getLimit(), InventoryHelper.isLimited());
        return isLimited() && itemsCount >= getLimit();
    }

    public static long getLimitedItemsCount(Context context) {
        long itemsCount = 0;
        Cursor c = ProviderAction.query(ITEM_URI)
                .projection("count(" + ItemTable.GUID + ")")
                .where(ItemTable.ITEM_REF_TYPE + " <> ? ", ItemRefType.Reference.ordinal())
                .perform(context);
        if (c != null && c.moveToFirst()) {
            itemsCount = c.getLong(0);
            c.close();
        }
        return itemsCount;
    }

    private InventoryHelper() {
    }

}
