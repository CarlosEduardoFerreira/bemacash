package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.VariantSubItemModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.VariantSubItemTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

public class AddVariantSubItemCommand extends AsyncCommand {

    private static final String ARG_VARIANT_SUB_ITEM = "arg_variant_sub_item";

    private VariantSubItemModel model;

    @Override
    protected TaskResult doCommand() {
        model = (VariantSubItemModel) getArgs().getSerializable(ARG_VARIANT_SUB_ITEM);
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newInsert(ShopProvider.contentUri(VariantSubItemTable.URI_CONTENT))
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.insert(model, getAppCommandContext());
    }

    public static void start(Context context, VariantSubItemModel model) {
        create(AddVariantSubItemCommand.class).arg(ARG_VARIANT_SUB_ITEM, model).queueUsing(context);
    }

}
