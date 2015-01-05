package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by vkompaniets on 04.12.13.
 */
public class EditItemCommand extends AsyncCommand {

    private static final Uri ITEM_URI = ShopProvider.getContentUri(ItemTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENT_URI = ShopProvider.getContentUri(ItemMovementTable.URI_CONTENT);

    private static final String ARG_ITEM = "arg_item";

    private ItemModel item;
    private ItemMovementModel movementModel;

    @Override
    protected TaskResult doCommand() {
        Logger.d("EditItemCommand doCommand");
        if (item == null)
            item = (ItemModel)getArgs().getSerializable(ARG_ITEM);
        movementModel = null;

        BigDecimal availableQty = null;

        Cursor c = ProviderAction.query(ITEM_URI)
                .projection(ItemTable.TMP_AVAILABLE_QTY)
                .where(ItemTable.GUID + " = ?", item.guid)
                .perform(getContext());
        if(c.moveToFirst()){
            availableQty = _decimalQty(c, 0);
        }
        c.close();
        if(item.isStockTracking && item.availableQty != null && item.availableQty.compareTo(availableQty) != 0){
            item.updateQtyFlag = UUID.randomUUID().toString();
            movementModel = new ItemMovementModel(item.guid, item.updateQtyFlag, item.availableQty, true, new Date());
        }

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newUpdate(ITEM_URI)
                .withSelection(ItemTable.GUID + " = ?", new String[]{item.guid})
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
        BatchSqlCommand batch = batchUpdate(item);
        batch.add(JdbcFactory.getConverter(item).updateSQL(item, getAppCommandContext()));
        if(movementModel != null){
            batch.add(JdbcFactory.getConverter(movementModel).insertSQL(movementModel, getAppCommandContext()));
        }
        return batch;
    }

    public static void start(Context context, ItemModel item){
        create(EditItemCommand.class).arg(ARG_ITEM, item).queueUsing(context);
    }

    /** use in import. can be standalone **/
    public boolean sync(Context context, ItemModel item, IAppCommandContext appCommandContext) {
        this.item = item;
        TaskResult result = syncStandalone(context, appCommandContext);
        return !isFailed(result);
    }
}