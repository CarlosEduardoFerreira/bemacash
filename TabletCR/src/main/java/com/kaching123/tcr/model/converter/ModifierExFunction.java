package com.kaching123.tcr.model.converter;

import android.database.Cursor;
import android.text.TextUtils;

import com.google.common.base.Function;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.payment.ModifierGroupCondition;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.ModifierView2.ItemGroupTable;
import com.kaching123.tcr.store.ShopSchema2.ModifierView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.ModifierView2.ModifierTable;

import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._modifierType;
import static com.kaching123.tcr.model.ContentValuesUtil._priceType;

/**
 * Created by mayer
 */
public class ModifierExFunction implements Function<Cursor, ModifierExModel> {

    @Override
    public ModifierExModel apply(Cursor c) {
        String shortCut;
        try {
            shortCut = c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.UnitLabelTable.SHORTCUT));
        } catch (IllegalArgumentException noItem) {
            shortCut = null;
        }
        String itemGuid = c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ModifierTable.ITEM_SUB_GUID));
        String itemGroupGuid = c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ModifierTable.ITEM_GROUP_GUID));

        ItemExModel child;
        ModifierGroupModel group;
        if (TextUtils.isEmpty(itemGuid)) {
            child = null;
        } else {
            String id = c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.GUID));
            child = new ItemExModel(
                    id,
                    null,
                    c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.DESCRIPTION)),
                    c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.CODE)),
                    c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.EAN_CODE)),
                    c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.PRODUCT_CODE)),
                    _priceType(c, c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.PRICE_TYPE)),
                    _decimal(c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.SALE_PRICE)), BigDecimal.ZERO),
                    _decimal(c.getString(c.getColumnIndex(ItemTable.PRICE_1)), BigDecimal.ZERO),
                    _decimal(c.getString(c.getColumnIndex(ItemTable.PRICE_2)), BigDecimal.ZERO),
                    _decimal(c.getString(c.getColumnIndex(ItemTable.PRICE_3)), BigDecimal.ZERO),
                    _decimal(c.getString(c.getColumnIndex(ItemTable.PRICE_4)), BigDecimal.ZERO),
                    _decimal(c.getString(c.getColumnIndex(ItemTable.PRICE_5)), BigDecimal.ZERO),
                    _decimalQty(c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.TMP_AVAILABLE_QTY))),
                    c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.UNIT_LABEL_ID)),
                    shortCut,
                    c.getInt(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.STOCK_TRACKING)) == 1,
                    c.getInt(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.LIMIT_QTY)) == 1,
                    c.getInt(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.ACTIVE_STATUS)) == 1,
                    false,
                    c.getInt(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.SALABLE)) == 1,
                    null,
                    null,
                    false,
                    _decimal(c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.COST)), BigDecimal.ZERO),
                    _decimalQty(c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.MINIMUM_QTY))),
                    _decimalQty(c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.RECOMMENDED_QTY))),
                    c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemTable.UPDATE_QTY_FLAG)),
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
                    null,
                    false,
                    false
            );
        }
        String id = c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ModifierTable.MODIFIER_GUID));
        if (TextUtils.isEmpty(itemGroupGuid)) {
            group = null;
        } else {
            group = new ModifierGroupModel(
                    c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemGroupTable.GUID)),
                    c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemGroupTable.ITEM_GUID)),
                    c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ItemGroupTable.TITLE)),
                    c.getInt(c.getColumnIndex(ItemGroupTable.ORDER_NUM)),
                    ModifierGroupCondition.valueOf(c.getInt(c.getColumnIndex(ItemGroupTable.CONDITION))),
                    c.getInt(c.getColumnIndex(ItemGroupTable.CONDITION_VALUE)),
                    null);
        }

        return new ModifierExModel(
                id,
                c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ModifierTable.ITEM_GUID)),
                _modifierType(c, c.getColumnIndex(ShopSchema2.ModifierView2.ModifierTable.TYPE)),
                TextUtils.isEmpty(itemGuid) ? c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ModifierTable.TITLE)) : null,
                _decimal(c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ModifierTable.EXTRA_COST)), BigDecimal.ZERO),
                itemGuid,
                _decimalQty(c.getString(c.getColumnIndex(ShopSchema2.ModifierView2.ModifierTable.ITEM_SUB_QTY))),
                itemGroupGuid,
                group,
                child,
                c.getInt(c.getColumnIndex(ShopSchema2.ModifierView2.ModifierTable.AUTO_APPLY)) == 1,
                c.getInt(c.getColumnIndex(ModifierTable.ORDER_NUM)));
    }
}
