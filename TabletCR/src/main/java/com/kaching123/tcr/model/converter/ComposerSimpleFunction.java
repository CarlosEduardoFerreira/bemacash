package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.store.ShopStore.ComposerTable;
import com.kaching123.tcr.util.ContentValuesUtilBase;


/**
 * Created by hans mayer
 */
public class ComposerSimpleFunction extends ListConverterFunction<ComposerModel> {

    @Override
    public ComposerModel apply(Cursor c) {
        super.apply(c);
        return new ComposerModel(
                c.getString(indexHolder.get(ComposerTable.ID)),
                c.getString(indexHolder.get(ComposerTable.ITEM_HOST_ID)),
                c.getString(indexHolder.get(ComposerTable.ITEM_CHILD_ID)),
                ContentValuesUtilBase._decimal(c.getString(indexHolder.get(ComposerTable.QUANTITY))),
                c.getInt(indexHolder.get(ComposerTable.STORE_TRACKING_ENABLED)) == 1,
                c.getInt(indexHolder.get(ComposerTable.FREE_OF_CHARGE_COMPOSER)) == 1);
    }
}
