package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 12.02.14.
 */
public class EditPrinterAliasCommand extends AsyncCommand{

    public static final String ARG_MODEL = "arg_model";

    private PrinterAliasModel model;

    @Override
    protected TaskResult doCommand() {
        model = (PrinterAliasModel) getArgs().getSerializable(ARG_MODEL);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(ShopProvider.getContentUri(PrinterAliasTable.URI_CONTENT))
                .withSelection(PrinterAliasTable.GUID + " = ?", new String[]{model.guid})
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(model).updateSQL(model, getAppCommandContext());
    }

    public static void start(Context context, PrinterAliasModel model){
        create(EditPrinterAliasCommand.class).arg(ARG_MODEL, model).queueUsing(context);
    }
}
