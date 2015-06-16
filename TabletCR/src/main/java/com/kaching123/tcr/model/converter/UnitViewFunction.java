package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.UnitsView2;
import com.kaching123.tcr.store.ShopStore.UnitTable;

import static com.kaching123.tcr.model.ContentValuesUtil._codeType;
import static com.kaching123.tcr.model.ContentValuesUtil._statusType;

/**
 * Created by mayer
 */
public class UnitViewFunction implements Function<Cursor, Unit> {

    @Override
    public Unit apply(Cursor c) {
        return new Unit(
                c.getString(c.getColumnIndex(UnitsView2.UnitTable.ID)),
                c.getString(c.getColumnIndex(UnitsView2.UnitTable.ITEM_ID)),
                c.getString(c.getColumnIndex(UnitsView2.UnitTable.SERIAL_CODE)),
                _codeType(c, c.getColumnIndex(UnitsView2.UnitTable.CODE_TYPE)),
                _statusType(c, c.getColumnIndex(UnitsView2.UnitTable.STATUS)),
                c.getInt(c.getColumnIndex(UnitsView2.UnitTable.WARRANTY_PERIOD)),
                c.getString(c.getColumnIndex(UnitsView2.UnitTable.SALE_ORDER_ID)),
                c.getString(c.getColumnIndex(UnitsView2.UnitTable.CHILD_ORDER_ID))
        );
    }
}
