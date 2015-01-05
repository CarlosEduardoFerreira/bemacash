package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 10.12.13.
 */
public class DeleteModifierCommand extends AsyncCommand {

    public static final String ARG_MODIFIER = "arg_modifier";

    private ModifierModel modifier;

    @Override
    protected TaskResult doCommand() {
        Logger.d("DeleteModifierCommand doCommand");
        modifier = (ModifierModel) getArgs().getSerializable(ARG_MODIFIER);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(ShopProvider.getContentUri(ModifierTable.URI_CONTENT))
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(ModifierTable.MODIFIER_GUID + " = ?", new String[]{modifier.modifierGuid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.update(modifier, getAppCommandContext());
    }

    public static void start(Context context, ModifierModel modifier){
        create(DeleteModifierCommand.class).arg(ARG_MODIFIER, modifier).queueUsing(context);
    }
}
