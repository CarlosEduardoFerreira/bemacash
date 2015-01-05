package com.kaching123.tcr.function;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.converter.UnitFunction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by mayer
 */
public class UnitWrapFunction implements Function<Cursor, List<Unit>> {

    private final UnitFunction function = new UnitFunction();

    @Override
    public List<Unit> apply(Cursor c) {
        if (!c.moveToFirst())
            return new ArrayList<Unit>();

        LinkedHashMap<String, Unit> map = new LinkedHashMap<String, Unit>();
        do {
            Unit item =  function.apply(c);

                map.put(item.guid, item);
        } while (c.moveToNext());

        return new ArrayList<Unit>(map.values());
    }

}
