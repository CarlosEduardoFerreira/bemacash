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
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by mboychenko on 4/26/2017.
 */

public class CopyModifiersFromToCommand extends AsyncCommand {
    private static final Uri URI_MODIFIERS = ShopProvider.getContentUri(ShopStore.ModifierTable.URI_CONTENT);

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
        sql = batchInsert(ModifierModel.class);

        Cursor c = ProviderAction.query(URI_MODIFIERS)
                .where(ShopStore.ModifierTable.ITEM_GUID + " = ?", itemFrom)
                .perform(getContext());

        ArrayList<ModifierModel> modifierModels = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                modifierModels.add(new ModifierModel(c));
            }while (c.moveToNext());
            c.close();
        }

        JdbcConverter<ModifierModel> jdbc = JdbcFactory.getConverter(ShopStore.ModifierTable.TABLE_NAME);

        for (ModifierModel modifierModel : modifierModels) {
            modifierModel.modifierGuid = UUID.randomUUID().toString();
            modifierModel.itemGuid = itemTo;

            operations.add(ContentProviderOperation.newInsert(URI_MODIFIERS)
                    .withValues(modifierModel.toValues())
                    .build());
            sql.add(jdbc.insertSQL(modifierModel, this.getAppCommandContext()));
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
