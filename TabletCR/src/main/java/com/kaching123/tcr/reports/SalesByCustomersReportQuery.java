package com.kaching123.tcr.reports;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.Loader;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.function.SalesByCustomersWrapFunction;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.SalesByCustomerModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.CustomerTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderView;

import java.util.List;

/**
 * Created by pkabakov on 18.02.14.
 */
public class SalesByCustomersReportQuery {

    private static final Uri SALE_ORDER_URI = ShopProvider.getContentUri(SaleOrderView.URI_CONTENT);

    public static Loader<List<SalesByCustomerModel>> query(Context context, long startTime, long endTime) {
        CursorLoaderBuilder loader = CursorLoaderBuilder.forUri(SALE_ORDER_URI)
                .where(SaleOrderTable.CREATE_TIME + " >= ? and " + SaleOrderTable.CREATE_TIME + " <= ?", startTime, endTime)
                .where("(" + SaleOrderTable.STATUS + " = ? or " + SaleOrderTable.STATUS + " = ?)", OrderStatus.COMPLETED.ordinal(), OrderStatus.RETURN.ordinal())
                .where(CustomerTable.GUID + " is not null")
                .orderBy(CustomerTable.LAST_NAME);
        return loader.wrap(new SalesByCustomersWrapFunction())
                .build(context);
    }

    public static Query syncQuery(long startTime, long endTime) {
       return ProviderAction.query(SALE_ORDER_URI)
                .where(SaleOrderTable.CREATE_TIME + " >= ? and " + SaleOrderTable.CREATE_TIME + " <= ?", startTime, endTime)
                .where("(" + SaleOrderTable.STATUS + " = ? or " + SaleOrderTable.STATUS + " = ?)", OrderStatus.COMPLETED.ordinal(), OrderStatus.RETURN.ordinal())
                .where(CustomerTable.GUID + " is not null")
                .orderBy(CustomerTable.LAST_NAME);
    }
}
