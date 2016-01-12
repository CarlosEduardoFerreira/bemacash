package com.kaching123.tcr.model;

import android.database.Cursor;

import com.google.common.base.Function;

/**
 * Created by aakimov on 29/04/15.
 */
public class VariantItemFunction implements Function<Cursor, VariantItemModel> {

    @Override
    public VariantItemModel apply(Cursor cursor) {
        return new VariantItemModel(cursor);
    }
}