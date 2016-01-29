package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.SaleOrderItemAddonModel;
/**
 * Created by alboyko on 28.01.2016.
 */
public class SaleOrderAddonItemFunction implements Function<Cursor, SaleOrderItemAddonModel> {
    @Override
    public SaleOrderItemAddonModel apply(Cursor cursor) {
        return new SaleOrderItemAddonModel(cursor);
    }
}
