package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.ItemMovementModelFactory;
import com.kaching123.tcr.service.BatchSqlCommand;
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
 * Created by gdubina on 20.01.14.
 */
public class UpdateItemQtyCommand extends AsyncCommand {

    private static final Uri ITEM_URI = ShopProvider.contentUri(ItemTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENT_URI = ShopProvider.contentUri(ItemMovementTable.URI_CONTENT);

    protected String guid;
    protected BigDecimal qty;

    private String updateFlag;
    private ItemMovementModel movementModel;
    private int updatedRowsCount;

    @Override
    protected TaskResult doCommand() {
        updateFlag = UUID.randomUUID().toString();
        movementModel = ItemMovementModelFactory.getNewModel(
                guid,
                updateFlag,
                qty,
                false,
                new Date()
        );

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newUpdate(ITEM_URI)
                .withValue(ItemTable.UPDATE_QTY_FLAG, updateFlag)
                .withSelection(ItemTable.GUID + " = ?", new String[]{guid})
                .build());
        operations.add(ContentProviderOperation.newInsert(ITEM_MOVEMENT_URI)
                .withValues(movementModel.toValues())
                .build());
        return operations;
    }

    @Override
    protected BatchSqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchUpdate(ItemModel.class);
        batch.add(((ItemsJdbcConverter) JdbcFactory.getConverter(ItemTable.TABLE_NAME)).updateQtyFlagSQL(guid, updateFlag, getAppCommandContext()));
        batch.add(JdbcFactory.getConverter(movementModel).insertSQL(movementModel, getAppCommandContext()));
        return batch;
    }

    @Override
    protected void afterCommand(ContentProviderResult[] dbOperationResults) {
        updatedRowsCount = dbOperationResults == null ? 0 : dbOperationResults[0].count;
    }

    /** use in import. can be standalone **/
    public int sync(Context context, String guid, BigDecimal qty, IAppCommandContext appCommandContext) {
        this.guid = guid;
        this.qty = qty;
        this.updatedRowsCount = 0;
        TaskResult result = super.syncStandalone(context, appCommandContext);
        return isFailed(result) ? 0 : updatedRowsCount;
    }
}
