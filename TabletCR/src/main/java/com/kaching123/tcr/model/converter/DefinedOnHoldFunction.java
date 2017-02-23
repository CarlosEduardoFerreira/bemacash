package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.DefinedOnHoldModel;

import javax.annotation.Nullable;

/**
 * Created by mboychenko on 2/7/2017.
 */

public class DefinedOnHoldFunction implements Function<Cursor, DefinedOnHoldModel> {

    @Override
    public DefinedOnHoldModel apply(@Nullable Cursor cursor) {
        return new DefinedOnHoldModel(cursor);
    }

}
