package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;
import java.util.UUID;

public class AddReferenceItemCommand extends AsyncCommand {

    private static final Uri ITEM_URI = ShopProvider.contentUri(ItemTable.URI_CONTENT);

    public static final String ARG_ITEM = "arg_item";

    private ItemModel item;

    @Override
    protected TaskResult doCommand() {
        Logger.d("AddReferenceItemCommand doCommand");
        item = (ItemModel) getArgs().getSerializable(ARG_ITEM);

        item.orderNum = 0;
        item.updateQtyFlag = UUID.randomUUID().toString();
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newInsert(ITEM_URI)
                .withValues(item.toValues())
                .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchInsert(item);
        batch.add(JdbcFactory.getConverter(item).insertSQL(item, getAppCommandContext()));
        return batch;
    }

    public static void start(Context context, ItemModel item, AddReferenceItemCommandCallback callback) {
        create(AddReferenceItemCommand.class).arg(ARG_ITEM, item).callback(callback).queueUsing(context);
    }

    /**
     * use in import. can be standalone
     **/
    public boolean sync(Context context, ItemModel item, PublicGroundyTask.IAppCommandContext appCommandContext) {
        TaskResult result = syncStandalone(context, arg(item), appCommandContext);
        return !isFailed(result);
    }

    private static Bundle arg(ItemModel item) {
        Bundle arg = new Bundle(1);
        arg.putSerializable(ARG_ITEM, item);
        return arg;
    }


    public static abstract class AddReferenceItemCommandCallback {

        @OnSuccess(AddReferenceItemCommand.class)
        public void onSuccess() {
            handleSuccess();
        }

        @OnFailure(AddReferenceItemCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleSuccess();

        protected abstract void handleFailure();

    }

}
