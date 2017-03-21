package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CategoryModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 08.01.14.
 */
public class DeleteCategoryCommand extends AsyncCommand {

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(ShopStore.ItemTable.URI_CONTENT);

    private static final String ARG_CATEGORY_MODEL = "arg_category_model";

    private static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    private static final String EXTRA_ITEMS_COUNT = "extra_items_count";

    private CategoryModel model;

    @Override
    protected TaskResult doCommand() {
        model = (CategoryModel) getArgs().getSerializable(ARG_CATEGORY_MODEL);
        Cursor c = ProviderAction.query(URI_ITEMS)
                .projection(ShopStore.ItemTable.CATEGORY_ID)
                .where(ShopStore.ItemTable.CATEGORY_ID + " = ?", model.guid)
                .perform(getContext());
        int count = c.getCount();
        c.close();
        
        if (count > 0){
            return failed().add(EXTRA_CATEGORY_NAME, model.title).add(EXTRA_ITEMS_COUNT, count);
        }

        return succeeded().add(EXTRA_CATEGORY_NAME, model.title);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(ShopProvider.getContentUri(CategoryTable.URI_CONTENT))
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(CategoryTable.GUID + " = ?", new String[]{model.guid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchDelete(CategoryTable.TABLE_NAME);
        batch.add(JdbcFactory.delete(model, getAppCommandContext()));

        new AtomicUpload().upload(batch, AtomicUpload.UploadType.WEB);

        return batch;
    }

    public static void start(Context context, CategoryModel model, DeleteCategoryCommandCallback callback){
        create(DeleteCategoryCommand.class).arg(ARG_CATEGORY_MODEL, model).callback(callback).queueUsing(context);
    }

    public static abstract class DeleteCategoryCommandCallback{

        @OnSuccess(DeleteCategoryCommand.class)
        public void onSuccess(@Param(EXTRA_CATEGORY_NAME) String categoryName){
            onCategoryDeleted(categoryName);
        }

        @OnFailure(DeleteCategoryCommand.class)
        public void onFailure(@Param(EXTRA_CATEGORY_NAME) String categoryName, @Param(EXTRA_ITEMS_COUNT) int itemsCount){
            onCategoryHasItems(categoryName, itemsCount);
        }

        protected abstract void onCategoryDeleted(String categoryName);
        protected abstract void onCategoryHasItems(String categoryName, int itemsCount);
    }
}
