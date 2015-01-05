package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.SaleOrderModel;

/**
 * Created by gdubina on 07/11/13.
 */
public class SaleOrderFunction implements Function<Cursor, SaleOrderModel> {

    @Override
    public SaleOrderModel apply(Cursor c) {
        return new SaleOrderModel(c);
    }
}
