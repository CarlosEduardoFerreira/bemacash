package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.store.ShopSchema2.ComposerView2.ItemHostTable;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._priceType;

/**
 * Created by gdubina on 22/11/13.
 */
public class ItemFromComposerHostViewExFunction extends ListConverterFunction<ItemExModel> {

    public static String[] PROJECTION = new String[]{
            ItemHostTable.GUID,
            ItemHostTable.DESCRIPTION,
            ItemHostTable.CODE,
            ItemHostTable.EAN_CODE,
            ItemHostTable.PRODUCT_CODE,
            ItemHostTable.PRICE_TYPE,
            ItemHostTable.SALE_PRICE,
            ItemHostTable.TMP_AVAILABLE_QTY,
            ItemHostTable.UNITS_LABEL,
            ItemHostTable.UNIT_LABEL_ID,
            ItemHostTable.STOCK_TRACKING,
            ItemHostTable.ACTIVE_STATUS,
            ItemHostTable.COST,
            ItemHostTable.MINIMUM_QTY,
            ItemHostTable.RECOMMENDED_QTY,
            ItemHostTable.IS_DELETED
    };

    @Override
    public ItemExModel apply(Cursor c) {
        super.apply(c);

        return new ItemExModel(
                c.getString(indexHolder.get(ItemHostTable.GUID)),
                null,
                c.getString(indexHolder.get(ItemHostTable.DESCRIPTION)),
                c.getString(indexHolder.get(ItemHostTable.CODE)),
                c.getString(indexHolder.get(ItemHostTable.EAN_CODE)),
                c.getString(indexHolder.get(ItemHostTable.PRODUCT_CODE)),
                _priceType(c, indexHolder.get(ItemHostTable.PRICE_TYPE)),
                _decimal(c.getString(indexHolder.get(ItemHostTable.SALE_PRICE))),
                _decimalQty(c.getString(indexHolder.get(ItemHostTable.TMP_AVAILABLE_QTY))),
                c.getString(indexHolder.get(ItemHostTable.UNIT_LABEL_ID)),
                null,
                c.getInt(indexHolder.get(ItemHostTable.STOCK_TRACKING)) == 1,
                c.getInt(indexHolder.get(ItemHostTable.ACTIVE_STATUS)) == 1,
                false,
                c.getInt(indexHolder.get(ItemHostTable.SALABLE)) == 1,
                null,
                null,
                false,
                _decimal(c.getString(indexHolder.get(ItemHostTable.COST))),
                _decimalQty(c.getString(indexHolder.get(ItemHostTable.MINIMUM_QTY))),
                _decimalQty(c.getString(indexHolder.get(ItemHostTable.RECOMMENDED_QTY))),
                null,
                null,
                null,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                null,
                null,
                null,
                null,
                0,
                null,
                0,
                false,
                false,
                null,
                false,
                null,
                null,
                null, null, false);
    }


}
