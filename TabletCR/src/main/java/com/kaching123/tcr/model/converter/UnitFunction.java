package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.store.ShopStore.UnitTable;

import static com.kaching123.tcr.model.ContentValuesUtil._codeType;
import static com.kaching123.tcr.model.ContentValuesUtil._statusType;

/**
 * Created by mayer
 */
public class UnitFunction implements Function<Cursor, Unit> {

    @Override
    public Unit apply(Cursor c) {
        return new Unit(
                c.getString(c.getColumnIndex(UnitTable.ID)),
                c.getString(c.getColumnIndex(UnitTable.ITEM_ID)),
                c.getString(c.getColumnIndex(UnitTable.SERIAL_CODE)),
                _codeType(c, c.getColumnIndex(UnitTable.CODE_TYPE)),
                _statusType(c, c.getColumnIndex(UnitTable.STATUS)),
                c.getInt(c.getColumnIndex(UnitTable.WARRANTY_PERIOD)),
                c.getString(c.getColumnIndex(UnitTable.SALE_ORDER_ID)),
                c.getString(c.getColumnIndex(UnitTable.CHILD_ORDER_ID))
        );
    }
}
