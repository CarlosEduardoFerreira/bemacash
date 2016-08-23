package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.function.ItemWrapFunction;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.util.CursorUtil;
import com.kaching123.tcr.util.InventoryUtils;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._in;

/**
 * Created by vkompaniets on 04.12.13.
 */
public class EditReferenceItemCommand extends AsyncCommand {

    private static final Uri ITEM_URI = ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT);
    private static final Uri ITEM_MATRIX_VIEW_URI = ShopProvider.contentUri(ShopStore.ItemMatrixByChildView.URI_CONTENT);

    private static final String ARG_ITEM = "arg_item";

    private ItemModel newItem;
    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        Logger.d("EditReferenceItemCommand doCommand");
        if (newItem == null) {
            newItem = (ItemModel) getArgs().getSerializable(ARG_ITEM);
        }

        operations = new ArrayList<>();
        sql = batchUpdate(newItem);

        Cursor c = ProviderAction.query(ITEM_URI)
                .projection(ItemTable.CATEGORY_ID)
                .where(ItemTable.GUID + " = ?", newItem.guid)
                .perform(getContext());
        String oldCategoryId = "";
        if (c.moveToFirst())
            oldCategoryId = c.getString(0);
        c.close();

        boolean categoryChanged = !newItem.categoryId.equals(oldCategoryId);
        int orderNum = ItemModel.getMaxOrderNum(getContext(), newItem.categoryId);
        if (!updateChildItems(categoryChanged, orderNum)) {
            return failed();
        }

        if (categoryChanged){
            newItem.orderNum = orderNum + 1;
            shiftOrderNums(oldCategoryId);
        }

        return succeeded();
    }

    private void shiftOrderNums(String categoryId) {
        ContentValues cv = new ContentValues(1);
        cv.put(ItemTable.CATEGORY_ID, newItem.categoryId);
        getContext().getContentResolver().update(ShopProvider.contentUri(ItemTable.URI_CONTENT), cv, ItemTable.GUID + " = ?", new String[]{newItem.guid});
        InventoryUtils.shiftOrderNums(categoryId, getContext(), getAppCommandContext(), operations, sql);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        operations.add(ContentProviderOperation.newUpdate(ITEM_URI)
                .withSelection(ShopStore.ItemTable.GUID + " = ?", new String[]{newItem.guid})
                .withValues(newItem.toValues())
                .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        sql.add(JdbcFactory.getConverter(newItem).updateSQL(newItem, getAppCommandContext()));
        return sql;
    }

    private boolean updateChildItems(boolean categoryChanged, int orderNum) {
        boolean success = true;
        Cursor c = ProviderAction.query(ITEM_MATRIX_VIEW_URI)
                .where(ShopStore.ItemMatrixByChildView.ITEM_MATRIX_PARENT_GUID + "=?", newItem.guid)
                .perform(getContext());
        List<String> childGuids = new ArrayList<>(c.getCount());
        if (c.moveToFirst()) {
            int childGuidIdx = c.getColumnIndex(ShopStore.ItemMatrixByChildView.CHILD_ITEM_GUID);
            do {
                if (!c.isNull(childGuidIdx)) {
                    childGuids.add(c.getString(childGuidIdx));
                }
            } while (c.moveToNext());
        }
        c.close();
        if (childGuids.size() > 0) {
            if (categoryChanged){
                ContentValues cv = new ContentValues(1);
                cv.put(ItemTable.CATEGORY_ID, newItem.categoryId);
                getContext().getContentResolver().update(ShopProvider.contentUri(ItemTable.URI_CONTENT), cv, _in(ItemTable.GUID, childGuids.size()), childGuids.toArray(new String[childGuids.size()]));
            }

            List<ItemModel> models = CursorUtil._wrap(ProviderAction.query(ITEM_URI)
                            .whereIn(ShopStore.ItemTable.GUID, childGuids).perform(getContext()),
                    new ItemWrapFunction());

            EditItemCommand editItemCommand = new EditItemCommand();
            int i = 2;
            for (ItemModel itemModel : models) {
                itemModel.categoryId = newItem.categoryId;
                itemModel.unitsLabelId = newItem.unitsLabelId;
                itemModel.priceType = newItem.priceType;
                itemModel.serializable = newItem.serializable;
                itemModel.codeType = newItem.codeType;
                itemModel.serializable = newItem.serializable;
                itemModel.isActiveStatus = newItem.isActiveStatus;
                itemModel.printerAliasGuid = newItem.printerAliasGuid;
                itemModel.hasNotes = newItem.hasNotes;
                itemModel.price = newItem.price;
                itemModel.price1 = newItem.price1;
                itemModel.price2 = newItem.price2;
                itemModel.price3 = newItem.price3;
                itemModel.price4 = newItem.price4;
                itemModel.price5 = newItem.price5;
                itemModel.isDiscountable = newItem.isDiscountable;
                itemModel.discountType = newItem.discountType;
                itemModel.discount = newItem.discount;
                itemModel.taxGroupGuid = newItem.taxGroupGuid;
                itemModel.cost = newItem.cost;
                itemModel.commissionEligible = newItem.commissionEligible;
                itemModel.commission = newItem.commission;
                itemModel.btnView = newItem.btnView;
                itemModel.loyaltyPoints = newItem.loyaltyPoints;
                if (categoryChanged)
                    itemModel.orderNum = orderNum + i++;
                success &= editItemCommand.sync(getContext(), itemModel, getAppCommandContext());
            }
        }
        return success;
    }

    public static void start(Context context, ItemModel item, EditReferenceItemCommandCallback callback) {
        create(EditReferenceItemCommand.class).arg(ARG_ITEM, item).callback(callback).queueUsing(context);
    }

    /**
     * use in import. can be standalone *
     */
    public boolean sync(Context context, ItemModel item, PublicGroundyTask.IAppCommandContext appCommandContext) {
        this.newItem = item;
        TaskResult result = syncStandalone(context, appCommandContext);
        return !isFailed(result);
    }

    public static abstract class EditReferenceItemCommandCallback {

        @OnSuccess(EditReferenceItemCommand.class)
        public void onSuccess() {
            handleSuccess();
        }

        @OnFailure(EditReferenceItemCommand.class)
        public void onFailure() {
            handleFailure();
        }

        protected abstract void handleSuccess();

        protected abstract void handleFailure();

    }
}
