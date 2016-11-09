package com.kaching123.tcr.store.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.RecalcItemMovementForItemTableView;
import com.kaching123.tcr.store.ShopStore.RecalcItemMovementTableView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by hamsterksu on 19.09.2014.
 */
public class RecalcItemMovementTable extends ProviderHelper {

    private static final Uri ITEM_MOVEMENT_REAL_URI = ShopProvider.contentUri(ItemMovementTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENT_SYNCED_URI = ShopProvider.contentUri(RecalcItemMovementTableView.URI_CONTENT);
    private static final Uri ITEM_MOVEMENT_4ITEM_SYNCED_URI = ShopProvider.contentUri(RecalcItemMovementForItemTableView.URI_CONTENT);

    private static final int BULK_UPDATE_SIZE = 1000;

    public RecalcItemMovementTable(Context context, SQLiteOpenHelper dbHelper) {
        super(context, dbHelper);
    }

    public void bulkRecalcAvailableItemMovementTableAfterSync(boolean forItem) {
        Logger.d("RecalcItemMovementTable. after sync");
        Cursor c = getContext().getContentResolver().query(
                forItem ? ITEM_MOVEMENT_4ITEM_SYNCED_URI : ITEM_MOVEMENT_SYNCED_URI,
                null,
                null, null,
                null);
        if (c!=null && c.getCount() == 0) {
            c.close();
            return;
        }
        handleCursor(c, true);
    }

    public void bulkRecalcAvailableItemMovementTable(ContentValues[] movements) {
        if (movements == null)
            return;
        HashSet<String> items = new HashSet<String>();
        HashSet<String> movementsFlags = new HashSet<String>();
        for (ContentValues m : movements) {
            if (m.containsKey(ItemMovementTable.IS_DELETED) && m.getAsBoolean(ItemMovementTable.IS_DELETED))
                continue;
            if (m.containsKey(ItemMovementTable.UPDATE_IS_DRAFT) && m.getAsBoolean(ItemMovementTable.UPDATE_IS_DRAFT))
                continue;
            items.add(m.getAsString(ItemMovementTable.ITEM_GUID));
            movementsFlags.add(m.getAsString(ItemMovementTable.ITEM_UPDATE_QTY_FLAG));
        }
        Logger.d("recalculateAvailableQty: bulkRecalcAvailableItemMovementTable ItemMovementTable: %d", items.size());
        if (!items.isEmpty()) {
            //itemTableHelper.recalculateAvailableQty(items);
        }
        if (!movementsFlags.isEmpty()) {
            recalculateMovementAvailableQty(movementsFlags);
        }

    }

    public void recalculateMovementAvailableQty(String movementFlag) {
        //TMP_AVAILABLE_QTY
        Logger.d("recalculateMovementAvailableQty: %s", movementFlag);

        //BigDecimal availableQty = BigDecimal.ZERO;

        Cursor c = ProviderAction.query(ITEM_MOVEMENT_REAL_URI)
                .projection(ItemMovementTable.ITEM_UPDATE_QTY_FLAG,
                        ItemMovementTable.QTY)
                .where(ItemMovementTable.ITEM_UPDATE_QTY_FLAG + " = ?", movementFlag)
                .orderBy(ItemMovementTable.ITEM_UPDATE_QTY_FLAG)
                .perform(getContext());
        /*while (c.moveToNext()) {
            availableQty = availableQty.add(_decimalQty(c, 0));
        }
        c.close();

        Logger.d("recalculateMovementAvailableQty: %s; available = %s", movementFlag, availableQty);
        ProviderAction.update(ITEM_MOVEMENT_REAL_URI)
                .value(ItemMovementTable.TMP_AVAILABLE_QTY, _decimalQty(availableQty))
                .where(ItemMovementTable.ITEM_UPDATE_QTY_FLAG + " = ?", movementFlag)
                .where(ItemMovementTable.MANUAL + " = ?", 1)
                .perform(getContext());*/

        if (c.getCount() == 0) {
            c.close();
            return;
        }
        handleCursor(c, false);
    }

    void recalculateMovementAvailableQty(HashSet<String> movementFlags) {
        if (movementFlags == null || movementFlags.isEmpty())
            return;

        Logger.d("recalculateMovementAvailableQty2");

        //BigDecimal availableQty = BigDecimal.ZERO;

        Cursor c = ProviderAction.query(ITEM_MOVEMENT_REAL_URI)
                .projection(
                        ItemMovementTable.ITEM_UPDATE_QTY_FLAG,
                        ItemMovementTable.QTY
                )
                .whereIn(ItemMovementTable.ITEM_UPDATE_QTY_FLAG, movementFlags)
                .orderBy(ItemMovementTable.ITEM_UPDATE_QTY_FLAG)
                .perform(getContext());
        if (c.getCount() == 0) {
            c.close();
            return;
        }
        handleCursor(c, false);
    }

    private void handleCursor(Cursor c, boolean fromSync) {
        Logger.d("RecalcItemMovementTable. handleCursor: fromSync: " + fromSync + "; size = %d", c.getCount());

        AvailableQuantityCursorWrapper result = new AvailableQuantityCursorWrapper(c);

        ArrayList<ContentValues> values = new ArrayList<ContentValues>();
        ArrayList<ContentValues> itemsValues = new ArrayList<ContentValues>();

        AvailableQuantity availableQuantity;
        while ((availableQuantity = result.getNextAvailableQuantity()) != null) {
            //Logger.d("recalculateMovementAvailableQty2 %s = %s", e.getKey(), e.getValue());

            String key = availableQuantity.flag;
            String decimal = _decimalQty(availableQuantity.availableQty);

            Logger.d("RecalcItemMovementTable. handleCursor: fromSync: " + fromSync + "; update %s = %s", key, decimal);
            ContentValues v = new ContentValues(2);
            v.put(ItemMovementTable.ITEM_UPDATE_QTY_FLAG, key);
            v.put(ItemMovementTable.TMP_AVAILABLE_QTY, decimal);
            values.add(v);

            ContentValues vi = new ContentValues(2);
            vi.put(ItemTable.UPDATE_QTY_FLAG, key);
            vi.put(ItemTable.TMP_AVAILABLE_QTY, decimal);
            itemsValues.add(vi);

            if (fromSync && (values.size() == BULK_UPDATE_SIZE || itemsValues.size() == BULK_UPDATE_SIZE)) {
                Logger.d("RecalcItemMovementTable. handleCursor.ItemMovementTable: fromSync: " + fromSync + "; %d", values.size());
                //update start of group
                bulkUpdate(ItemMovementTable.TABLE_NAME, values, ItemMovementTable.ITEM_UPDATE_QTY_FLAG,
                        ItemMovementTable.MANUAL + " = ?", new String[]{"1"});
                values.clear();

                Logger.d("RecalcItemMovementTable. handleCursor.ItemTable: fromSync: " + fromSync + "; %d", itemsValues.size());
                //update item QTY
                bulkUpdate(ItemTable.TABLE_NAME, itemsValues, ItemTable.UPDATE_QTY_FLAG, null, null);
                Logger.d("RecalcItemMovementTable. handleCursor fromSync: " + fromSync + " end");
                itemsValues.clear();
            }
        }
        result.close();

        if (!values.isEmpty() || !itemsValues.isEmpty()) {
            Logger.d("RecalcItemMovementTable. handleCursor.ItemMovementTable: fromSync: " + fromSync + "; %d", values.size());
            //update start of group
            bulkUpdate(ItemMovementTable.TABLE_NAME, values, ItemMovementTable.ITEM_UPDATE_QTY_FLAG,
                    ItemMovementTable.MANUAL + " = ?", new String[]{"1"});

            Logger.d("RecalcItemMovementTable. handleCursor.ItemTable: fromSync: " + fromSync + "; %d", itemsValues.size());
            //update item QTY
            bulkUpdate(ItemTable.TABLE_NAME, itemsValues, ItemTable.UPDATE_QTY_FLAG, null, null);
            Logger.d("RecalcItemMovementTable. handleCursor fromSync: " + fromSync + "  end");
        }
    }

    public static class AvailableQuantity {

        public final String flag;
        public final BigDecimal availableQty;

        public AvailableQuantity(String flag, BigDecimal availableQty) {
            this.flag = flag;
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

            String flag = c.getString(0);
            BigDecimal availableQty = _decimalQty(c.getString(1));
            Logger.d("RecalcItemMovementTable. handleCursor: %s = %s", flag, c.getString(1));

            String nextFlag;
            while (c.moveToNext()) {
                nextFlag = c.getString(0);
                if (!nextFlag.equals(flag))
                    break;

                availableQty = availableQty.add(_decimalQty(c, 1, BigDecimal.ZERO));
            }

            return new AvailableQuantity(flag, availableQty);
        }
    }

}
