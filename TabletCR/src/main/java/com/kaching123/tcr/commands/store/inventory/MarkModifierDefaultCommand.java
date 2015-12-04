package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemsModifierGroupsJdbcConverter;
import com.kaching123.tcr.model.ModifierExModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created alboyko on 01.12.2015.
 */
public class MarkModifierDefaultCommand extends AsyncCommand {

    private static final Uri URI_MODIFIER_GROUP = ShopProvider.contentUri(ShopStore.ModifierGroupTable.URI_CONTENT);
    private static final Uri URI_ITEM = ShopProvider.contentUri(ItemTable.URI_CONTENT);

    private static final String ARG_MODIFIER = "arg_modifier";
    private static final String ARG_USE_AS_DEFAULT = "ARG_USE_AS_DEFAULT";
    private static final String ARG_RESET_DEFAULT_MODIFIER = "ARG_RESET_DEFAULT_MODIFIER";

    private ModifierExModel modifier;

    @Override
    protected TaskResult doCommand() {
        // Sync-stable pattern
        modifier = (ModifierExModel) getArgs().getSerializable(ARG_MODIFIER);
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        if (!TextUtils.isEmpty(modifier.modifierGroupGuid)) {
            // there is a group - we mod the group's default guid
            operations.add(ContentProviderOperation.newUpdate(URI_MODIFIER_GROUP)
                    .withSelection(ShopStore.ModifierGroupTable.GUID + " = ?", new String[]{modifier.modifierGroupGuid})
                    .withValues(modifier.toDefaultValues())
                    .build());
        } else {
            // there is no group as old way, we mod item's default guid
            ContentValues itemUpdateValues = new ContentValues();
            itemUpdateValues.put(ItemTable.DEFAULT_MODIFIER_GUID, modifier.modifierGuid);
            operations.add(ContentProviderOperation.newUpdate(URI_ITEM)
                    .withValues(itemUpdateValues)
                    .withSelection(ItemTable.GUID + " = ?", new String[]{modifier.itemGuid})
                    .build());
        }
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchUpdate(modifier);
        if (!TextUtils.isEmpty(modifier.modifierGroupGuid)) {
            ItemsModifierGroupsJdbcConverter itemConverter = (ItemsModifierGroupsJdbcConverter)JdbcFactory
                    .getConverter(ShopStore.ModifierGroupTable.TABLE_NAME);
            batch.add(itemConverter.setDefault(modifier.modifierGroupGuid, modifier.modifierGuid, getAppCommandContext()));
        } else {
            ItemsJdbcConverter itemConverter = (ItemsJdbcConverter) JdbcFactory.getConverter(ItemTable.TABLE_NAME);
            batch.add(itemConverter.updateDefaultModifierGuid(modifier.itemGuid, modifier.modifierGuid, getAppCommandContext()));
        }
        return batch;
    }

    public static void start(Context context, ModifierExModel modifier) {
        create(MarkModifierDefaultCommand.class).arg(ARG_MODIFIER, modifier).queueUsing(context);
    }
}