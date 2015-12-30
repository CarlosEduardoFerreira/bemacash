package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.collect.FluentIterable;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.converter.ModifierFunction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by gdubina on 13.12.13.
 */
public class CopyModifiersCommand extends AsyncCommand {

    private static final Uri URI = ShopProvider.getContentUri(ModifierTable.URI_CONTENT);

    private static final String ARG_MODIFIERS = "ARG_MODIFIERS";
    private static final String ARG_ITEM = "ARG_ITEM";

    private ArrayList<String> modifiersGuids;
    private String itemGuid;

    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        modifiersGuids = getArgs().getStringArrayList(ARG_MODIFIERS);
        itemGuid = getStringArg(ARG_ITEM);

        if (modifiersGuids == null || modifiersGuids.isEmpty()) {
            return succeeded();
        }

        operations = new ArrayList<ContentProviderOperation>();
        sql = batchInsert(ModifierModel.class);

        JdbcConverter<ModifierModel> jdbc = JdbcFactory.getConverter(ModifierTable.TABLE_NAME);
        FluentIterable<ModifierModel> models = ProviderAction.query(URI)
                .whereIn(ModifierTable.MODIFIER_GUID, modifiersGuids)
                .perform(getContext())
                .toFluentIterable(new ModifierFunction());
        //.toImmutableList();

        for (ModifierModel m : models) {
            ModifierModel newModel = new ModifierModel(
                    UUID.randomUUID().toString(),
                    itemGuid,
                    m.type,
                    m.title,
                    m.cost,
                    m.childItemGuid,
                    m.childItemQty,
                    null
            );
            operations.add(ContentProviderOperation.newInsert(URI)
                    .withValues(newModel.toValues())
                    .build());
            sql.add(jdbc.insertSQL(newModel, this.getAppCommandContext()));
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

    public static void start(Context context, String itemGuid, ArrayList<String> modifiersGuids){
        create(CopyModifiersCommand.class).arg(ARG_ITEM, itemGuid).arg(ARG_MODIFIERS, modifiersGuids).queueUsing(context);
    }
}
