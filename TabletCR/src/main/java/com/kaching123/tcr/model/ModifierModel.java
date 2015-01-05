package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.ModifierTable;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by gdubina on 06/11/13.
 */
public class ModifierModel implements IValueModel, Serializable {

    public String modifierGuid;
    public String itemGuid;
    public ModifierType type;
    public BigDecimal cost;
    public String title;

    public ModifierModel(String modifierGuid, String itemGuid, ModifierType type, String title, BigDecimal cost) {
        this.modifierGuid = modifierGuid;
        this.itemGuid = itemGuid;
        this.type = type;
        this.cost = cost;
        this.title = title;
    }

    @Override
    public String getGuid() {
        return modifierGuid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ModifierTable.MODIFIER_GUID, modifierGuid);
        values.put(ModifierTable.ITEM_GUID, itemGuid);
        values.put(ModifierTable.TYPE, _enum(type));
        values.put(ModifierTable.TITLE, title);
        values.put(ModifierTable.EXTRA_COST, _decimal(cost));
        return values;
    }

}
