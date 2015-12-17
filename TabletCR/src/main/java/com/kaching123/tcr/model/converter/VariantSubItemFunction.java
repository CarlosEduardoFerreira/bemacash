package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Function;

import com.kaching123.tcr.model.VariantSubItemModel;

/**
 * Created by aakimov on 29/04/15.
 */
public class VariantSubItemFunction implements Function<Cursor, VariantSubItemModel> {

    @Override
    public VariantSubItemModel apply(Cursor cursor) {
        return new VariantSubItemModel(cursor);
    }
}