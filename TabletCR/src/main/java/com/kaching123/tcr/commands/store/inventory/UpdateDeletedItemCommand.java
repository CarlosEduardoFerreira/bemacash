package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.kaching123.tcr.InventoryHelper;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;

/**
 * Created by alboyko on 05.01.2016.
 */
public class UpdateDeletedItemCommand extends AsyncCommand {
    private static final Uri URI_ITEM = ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT);
    private static final String ARG_ITEM_GUID = "ARG_ITEM_GUID";
    private String guid;
    private int updatedRowsCount;

    @Override
    protected TaskResult doCommand() {
        Logger.d("UpdateDeletedItemCommand doCommand");
        if (InventoryHelper.isLimitReached(getContext())) {
            Logger.d("UpdateDeletedItemCommand doCommand. InventoryLimitReached");
            return failed();
        }

        if (TextUtils.isEmpty(guid)) {
            guid = getArgs().getString(ARG_ITEM_GUID); // command was called with start()
        }
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>(1);

        ContentValues v = new ContentValues(1);
        v.put(ShopStore.ItemTable.IS_DELETED, false);

        operations.add(ContentProviderOperation.newUpdate(URI_ITEM)
                .withValues(v)
                .withSelection(ShopStore.ItemTable.GUID + " = ?", new String[]{guid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return ((ItemsJdbcConverter) JdbcFactory.getConverter(ShopStore.ItemTable.TABLE_NAME)).updateIsDeletedSQL(guid, false, getAppCommandContext());
    }

    @Override
    protected void afterCommand(ContentProviderResult[] dbOperationResults) {
        updatedRowsCount = dbOperationResults == null ? 0 : dbOperationResults[0].count;
    }

    /**
     * use in import. can be standalone
     **/
    public int sync(Context context, String guid, IAppCommandContext appCommandContext) {
        this.guid = guid;
        this.updatedRowsCount = 0;
        TaskResult result = super.syncStandalone(context, appCommandContext);
        return isFailed(result) ? 0 : updatedRowsCount;
    }

    public static void start(Context context, String guid) {
        create(UpdateDeletedItemCommand.class)
                .arg(ARG_ITEM_GUID, guid)
                .queueUsing(context);
    }

    public static void start(Context context, String guid, BaseUpdateDeletedItemCallback callback) {
        create(UpdateDeletedItemCommand.class)
                .arg(ARG_ITEM_GUID, guid)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseUpdateDeletedItemCallback {

        @OnSuccess(UpdateDeletedItemCommand.class)
        public void onSuccess() {
            onItemChangedSuccess();
        }

        protected abstract void onItemChangedSuccess();


    }


}
