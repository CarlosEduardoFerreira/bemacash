package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by vkompaniets on 12.02.14.
 */
public class AddPrinterAliasCommand extends AsyncCommand {

    private static final String ARG_TITLE = "arg_title";

    private PrinterAliasModel model;

    @Override
    protected TaskResult doCommand() {
        model = new PrinterAliasModel(
                UUID.randomUUID().toString(),
                getArgs().getString(ARG_TITLE)
        );

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newInsert(ShopProvider.getContentUri(PrinterAliasTable.URI_CONTENT))
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {

        BatchSqlCommand sqlPrinterAlias = batchInsert(model);
        sqlPrinterAlias.add(JdbcFactory.insert(model, getAppCommandContext()));
        new AtomicUpload().upload(sqlPrinterAlias, AtomicUpload.UploadType.WEB);

        return sqlPrinterAlias;
    }

    public static void start(Context context, String title){
        create(AddPrinterAliasCommand.class).arg(ARG_TITLE, title).queueUsing(context);
    }
}
