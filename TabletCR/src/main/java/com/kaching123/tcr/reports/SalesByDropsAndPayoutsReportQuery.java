package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.base.Function;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import java.math.BigDecimal;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._long;

/**
 * Created by gdubina on 23.01.14.
 */
public class SalesByDropsAndPayoutsReportQuery {

    private final Uri URI_CASH_DRAWER_MOVEMENT = ShopProvider.getContentUri(ShopStore.CashDrawerMovementTable.URI_CONTENT);

    public List<DropsAndPayoutsState> getItems(long resisterId, Context context, long type, long startTime, long endTime) {
        Query query = ProviderAction.query(URI_CASH_DRAWER_MOVEMENT)
                .projection(ShopStore.CashDrawerMovementTable.MOVEMENT_TIME, ShopStore.CashDrawerMovementTable.COMMENT, ShopStore.CashDrawerMovementTable.AMOUNT, ShopStore.CashDrawerMovementTable.TYPE)
                .where(ShopStore.CashDrawerMovementTable.TYPE + " = ?", type)
                .where(ShopStore.CashDrawerMovementTable.MOVEMENT_TIME + " >= ? and " + ShopStore.CashDrawerMovementTable.MOVEMENT_TIME + " <= ?", startTime, endTime);

        if (resisterId != 0)
            query.where(ShopStore.CashDrawerMovementTable.MANAGER_GUID + " = ?", resisterId);

        List list = query.perform(context)
                .toFluentIterable(new ConvertFunction()).toImmutableList();

        return list;
    }

    public class DropsAndPayoutsState {
        public String date;
        public String comment;
        public BigDecimal amount;
        public long type;

        public DropsAndPayoutsState(String date, String comment, BigDecimal amount, long type) {
            this.date = date;
            this.comment = comment;
            this.amount = amount;
            this.type = type;
        }
    }

    private class ConvertFunction implements Function<Cursor, DropsAndPayoutsState> {
        @Override
        public DropsAndPayoutsState apply(Cursor c) {
            return new DropsAndPayoutsState(
                    c.getString(0),
                    c.getString(1),
                    _decimalQty(c.getString(2)),
                    _long(c.getString(3))
            );
        }
    }
}
