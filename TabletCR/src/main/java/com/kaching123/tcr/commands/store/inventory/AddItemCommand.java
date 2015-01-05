package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.TaskResult;

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

	@Override
	protected TaskResult doCommand() {
		Logger.d("AddItemCommand doCommand");
        if (isMaxItemsCountError = !checkMaxItemsCount())
            return failed();

		item = (ItemModel)getArgs().getSerializable(ARG_ITEM);
        movementModel = null;

        item.orderNum = 0;
        item.updateQtyFlag = UUID.randomUUID().toString();

        if(item.isStockTracking){
            movementModel = new ItemMovementModel(item.guid, item.updateQtyFlag, item.availableQty, true, new Date());
        }

		return succeeded();
	}

    private boolean checkMaxItemsCount() {
        long itemsCount = 0;
        Cursor c = ProviderAction.query(ITEM_URI)
                .projection("count(" + ItemTable.GUID + ")")
                .perform(getContext());
        if (c.moveToFirst())
            itemsCount = c.getLong(0);
        c.close();
        return itemsCount < getAppCommandContext().getMaxItemsCount();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newInsert(ITEM_URI)
                .withValues(item.toValues())
                .build());
        if(movementModel != null){
            operations.add(ContentProviderOperation.newInsert(ITEM_MOVEMENT_URI)
                    .withValues(movementModel.toValues())
                    .build());
        }

        return operations;
    }

    @Override
	protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchInsert(item);
        batch.add(JdbcFactory.getConverter(item).insertSQL(item, getAppCommandContext()));
        if(movementModel != null){
            batch.add(JdbcFactory.getConverter(movementModel).insertSQL(movementModel, getAppCommandContext()));
        }
        return batch;
	}

	public static void start(Context context, ItemModel item){
		create(AddItemCommand.class).arg(ARG_ITEM, item).queueUsing(context);
	}

    /** use in import. can be standalone **/
    public AddItemResult sync(Context context, ItemModel item, IAppCommandContext appCommandContext) {
        boolean isSuccess = !isFailed(syncStandalone(context, arg(item), appCommandContext));
        return new AddItemResult(isSuccess, isMaxItemsCountError);
    }

    private static Bundle arg(ItemModel item){
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

}
