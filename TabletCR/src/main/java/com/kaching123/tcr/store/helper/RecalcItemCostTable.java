package com.kaching123.tcr.store.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.RecalcCostQuery;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.util.ContentValuesUtilBase._decimal;
import static com.kaching123.tcr.util.ContentValuesUtilBase._decimalQty;

/**
 * Created by mayer
 */
public class RecalcItemCostTable extends ProviderHelper {

    private static final Uri ITEM_COST_URI = ShopProvider.contentUri(RecalcCostQuery.URI_CONTENT);

    public RecalcItemCostTable(Context context, SQLiteOpenHelper dbHelper) {
        super(context, dbHelper);
    }

    public void recalcAfterSync() {
        Cursor c = getContext().getContentResolver().query(ITEM_COST_URI, null, null, null, null);
        if (c.getCount() == 0) {
            c.close();
        } else {
            handleCursor(c);
        }
    }

    private void handleCursor(Cursor c) {

        AvailableQuantityCursorWrapper result = new AvailableQuantityCursorWrapper(c);

        ArrayList<ContentValues> values = new ArrayList<ContentValues>();

        AvailableQuantity availableQuantity;
        while ((availableQuantity = result.getNextAvailableQuantity()) != null) {

            String key = availableQuantity.guid;
            String decimal = _decimal(availableQuantity.cost);

            ContentValues v = new ContentValues(2);
            v.put(ItemTable.GUID, key);
            v.put(ItemTable.COST, decimal);
            values.add(v);


            bulkUpdate(ItemTable.TABLE_NAME, values, ItemTable.GUID);
            values.clear();

        }
        result.close();
    }

    public static class AvailableQuantity {

        public final String guid;
        public final BigDecimal cost;

        public AvailableQuantity(String guid, BigDecimal cost) {
            this.guid = guid;
            this.cost = cost;
        }
    }

    public static class AvailableQuantityCursorWrapper {

        public final Cursor c;

        public AvailableQuantityCursorWrapper(Cursor c) {
            this.c = c;
        }

        public void close() {
            if (!c.isClosed())
                c.close();
        }

        public AvailableQuantity getNextAvailableQuantity() {
            if (c.getCount() == 0)
                return null;

            if (c.isBeforeFirst())
                c.moveToFirst();

            if (c.isAfterLast())
                return null;

            String guid = c.getString(0);
            BigDecimal cost = _decimalQty(c.getString(1));
            String nextFlag;
            while (c.moveToNext()) {
                nextFlag = c.getString(0);
                if (!nextFlag.equals(guid))
                    break;

                cost = cost.add(_decimalQty(c, 1));
            }
            return new AvailableQuantity(guid, cost);
        }
    }

}
