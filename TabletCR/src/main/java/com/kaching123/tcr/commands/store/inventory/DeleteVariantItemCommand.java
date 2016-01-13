package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.VariantItemModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.VariantItemTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

public class DeleteVariantItemCommand extends AsyncCommand {

    private static final String ARG_VARIANT = "arg_variant";

    private String guid;

    @Override
    protected TaskResult doCommand() {
        guid = getArgs().getString(ARG_VARIANT);
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation
                .newUpdate(ShopProvider.contentUri(VariantItemTable.URI_CONTENT))
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(VariantItemTable.GUID + " = ?", new String[]{guid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.delete(new VariantItemModel(guid), getAppCommandContext());
    }

    public static void start(Context context, String guid) {
        create(DeleteVariantItemCommand.class).arg(ARG_VARIANT, guid).queueUsing(context);
    }

}
