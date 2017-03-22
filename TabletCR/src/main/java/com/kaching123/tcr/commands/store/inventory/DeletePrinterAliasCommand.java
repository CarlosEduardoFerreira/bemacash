package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.PrinterAliasJdbcConverter;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.kaching123.tcr.store.ShopStore.PrinterTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.util.ArrayList;

import static com.kaching123.tcr.model.SqlCommandHelper.getContentValues;

/**
 * Created by vkompaniets on 12.02.14.
 */
public class DeletePrinterAliasCommand extends AsyncCommand {

    private static final Uri URI_PRINTER_ALIAS = ShopProvider.getContentUri(PrinterAliasTable.URI_CONTENT);

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(ItemTable.URI_CONTENT);

    private static final Uri URI_PRINTER = ShopProvider.getContentUri(PrinterTable.URI_CONTENT);

    private static final String EXTRA_ALIAS_NAME = "EXTRA_ALIAS_NAME";
    private static final String EXTRA_ITEMS_COUNT = "EXTRA_ITEMS_COUNT";

    private static final String ARG_MODEL = "printer_alias_model";

    private PrinterAliasModel model;

    protected BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        model = (PrinterAliasModel) getArgs().getSerializable(ARG_MODEL);

        Cursor c = ProviderAction.query(URI_ITEMS)
                .projection(ItemTable.PRINTER_ALIAS_GUID)
                .where(ShopStore.ItemTable.PRINTER_ALIAS_GUID + " = ?", model.guid)
                .perform(getContext());

        int count = c.getCount();
        c.close();

        if (count > 0){
            return failed().add(EXTRA_ALIAS_NAME, model.alias).add(EXTRA_ITEMS_COUNT, count);
        }

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newUpdate(URI_ITEMS)
                .withSelection(ItemTable.PRINTER_ALIAS_GUID + " = ?", new String[]{model.guid})
                .withValue(ItemTable.PRINTER_ALIAS_GUID, null)
                .build());

        operations.add(ContentProviderOperation.newUpdate(URI_PRINTER)
                .withSelection(PrinterTable.ALIAS_GUID + " = ?", new String[]{model.guid})
                .withValue(PrinterTable.ALIAS_GUID, null)
                .build());

        operations.add(ContentProviderOperation.newUpdate(URI_PRINTER_ALIAS)
                .withValue(PrinterAliasTable.IS_DELETED, 1)
                .withSelection(PrinterAliasTable.GUID + " = ?", new String[]{model.guid})
                .build());

        return operations;
    }


    @Override
    protected ISqlCommand createSqlCommand() {

        PrinterAliasJdbcConverter printerAliasConverter = (PrinterAliasJdbcConverter) JdbcFactory.getConverter(PrinterAliasTable.TABLE_NAME);
        BatchSqlCommand sqlPrinterAlias = batchUpdate(model);
        sqlPrinterAlias.add(printerAliasConverter.deletePrinterAlias(model, getAppCommandContext()));
        new AtomicUpload().upload(sqlPrinterAlias, AtomicUpload.UploadType.WEB);

        ItemsJdbcConverter itemsJdbcConverter = (ItemsJdbcConverter) JdbcFactory.getConverter(ItemTable.TABLE_NAME);
        BatchSqlCommand sqlItem = batchUpdate(ItemModel.class);
        sqlItem.add(itemsJdbcConverter.removePrinterAlias(model.guid, getAppCommandContext()));
        new AtomicUpload().upload(sqlItem, AtomicUpload.UploadType.WEB);

        return sqlPrinterAlias;
    }


    public static void start(Context context, PrinterAliasModel model, BaseDeletePrinterAliasCallback callback){
        create(DeletePrinterAliasCommand.class).arg(ARG_MODEL, model).callback(callback).queueUsing(context);
    }

    public static abstract class BaseDeletePrinterAliasCallback{

        @OnSuccess(DeletePrinterAliasCommand.class)
        public void onSuccess() {
            onAliasDeleted();
        }

        @OnFailure(DeletePrinterAliasCommand.class)
        public void onFailure(@Param(EXTRA_ALIAS_NAME) String alias, @Param(EXTRA_ITEMS_COUNT) int itemsCount) {
            onAliasHasItems(alias, itemsCount);
        }

        protected abstract void onAliasDeleted();
        protected abstract void onAliasHasItems(String aliasName, int itemsCount);

    }

}
