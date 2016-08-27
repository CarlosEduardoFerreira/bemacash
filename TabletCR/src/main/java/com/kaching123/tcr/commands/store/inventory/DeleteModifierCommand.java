package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.converter.ModifierFunction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vkompaniets on 10.12.13.
 */
public class DeleteModifierCommand extends AsyncCommand {

    public static final String ARG_MODIFIER = "arg_modifier";

    private ModifierModel modifier;
    private List<SyncResult> shiftModifiersResults;

    @Override
    protected TaskResult doCommand() {
        Logger.d("DeleteModifierCommand doCommand");
        if (modifier == null) {
            modifier = (ModifierModel) getArgs().getSerializable(ARG_MODIFIER);
        }

        List<ModifierModel> modifiers2Shift = getModifiers2Shift(getContext(), modifier);
        UpdateModifierOrderNumCommand cmd = new UpdateModifierOrderNumCommand();
        shiftModifiersResults = new ArrayList<>();
        for (ModifierModel mod : modifiers2Shift){
            SyncResult result = cmd.syncDependent(getContext(), mod.modifierGuid, mod.orderNum - 1, getAppCommandContext());
            if (result == null)
                return failed();
            shiftModifiersResults.add(result);
        }

        return succeeded();
    }

    private static List<ModifierModel> getModifiers2Shift(Context context, ModifierModel modifier){
        Query query = ProviderAction.query(ShopProvider.contentUri(ModifierTable.URI_CONTENT))
                .orderBy(ModifierTable.ORDER_NUM)
                .where(ModifierTable.ITEM_GUID + " = ?", modifier.itemGuid)
                .where(ModifierTable.TYPE + " = ?", modifier.type.ordinal())
                .where(ModifierTable.ORDER_NUM + " > ?", modifier.orderNum);
        if (modifier.modifierGroupGuid != null)
            query.where(ModifierTable.ITEM_GROUP_GUID + " = ?", modifier.modifierGroupGuid);

        return query.perform(context).toFluentIterable(new ModifierFunction()).toImmutableList();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(ModifierTable.URI_CONTENT))
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(ModifierTable.MODIFIER_GUID + " = ?", new String[]{modifier.modifierGuid})
                .build());
        for (SyncResult syncResult : shiftModifiersResults){
            operations.addAll(syncResult.getLocalDbOperations());
        }
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand sql = batchDelete(modifier);
        sql.add(JdbcFactory.delete(modifier, getAppCommandContext()));
        for (SyncResult syncResult : shiftModifiersResults){
            sql.add(syncResult.getSqlCmd());
        }
        return sql;
    }

    public static void start(Context context, ModifierModel modifier){
        create(DeleteModifierCommand.class).arg(ARG_MODIFIER, modifier).queueUsing(context);
    }

    public SyncResult sync(Context context, ModifierModel model, IAppCommandContext appCommandContext) {
        this.modifier = model;
        return syncDependent(context, appCommandContext);
    }
}
