package com.kaching123.tcr.function;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.converter.ComposerSimpleFunction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by mayer
 */
public class ComposerWrapFunction implements Function<Cursor, List<ComposerModel>> {

    private final ComposerSimpleFunction function = new ComposerSimpleFunction();

    @Override
    public List<ComposerModel> apply(Cursor c) {
        if (!c.moveToFirst())
            return new ArrayList<ComposerModel>();

        LinkedHashMap<String, ComposerModel> map = new LinkedHashMap<String, ComposerModel>();
        do {
            ComposerModel item = function.apply(c);

            map.put(item.guid, item);
        } while (c.moveToNext());

        return new ArrayList<ComposerModel>(map.values());
    }

}
