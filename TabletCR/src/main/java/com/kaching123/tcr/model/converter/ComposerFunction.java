package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.kaching123.tcr.model.ComposerExModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.ComposerView2.ComposerTable;
import com.kaching123.tcr.store.ShopSchema2.ComposerView2.ItemHostTable;
import com.kaching123.tcr.util.ContentValuesUtilBase;

import static com.kaching123.tcr.model.ContentValuesUtil._priceType;

/**
 * Created by mayer
 */
public class ComposerFunction implements Function<Cursor, ComposerExModel> {

    @Override
    public ComposerExModel apply(Cursor c) {
        ItemExModel parent = new ItemExModel(
                c.getString(c.getColumnIndex(ItemHostTable.GUID)),
                null,
                c.getString(c.getColumnIndex(ItemHostTable.DESCRIPTION)),
                c.getString(c.getColumnIndex(ItemHostTable.CODE)),
                c.getString(c.getColumnIndex(ItemHostTable.EAN_CODE)),
                c.getString(c.getColumnIndex(ItemHostTable.PRODUCT_CODE)),
                _priceType(c, c.getColumnIndex(ItemHostTable.PRICE_TYPE)),
                ContentValuesUtilBase._decimal(c.getString(c.getColumnIndex(ItemHostTable.SALE_PRICE))),
                ContentValuesUtilBase._decimalQty(c.getString(c.getColumnIndex(ItemHostTable.TMP_AVAILABLE_QTY))),
                c.getString(c.getColumnIndex(ItemHostTable.UNITS_LABEL)),
                c.getString(c.getColumnIndex(ItemHostTable.UNIT_LABEL_ID)),
                null,
                c.getInt(c.getColumnIndex(ItemHostTable.STOCK_TRACKING)) == 1,
                c.getInt(c.getColumnIndex(ItemHostTable.ACTIVE_STATUS)) == 1,
                false,
                c.getInt(c.getColumnIndex(ItemHostTable.SALABLE)) == 1,
                null,
                null,
                false,
                ContentValuesUtilBase._decimal(c.getString(c.getColumnIndex(ItemHostTable.COST))),
                ContentValuesUtilBase._decimalQty(c.getString(c.getColumnIndex(ItemHostTable.MINIMUM_QTY))),
                ContentValuesUtilBase._decimalQty(c.getString(c.getColumnIndex(ItemHostTable.RECOMMENDED_QTY))),
                c.getString(c.getColumnIndex(ItemHostTable.UPDATE_QTY_FLAG)),
                null,
                null,
                0, // modifiers count
                0,
                0,
                null,
                null,
                null,
                0,
                null,//printer alias
                0,//btnView
                false, //hasNotes
                false,//serializable
                null,
                false,
                null,
                null,
                null,
                false);

        String shortCut;
        try {
            shortCut = c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.UnitLabelTable.SHORTCUT));
        } catch (IllegalArgumentException noItem) {
            shortCut = null;
        }
        ItemExModel child = new ItemExModel(
                c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.GUID)),
                null,
                c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.DESCRIPTION)),
                c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.CODE)),
                c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.EAN_CODE)),
                c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.PRODUCT_CODE)),
                _priceType(c, c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.PRICE_TYPE)),
                ContentValuesUtilBase._decimal(c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.SALE_PRICE))),
                ContentValuesUtilBase._decimalQty(c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.TMP_AVAILABLE_QTY))),
                c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.UNITS_LABEL)),
                c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.UNIT_LABEL_ID)),
                shortCut,
                c.getInt(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.STOCK_TRACKING)) == 1,
                c.getInt(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.ACTIVE_STATUS)) == 1,
                false,
                c.getInt(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.SALABLE)) == 1,
                null,
                null,
                false,
                ContentValuesUtilBase._decimal(c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.COST))),
                ContentValuesUtilBase._decimalQty(c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.MINIMUM_QTY))),
                ContentValuesUtilBase._decimalQty(c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.RECOMMENDED_QTY))),
                c.getString(c.getColumnIndex(ShopSchema2.ComposerView2.ItemChildTable.UPDATE_QTY_FLAG)),
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
                null,
                null,
                null,
                false);

        return new ComposerExModel(
                c.getString(c.getColumnIndex(ComposerTable.ID)),
                c.getString(c.getColumnIndex(ComposerTable.ITEM_HOST_ID)),
                c.getString(c.getColumnIndex(ComposerTable.ITEM_CHILD_ID)),
                ContentValuesUtilBase._decimal(c.getString(c.getColumnIndex(ComposerTable.QUANTITY))),
                c.getInt(c.getColumnIndex(ComposerTable.STORE_TRACKING_ENABLED)) == 1,
                c.getInt(c.getColumnIndex(ComposerTable.FREE_OF_CHARGE_COMPOSER)) == 1,
                child,
                parent);
    }

    public class WrapParent implements Function<Cursor, Optional<ItemExModel>> {
        @Override
        public Optional<ItemExModel> apply(Cursor c) {
            if (!c.moveToFirst()) {
                return Optional.absent();
            }
            ItemExModel item = new ItemFromComposerHostViewExFunction().apply(c);
            return Optional.of(item);
        }
    }

    public class WrapChild implements Function<Cursor, Optional<ItemExModel>> {
        @Override
        public Optional<ItemExModel> apply(Cursor c) {
            if (!c.moveToFirst()) {
                return Optional.absent();
            }
            ItemExModel item = new ItemFromComposerChildViewExFunction().apply(c);
            return Optional.of(item);
        }
    }
}
