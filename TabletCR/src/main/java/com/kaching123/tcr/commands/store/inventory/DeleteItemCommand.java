package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by gdubina on 04.12.13.
 */
public class DeleteItemCommand extends AsyncCommand {

    private static final Uri ITEM_URI = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    private static final String ARG_ITEM_GUID = "ARG_ITEM_GUID";

    private String itemGuid;

    @Override
    protected TaskResult doCommand() {
        Logger.d("EditItemCommand doCommand");
        itemGuid = getStringArg(ARG_ITEM_GUID);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(ITEM_URI)
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(ItemTable.GUID + " = ?", new String[]{itemGuid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        ItemModel model = new ItemModel(itemGuid);
        return JdbcFactory.delete(model, getAppCommandContext());
    }

    public static void start(Context context, String itemGuid){
        create(DeleteItemCommand.class).arg(ARG_ITEM_GUID, itemGuid).queueUsing(context);
    }
}
