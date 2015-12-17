package com.kaching123.tcr.function;

import android.database.Cursor;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.model.VariantSubItemModel;
import com.kaching123.tcr.model.converter.VariantSubItemFunction;

/**
 * Created by aakimov on 29/04/15.
 */
public class VariantSubItemWrapFunction implements Function<Cursor, List<VariantSubItemModel>> {
    private final VariantSubItemFunction variantSubItemFunction = new VariantSubItemFunction();

    @Override
    public List<VariantSubItemModel> apply(Cursor cursor) {
        List<VariantSubItemModel> list = new ArrayList<VariantSubItemModel>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                list.add(variantSubItemFunction.apply(cursor));
            } while (cursor.moveToNext());
        }
        return list;
    }
}