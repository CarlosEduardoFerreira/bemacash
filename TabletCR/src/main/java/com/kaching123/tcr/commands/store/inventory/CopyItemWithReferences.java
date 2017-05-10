package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.ItemMovementModelFactory;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by mboychenko on 4/28/2017.
 */

public class CopyItemWithReferences extends AsyncCommand {
    private static final Uri ITEM_URI = ShopProvider.getContentUri(ShopStore.ItemTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENT_URI = ShopProvider.getContentUri(ShopStore.ItemMovementTable.URI_CONTENT);

    public static final String ARG_ITEM = "arg_item";
    public static final String ARG_SOURCE_ITEM_GUID = "arg_source_item_guid";

    private ItemModel item;
    private ItemMovementModel movementModel;
    private String sourceItemGuid;
    private SyncResult matrixResult;

    BatchSqlCommand batch;

    @Override
    protected TaskResult doCommand() {
        Logger.d("CopyItemWithReferences doCommand");

        item = (ItemModel) getArgs().getSerializable(ARG_ITEM);
        sourceItemGuid = getStringArg(ARG_SOURCE_ITEM_GUID);
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

        String referenceItemGuid = null;
        ItemExModel parentItem = null;
        ItemMatrixModel parentItemMatrix = null;
        Cursor itemMatrixCursor = ProviderAction.query(ShopProvider.contentUri(ShopStore.ItemMatrixByParentView.URI_CONTENT))
                .where(ShopStore.ItemMatrixByParentView.CHILD_ITEM_GUID + " = ?", sourceItemGuid)
                .perform(getContext());
        if (itemMatrixCursor.moveToFirst()) {
            referenceItemGuid = itemMatrixCursor.getString(itemMatrixCursor.getColumnIndex(ShopStore.ItemMatrixByParentView.PARENT_ITEM_GUID));
        }
        itemMatrixCursor.close();
        if (!TextUtils.isEmpty(referenceItemGuid)) {
            Cursor parentItemCursor = ProviderAction.query(ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT))
                    .where(ShopStore.ItemTable.GUID + " = ?", referenceItemGuid)
                    .perform(getContext());
            if (parentItemCursor.moveToFirst()) {
                parentItem = new ItemExModel(new ItemModel(parentItemCursor));
            }
            if (parentItem != null) { //item was linked to reference item
                parentItemMatrix = new ItemMatrixModel(
                        UUID.randomUUID().toString(),
                        item.description,
                        parentItem.guid,
                        item.guid, null
                );
                ArrayList<ItemMatrixModel> matrix = new ArrayList<>(1);
                matrix.add(parentItemMatrix);
                matrixResult = new AddVariantMatrixItemsCommand().sync(getContext(), matrix, getAppCommandContext());
            }
        }
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

        if (matrixResult != null && matrixResult.getLocalDbOperations() != null) {
            operations.addAll(matrixResult.getLocalDbOperations());
        }

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        if (matrixResult != null && matrixResult.getSqlCmd() != null) {
            batch.add(matrixResult.getSqlCmd());
        }
        return batch;
    }

    public static void start(Context context, String sourceItemGuid, ItemModel item) {
        create(CopyItemWithReferences.class).arg(ARG_ITEM, item).arg(ARG_SOURCE_ITEM_GUID, sourceItemGuid).queueUsing(context);
    }

    public static void start(Context context, String sourceItemGuid, ItemModel item, CommandCallback callback) {
        create(CopyItemWithReferences.class).arg(ARG_ITEM, item).arg(ARG_SOURCE_ITEM_GUID, sourceItemGuid).callback(callback).queueUsing(context);
    }

    public static abstract class CommandCallback {

        @OnSuccess(CopyItemWithReferences.class)
        public void onSuccess() {
            handleSuccess();
        }

        protected abstract void handleSuccess();

    }

}
