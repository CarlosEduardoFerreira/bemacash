package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by gdubina on 20.01.14.
 */
public class UpdateItemQtyCommand extends AsyncCommand {

    private static final Uri ITEM_URI = ShopProvider.getContentUri(ItemTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENT_URI = ShopProvider.getContentUri(ItemMovementTable.URI_CONTENT);

    protected String guid;
    protected BigDecimal qty;

    private ItemMovementModel movementModel;
    private boolean setItemStockTracking;

    private int updatedRowsCount;

    @Override
    protected TaskResult doCommand() {
        boolean isStockTracking;
        BigDecimal availableQty;

        Cursor c = ProviderAction.query(ITEM_URI)
                .projection(ItemTable.STOCK_TRACKING, ItemTable.TMP_AVAILABLE_QTY)
                .where(ItemTable.GUID + " = ?", guid)
                .perform(getContext());

        if (!c.moveToFirst()){
            c.close();
            return failed();
        }

        isStockTracking = _bool(c, 0);
        availableQty = _decimalQty(c, 1);

        c.close();

        setItemStockTracking = !isStockTracking;

        if (availableQty.compareTo(qty) != 0) {
            String updateQtyFlag = UUID.randomUUID().toString();
            movementModel = new ItemMovementModel(guid, updateQtyFlag, qty, true, new Date());
        }

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        if (!setItemStockTracking && movementModel == null)
            return null;

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        if (setItemStockTracking) {
            operations.add(ContentProviderOperation.newUpdate(ITEM_URI)
                    .withValue(ItemTable.STOCK_TRACKING, true)
                    .withSelection(ItemTable.GUID + " = ?", new String[]{guid})
                    .build());
        }

        if (movementModel != null) {
            operations.add(ContentProviderOperation.newUpdate(ITEM_URI)
                    .withValue(ItemTable.UPDATE_QTY_FLAG, movementModel.itemUpdateFlag)
                    .withSelection(ItemTable.GUID + " = ?", new String[]{guid})
                    .build());

            operations.add(ContentProviderOperation.newInsert(ITEM_MOVEMENT_URI)
                    .withValues(movementModel.toValues())
                    .build());
        }

        return operations;
    }

    @Override
    protected BatchSqlCommand createSqlCommand() {
        if (!setItemStockTracking && movementModel == null)
            return null;

        BatchSqlCommand batch = batchUpdate(ItemModel.class);

        if (setItemStockTracking) {
            ((ItemsJdbcConverter) JdbcFactory.getConverter(ItemTable.TABLE_NAME)).updateStockTrackingSQL(guid, true, getAppCommandContext());
        }

        if (movementModel != null) {
            batch.add(((ItemsJdbcConverter) JdbcFactory.getConverter(ItemTable.TABLE_NAME)).updateQtyFlagSQL(guid, movementModel.itemUpdateFlag, getAppCommandContext()));
            batch.add(JdbcFactory.getConverter(movementModel).insertSQL(movementModel, getAppCommandContext()));
        }

        return batch;
    }

    @Override
    protected void afterCommand(ContentProviderResult[] dbOperationResults) {
        updatedRowsCount = dbOperationResults == null ? 0 : dbOperationResults[0].count;
    }

    /** use in import. can be standalone **/
    public Integer sync(Context context, String guid, BigDecimal qty, IAppCommandContext appCommandContext) {
        this.guid = guid;
        this.qty = qty;
        this.updatedRowsCount = 0;
        TaskResult result = super.syncStandalone(context, appCommandContext);
        return isFailed(result) ? null : updatedRowsCount;
    }
}
