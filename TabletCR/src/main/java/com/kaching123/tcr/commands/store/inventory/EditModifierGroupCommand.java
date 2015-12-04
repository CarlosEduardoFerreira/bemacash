package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by Mayer
 */
public class EditModifierGroupCommand extends AsyncCommand {

    private static final Uri URI_MODIFIERS = ShopProvider.contentUri(ShopStore.ModifierGroupTable.URI_CONTENT);

    private static final String ARG_MODIFIER = "arg_modifier";

    private ModifierGroupModel modifier;

    @Override
    protected TaskResult doCommand() {
        modifier = (ModifierGroupModel) getArgs().getSerializable(ARG_MODIFIER);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newUpdate(URI_MODIFIERS)
                .withSelection(ShopStore.ModifierGroupTable.GUID + " = ?", new String[]{modifier.guid})
                .withValues(modifier.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchUpdate(modifier);
        batch.add(JdbcFactory.getConverter(modifier).updateSQL(modifier, getAppCommandContext()));
        return batch;
    }

    public static void start(Context context, ModifierGroupModel modifier){
        create(EditModifierGroupCommand.class).arg(ARG_MODIFIER, modifier).queueUsing(context);
    }
}