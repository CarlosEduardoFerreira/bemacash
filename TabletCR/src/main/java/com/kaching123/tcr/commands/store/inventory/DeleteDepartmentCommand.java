package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.DepartmentModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 09.01.14.
 */
public class DeleteDepartmentCommand extends AsyncCommand {

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    private static final Uri URI_CATEGORIES = ShopProvider.getContentUri(CategoryTable.URI_CONTENT);

    private static final String ARG_DEPARTMENT_MODEL = "arg_department_model";

    private static final String EXTRA_DEPARTMENT_GUID = "extra_department_guid";

    private static final String EXTRA_DEPARTMENT_NAME = "extra_department_name";

    private static final String EXTRA_ITEMS_COUNT = "extra_items_count";

    private DepartmentModel model;
    private SyncResult subResult;

    @Override
    protected TaskResult doCommand() {
        model = (DepartmentModel) getArgs().getSerializable(ARG_DEPARTMENT_MODEL);

        int count = getItemsCount(model.guid);
        if (count > 0) {
            return failed().add(EXTRA_DEPARTMENT_NAME, model.title).add(EXTRA_ITEMS_COUNT, count).add(EXTRA_DEPARTMENT_GUID, model.guid);
        }

        subResult = new DeleteCategoriesCommand().sync(getContext(), model.guid, getAppCommandContext());
        if (subResult == null)
            return failed().add(EXTRA_DEPARTMENT_NAME, model.title).add(EXTRA_ITEMS_COUNT, count).add(EXTRA_DEPARTMENT_GUID, model.guid);

        return succeeded().add(EXTRA_DEPARTMENT_NAME, model.title);
    }

    private int getItemsCount(String departmentGuid) {
        int count = 0;
        Cursor categoryCursor = ProviderAction.query(URI_CATEGORIES)
                .projection(CategoryTable.GUID)
                .where(CategoryTable.DEPARTMENT_GUID + " = ?", departmentGuid)
                .perform(getContext());

        if (categoryCursor.moveToFirst()) {
            do {
                Cursor itemCursor = ProviderAction.query(URI_ITEMS)
                        .projection(ItemTable.CATEGORY_ID)
                        .where(ItemTable.CATEGORY_ID + " = ?", categoryCursor.getString(0))
                        .perform(getContext());

                count += itemCursor.getCount();
                itemCursor.close();
            } while (categoryCursor.moveToNext());
        }
        categoryCursor.close();
        return count;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        if (subResult.getLocalDbOperations() != null)
            operations.addAll(subResult.getLocalDbOperations());

        operations.add(ContentProviderOperation.newUpdate(ShopProvider.getContentUri(DepartmentTable.URI_CONTENT))
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(DepartmentTable.GUID + " = ?", new String[]{model.guid})
                .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand sqlCommand = batchDelete(model);
        if (subResult.getSqlCmd() != null)
            sqlCommand.add(subResult.getSqlCmd());
        sqlCommand.add(JdbcFactory.getConverter(model).deleteSQL(model, getAppCommandContext()));
        return sqlCommand;
    }

    public static void start(Context context, DepartmentModel model, DeleteDepartmentCallback callback){
        create(DeleteDepartmentCommand.class).arg(ARG_DEPARTMENT_MODEL, model).callback(callback).queueUsing(context);
    }

    public static abstract class DeleteDepartmentCallback {

        @OnSuccess(DeleteDepartmentCommand.class)
        public void onSuccess(@Param(EXTRA_DEPARTMENT_NAME) String departmentName){
            onDepartmentDeleted(departmentName);
        }

        @OnFailure(DeleteDepartmentCommand.class)
        public void onFailure(@Param(EXTRA_DEPARTMENT_NAME) String departmentName, @Param(EXTRA_ITEMS_COUNT) int itemsCount){
            onDepartmentHasItems(departmentName, itemsCount);
        }

        protected abstract void onDepartmentDeleted(String departmentName);
        protected abstract void onDepartmentHasItems(String departmentName, int itemsCount);
    }
}
