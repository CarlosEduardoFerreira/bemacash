package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.base.Predicate;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.SaleOrderViewModel;
import com.kaching123.tcr.model.converter.SaleOrderViewFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gdubina on 03/02/14.
 */
public class SoldOrdersReportQuery {

    private static final Uri SALE_ORDER_URI = ShopProvider.getContentUri(SaleOrderView.URI_CONTENT);

    public static List<SaleOrderViewModel> getItems(final Context context, final boolean isSold, long startTime, long endTime, long registerId, String managerGuid) {
        Query query = ProviderAction.query(SALE_ORDER_URI)
                .where(SaleOrderTable.CREATE_TIME + " >= ? and " + SaleOrderTable.CREATE_TIME + " <= ?", startTime, endTime);
        if (isSold) {
            query.where("(" + SaleOrderTable.STATUS + " = ? or " + SaleOrderTable.STATUS + " = ?)", OrderStatus.COMPLETED.ordinal(), OrderStatus.RETURN.ordinal());
        } else {
            query.where(SaleOrderTable.STATUS + " = ?", OrderStatus.RETURN.ordinal());
        }
        if (registerId > 0) {
            query.where(SaleOrderTable.REGISTER_ID + " = ?", registerId);
        }
        if(managerGuid != null){
            query.where(SaleOrderTable.OPERATOR_GUID + " = ?", managerGuid);
        }

//        query.where(SaleOrderTable.TML_TOTAL_PRICE + " IS NOT NULL");

        Cursor cursor = query.orderBy(SaleOrderTable.CREATE_TIME + " desc").perform(context);

        SaleOrderViewFunction transform = new SaleOrderViewFunction();
        ArrayList<SaleOrderViewModel> items = new ArrayList<SaleOrderViewModel>(cursor.getCount());
        while (cursor.moveToNext()) {
            items.add(transform.apply(cursor));
        }

        cursor.close();

        return items;
    }

    public static List<SaleOrderViewModel> getItemsWithoutTipRefunds(final Context context, final boolean isSold, long startTime, long endTime, long registerId, String managerGuid) {
        List<SaleOrderViewModel> result = getItems(context, isSold, startTime, endTime, registerId, managerGuid);
        return filter(result, new Predicate<SaleOrderViewModel>() {
            @Override
            public boolean apply(SaleOrderViewModel m) {
                return OrderStatus.RETURN != m.orderStatus
                        || (m.tmpTotalPrice != null && m.tmpTotalPrice.compareTo(BigDecimal.ZERO) != 0)
                        || (m.tipsAmount == null || m.tipsAmount.compareTo(BigDecimal.ZERO) == 0);
            }
        });
    }

    public static List<SaleOrderViewModel> getItemsWithoutRefunds(final Context context, final boolean isSold, long startTime, long endTime, long registerId, String managerGuid) {
        List<SaleOrderViewModel> result = getItems(context, isSold, startTime, endTime, registerId, managerGuid);
        return filter(result, new Predicate<SaleOrderViewModel>() {
            @Override
            public boolean apply(SaleOrderViewModel m) {
                return OrderStatus.RETURN != m.orderStatus
                        || (m.tmpTotalPrice != null && m.tmpTotalPrice.compareTo(BigDecimal.ZERO) != 0);
            }
        });
    }

    private static List<SaleOrderViewModel> filter(List<SaleOrderViewModel> list, Predicate<SaleOrderViewModel> predicate) {
        if (list == null)
            return null;
        Iterator<SaleOrderViewModel> it = list.iterator();
        while (it.hasNext()) {
            SaleOrderViewModel m = it.next();
            if (!predicate.apply(m)) {
                it.remove();
            }
        }
        return list;
    }

    /*.wrap(new Function<Cursor, List<SaleOrderViewModel>>() {
        @Override
        public List<SaleOrderViewModel> apply(Cursor c) {
            SaleOrderViewFunction transform = new SaleOrderViewFunction();
            ArrayList<SaleOrderViewModel> list = new ArrayList<SaleOrderViewModel>(c.getCount());
            if (c.moveToFirst()) {
                do {
                    SaleOrderViewModel model = transform.apply(c);
                    if (isSold) {
                        ReturnInfo returnInfo = ExportBaseOrdersCommand.getReturns(context, model.guid);
                        model.tmpTotalPrice = model.tmpTotalPrice.add(returnInfo.totalPrice);
                        model.tmpTotalDiscount = model.tmpTotalDiscount.add(returnInfo.totalDiscount);
                        model.tmpTotalTax = model.tmpTotalTax.add(returnInfo.totalTax);
                    }

                    list.add(model);
                } while (c.moveToNext());
            }
            return list;
        }
    })*/
}
