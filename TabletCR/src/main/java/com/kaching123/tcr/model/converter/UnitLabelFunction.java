package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.store.ShopStore.UnitLabelTable;

/**
 * Created by alboyko 07.12.2015
 */
public class UnitLabelFunction implements Function<Cursor, UnitLabelModel> {

    @Override
    public UnitLabelModel apply(Cursor c) {
        return new UnitLabelModel(
                c.getString(c.getColumnIndex(UnitLabelTable.GUID)),
                c.getString(c.getColumnIndex(UnitLabelTable.DESCRIPTION)),
                c.getString(c.getColumnIndex(UnitLabelTable.SHORTCUT))
        );
    }
}