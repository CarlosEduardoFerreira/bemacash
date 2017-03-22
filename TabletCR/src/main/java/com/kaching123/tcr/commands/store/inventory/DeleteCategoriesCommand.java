package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.CategoryJdbcConverter;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 24.01.14.
 */
public class DeleteCategoriesCommand extends AsyncCommand {

    private static final Uri URI_CATEGORIES = ShopProvider.getContentUri(CategoryTable.URI_CONTENT);

    private static final  String ARG_DEPARTMENT_GUID = "arg_department_guid";

    private String departmentGuid;

    @Override
    protected TaskResult doCommand() {
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newUpdate(URI_CATEGORIES)
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(CategoryTable.DEPARTMENT_GUID + " = ?", new String[]{departmentGuid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        CategoryJdbcConverter categoryJdbcConverter = (CategoryJdbcConverter)JdbcFactory.getConverter(CategoryTable.TABLE_NAME);
        Log.d("BemaCarl7","DeleteCategoriesCommand.createSqlCommand.departmentGuid: " + departmentGuid);
        BatchSqlCommand batch = batchDelete(CategoryTable.TABLE_NAME);
        batch.add(categoryJdbcConverter.deleteByDepartment(departmentGuid, getAppCommandContext()));

        new AtomicUpload().upload(batch, AtomicUpload.UploadType.WEB);

        return batch;
    }

    public SyncResult sync(Context context, String departmentGuid, IAppCommandContext appCommandContext) {
        Log.d("BemaCarl7","DeleteCategoriesCommand.sync.departmentGuid: " + departmentGuid);
        this.departmentGuid = departmentGuid;
        return syncDependent(context, appCommandContext);
    }

    public static void start(Context context, String departmentGuid){
        create(DeleteCategoriesCommand.class).arg(ARG_DEPARTMENT_GUID, departmentGuid).queueUsing(context);
    }
}
