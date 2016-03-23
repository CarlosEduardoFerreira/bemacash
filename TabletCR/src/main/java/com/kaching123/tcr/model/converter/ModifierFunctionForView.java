package com.kaching123.tcr.model.converter;

import android.database.Cursor;

import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.store.ShopSchema2.ModifierView2;
import com.kaching123.tcr.store.ShopStore.ModifierTable;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._modifierType;

/**
 * Created by vkompaniets on 20.11.13.
 */
public class ModifierFunctionForView extends ListConverterFunction<ModifierModel>{

    @Override
    public ModifierModel apply(Cursor c) {
        super.apply(c);
        return new ModifierModel(
                c.getString(indexHolder.get(ModifierView2.ModifierTable.MODIFIER_GUID)),
                c.getString(indexHolder.get(ModifierView2.ModifierTable.ITEM_GUID)),
                _modifierType(c, indexHolder.get(ModifierView2.ModifierTable.TYPE)),
                c.getString(indexHolder.get(ModifierView2.ModifierTable.TITLE)),
                _decimal(c.getString(indexHolder.get(ModifierView2.ModifierTable.EXTRA_COST))),
                c.getString(indexHolder.get(ModifierView2.ModifierTable.ITEM_SUB_GUID)),
                _decimalQty(c.getString(indexHolder.get(ModifierView2.ModifierTable.ITEM_SUB_QTY))),
                c.getString(indexHolder.get(ModifierView2.ModifierTable.ITEM_GROUP_GUID)),
                c.getInt(indexHolder.get(ModifierTable.AUTO_APPLY))==1
        );
    }
}