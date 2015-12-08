package com.kaching123.tcr.function;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.ComposerExModel;
import com.kaching123.tcr.model.converter.ComposerFunction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by mayer
 */
public class ComposerExWrapFunction implements Function<Cursor, List<ComposerExModel>> {

    private final ComposerFunction function = new ComposerFunction();

    @Override
    public List<ComposerExModel> apply(Cursor c) {
        if (!c.moveToFirst())
            return new ArrayList<ComposerExModel>();

        LinkedHashMap<String, ComposerExModel> map = new LinkedHashMap<String, ComposerExModel>();
        do {
            ComposerExModel item = function.apply(c);

            map.put(item.guid, item);
        } while (c.moveToNext());

        return new ArrayList<ComposerExModel>(map.values());
    }

}
