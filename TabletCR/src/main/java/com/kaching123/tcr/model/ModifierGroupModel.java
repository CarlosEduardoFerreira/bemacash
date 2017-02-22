package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.converter.IntegerFunction;
import com.kaching123.tcr.model.payment.ModifierGroupCondition;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ModifierGroupTable;

import java.io.Serializable;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._max;

/**
 * Created by alboyko 02.12.2015
 */
public class ModifierGroupModel implements IValueModel, IOrderedModel, Serializable {

    public String guid;
    public String itemGuid;
    public String title;
    public int orderNum;
    public ModifierGroupCondition condition;
    public int conditionValue;

    private List<String> mIgnoreFields;

    public ModifierGroupModel(String guid, String itemGuid, String title, int orderNum,
                              ModifierGroupCondition condition, int conditionValue, List<String> ignoreFields) {
        this.guid = guid;
        this.itemGuid = itemGuid;
        this.title = title;
        this.orderNum = orderNum;
        this.condition = condition;
        this.conditionValue = conditionValue;

        this.mIgnoreFields = ignoreFields;
    }

    public ModifierGroupModel() {
    }

    public ModifierGroupModel(Cursor c){
        this(
            c.getString(c.getColumnIndex(ModifierGroupTable.GUID)),
            c.getString(c.getColumnIndex(ModifierGroupTable.ITEM_GUID)),
            c.getString(c.getColumnIndex(ModifierGroupTable.TITLE)),
            c.getInt(c.getColumnIndex(ModifierGroupTable.ORDER_NUM)),
            ModifierGroupCondition.valueOf(c.getInt(c.getColumnIndex(ModifierGroupTable.CONDITION))),
                c.getInt(c.getColumnIndex(ModifierGroupTable.CONDITION_VALUE)),
                null);
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierGroupTable.GUID)) values.put(ModifierGroupTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierGroupTable.ITEM_GUID)) values.put(ModifierGroupTable.ITEM_GUID, itemGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierGroupTable.TITLE)) values.put(ModifierGroupTable.TITLE, title);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierGroupTable.ORDER_NUM)) values.put(ModifierGroupTable.ORDER_NUM, orderNum);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierGroupTable.CONDITION)) values.put(ModifierGroupTable.CONDITION, condition.ordinal());
        if (mIgnoreFields == null || !mIgnoreFields.contains(ModifierGroupTable.CONDITION_VALUE)) values.put(ModifierGroupTable.CONDITION_VALUE, conditionValue);

        return values;
    }

    @Override
    public String getIdColumn() {
        return ModifierGroupTable.GUID;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ModifierGroupModel) {
            return guid.equals(((ModifierGroupModel)o).guid);
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        if (guid == null)
            return 0;

        return guid.hashCode();
    }

    @Override
    public int getOrderNum() {
        return orderNum;
    }

    public static int getMaxOrderNum(Context context, String itemId){
        return ProviderAction.query(ShopProvider.contentUri(ModifierGroupTable.URI_CONTENT))
                .projection(_max(ModifierGroupTable.ORDER_NUM))
                .where(ModifierGroupTable.ITEM_GUID + " = ?", itemId)
                .perform(context)
                .toFluentIterable(new IntegerFunction())
                .first().or(0);
    }
}