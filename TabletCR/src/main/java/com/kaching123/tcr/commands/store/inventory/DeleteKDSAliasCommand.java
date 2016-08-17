package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.model.KDSAliasModel;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.kaching123.tcr.store.ShopStore.PrinterTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 12.02.14.
 */
public class DeleteKDSAliasCommand extends AsyncCommand {

    private static final Uri URI_KDS_ALIAS = ShopProvider.getContentUri(ShopStore.KDSAliasTable.URI_CONTENT);

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    private static final Uri URI_KDS = ShopProvider.getContentUri(ShopStore.KDSTable.URI_CONTENT);

    private static final String ARG_MODEL = "kds_alias_model";

    private KDSAliasModel model;


    @Override
    protected TaskResult doCommand() {
        model = (KDSAliasModel) getArgs().getSerializable(ARG_MODEL);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newUpdate(URI_ITEMS)
                .withSelection(ItemTable.KDS_ALIAS_GUID + " = ?", new String[]{model.guid})
                .withValue(ItemTable.KDS_ALIAS_GUID, null)
                .build());

        operations.add(ContentProviderOperation.newUpdate(URI_KDS)
                .withSelection(PrinterTable.ALIAS_GUID + " = ?", new String[]{model.guid})
                .withValue(PrinterTable.ALIAS_GUID, null)
                .build());

        operations.add(ContentProviderOperation.newUpdate(URI_KDS_ALIAS)
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(PrinterAliasTable.GUID + " = ?", new String[]{model.guid})
                .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        /** create SQL batch **/
        ItemsJdbcConverter converter = (ItemsJdbcConverter) JdbcFactory.getConverter(ItemTable.TABLE_NAME);
        BatchSqlCommand batchSqlCommand = batchDelete(model);
        batchSqlCommand.add(converter.removePrinterAlias(model.guid, getAppCommandContext()));
        batchSqlCommand.add(JdbcFactory.getConverter(model).deleteSQL(model, getAppCommandContext()));
        return batchSqlCommand;
    }

    public static void start(Context context, KDSAliasModel model){
        create(DeleteKDSAliasCommand.class).arg(ARG_MODEL, model).queueUsing(context);
    }

}
