package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.VariantItemModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.VariantItemTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

public class EditVariantItemCommand extends AsyncCommand {

    private static final String ARG_VARIANT = "arg_variant";

    private VariantItemModel model;

    @Override
    protected TaskResult doCommand() {
        model = (VariantItemModel) getArgs().getSerializable(ARG_VARIANT);
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(VariantItemTable.URI_CONTENT))
                .withValues(model.toValues())
                .withSelection(VariantItemTable.GUID + " = ?", new String[]{model.guid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.update(model, getAppCommandContext());
    }

    public static void start(Context context, VariantItemModel model) {
        create(EditVariantItemCommand.class).arg(ARG_VARIANT, model).queueUsing(context);
    }

}
