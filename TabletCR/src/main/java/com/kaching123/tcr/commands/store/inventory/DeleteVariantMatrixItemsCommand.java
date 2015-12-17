package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

public class DeleteVariantMatrixItemsCommand extends AsyncCommand {

    private static final String ARG_VARIANT_MATRIX = "arg_variant_matrix";

    private List<ItemMatrixModel> models;
    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        if (models == null) {
            models = (List<ItemMatrixModel>) getArgs().getSerializable(ARG_VARIANT_MATRIX);
        }
        JdbcConverter<ItemMatrixModel> jdbc = JdbcFactory.getConverter(ShopStore.ItemMatrixTable.TABLE_NAME);
        operations = new ArrayList<>();
        sql = batchDelete(ItemMatrixModel.class);
        for (ItemMatrixModel m : models) {
            operations.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(ShopStore.ItemMatrixTable.URI_CONTENT))
                    .withValues(ShopStore.DELETE_VALUES).withSelection(ShopStore.ItemMatrixTable.GUID + "=?", new String[]{m.guid})
                    .build());
            sql.add(jdbc.deleteSQL(m, this.getAppCommandContext()));
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

    public static void start(Context context, ArrayList<ItemMatrixModel> models) {
        create(DeleteVariantMatrixItemsCommand.class).arg(ARG_VARIANT_MATRIX, models).queueUsing(context);
    }

    public SyncResult sync(Context context, List<ItemMatrixModel> models, IAppCommandContext appCommandContext) {
        this.models = models;
        return syncDependent(context, appCommandContext);
    }
}
