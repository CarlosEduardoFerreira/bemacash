package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 10.12.13.
 */
public class EditModifiersCommand extends AsyncCommand {

    private static final Uri URI_MODIFIERS = ShopProvider.getContentUri(ModifierTable.URI_CONTENT);
    private static final Uri URI_ITEM = ShopProvider.getContentUri(ShopStore.ItemTable.URI_CONTENT);

    private static final String ARG_MODIFIER = "arg_modifier";
    private static final String ARG_USE_AS_DEFAULT = "ARG_USE_AS_DEFAULT";
    private static final String ARG_RESET_DEFAULT_MODIFIER = "ARG_RESET_DEFAULT_MODIFIER";

    private ModifierModel modifier;
    private boolean useAsDefault;
    private boolean resetDefaultModifier;

    @Override
    protected TaskResult doCommand() {
        Logger.d("EditModifierCommand doCommand");
        modifier = (ModifierModel) getArgs().getSerializable(ARG_MODIFIER);
        useAsDefault = getBooleanArg(ARG_USE_AS_DEFAULT);
        resetDefaultModifier = getBooleanArg(ARG_RESET_DEFAULT_MODIFIER);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newUpdate(URI_MODIFIERS)
                .withSelection(ModifierTable.MODIFIER_GUID + " = ?", new String[]{modifier.modifierGuid})
                .withValues(modifier.toValues())
                .build());

        ContentValues itemUpdateValues = new ContentValues();
        if(useAsDefault){
            itemUpdateValues.put(ShopStore.ItemTable.DEFAULT_MODIFIER_GUID, modifier.modifierGuid);
        }else if(resetDefaultModifier){
            itemUpdateValues.putNull(ShopStore.ItemTable.DEFAULT_MODIFIER_GUID);
        }
        if(useAsDefault || resetDefaultModifier){
            operations.add(ContentProviderOperation.newUpdate(URI_ITEM)
                    .withValues(itemUpdateValues)
                    .withSelection(ShopStore.ItemTable.GUID + " = ?", new String[]{modifier.itemGuid})
                    .build());
        }

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        ItemsJdbcConverter itemConverter = (ItemsJdbcConverter)JdbcFactory.getConverter(ShopStore.ItemTable.TABLE_NAME);

        BatchSqlCommand batch = batchUpdate(modifier);
        batch.add(JdbcFactory.getConverter(modifier).updateSQL(modifier, getAppCommandContext()));
        if(useAsDefault){
            batch.add(itemConverter.updateDefaultModifierGuid(modifier.itemGuid, modifier.modifierGuid, getAppCommandContext()));
        }else if(resetDefaultModifier){
            batch.add(itemConverter.updateDefaultModifierGuid(modifier.itemGuid, null, getAppCommandContext()));
        }
        return batch;
    }

    public static void start(Context context, ModifierModel modifier, boolean useAsDefault, boolean resetDefaultModifier){
        create(EditModifiersCommand.class)
                .arg(ARG_MODIFIER, modifier)
                .arg(ARG_RESET_DEFAULT_MODIFIER, resetDefaultModifier)
                .arg(ARG_USE_AS_DEFAULT, useAsDefault)
                .queueUsing(context);
    }
}