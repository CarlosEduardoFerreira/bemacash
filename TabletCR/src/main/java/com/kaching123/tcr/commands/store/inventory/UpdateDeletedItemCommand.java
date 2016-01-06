package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.model.CategoryModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.ItemMovementModelFactory;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.MovementUtils;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

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
        if (isInventoryLimitReached()) {
            Logger.d("UpdateDeletedItemCommand doCommand. InventoryLimitReached");
            return failed();
        }

        if(TextUtils.isEmpty(guid)) {
            guid = getArgs().getString(ARG_ITEM_GUID); // command was called with start()
        }
        return succeeded();
    }

    private boolean isInventoryLimitReached() {
        long itemsCount = 0;
        Cursor c = ProviderAction.query(URI_ITEM)
                .projection("count(" + ShopStore.ItemTable.GUID + ")")
                .perform(getContext());
        if (c.moveToFirst())
            itemsCount = c.getLong(0);
        c.close();
        Logger.d("UpdateDeletedItemCommand InventoryLimitReached. itemsCount = " + itemsCount
                + ", inventoryLimit() = " + PlanOptions.getInventoryLimit()+ ", PlanOptions.isInventoryLimited() = " + PlanOptions.isInventoryLimited());
        return   PlanOptions.isInventoryLimited() && itemsCount >= PlanOptions.getInventoryLimit();
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

    /** use in import. can be standalone **/
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

    public static void start(Context context, String guid, BaseUpdateDeletedItemCallback callback){
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
