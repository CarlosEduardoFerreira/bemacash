package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.kaching123.tcr.InventoryHelper;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.ItemMovementModelFactory;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class AddItemCommand extends AsyncCommand {

    private static final Uri ITEM_URI = ShopProvider.getContentUri(ItemTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENT_URI = ShopProvider.getContentUri(ItemMovementTable.URI_CONTENT);

    public static final String ARG_ITEM = "arg_item";

    private ItemModel item;
    private ItemMovementModel movementModel;

    private boolean isMaxItemsCountError;

    BatchSqlCommand batch;

    @Override
    protected TaskResult doCommand() {
        Logger.d("AddItemCommand doCommand");
        if (isMaxItemsCountError = InventoryHelper.isLimitReached(getContext())) {
            Logger.d("AddItemCommand doCommand. InventoryLimitReached");
            return failed();
        }

        item = (ItemModel) getArgs().getSerializable(ARG_ITEM);
        movementModel = null;
        item.orderNum = ItemModel.getMaxOrderNum(getContext(), item.categoryId) + 1;
        item.updateQtyFlag = UUID.randomUUID().toString();
        if (item.isStockTracking) {
            movementModel = ItemMovementModelFactory.getNewModel(
                    item.guid,
                    item.updateQtyFlag,
                    item.availableQty,
                    true,
                    new Date()
            );
        }


        batch = batchInsert(item);
        batch.add(JdbcFactory.getConverter(item).insertSQL(item, getAppCommandContext()));
        if (movementModel != null) {
            batch.add(JdbcFactory.getConverter(movementModel)
                    .insertSQL(movementModel, getAppCommandContext()));
        }

        new AtomicUpload().upload(batch, AtomicUpload.UploadType.WEB);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newInsert(ITEM_URI)
                .withValues(item.toValues())
                .build());
        if (movementModel != null) {
            operations.add(ContentProviderOperation.newInsert(ITEM_MOVEMENT_URI)
                    .withValues(movementModel.toValues())
                    .build());
        }

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return batch;
    }

    public static void start(Context context, ItemModel item, AddItemCommandCallback callback) {
        create(AddItemCommand.class).arg(ARG_ITEM, item).callback(callback).queueUsing(context);
    }

    /**
     * use in import. can be standalone
     **/
    public AddItemResult sync(Context context, ItemModel item, IAppCommandContext appCommandContext) {
        boolean isSuccess = !isFailed(syncStandalone(context, arg(item), appCommandContext));
        return new AddItemResult(isSuccess, isMaxItemsCountError);
    }

    private static Bundle arg(ItemModel item) {
        Bundle arg = new Bundle(1);
        arg.putSerializable(ARG_ITEM, item);
        return arg;
    }

    public final static class AddItemResult {
        public final boolean isSuccess;
        public final boolean isMaxItemsCountError;

        public AddItemResult(boolean isSuccess, boolean isMaxItemsCountError) {
            this.isSuccess = isSuccess;
            this.isMaxItemsCountError = isMaxItemsCountError;
        }
    }

    public static abstract class AddItemCommandCallback {

        @OnSuccess(AddItemCommand.class)
        public void onSuccess() {
            handleSuccess();
        }

        @OnFailure(AddItemCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleSuccess();

        protected abstract void handleFailure();

    }
}
