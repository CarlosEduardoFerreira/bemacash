package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.base.Function;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierTable;

import java.io.Serializable;
import java.math.BigDecimal;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;
import static com.kaching123.tcr.model.ContentValuesUtil._max;

/**
 * Created by gdubina on 06/11/13.
 */
public class ModifierModel implements IValueModel, Serializable {

    public String modifierGuid;
    public String itemGuid;
    public ModifierType type;
    public BigDecimal cost;
    public String title;
    public boolean autoApply;
    public int orderNum;

    // Item, that will be added via modifier, or Linked modifier item, that might be affected, e.g. with no-option.
    public String childItemGuid;
    // item to be added or to be removed. Should be positive
    public BigDecimal childItemQty;
    // items could be grouped
    public String modifierGroupGuid;

    public ModifierModel(String modifierGuid, String itemGuid,
                         ModifierType type, String title, BigDecimal cost,
                         String childItemGuid,
                         BigDecimal childItemQty,
                         String modifierGroupGuid,
                         boolean autoApply,
                         int orderNum) {
        this.modifierGuid = modifierGuid;
        this.itemGuid = itemGuid;
        this.type = type;
        this.cost = cost;
        this.title = title;
        this.childItemGuid = childItemGuid;
        this.childItemQty = childItemQty;
        this.modifierGroupGuid = modifierGroupGuid;
        this.autoApply = autoApply;
        this.orderNum = orderNum;
    }

    public ModifierModel() {

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
        values.put(ModifierTable.ITEM_GROUP_GUID, modifierGroupGuid);
        values.put(ModifierTable.ITEM_SUB_GUID, childItemGuid);
        values.put(ModifierTable.ITEM_SUB_QTY, _decimalQty(childItemQty));
        values.put(ModifierTable.TYPE, _enum(type));
        values.put(ModifierTable.TITLE, title);
        values.put(ModifierTable.EXTRA_COST, _decimal(cost));
        values.put(ModifierTable.AUTO_APPLY, autoApply);
        values.put(ModifierTable.ORDER_NUM, orderNum);
        return values;
    }

    public static ContentValues toClearGroupValue() {
        ContentValues values = new ContentValues();
        values.put(ModifierTable.ITEM_GROUP_GUID, (String)null);
        return values;
    }

    public static int getMaxOrderNum(Context context, ModifierType type, String itemGuid, String modifierGroupGuid){
        Query query = ProviderAction.query(ShopProvider.contentUri(ModifierTable.URI_CONTENT))
                .projection(_max(ModifierTable.ORDER_NUM));

        query.where(ModifierTable.ITEM_GUID + " = ?", itemGuid);
        query.where(ModifierTable.TYPE + " = ?", type.ordinal());
        if (modifierGroupGuid != null)
            query.where(ModifierTable.ITEM_GROUP_GUID + " = ?", modifierGroupGuid);

        Integer i = query.perform(context)
                .toFluentIterable(new Function<Cursor, Integer>() {
                    @Override
                    public Integer apply(Cursor input) {
                        return input.getInt(0);
                    }
                }).first().or(0);

        return i;
    }
}
