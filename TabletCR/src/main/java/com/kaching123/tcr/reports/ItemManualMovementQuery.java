package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.ItemManualMovementView2.ItemMovementTable;
import com.kaching123.tcr.store.ShopSchema2.ItemManualMovementView2.ItemTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemManualMovementView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by vkompaniets on 30.01.14.
 */
public class ItemManualMovementQuery {

    private static final Uri URI_LOG_QTY = ShopProvider.getContentUri(ItemManualMovementView.URI_CONTENT);
    private static final Uri URI_ITEM_MOVEMENT = ShopProvider.getContentUri(ShopStore.ItemMovementTable.URI_CONTENT);

    public static ArrayList<MovementInfo> getItems(Context context, long startTime, long endTime) {

        Cursor c = ProviderAction.query(URI_LOG_QTY)
                .projection(
                        ItemMovementTable.ITEM_GUID,
                        ItemTable.DESCRIPTION,
                        ItemMovementTable.QTY,
                        ItemMovementTable.TMP_AVAILABLE_QTY,
                        ItemMovementTable.CREATE_TIME
                )
                .where(ItemMovementTable.CREATE_TIME + " >= ? and " + ItemMovementTable.CREATE_TIME + " <= ?", startTime, endTime)
                .where(ItemMovementTable.MANUAL + " = ?", 1)
                .orderBy(ItemMovementTable.CREATE_TIME)
                .perform(context);

        HashMap<String, BigDecimal> prevValue = new HashMap<String, BigDecimal>();
        ArrayList<MovementInfo> result = new ArrayList<MovementInfo>();
        while (c.moveToNext()) {
            String guid = c.getString(0);
            String itemName = c.getString(1);
            BigDecimal qty = _decimalQty(c, 2, BigDecimal.ZERO);
            BigDecimal availableQty = _decimalQty(c, 3, BigDecimal.ZERO);
            long date = c.getLong(4);
            BigDecimal prev = prevValue.get(guid);
            BigDecimal diff = qty;
            Logger.d("[LOG] %s (%s) %s = %s", new Date(date), guid, itemName, qty );
            if(prev == null){
                prev = loadPrevData(context, guid, date);
            }
            if(prev != null){
                diff = qty.subtract(prev);
            }
            result.add(new MovementInfo(itemName, date, diff));
            prevValue.put(guid, availableQty);
        }
        c.close();

        return result;
    }

    private static BigDecimal loadPrevData(Context context, String itemGuid, long date){
        Cursor c = ProviderAction
                .query(URI_ITEM_MOVEMENT)
                .projection(
                        ShopStore.ItemMovementTable.TMP_AVAILABLE_QTY,
                        ShopStore.ItemMovementTable.GUID
                )
                .where(ShopStore.ItemMovementTable.ITEM_GUID + " = ?", itemGuid)
                .where(ShopStore.ItemMovementTable.CREATE_TIME + " < ?", date)
                .where(ShopStore.ItemMovementTable.MANUAL + " = ?", 1)
                .orderBy(ShopStore.ItemMovementTable.CREATE_TIME + " DESC")
                .perform(context);

        BigDecimal result = BigDecimal.ZERO;
        if(c.moveToFirst()){
            result = _decimalQty(c.getString(0));
            Logger.d("[LOG] loadPrevData for %s = %s", itemGuid, c.getString(1));
        }
        c.close();
        return result;
    }

    public static class MovementInfo {
        public String itemName;
        public long date;
        public BigDecimal qty;

        public MovementInfo(String itemName, long date, BigDecimal qty) {
            this.itemName = itemName;
            this.date = date;
            this.qty = qty;
        }

        @Override
        public String toString() {
            return date + itemName + qty + "\n";
        }
    }
}
