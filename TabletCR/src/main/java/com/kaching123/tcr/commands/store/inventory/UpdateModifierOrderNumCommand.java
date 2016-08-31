package com.kaching123.tcr.commands.store.inventory;

import android.net.Uri;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.IOrderNumUpdater;
import com.kaching123.tcr.jdbc.converters.ItemsModifiersJdbcConverter;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierTable;

/**
 * Created by vkompaniets on 27.08.2016.
 */
public class UpdateModifierOrderNumCommand extends BaseUpdateOrderNumCommand {

    @Override
    protected IOrderNumUpdater createConverter() {
        return (ItemsModifiersJdbcConverter) JdbcFactory.getConverter(ModifierTable.TABLE_NAME);
    }

    @Override
    protected Uri getUri() {
        return ShopProvider.contentUri(ModifierTable.URI_CONTENT);
    }

    @Override
    protected String getIdColumn() {
        return ModifierTable.MODIFIER_GUID;
    }

    @Override
    protected String getOrderNumColumn() {
        return ModifierTable.ORDER_NUM;
    }
}
