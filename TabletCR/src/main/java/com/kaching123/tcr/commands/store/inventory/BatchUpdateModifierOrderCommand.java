package com.kaching123.tcr.commands.store.inventory;

import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsModifiersJdbcConverter;
import com.kaching123.tcr.jdbc.converters.OrderNumJdbcConverter;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierTable;

/**
 * Created by Vladimir on 29.08.2016.
 */
public class BatchUpdateModifierOrderCommand extends BaseBatchUpdateOrderCommand<ModifierModel> {

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

    @Override
    protected OrderNumJdbcConverter createConverter() {
        return (ItemsModifiersJdbcConverter) JdbcFactory.getConverter(ModifierTable.TABLE_NAME);
    }

    @Override
    protected BatchSqlCommand createBatch() {
        return batchUpdate(ModifierModel.class);
    }

    public static void start(Context context, ModifierModel[] oldOrder, ModifierModel[] newOrder){
        create(BatchUpdateModifierOrderCommand.class).arg(ARG_OLD_ORDER, oldOrder).arg(ARG_NEW_ORDER, newOrder).queueUsing(context);
    }
}
