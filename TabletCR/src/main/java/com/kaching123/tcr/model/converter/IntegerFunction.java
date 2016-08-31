package com.kaching123.tcr.model.converter;

import android.database.Cursor;

/**
 * Created by vkompaniets on 31.08.2016.
 */
public class IntegerFunction extends ListConverterFunction<Integer> {

    @Override
    public Integer apply(Cursor cursor) {
        return cursor.getInt(0);
    }
}
