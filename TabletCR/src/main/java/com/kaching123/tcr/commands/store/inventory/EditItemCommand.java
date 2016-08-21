package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.ItemMovementModelFactory;
import com.kaching123.tcr.model.converter.ItemMatrixFunction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.util.CursorUtil;
import com.kaching123.tcr.util.InventoryUtils;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

/**
 * Created by vkompaniets on 04.12.13.
 */
public class EditItemCommand extends AsyncCommand {

    private static final Uri ITEM_URI = ShopProvider.getContentUri(ItemTable.URI_CONTENT);
    private static final Uri ITEM_MOVEMENT_URI = ShopProvider.getContentUri(ItemMovementTable.URI_CONTENT);
    private static final Uri ITEM_MATRIX_URI = ShopProvider.contentUri(ShopStore.ItemMatrixTable.URI_CONTENT);

    private static final String ARG_ITEM = "arg_item";

    private ItemModel item;
    private ItemMovementModel movementModel;
    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        Logger.d("EditItemCommand doCommand");
        if (item == null)
            item = (ItemModel) getArgs().getSerializable(ARG_ITEM);

        movementModel = null;

        BigDecimal currentAvailableQty = BigDecimal.ZERO;
        String oldCategoryId = "";
        Cursor c = ProviderAction.query(ITEM_URI)
                .projection(ItemTable.TMP_AVAILABLE_QTY, ItemTable.DEFAULT_MODIFIER_GUID, ItemTable.CATEGORY_ID)
                .where(ItemTable.GUID + " = ?", item.guid)
                .perform(getContext());
        if (c.moveToFirst()) {
            currentAvailableQty = _decimalQty(c, c.getColumnIndex(ItemTable.TMP_AVAILABLE_QTY), BigDecimal.ZERO);
            item.defaultModifierGuid = c.getString(c.getColumnIndex(ItemTable.DEFAULT_MODIFIER_GUID));
            oldCategoryId = c.getString(c.getColumnIndex(ItemTable.CATEGORY_ID));
        }
        c.close();

        boolean categoryChanged = !oldCategoryId.equals(item.categoryId);
        if (categoryChanged){
            item.orderNum = ItemModel.getMaxOrderNum(getContext(), item.categoryId) + 1;
        }

        if (currentAvailableQty.compareTo(item.availableQty) != 0){
            item.updateQtyFlag = UUID.randomUUID().toString();
            movementModel = ItemMovementModelFactory.getNewModel(
                    item.guid,
                    item.updateQtyFlag,
                    item.availableQty,
                    true,
                    new Date());
        }

        operations = new ArrayList<>();

        operations.add(ContentProviderOperation.newUpdate(ITEM_URI)
                .withSelection(ItemTable.GUID + " = ?", new String[]{item.guid})
                .withValues(item.toValues())
                .build());
        if (movementModel != null) {
            operations.add(ContentProviderOperation.newInsert(ITEM_MOVEMENT_URI)
                    .withValues(movementModel.toValues())
                    .build());
        }
        sql = batchUpdate(item);
        sql.add(JdbcFactory.getConverter(item).updateSQL(item, getAppCommandContext()));
        if (movementModel != null) {
            sql.add(JdbcFactory.getConverter(movementModel).insertSQL(movementModel,
                    getAppCommandContext()));
        }

        if (item.codeType != null){
            if (!InventoryUtils.removeComposers(item.guid, getContext(), getAppCommandContext(), operations, sql))
                return failed();
        }else{
            if (!InventoryUtils.removeUnits(item.guid, getContext(), getAppCommandContext(), operations, sql))
                return failed();
        }
        item.serializable = item.codeType != null;

        if (item.referenceItemGuid != null) {
            clearParentDuplicates();
        }

        if (categoryChanged){
            shiftOrderNums(oldCategoryId);
        }

        return succeeded();
    }

    private void shiftOrderNums(String categoryId){
        ContentValues cv = new ContentValues(1);
        cv.put(ItemTable.CATEGORY_ID, item.categoryId);
        getContext().getContentResolver().update(ShopProvider.contentUri(ItemTable.URI_CONTENT), cv, ItemTable.GUID + " = ?", new String[]{item.guid});
        InventoryUtils.shiftOrderNums(categoryId, getContext(), getAppCommandContext(), operations, sql);
    }

    private void updateItemOrderNums(){
        ItemModel model = ProviderAction.query(ITEM_URI)
                .projection(ItemTable.CATEGORY_ID, ItemTable.ORDER_NUM)
                .where(ItemTable.GUID + " = ?", item.guid)
                .perform(getContext())
                .toFluentIterable(new Function<Cursor, ItemModel>() {
                    @Override
                    public ItemModel apply(Cursor input) {
                        ItemModel model = new ItemModel();
                        model.categoryId = input.getString(0);
                        model.orderNum = input.getInt(1);
                        return model;
                    }
                }).first().orNull();

        if (model == null)
            return;

        List<ItemModel> models = ProviderAction.query(ITEM_URI)
                .projection(ItemTable.GUID, ItemTable.ORDER_NUM)
                .where(ItemTable.CATEGORY_ID + " = ?", model.categoryId)
                .where(ItemTable.ORDER_NUM + " > ?", model.orderNum)
                .orderBy(ItemTable.ORDER_NUM)
                .perform(getContext())
                .toFluentIterable(new Function<Cursor, ItemModel>() {
                    @Override
                    public ItemModel apply(Cursor input) {
                        ItemModel model = new ItemModel(input.getString(0));
                        model.orderNum = input.getInt(1);
                        return model;
                    }
                }).toImmutableList();

        for (ItemModel m : models){
            InventoryUtils.updateOrderNum(m.guid, m.orderNum - 1, getAppCommandContext(), operations, sql);
        }
    }

    private TaskResult clearParentDuplicates() {
        Cursor c = ProviderAction.query(ITEM_MATRIX_URI)
                .where(ShopStore.ItemMatrixTable.CHILD_GUID + "=?", item.guid)
                .perform(getContext());
        if (c.moveToFirst()) {
            ItemMatrixModel itemMatrixModel = CursorUtil._wrap(c, new ItemMatrixFunction());
            itemMatrixModel.childItemGuid = null;
            SyncResult syncResult = new EditVariantMatrixItemCommand().syncDependent(getContext(), itemMatrixModel, getAppCommandContext());
            operations.addAll(syncResult.getLocalDbOperations());
            sql.add(syncResult.getSqlCmd());
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

    public static void start(Context context, ItemModel item) {
        create(EditItemCommand.class)
                .arg(ARG_ITEM, item)
                .queueUsing(context);
    }

    /**
     * use in import. can be standalone
     **/
    public boolean sync(Context context, ItemModel item, IAppCommandContext appCommandContext) {
        this.item = item;
        TaskResult result = syncStandalone(context, appCommandContext);
        return !isFailed(result);
    }

    public SyncResult syncDependent(Context context, ItemModel item, IAppCommandContext appCommandContext) {
        this.item = item;
        return syncDependent(context, appCommandContext);
    }
}
