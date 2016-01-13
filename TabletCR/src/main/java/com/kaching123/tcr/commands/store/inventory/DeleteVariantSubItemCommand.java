package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.VariantSubItemModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.List;

public class DeleteVariantSubItemCommand extends AsyncCommand {

    private static final String ARG_VARIANT_SUB_ITEMS = "arg_variant_sub_items";

    private ArrayList<String> guids;
    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        if (guids == null) {
            guids = getArgs().getStringArrayList(ARG_VARIANT_SUB_ITEMS);
        }
        operations = new ArrayList<ContentProviderOperation>();
        sql = batchDelete(VariantSubItemModel.class);
        JdbcConverter<VariantSubItemModel> jdbc = JdbcFactory.getConverter(ShopStore.VariantSubItemTable.TABLE_NAME);
        for (String guid : guids) {
            operations.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(ShopStore.VariantSubItemTable.URI_CONTENT))
                    .withValues(ShopStore.DELETE_VALUES).withSelection(ShopStore.VariantSubItemTable.GUID + "=?",
                            new String[]{guid})
                    .build());
            sql.add(jdbc.deleteSQL(new VariantSubItemModel(guid), this.getAppCommandContext()));
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

    public static void start(Context context, ArrayList<String> guids) {
        create(DeleteVariantSubItemCommand.class).arg(ARG_VARIANT_SUB_ITEMS, guids).queueUsing(context);
    }

    public SyncResult sync(Context context, List<VariantSubItemModel> models, PublicGroundyTask.IAppCommandContext appCommandContext) {
        this.guids = new ArrayList<String>(models.size());
        for (int i = 0; i < models.size(); i++) {
            this.guids.add(models.get(i).guid);
        }
        return syncDependent(context, appCommandContext);
    }

}
