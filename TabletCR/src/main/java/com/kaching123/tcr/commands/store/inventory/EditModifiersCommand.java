package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.converter.StringFunction;
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

    private ModifierModel modifier;

    @Override
    protected TaskResult doCommand() {
        Logger.d("EditModifierCommand doCommand");
        modifier = (ModifierModel) getArgs().getSerializable(ARG_MODIFIER);

        if (modifier.modifierGroupGuid != null){
            String currentGroupId = getCurrentGroupId(getContext(), modifier.modifierGuid);
            if (!modifier.modifierGroupGuid.equals(currentGroupId)){
                modifier.orderNum = ModifierModel.getMaxOrderNum(getContext(), modifier.type, modifier.itemGuid, modifier.modifierGroupGuid) + 1;
            }
        }

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newUpdate(URI_MODIFIERS)
                .withSelection(ModifierTable.MODIFIER_GUID + " = ?", new String[]{modifier.modifierGuid})
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

    private static String getCurrentGroupId(Context context, String modifierId){
        return ProviderAction.query(URI_MODIFIERS)
                .where(ModifierTable.MODIFIER_GUID + " = ?", modifierId)
                .projection(ModifierTable.ITEM_GROUP_GUID)
                .perform(context)
                .toFluentIterable(new StringFunction())
                .first().orNull();
    }

    public static void start(Context context, ModifierModel modifier){
        create(EditModifiersCommand.class)
                .arg(ARG_MODIFIER, modifier)
                .queueUsing(context);
    }
}