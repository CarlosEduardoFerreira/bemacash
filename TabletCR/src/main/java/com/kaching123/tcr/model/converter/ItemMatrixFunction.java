package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.kaching123.tcr.model.ItemMatrixModel;

import com.kaching123.tcr.store.ShopStore;


/**
 * Created by aakimov on 20/05/15.
 */
public class ItemMatrixFunction extends ListConverterFunction<ItemMatrixModel> {


    @Override
    public ItemMatrixModel apply(Cursor c) {
        super.apply(c);

        return new ItemMatrixModel(
                c.getString(indexHolder.get(ShopStore.ItemMatrixTable.GUID)),
                c.getString(indexHolder.get(ShopStore.ItemMatrixTable.NAME)),
                c.getString(indexHolder.get(ShopStore.ItemMatrixTable.PARENT_GUID)),
                c.isNull(indexHolder.get(ShopStore.ItemMatrixTable.CHILD_GUID))
                        ? null : c.getString(indexHolder.get(ShopStore.ItemMatrixTable.CHILD_GUID))
        );
    }

    public static class Wrap implements Function<Cursor, Optional<ItemMatrixModel>> {
        @Override
        public Optional<ItemMatrixModel> apply(Cursor c) {
            if (!c.moveToFirst()) {
                return Optional.absent();
            }
            ItemMatrixModel item = new ItemMatrixFunction().apply(c);
            return Optional.of(item);
        }
    }
}
