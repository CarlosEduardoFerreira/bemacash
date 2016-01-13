package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.converter.ItemFunction;
import com.kaching123.tcr.model.converter.ItemMatrixFunction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.CursorUtil;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

public class EditVariantMatrixItemCommand extends AsyncCommand {

    private static final Uri ITEM_MATRIX_URI = ShopProvider.contentUri(ShopStore.ItemMatrixTable.URI_CONTENT);
    private static final Uri ITEM_URI = ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT);

    private static final String ARG_ITEM_MATRIX = "arg_item_matrix";
    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;
    private ItemMatrixModel itemMatrixModel;

    @Override
    protected TaskResult doCommand() {
        if (itemMatrixModel == null)
            itemMatrixModel = (ItemMatrixModel) getArgs().getSerializable(ARG_ITEM_MATRIX);
        operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newUpdate(ITEM_MATRIX_URI)
                .withSelection(ShopStore.ItemMatrixTable.GUID + "=?", new String[]{itemMatrixModel.guid})
                .withValues(itemMatrixModel.toValues())
                .build());
        sql = batchUpdate(ItemMatrixModel.class);
        sql.add(JdbcFactory.update(itemMatrixModel, getAppCommandContext()));
        TaskResult taskResult;
        if (itemMatrixModel.childItemGuid != null) {
            taskResult = clearParentDuplicates();
        } else {
            taskResult = succeeded();
        }
        return taskResult;
    }

    private TaskResult clearParentDuplicates() {
        Cursor c = ProviderAction.query(ITEM_MATRIX_URI).where(ShopStore.ItemMatrixTable.CHILD_GUID + "=?",
                itemMatrixModel.childItemGuid).perform(getContext());
        if (c.moveToFirst()) {
            ItemMatrixModel itemMatrixModel = CursorUtil._wrap(c, new ItemMatrixFunction());
            itemMatrixModel.childItemGuid = null;
            operations.add(ContentProviderOperation.newUpdate(ITEM_MATRIX_URI)
                    .withSelection(ShopStore.ItemMatrixTable.GUID + "=?", new String[]{itemMatrixModel.guid})
                    .withValues(itemMatrixModel.toValues())
                    .build());
            sql.add(JdbcFactory.update(itemMatrixModel, getAppCommandContext()));
        }
        Cursor itemCursor = ProviderAction.query(ITEM_URI).where(ShopStore.ItemTable.GUID + "=?",
                itemMatrixModel.childItemGuid).perform(getContext());
        if (itemCursor.moveToFirst()) {
            ItemModel itemModel = CursorUtil._wrap(itemCursor, new ItemFunction());
            if (itemModel.referenceItemGuid != null) {
                itemModel.referenceItemGuid = null;
                SyncResult syncResult = new EditItemCommand().syncDependent(getContext(),
                        itemModel, getAppCommandContext());
                if (syncResult != null) {
                    operations.addAll(syncResult.getLocalDbOperations());
                    sql.add(syncResult.getSqlCmd());
                } else {
                    return failed();
                }
            }
        }
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sql;
    }

    public static void start(Context context, ItemMatrixModel model) {
        create(EditVariantMatrixItemCommand.class).arg(ARG_ITEM_MATRIX, model).queueUsing(context);
    }

    public SyncResult syncDependent(Context context, ItemMatrixModel itemMatrixModel, PublicGroundyTask.IAppCommandContext appCommandContext) {
        this.itemMatrixModel = itemMatrixModel;
        return syncDependent(context, appCommandContext);
    }

    /**
     * use in import. can be standalone
     **/
    public boolean sync(Context context, ItemMatrixModel itemMatrixModel, PublicGroundyTask.IAppCommandContext appCommandContext) {
        this.itemMatrixModel = itemMatrixModel;
        TaskResult result = syncStandalone(context, appCommandContext);
        return !isFailed(result);
    }
}
