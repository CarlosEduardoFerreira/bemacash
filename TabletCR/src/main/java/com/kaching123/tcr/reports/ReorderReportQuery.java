package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.Loader;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopStore.ItemTable;

import java.math.BigDecimal;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._castToReal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by gdubina on 31.01.14.
 */
public class ReorderReportQuery {

    private static final Uri ITEMS_URI = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    public static Loader<List<ItemQtyInfo>> getLoader(Context context) {
        return CursorLoaderBuilder
                .forUri(ITEMS_URI)
                .projection(ItemTable.DESCRIPTION, ItemTable.TMP_AVAILABLE_QTY, ItemTable.RECOMMENDED_QTY)
                .where(ItemTable.ACTIVE_STATUS + " = ?", 1)
                .where(ItemTable.STOCK_TRACKING + " = ?", 1)
                .where(_castToReal(ItemTable.TMP_AVAILABLE_QTY) + " <= " + _castToReal(ItemTable.MINIMUM_QTY))
                .where(_castToReal(ItemTable.TMP_AVAILABLE_QTY) + " < "  + _castToReal(ItemTable.RECOMMENDED_QTY))
                .transform(new ConvertFunction())
                .build(context);
    }

    public static Loader<List<ItemQtyInfo>> getLoader(Context context, String textFilter) {
        return CursorLoaderBuilder
                .forUri(ITEMS_URI)
                .projection(ItemTable.DESCRIPTION, ItemTable.TMP_AVAILABLE_QTY, ItemTable.RECOMMENDED_QTY)
                .where(ItemTable.ACTIVE_STATUS + " = ?", 1)
                .where(ItemTable.STOCK_TRACKING + " = ? ", 1)
                .where(ItemTable.DESCRIPTION + " like ? ", "%" + textFilter + "%")
                .where(_castToReal(ItemTable.TMP_AVAILABLE_QTY) + " <= " + _castToReal(ItemTable.MINIMUM_QTY))
                .where(_castToReal(ItemTable.TMP_AVAILABLE_QTY) + " < "  + _castToReal(ItemTable.RECOMMENDED_QTY))
                .transform(new ConvertFunction())
                .build(context);
    }

    public static List<ItemQtyInfo> getItems(Context context) {
        return ProviderAction.query(ITEMS_URI)
                .projection(ItemTable.DESCRIPTION, ItemTable.TMP_AVAILABLE_QTY, ItemTable.RECOMMENDED_QTY)
                .where(ItemTable.ACTIVE_STATUS + " = ?", 1)
                .where(ItemTable.STOCK_TRACKING + " = ?", 1)
                .where(_castToReal(ItemTable.TMP_AVAILABLE_QTY) + " <= " + _castToReal(ItemTable.MINIMUM_QTY))
                .where(_castToReal(ItemTable.TMP_AVAILABLE_QTY) + " < "  + _castToReal(ItemTable.RECOMMENDED_QTY))
                .perform(context)
                .toFluentIterable(new ConvertFunction()).toImmutableList();
    }

    public static class ItemQtyInfo {
        public final String description;
        public final BigDecimal qty;
        public final BigDecimal recQty;

        public ItemQtyInfo(String description, BigDecimal qty, BigDecimal recQty) {
            this.description = description;
            this.qty = qty;
            this.recQty = recQty;
        }
    }

    private static class ConvertFunction implements Function<Cursor, ItemQtyInfo> {
        @Override
        public ItemQtyInfo apply(Cursor c) {
            return new ItemQtyInfo(
                    c.getString(0),
                    _decimalQty(c.getString(1)),
                    _decimalQty(c.getString(2))
            );
        }
    }
}
