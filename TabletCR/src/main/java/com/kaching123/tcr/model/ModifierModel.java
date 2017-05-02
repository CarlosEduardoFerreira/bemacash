package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.converter.IntegerFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ModifierTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;
import static com.kaching123.tcr.model.ContentValuesUtil._max;
import static com.kaching123.tcr.model.ContentValuesUtil._modifierType;

/**
 * Created by gdubina on 06/11/13.
 */
public class ModifierModel implements IValueModel, IOrderedModel, Serializable {

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

    private List<String> mIgnoreFields;

    public ModifierModel(String modifierGuid, String itemGuid,
                         ModifierType type, String title, BigDecimal cost,
                         String childItemGuid,
                         BigDecimal childItemQty,
                         String modifierGroupGuid,
                         boolean autoApply,
                         int orderNum, List<String> ignoreFields) {
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

        this.mIgnoreFields = ignoreFields;
    }

    public ModifierModel(Cursor c) {
        this(
                c.getString(c.getColumnIndex(ModifierTable.MODIFIER_GUID)),
                c.getString(c.getColumnIndex(ModifierTable.ITEM_GUID)),
                _modifierType(c, c.getColumnIndex(ModifierTable.TYPE)),
                c.getString(c.getColumnIndex(ModifierTable.TITLE)),
                new BigDecimal(c.getDouble(c.getColumnIndex(ModifierTable.EXTRA_COST))),
                c.getString(c.getColumnIndex(ModifierTable.ITEM_SUB_GUID)),
                new BigDecimal(c.getDouble(c.getColumnIndex(ModifierTable.ITEM_SUB_QTY))),
                c.getString(c.getColumnIndex(ModifierTable.ITEM_GROUP_GUID)),
                false,
                c.getInt(c.getColumnIndex(ShopStore.ItemTable.ORDER_NUM)),
                null
        );
    }

    public ModifierModel() {

    }

    public ModifierModel(String guid){
        this.modifierGuid = guid;
    }

    @Override
    public String getGuid() {
        return modifierGuid;
    }

    @Override
    public int getOrderNum() {
        return orderNum;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierTable.MODIFIER_GUID)) values.put(ModifierTable.MODIFIER_GUID, modifierGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierTable.ITEM_GUID)) values.put(ModifierTable.ITEM_GUID, itemGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierTable.ITEM_GROUP_GUID)) values.put(ModifierTable.ITEM_GROUP_GUID, modifierGroupGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierTable.ITEM_SUB_GUID)) values.put(ModifierTable.ITEM_SUB_GUID, childItemGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierTable.ITEM_SUB_QTY)) values.put(ModifierTable.ITEM_SUB_QTY, _decimalQty(childItemQty));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierTable.TYPE)) values.put(ModifierTable.TYPE, _enum(type));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierTable.TITLE)) values.put(ModifierTable.TITLE, title);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierTable.EXTRA_COST)) values.put(ModifierTable.EXTRA_COST, _decimal(cost));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierTable.AUTO_APPLY)) values.put(ModifierTable.AUTO_APPLY, autoApply);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierTable.ORDER_NUM)) values.put(ModifierTable.ORDER_NUM, orderNum);
        return values;
    }

    @Override
    public String getIdColumn() {
        return ModifierTable.MODIFIER_GUID;
    }

    public static int getMaxOrderNum(Context context, ModifierType type, String itemGuid, String modifierGroupGuid){
        Query query = ProviderAction.query(ShopProvider.contentUri(ModifierTable.URI_CONTENT))
                .projection(_max(ModifierTable.ORDER_NUM));

        query.where(ModifierTable.ITEM_GUID + " = ?", itemGuid);
        query.where(ModifierTable.TYPE + " = ?", type.ordinal());
        if (modifierGroupGuid != null)
            query.where(ModifierTable.ITEM_GROUP_GUID + " = ?", modifierGroupGuid);

        Integer i = query.perform(context)
                .toFluentIterable(new IntegerFunction())
                .first().or(0);

        return i;
    }
}
