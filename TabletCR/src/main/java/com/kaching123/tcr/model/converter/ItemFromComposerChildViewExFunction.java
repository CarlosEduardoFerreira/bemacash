package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.ComposerView2.ItemChildTable;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._priceType;

/**
 * Created by gdubina on 22/11/13.
 */
public class ItemFromComposerChildViewExFunction extends ListConverterFunction<ItemExModel> {

    public static String[] PROJECTION = new String[]{
            ItemChildTable.GUID,
            ItemChildTable.DESCRIPTION,
            ItemChildTable.CODE,
            ItemChildTable.EAN_CODE,
            ItemChildTable.PRODUCT_CODE,
            ItemChildTable.PRICE_TYPE,
            ItemChildTable.SALE_PRICE,
            ItemChildTable.TMP_AVAILABLE_QTY,
            ItemChildTable.UNITS_LABEL,
            ItemChildTable.UNIT_LABEL_ID,
            ItemChildTable.STOCK_TRACKING,
            ItemChildTable.ACTIVE_STATUS,
            ItemChildTable.COST,
            ItemChildTable.MINIMUM_QTY,
            ItemChildTable.RECOMMENDED_QTY,
            ItemChildTable.IS_DELETED
    };

    @Override
    public ItemExModel apply(Cursor c) {
        super.apply(c);
        String shortCut;
        try {
            shortCut = c.getString(indexHolder.get(ShopSchema2.ComposerView2.UnitLabelTable.SHORTCUT));
        } catch (IllegalArgumentException noItem) {
            shortCut = null;
        }

        return new ItemExModel(
                c.getString(indexHolder.get(ItemChildTable.GUID)),
                null,
                c.getString(indexHolder.get(ItemChildTable.DESCRIPTION)),
                c.getString(indexHolder.get(ItemChildTable.CODE)),
                c.getString(indexHolder.get(ItemChildTable.EAN_CODE)),
                c.getString(indexHolder.get(ItemChildTable.PRODUCT_CODE)),
                _priceType(c, indexHolder.get(ItemChildTable.PRICE_TYPE)),
                _decimal(c.getString(indexHolder.get(ItemChildTable.SALE_PRICE))),
                _decimalQty(c.getString(indexHolder.get(ItemChildTable.TMP_AVAILABLE_QTY))),
                c.getString(indexHolder.get(ItemChildTable.UNITS_LABEL)),
                c.getString(indexHolder.get(ItemChildTable.UNIT_LABEL_ID)),
                //shortCut,
                c.getInt(indexHolder.get(ItemChildTable.STOCK_TRACKING)) == 1,
                c.getInt(indexHolder.get(ItemChildTable.ACTIVE_STATUS)) == 1,
                false,
                c.getInt(indexHolder.get(ItemChildTable.SALABLE)) == 1,
                null,
                null,
                false,
                _decimal(c.getString(indexHolder.get(ItemChildTable.COST))),
                _decimalQty(c.getString(indexHolder.get(ItemChildTable.MINIMUM_QTY))),
                _decimalQty(c.getString(indexHolder.get(ItemChildTable.RECOMMENDED_QTY))),
                //null,
                null,
                null,
                0,
                0,
                0,
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
                null);
    }
}

