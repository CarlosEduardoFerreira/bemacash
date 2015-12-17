package com.kaching123.tcr.function;

import android.database.Cursor;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.converter.ItemMatrixFunction;

/**
 * Created by aakimov on 22/05/15.
 */
public class ItemMatrixWrapFunction implements Function<Cursor, List<ItemMatrixModel>> {
    private final ItemMatrixFunction itemMatrixFunction = new ItemMatrixFunction();

    @Override
    public List<ItemMatrixModel> apply(Cursor cursor) {
        List<ItemMatrixModel> list = new ArrayList<ItemMatrixModel>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                list.add(itemMatrixFunction.apply(cursor));
            } while (cursor.moveToNext());
        }
        return list;
    }
}