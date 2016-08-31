package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.converter.StringFunction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ModifierGroupTable;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Mayer
 */
public class DeleteModifierGroupCommand extends AsyncCommand {

    public static final String ARG_GROUP = "ARG_GROUP";

    private ModifierGroupModel group;
    private List<SyncResult> deleteResults;

    @Override
    protected TaskResult doCommand() {
        group = (ModifierGroupModel) getArgs().getSerializable(ARG_GROUP);
        List<String> modifiers = getModifiers(getContext(), group.guid);
        DeleteModifierCommand cmd = new DeleteModifierCommand();
        deleteResults = new ArrayList<>(modifiers.size());
        for (String id : modifiers){
            SyncResult result = cmd.sync(getContext(), new ModifierModel(id), getAppCommandContext());
            if (result == null)
                return failed();
            deleteResults.add(result);
        }
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(ModifierGroupTable.URI_CONTENT))
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(ModifierGroupTable.GUID + " = ?", new String[]{group.guid})
                .build());

        for (SyncResult deleteResult : deleteResults){
            operations.addAll(deleteResult.getLocalDbOperations());
        }

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchUpdate(ModifierTable.TABLE_NAME);
        batch.add(JdbcFactory.delete(group, getAppCommandContext()));

        for (SyncResult deleteResult : deleteResults){
            batch.add(deleteResult.getSqlCmd());
        }

        return batch;
    }

    private static List<String> getModifiers(Context context, String groupId){
        return ProviderAction.query(ShopProvider.contentUri(ModifierTable.URI_CONTENT))
                .projection(ModifierTable.MODIFIER_GUID)
                .where(ModifierTable.ITEM_GROUP_GUID + " = ?", groupId)
                .perform(context)
                .toFluentIterable(new StringFunction())
                .toImmutableList();

    }

    public static void start(Context context, ModifierGroupModel group) {
        create(DeleteModifierGroupCommand.class).arg(ARG_GROUP, group).queueUsing(context);
    }
}
