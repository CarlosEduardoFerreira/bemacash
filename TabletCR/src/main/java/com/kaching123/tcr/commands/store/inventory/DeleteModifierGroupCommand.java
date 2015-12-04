package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsModifiersJdbcConverter;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.kaching123.tcr.store.ShopStore.ModifierGroupTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;


/**
 * Created by Mayer
 */
public class DeleteModifierGroupCommand extends AsyncCommand {

    public static final String ARG_MODIFIER = "arg_modifier";

    private ModifierGroupModel modifier;

    @Override
    protected TaskResult doCommand() {
        modifier = (ModifierGroupModel) getArgs().getSerializable(ARG_MODIFIER);
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(ModifierGroupTable.URI_CONTENT))
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(ModifierGroupTable.GUID + " = ?", new String[]{modifier.guid})
                .build());
        operations.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(ModifierTable.URI_CONTENT))
                .withValues(ModifierModel.toClearGroupValue())
                .withSelection(ModifierTable.ITEM_GROUP_GUID + " = ?", new String[]{modifier.guid})
                .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchUpdate(ModifierTable.TABLE_NAME);

        ItemsModifiersJdbcConverter converter = (ItemsModifiersJdbcConverter) JdbcFactory.getConverter(ModifierTable.TABLE_NAME);

        batch.add(converter.clearGroups(modifier.guid, getAppCommandContext()));
        batch.add(JdbcFactory.delete(modifier, getAppCommandContext()));
        return batch;
    }

    public static void start(Context context, ModifierGroupModel modifier) {
        create(DeleteModifierGroupCommand.class).arg(ARG_MODIFIER, modifier).queueUsing(context);
    }
}
