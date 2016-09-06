package com.kaching123.tcr.model.converter;

import android.database.Cursor;

/**
 * Created by vkompaniets on 31.08.2016.
 */
public class StringFunction extends ListConverterFunction<String> {

    @Override
    public String apply(Cursor cursor) {
        return cursor.getString(0);
    }
}
