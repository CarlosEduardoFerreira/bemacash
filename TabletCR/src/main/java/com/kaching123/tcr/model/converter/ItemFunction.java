package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.store.ShopStore.ItemTable;

import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._codeType;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._itemRefType;
import static com.kaching123.tcr.model.ContentValuesUtil._priceType;

/**
 * Created by gdubina on 22/11/13.
 */
public class ItemFunction extends ListConverterFunction<ItemModel> {

    public static String[] PROJECTION = new String[]{
            ItemTable.GUID,
            ItemTable.CATEGORY_ID,
            ItemTable.DESCRIPTION,
            ItemTable.CODE,
            ItemTable.EAN_CODE,
            ItemTable.PRODUCT_CODE,
            ItemTable.PRICE_TYPE,
            ItemTable.SALE_PRICE,
            ItemTable.PRICE_1,
            ItemTable.PRICE_2,
            ItemTable.PRICE_3,
            ItemTable.PRICE_4,
            ItemTable.PRICE_5,
            ItemTable.TMP_AVAILABLE_QTY,
            ItemTable.UNIT_LABEL_ID,
            ItemTable.STOCK_TRACKING,
            ItemTable.ACTIVE_STATUS,
            ItemTable.DISCOUNTABLE,
            ItemTable.SALABLE,
            ItemTable.DISCOUNT,
            ItemTable.DISCOUNT_TYPE,
            ItemTable.TAXABLE,
            ItemTable.COST,
            ItemTable.MINIMUM_QTY,
            ItemTable.RECOMMENDED_QTY,
            ItemTable.UPDATE_QTY_FLAG,
            ItemTable.TAX_GROUP_GUID,
            ItemTable.TAX_GROUP_GUID2,
            ItemTable.DEFAULT_MODIFIER_GUID,
            ItemTable.ORDER_NUM,
            ItemTable.PRINTER_ALIAS_GUID,
            ItemTable.BUTTON_VIEW,
            ItemTable.HAS_NOTES,
            ItemTable.SERIALIZABLE,
            ItemTable.CODE_TYPE,
            ItemTable.ELIGIBLE_FOR_COMMISSION,
            ItemTable.COMMISSION,
            ItemTable.REFERENCE_ITEM_ID,
            ItemTable.ITEM_REF_TYPE,
            ItemTable.LOYALTY_POINTS,
            ItemTable.EXCLUDE_FROM_LOYALTY_PLAN
    };

    @Override
    public ItemModel apply(Cursor c) {
        super.apply(c);
        return new ItemModel(
                c.getString(indexHolder.get(ItemTable.GUID)),
                c.getString(indexHolder.get(ItemTable.CATEGORY_ID)),
                c.getString(indexHolder.get(ItemTable.DESCRIPTION)),
                c.getString(indexHolder.get(ItemTable.CODE)),
                c.getString(indexHolder.get(ItemTable.EAN_CODE)),
                c.getString(indexHolder.get(ItemTable.PRODUCT_CODE)),
                _priceType(c, indexHolder.get(ItemTable.PRICE_TYPE)),
                _decimal(c.getString(indexHolder.get(ItemTable.SALE_PRICE)), BigDecimal.ZERO),
                _decimal(c.getString(indexHolder.get(ItemTable.PRICE_1)), BigDecimal.ZERO),
                _decimal(c.getString(indexHolder.get(ItemTable.PRICE_2)), BigDecimal.ZERO),
                _decimal(c.getString(indexHolder.get(ItemTable.PRICE_3)), BigDecimal.ZERO),
                _decimal(c.getString(indexHolder.get(ItemTable.PRICE_4)), BigDecimal.ZERO),
                _decimal(c.getString(indexHolder.get(ItemTable.PRICE_5)), BigDecimal.ZERO),
                _decimalQty(c.getString(indexHolder.get(ItemTable.TMP_AVAILABLE_QTY))),
                c.getString(indexHolder.get(ItemTable.UNIT_LABEL_ID)),
                c.getInt(indexHolder.get(ItemTable.STOCK_TRACKING)) == 1,
                c.getInt(indexHolder.get(ItemTable.LIMIT_QTY)) == 1,
                c.getInt(indexHolder.get(ItemTable.ACTIVE_STATUS)) == 1,
                c.getInt(indexHolder.get(ItemTable.DISCOUNTABLE)) == 1,
                c.getInt(indexHolder.get(ItemTable.SALABLE)) == 1,
                _decimal(c.getString(indexHolder.get(ItemTable.DISCOUNT)), BigDecimal.ZERO),
                _discountType(c, indexHolder.get(ItemTable.DISCOUNT_TYPE)),
                c.getInt(indexHolder.get(ItemTable.TAXABLE)) == 1,
                _decimal(c.getString(indexHolder.get(ItemTable.COST)), BigDecimal.ZERO),
                _decimalQty(c.getString(indexHolder.get(ItemTable.MINIMUM_QTY))),
                _decimalQty(c.getString(indexHolder.get(ItemTable.RECOMMENDED_QTY))),
                c.getString(indexHolder.get(ItemTable.UPDATE_QTY_FLAG)),
                c.getString(indexHolder.get(ItemTable.TAX_GROUP_GUID)),
                c.getString(indexHolder.get(ItemTable.TAX_GROUP_GUID2)),
                c.getString(c.getColumnIndex(ItemTable.DEFAULT_MODIFIER_GUID)),
                c.getInt(indexHolder.get(ItemTable.ORDER_NUM)),
                c.getString(indexHolder.get(ItemTable.PRINTER_ALIAS_GUID)),
                c.getInt(indexHolder.get(ItemTable.BUTTON_VIEW)),
                c.getInt(indexHolder.get(ItemTable.HAS_NOTES)) == 1,
                c.getInt(indexHolder.get(ItemTable.SERIALIZABLE)) == 1,
                _codeType(c, indexHolder.get(ItemTable.CODE_TYPE)),
                _bool(c, indexHolder.get(ItemTable.ELIGIBLE_FOR_COMMISSION)),
                _decimal(c, indexHolder.get(ItemTable.COMMISSION), BigDecimal.ZERO),
                c.getString(indexHolder.get(ItemTable.REFERENCE_ITEM_ID)),
                _itemRefType(c, indexHolder.get(ItemTable.ITEM_REF_TYPE)),
                _decimal(c.getString(indexHolder.get(ItemTable.LOYALTY_POINTS)), BigDecimal.ZERO),
                _bool(c, indexHolder.get(ItemTable.EXCLUDE_FROM_LOYALTY_PLAN)),
                _bool(c, indexHolder.get(ItemTable.EBT_ELIGIBLE))
                );
    }

}
