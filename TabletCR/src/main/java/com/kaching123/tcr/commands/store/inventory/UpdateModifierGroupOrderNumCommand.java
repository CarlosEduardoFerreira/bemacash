package com.kaching123.tcr.commands.store.inventory;

import android.net.Uri;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.IOrderNumUpdater;
import com.kaching123.tcr.jdbc.converters.ItemsModifierGroupsJdbcConverter;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierGroupTable;

/**
 * Created by vkompaniets on 30.08.2016.
 */
public class UpdateModifierGroupOrderNumCommand extends BaseUpdateOrderNumCommand {

    @Override
    protected IOrderNumUpdater createConverter() {
        return (ItemsModifierGroupsJdbcConverter) JdbcFactory.getConverter(ModifierGroupTable.TABLE_NAME);
    }

    @Override
    protected Uri getUri() {
        return ShopProvider.contentUri(ModifierGroupTable.URI_CONTENT);
    }

    @Override
    protected String getIdColumn() {
        return ModifierGroupTable.GUID;
    }

    @Override
    protected String getOrderNumColumn() {
        return ModifierGroupTable.ORDER_NUM;
    }
}
