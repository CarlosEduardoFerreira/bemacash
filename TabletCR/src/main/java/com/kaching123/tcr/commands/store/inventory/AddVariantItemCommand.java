package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.VariantItemModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

public class AddVariantItemCommand extends AsyncCommand {

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
        operations.add(ContentProviderOperation.newInsert(
                ShopProvider.contentUri(ShopStore.VariantItemTable.URI_CONTENT))
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.insert(model, getAppCommandContext());
    }

    public static void start(Context context, VariantItemModel model) {
        create(AddVariantItemCommand.class).arg(ARG_VARIANT, model).queueUsing(context);
    }

}
