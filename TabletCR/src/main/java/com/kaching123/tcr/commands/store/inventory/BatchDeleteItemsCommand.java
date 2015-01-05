package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by gdubina on 25.04.14.
 */
public class BatchDeleteItemsCommand extends AsyncCommand {

    private static final Uri ITEM_URI = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    private ArrayList<String> items;

    @Override
    protected TaskResult doCommand() {
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        //TODO: remove related entities?
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        Logger.d("BatchDeleteItemsCommand.createDbOperations %d", items.size());
        for(String itemGuid : items){
            ops.add(ContentProviderOperation
                    .newUpdate(ITEM_URI)
                    .withValues(ShopStore.DELETE_VALUES)
                    .withSelection(ItemTable.GUID + " = ?", new String[]{itemGuid})
                    .build());
        }
        return ops;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        Logger.d("BatchDeleteItemsCommand.createSqlCommand %d", items.size());
        BatchSqlCommand batch = batchDelete(ItemTable.TABLE_NAME);
        JdbcConverter cvr = JdbcFactory.getConverter(ItemTable.TABLE_NAME);
        for(String itemGuid : items){
            batch.add(cvr.deleteSQL(null, itemGuid, getAppCommandContext()));
        }
        return batch;
    }

    /** use in import. can be standalone **/
    public boolean sync(Context context, ArrayList<String> items, IAppCommandContext appCommandContext){
        this.items = items;
        TaskResult result = syncStandalone(context, appCommandContext);
        return !isFailed(result);
    }

}
