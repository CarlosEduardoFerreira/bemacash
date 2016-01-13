package com.kaching123.tcr.function;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.VariantItemFunction;
import com.kaching123.tcr.model.VariantItemModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aakimov on 22/05/15.
 */
public class VariantItemWrapFunction implements Function<Cursor, List<VariantItemModel>> {
    private final VariantItemFunction variantItemFunction = new VariantItemFunction();

    @Override
    public List<VariantItemModel> apply(Cursor cursor) {
        List<VariantItemModel> list = new ArrayList<>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                list.add(variantItemFunction.apply(cursor));
            } while (cursor.moveToNext());
        }
        return list;
    }
}