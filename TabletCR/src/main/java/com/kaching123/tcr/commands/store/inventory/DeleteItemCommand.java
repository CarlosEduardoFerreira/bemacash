package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.util.InventoryUtils;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

import static com.kaching123.tcr.store.ShopStore.DELETE_VALUES;


/**
 * Created by gdubina on 04.12.13.
 */
public class DeleteItemCommand extends AsyncCommand {

    private static final Uri ITEM_URI = ShopProvider.contentUri(ItemTable.URI_CONTENT);
    private static final String ARG_ITEM_GUID = "ARG_ITEM_GUID";

    private String itemGuid;
    ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        Logger.d("DeleteItemCommand doCommand");
        itemGuid = getStringArg(ARG_ITEM_GUID);

        operations = new ArrayList<>(1);
        operations.add(ContentProviderOperation.newUpdate(ITEM_URI)
                .withValues(DELETE_VALUES)
                .withSelection(ItemTable.GUID + " = ?", new String[]{itemGuid}).build());
        JdbcConverter<ItemModel> jdbc = JdbcFactory.getConverter(ShopStore.ItemTable.TABLE_NAME);
        ItemModel m = new ItemModel(itemGuid);
        sql = batchDelete(m);
        sql.add(jdbc.deleteSQL(m, this.getAppCommandContext()));

        if (!InventoryUtils.pollItem(itemGuid, getContext(), getAppCommandContext(), operations, sql)) {
            return failed();
        } else {
            return succeeded();
        }
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sql;
    }

    public static void start(Context context, String itemGuid) {
        create(DeleteItemCommand.class).arg(ARG_ITEM_GUID, itemGuid).queueUsing(context);
    }
}
