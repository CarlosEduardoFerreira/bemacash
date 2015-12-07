package com.kaching123.tcr.function;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.UnitLabelModel;
import com.kaching123.tcr.model.converter.UnitLabelFunction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by alboyko 07.12.2015
 */
public class UnitLabelWrapFunction implements Function<Cursor, List<UnitLabelModel>> {

    private final UnitLabelFunction function = new UnitLabelFunction();

    @Override
    public List<UnitLabelModel> apply(Cursor c) {
        if (!c.moveToFirst())
            return new ArrayList<UnitLabelModel>();

        LinkedHashMap<String, UnitLabelModel> map = new LinkedHashMap<String, UnitLabelModel>();
        do {
            UnitLabelModel item =  function.apply(c);

                map.put(item.guid, item);
        } while (c.moveToNext());

        return new ArrayList<UnitLabelModel>(map.values());
    }

}
