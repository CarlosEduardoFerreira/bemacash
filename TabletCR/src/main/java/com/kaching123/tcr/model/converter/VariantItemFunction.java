package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.VariantItemModel;

/**
 * Created by aakimov on 29/04/15.
 */
public class VariantItemFunction implements Function<Cursor, VariantItemModel> {

    @Override
    public VariantItemModel apply(Cursor cursor) {
        return new VariantItemModel(cursor);
    }
}