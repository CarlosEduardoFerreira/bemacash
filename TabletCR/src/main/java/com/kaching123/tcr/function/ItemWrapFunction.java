package com.kaching123.tcr.function;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.converter.ItemFunction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by alboyko 07.12.2015
 */
public class ItemWrapFunction implements Function<Cursor, List<ItemModel>> {

    private final ItemFunction function = new ItemFunction();

    @Override
    public List<ItemModel> apply(Cursor c) {
        if (!c.moveToFirst())
            return new ArrayList<ItemModel>();

        LinkedHashMap<String, ItemModel> map = new LinkedHashMap<String, ItemModel>();
        do {
            ItemModel item =  function.apply(c);

            map.put(item.guid, item);
        } while (c.moveToNext());

        return new ArrayList<ItemModel>(map.values());
    }

}
