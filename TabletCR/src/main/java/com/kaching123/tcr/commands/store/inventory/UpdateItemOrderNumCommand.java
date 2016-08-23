package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 20.08.2016.
 */
public class UpdateItemOrderNumCommand extends AsyncCommand {

    private String itemId;
    private int orderNum;

    @Override
    protected TaskResult doCommand() {
        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        ItemsJdbcConverter jdbcConverter = (ItemsJdbcConverter) JdbcFactory.getConverter(ItemTable.TABLE_NAME);
        return jdbcConverter.updateOrderSQL(itemId, orderNum, getAppCommandContext());
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>(1);
        ops.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(ItemTable.URI_CONTENT))
                .withSelection(ItemTable.GUID + " = ?", new String[]{itemId})
                .withValue(ItemTable.ORDER_NUM, orderNum)
                .build()
        );
        return ops;
    }

    public SyncResult sync(Context context, String itemId, int orderNum, IAppCommandContext appCommandContext){
        this.itemId = itemId;
        this.orderNum = orderNum;
        return syncDependent(context, appCommandContext);
    }
}
