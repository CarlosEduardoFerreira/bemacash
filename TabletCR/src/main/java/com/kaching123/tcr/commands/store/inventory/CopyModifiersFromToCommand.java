package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by mboychenko on 4/26/2017.
 */

public class CopyModifiersFromToCommand extends AsyncCommand {
    private static final Uri URI_MODIFIERS = ShopProvider.getContentUri(ShopStore.ModifierTable.URI_CONTENT);
    private static final Uri URI_MODIFIERS_GROUP = ShopProvider.contentUri(ShopStore.ModifierGroupTable.URI_CONTENT);

    private static final String ARG_ITEM_FROM = "ARG_ITEM_FROM";
    private static final String ARG_ITEM_TO = "ARG_ITEM_TO";

    private String itemFrom;
    private String itemTo;

    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        itemFrom = getStringArg(ARG_ITEM_FROM);
        itemTo = getStringArg(ARG_ITEM_TO);

        operations = new ArrayList<ContentProviderOperation>();
        sql = batchInsert(ModifierGroupModel.class);

        Cursor groupCursor = ProviderAction.query(URI_MODIFIERS_GROUP)
                .where(ShopStore.ModifierGroupTable.ITEM_GUID + " = ?", itemFrom)
                .perform(getContext());

        ArrayList<ModifierGroupModel> modifGroups = new ArrayList<>();
        if (groupCursor != null && groupCursor.moveToFirst()) {
            do {
                modifGroups.add(new ModifierGroupModel(groupCursor));
            }while (groupCursor.moveToNext());
            groupCursor.close();
        }

        Cursor modifCursor = ProviderAction.query(URI_MODIFIERS)
                .where(ShopStore.ModifierTable.ITEM_GUID + " = ?", itemFrom)
                .perform(getContext());

        ArrayList<ModifierModel> modifierModels = new ArrayList<>();
        if (modifCursor != null && modifCursor.moveToFirst()) {
            do {
                modifierModels.add(new ModifierModel(modifCursor));
            }while (modifCursor.moveToNext());
            modifCursor.close();
        }

        HashMap<String, String> modifGroupOldToNewGuids = new HashMap<>();

        for (ModifierGroupModel modifGroup : modifGroups) {
            modifGroupOldToNewGuids.put(modifGroup.guid, UUID.randomUUID().toString());
            modifGroup.guid = modifGroupOldToNewGuids.get(modifGroup.guid);
            modifGroup.itemGuid = itemTo;
            modifGroup.orderNum = ModifierGroupModel.getMaxOrderNum(getContext(), modifGroup.itemGuid) + 1;
            operations.add(ContentProviderOperation.newInsert(URI_MODIFIERS_GROUP)
                    .withValues(modifGroup.toValues())
                    .build());
            sql.add(JdbcFactory.getConverter(modifGroup).insertSQL(modifGroup, getAppCommandContext()));
        }

        for (ModifierModel modifierModel : modifierModels) {
            modifierModel.modifierGuid = UUID.randomUUID().toString();
            modifierModel.itemGuid = itemTo;
            modifierModel.modifierGroupGuid = modifGroupOldToNewGuids.get(modifierModel.modifierGroupGuid);
            modifierModel.orderNum = ModifierModel.getMaxOrderNum(getContext(), modifierModel.type, modifierModel.itemGuid, modifierModel.modifierGroupGuid) + 1;
            operations.add(ContentProviderOperation.newInsert(URI_MODIFIERS)
                    .withValues(modifierModel.toValues())
                    .build());
            sql.add(JdbcFactory.getConverter(modifierModel).insertSQL(modifierModel, getAppCommandContext()));
        }

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sql;
    }

    public static void start(Context context, String itemFrom, String itemTo){
        create(CopyModifiersFromToCommand.class)
                .arg(ARG_ITEM_FROM, itemFrom)
                .arg(ARG_ITEM_TO, itemTo)
                .queueUsing(context);
    }
}
