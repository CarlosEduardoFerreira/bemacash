package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CategoryModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 18.12.13.
 */
public class EditCategoryCommand extends AsyncCommand{

    public static final String ARG_MODEL = "arg_model";

    private CategoryModel model;

    @Override
    protected TaskResult doCommand() {
        Logger.d("EditCategoryCommand doCommand");
        model = (CategoryModel) getArgs().getSerializable(ARG_MODEL);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(ShopProvider.getContentUri(CategoryTable.URI_CONTENT))
                .withSelection(CategoryTable.GUID + " = ?", new String[]{model.guid})
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchUpdate(model);
        batch.add(JdbcFactory.getConverter(model).updateSQL(model, getAppCommandContext()));

        new AtomicUpload().upload(batch, AtomicUpload.UploadType.WEB);

        return batch;
    }

    public static void start(Context context, CategoryModel model){
        create(EditCategoryCommand.class).arg(ARG_MODEL, model).queueUsing(context);
    }
}
