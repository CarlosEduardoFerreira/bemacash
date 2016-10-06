package com.kaching123.tcr.model.converter;

import android.content.Context;
import android.support.v4.content.Loader;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;

import java.util.List;

/**
 * Created by gdubina on 03.12.13.
 */

public class SaleOrderItemViewModelWrapFunction extends SaleItemWrapFunction {

    public SaleOrderItemViewModelWrapFunction(Context context) {
        super(context);
    }

    @Override
    protected boolean loadSerialItems() {
        return true;
    }

    @Override
    protected boolean recalcSaleItems() {
        return true;
    }

    public static Loader<List<SaleOrderItemViewModel>> createLoader(Context context, String orderGuid) {
        return CursorLoaderBuilder.forUri(URI_ORDER_ITEMS)
                .where(SaleItemTable.ORDER_GUID + " = ? ", orderGuid)
                .orderBy(ORDER_BY)
                .transform(new SaleOrderItemViewModelWrapFunction(context))
                .build(context);
    }
}