package com.kaching123.tcr.store.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.RecalcComposersQuery;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.kaching123.tcr.util.ContentValuesUtilBase._decimal;
import static com.kaching123.tcr.util.ContentValuesUtilBase._decimalQty;

/**
 * Created by mayer
 */
public class RecalcItemComposerTable extends ProviderHelper {

    private static final Uri ITEM_COMPOSER_URI = ShopProvider.contentUri(RecalcComposersQuery.URI_CONTENT);

    public RecalcItemComposerTable(Context context, SQLiteOpenHelper dbHelper) {
        super(context, dbHelper);
    }

    public void recalcAfterSync() {
        Logger.d("RecalcItemMovementTable. after sync");
        Cursor c = getContext().getContentResolver().query(ITEM_COMPOSER_URI, null, null, null, null);
        if (c.getCount() == 0) {
            c.close();
        } else {
            handleCursor(c);
        }
    }

    private void handleCursor(Cursor c) {

        AvailableQuantityCursorWrapper result = new AvailableQuantityCursorWrapper(c);

        ArrayList<ContentValues> values = new ArrayList<>();

        AvailableQuantity availableQuantity;
        while ((availableQuantity = result.getNextAvailableQuantity()) != null) {

            String key = availableQuantity.guid;
            String decimal = _decimalQty(availableQuantity.availableQty);

            ContentValues v = new ContentValues(2);
            v.put(ItemTable.GUID, key);
            v.put(ItemTable.TMP_AVAILABLE_QTY, decimal);
            values.add(v);


            bulkUpdate(ItemTable.TABLE_NAME, values, ItemTable.GUID);
            values.clear();

        }
        result.close();
    }

    public static class AvailableQuantity {

        public final String guid;
        public final BigDecimal availableQty;

        public AvailableQuantity(String guid, BigDecimal availableQty) {
            this.guid = guid;
            this.availableQty = availableQty;
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
            BigDecimal availableQty = _decimal(c, 1, 0);
            c.moveToNext();
            return new AvailableQuantity(guid, availableQty);
        }
    }

}
