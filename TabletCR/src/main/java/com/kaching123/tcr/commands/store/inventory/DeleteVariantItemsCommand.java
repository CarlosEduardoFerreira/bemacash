package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.VariantItemModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;

import static com.kaching123.tcr.store.ShopStore.DELETE_VALUES;
import static com.kaching123.tcr.store.ShopStore.VariantItemTable;

public class DeleteVariantItemsCommand extends AsyncCommand {

    private static final String ARG_VARIANT_ITEMS = "arg_variant_items";

    private List<VariantItemModel> models;
    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        if (models == null) {
            models = (List<VariantItemModel>) getArgs().getSerializable(ARG_VARIANT_ITEMS);
        }
        operations = new ArrayList<ContentProviderOperation>();
        sql = batchDelete(VariantItemModel.class);
        JdbcConverter<VariantItemModel> jdbc = JdbcFactory.getConverter(VariantItemTable.TABLE_NAME);
        for (VariantItemModel model : models) {
            operations.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(VariantItemTable.URI_CONTENT))
                    .withValues(DELETE_VALUES).withSelection(VariantItemTable.GUID + "=?",
                            new String[]{model.guid})
                    .build());
            sql.add(jdbc.deleteSQL(model, this.getAppCommandContext()));
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

    public static void start(Context context, ArrayList<VariantItemModel> models) {
        create(DeleteVariantItemsCommand.class).arg(ARG_VARIANT_ITEMS, models).queueUsing(context);
    }

    public SyncResult sync(Context context, List<VariantItemModel> models, IAppCommandContext appCommandContext) {
        this.models = models;
        return syncDependent(context, appCommandContext);
    }

}
