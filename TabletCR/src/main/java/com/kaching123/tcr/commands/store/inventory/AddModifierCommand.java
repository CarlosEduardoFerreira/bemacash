package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 10.12.13.
 */
public class AddModifierCommand extends AsyncCommand {

    private static final Uri URI_MODIFIERS = ShopProvider.getContentUri(ModifierTable.URI_CONTENT);

    private static final String ARG_MODIFIER= "arg_modifier";

    private ModifierModel modifier;

    @Override
    protected TaskResult doCommand() {
        Logger.d("AddModifierCommand doCommand");
        modifier = (ModifierModel) getArgs().getSerializable(ARG_MODIFIER);
        modifier.orderNum = ModifierModel.getMaxOrderNum(getContext(), modifier.type, modifier.itemGuid, modifier.modifierGroupGuid);
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newInsert(URI_MODIFIERS)
                .withValues(modifier.toValues())
                .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchInsert(modifier);
        batch.add(JdbcFactory.getConverter(modifier).insertSQL(modifier, getAppCommandContext()));
        return batch;
    }

    public static void start(Context context, ModifierModel modifier){
        create(AddModifierCommand.class)
                .arg(ARG_MODIFIER, modifier)
                .queueUsing(context);
    }
}
