package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.KDSAliasModel;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by long.jiao on 6.7.16.
 */
public class EditKDSAliasCommand extends AsyncCommand{

    public static final String ARG_MODEL = "arg_model";

    private KDSAliasModel model;

    @Override
    protected TaskResult doCommand() {
        Logger.d("EditPrinterAliasCommand doCommand");
        model = (KDSAliasModel) getArgs().getSerializable(ARG_MODEL);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(ShopProvider.getContentUri(ShopStore.KDSAliasTable.URI_CONTENT))
                .withSelection(ShopStore.KDSAliasTable.GUID + " = ?", new String[]{model.guid})
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(model).updateSQL(model, getAppCommandContext());
    }

    public static void start(Context context, KDSAliasModel model){
        create(EditKDSAliasCommand.class).arg(ARG_MODEL, model).queueUsing(context);
    }
}
