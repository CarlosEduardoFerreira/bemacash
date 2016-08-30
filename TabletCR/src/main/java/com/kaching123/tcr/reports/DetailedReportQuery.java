package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mboychenko on 25.08.2016.
 */
public class DetailedReportQuery {

    protected static final Uri URI_SALE_ORDER = ShopProvider.contentUri(ShopStore.SaleOrderTable.URI_CONTENT);

    public static List<String> loadReceiptsReport(Context context, long registerId, long fromDate, long toDate) {
        Query query = ProviderAction.query(URI_SALE_ORDER)
                .where(ShopStore.SaleOrderTable.CREATE_TIME + " BETWEEN ? AND ?", fromDate, toDate)
                .where("(" + ShopStore.SaleOrderTable.STATUS + " = ? OR " + ShopStore.SaleOrderTable.STATUS + " = ?)", OrderStatus.COMPLETED.ordinal(), OrderStatus.RETURN.ordinal())
                .orderBy(ShopStore.SaleOrderTable.CREATE_TIME + " DESC");
        if(registerId == 0){
            query.projection(ShopStore.SaleOrderTable.GUID);
        } else {
            query.projection(ShopStore.SaleOrderTable.GUID)
                    .where(ShopStore.SaleOrderTable.REGISTER_ID + " = ?", registerId);
        }

        Cursor c = query.perform(context);
        return readGuidsCursor(c);
    }

    public static String loadSumOfOrdersTotal(Context context, long registerId, long fromDate, long toDate) {
        Query query = ProviderAction.query(URI_SALE_ORDER)
                .where(ShopStore.SaleOrderTable.CREATE_TIME + " BETWEEN ? AND ?", fromDate, toDate)
                .where("(" + ShopStore.SaleOrderTable.STATUS + " = ? OR " + ShopStore.SaleOrderTable.STATUS + " = ?)", OrderStatus.COMPLETED.ordinal(), OrderStatus.RETURN.ordinal())
                .orderBy(ShopStore.SaleOrderTable.CREATE_TIME + " DESC");
        if(registerId == 0){
            query.projection("sum(" + ShopStore.SaleOrderTable.TML_TOTAL_PRICE + ")");
        } else {
            query.projection("sum(" + ShopStore.SaleOrderTable.TML_TOTAL_PRICE + ")")
                    .where(ShopStore.SaleOrderTable.REGISTER_ID + " = ?", registerId);
        }

        Cursor c = query.perform(context);
        return readSumCursor(c);
    }

    private static List<String> readGuidsCursor(Cursor c) {
        if (c == null) {
            return new ArrayList<>(0);
        }
        ArrayList<String> result = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                String orderGuid = c.getString(c.getColumnIndex(ShopStore.SaleOrderTable.GUID));
                result.add(orderGuid);
            } while (c.moveToNext());
        }
        return result;
    }

    private static String readSumCursor(Cursor c) {
        if (c == null) {
            return "0";
        }
        if (c.moveToFirst()) {
            return c.getString(0);
        }
        return "0";
    }
}
