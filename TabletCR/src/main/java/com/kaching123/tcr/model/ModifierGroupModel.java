package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierGroupTable;

import java.io.Serializable;

import static com.kaching123.tcr.model.ContentValuesUtil._max;

/**
 * Created by alboyko 02.12.2015
 */
public class ModifierGroupModel implements IValueModel, IOrderedModel, Serializable {

    public String guid;
    public String itemGuid;
    public String title;
    public int orderNum;

    public ModifierGroupModel(String guid, String itemGuid, String title, int orderNum) {
        this.guid = guid;
        this.itemGuid = itemGuid;
        this.title = title;
        this.orderNum = orderNum;
    }

    public ModifierGroupModel() {
    }

    public ModifierGroupModel(Cursor c){
        this(
            c.getString(c.getColumnIndex(ModifierGroupTable.GUID)),
            c.getString(c.getColumnIndex(ModifierGroupTable.ITEM_GUID)),
            c.getString(c.getColumnIndex(ModifierGroupTable.TITLE)),
            c.getInt(c.getColumnIndex(ModifierGroupTable.ORDER_NUM))
        );
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ModifierGroupTable.GUID, guid);
        values.put(ModifierGroupTable.ITEM_GUID, itemGuid);
        values.put(ModifierGroupTable.TITLE, title);
        values.put(ModifierGroupTable.ORDER_NUM, orderNum);

        return values;
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
                .toFluentIterable(new Function<Cursor, Integer>() {
                    @Override
                    public Integer apply(Cursor input) {
                        return input.getInt(0);
                    }
                }).first().or(0);
    }
}