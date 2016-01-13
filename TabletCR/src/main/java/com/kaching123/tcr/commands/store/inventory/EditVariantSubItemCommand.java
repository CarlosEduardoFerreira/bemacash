package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.VariantSubItemModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

public class EditVariantSubItemCommand extends AsyncCommand {

    private static final String ARG_VARIANT_SUB_ITEM = "arg_variant_sub_item";

    private VariantSubItemModel model;

    @Override
    protected TaskResult doCommand() {
        model = (VariantSubItemModel) getArgs().getSerializable(ARG_VARIANT_SUB_ITEM);
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(ShopStore.VariantSubItemTable.URI_CONTENT))
                .withSelection(ShopStore.VariantSubItemTable.GUID + "=?", new String[]{model.guid})
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.update(model, getAppCommandContext());
    }

    public static void start(Context context, VariantSubItemModel model) {
        create(EditVariantSubItemCommand.class).arg(ARG_VARIANT_SUB_ITEM, model).queueUsing(context);
    }

}
