package com.kaching123.tcr.function;

import android.database.Cursor;

import com.google.common.base.Function;
import com.kaching123.tcr.model.DiscountBundle;
import com.kaching123.tcr.model.MultipleDiscountModel;
import com.kaching123.tcr.store.ShopStore.MultipleDiscountTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

/**
 * Created by vkompaniets on 24.08.2016.
 */
public class MultipleDiscountWrapFunction implements Function<Cursor, List<DiscountBundle>> {

    @Nullable
    @Override
    public List<DiscountBundle> apply(@Nullable Cursor c) {
        if (c == null || c.getCount() == 0)
            return Collections.EMPTY_LIST;

        HashMap<String, List<MultipleDiscountModel>> map = new HashMap<>();
        while (c.moveToNext()){
            String bundleId = c.getString(c.getColumnIndex(MultipleDiscountTable.BUNDLE_ID));
            List<MultipleDiscountModel> bundleItems = map.get(bundleId);
            if (bundleItems == null){
                bundleItems = new ArrayList<>();
                map.put(bundleId, bundleItems);
            }
            MultipleDiscountModel bundleItem = new MultipleDiscountModel(c);
            bundleItems.add(bundleItem);
        }

        ArrayList<DiscountBundle> result = new ArrayList<>(map.size());
        for (Entry<String, List<MultipleDiscountModel>> entry : map.entrySet()){
            result.add(new DiscountBundle(entry.getKey(), entry.getValue()));
        }

        return result;
    }
}
