package com.kaching123.tcr.commands.store.inventory;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.IOrderNumUpdater;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;

/**
 * Created by vkompaniets on 20.08.2016.
 */
public class UpdateItemOrderNumCommand extends BaseUpdateOrderNumCommand {

    @Override
    protected IOrderNumUpdater createConverter() {
        return (ItemsJdbcConverter) JdbcFactory.getConverter(ItemTable.TABLE_NAME);
    }

    @Override
    protected Uri getUri() {
        return ShopProvider.contentUri(ItemTable.URI_CONTENT);
    }

    @Override
    protected String getIdColumn() {
        return ItemTable.GUID;
    }

    @Override
    protected String getOrderNumColumn() {
        return ItemTable.ORDER_NUM;
    }

    public SyncResult sync(Context context, String itemId, int orderNum, IAppCommandContext appCommandContext){
        Bundle args = new Bundle(2);
        args.putString(ARG_ID, itemId);
        args.putInt(ARG_ORDER_NUM, orderNum);
        return syncDependent(context, args, appCommandContext);
    }
}
