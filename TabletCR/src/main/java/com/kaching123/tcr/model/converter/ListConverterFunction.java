package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.util.ColumnIndexHolder;

/**
 * Created by gdubina on 12/11/13.
 */
public abstract class ListConverterFunction<T> implements Function<Cursor, T>{

    protected ColumnIndexHolder indexHolder = new ColumnIndexHolder();

    @Override
    public T apply(Cursor cursor) {
        indexHolder.updateLazy(cursor);
        return null;
    }
}
