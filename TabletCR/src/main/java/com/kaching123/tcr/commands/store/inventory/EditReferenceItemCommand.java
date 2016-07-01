package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
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
import com.kaching123.tcr.util.CursorUtil;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vkompaniets on 04.12.13.
 */
public class EditReferenceItemCommand extends AsyncCommand {

    private static final Uri ITEM_URI = ShopProvider.contentUri(ShopStore.ItemTable.URI_CONTENT);
    private static final Uri ITEM_MATRIX_VIEW_URI = ShopProvider.contentUri(ShopStore.ItemMatrixByChildView.URI_CONTENT);

    private static final String ARG_ITEM = "arg_item";

    private ItemModel newItem;

    @Override
    protected TaskResult doCommand() {
        Logger.d("EditReferenceItemCommand doCommand");
        if (newItem == null)
            newItem = (ItemModel) getArgs().getSerializable(ARG_ITEM);
        if (!updateChildItems()) {
            return failed();
        }
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newUpdate(ITEM_URI)
                .withSelection(ShopStore.ItemTable.GUID + " = ?", new String[]{newItem.guid})
                .withValues(newItem.toValues())
                .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchUpdate(newItem);
        batch.add(JdbcFactory.getConverter(newItem).updateSQL(newItem, getAppCommandContext()));
        return batch;
    }

    private boolean updateChildItems() {
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
            List<ItemModel> models = CursorUtil._wrap(ProviderAction.query(ITEM_URI)
                            .whereIn(ShopStore.ItemTable.GUID, childGuids).perform(getContext()),
                    new ItemWrapFunction());
            EditItemCommand editItemCommand = new EditItemCommand();
            for (ItemModel itemModel : models) {
                itemModel.categoryId = newItem.categoryId;
                itemModel.unitsLabel = newItem.unitsLabel;
                itemModel.unitsLabelId = newItem.unitsLabelId;
                itemModel.priceType = newItem.priceType;
                itemModel.serializable = newItem.serializable;
                itemModel.codeType = newItem.codeType;
                itemModel.serializable = newItem.serializable;
                itemModel.isActiveStatus = newItem.isActiveStatus;
                itemModel.printerAliasGuid = newItem.printerAliasGuid;
                itemModel.hasNotes = newItem.hasNotes;
                itemModel.price = newItem.price;
                itemModel.isDiscountable = newItem.isDiscountable;
                itemModel.discountType = newItem.discountType;
                itemModel.discount = newItem.discount;
                itemModel.taxGroupGuid = newItem.taxGroupGuid;
                itemModel.cost = newItem.cost;
                itemModel.commissionEligible = newItem.commissionEligible;
                itemModel.commission = newItem.commission;
                itemModel.btnView = newItem.btnView;
                itemModel.loyaltyPoints = newItem.loyaltyPoints;
                success = success && editItemCommand.sync(getContext(), itemModel, getAppCommandContext());
            }
        }
        return success;
    }

    public static void start(Context context, ItemModel item) {
        create(EditReferenceItemCommand.class).arg(ARG_ITEM, item).queueUsing(context);
    }

    /**
     * use in import. can be standalone *
     */
    public boolean sync(Context context, ItemModel item, PublicGroundyTask.IAppCommandContext appCommandContext) {
        this.newItem = item;
        TaskResult result = syncStandalone(context, appCommandContext);
        return !isFailed(result);
    }
}
