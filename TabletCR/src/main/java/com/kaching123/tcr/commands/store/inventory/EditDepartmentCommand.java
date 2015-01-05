package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.DepartmentModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 17.12.13.
 */
public class EditDepartmentCommand extends AsyncCommand{

    public static final String ARG_MODEL = "arg_model";

    private DepartmentModel model;

    @Override
    protected TaskResult doCommand() {
        Logger.d("EditDepartmentCommand doCommand");
        model = (DepartmentModel) getArgs().getSerializable(ARG_MODEL);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(ShopProvider.getContentUri(DepartmentTable.URI_CONTENT))
                .withSelection(DepartmentTable.GUID + " = ?", new String[]{model.guid})
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(model).updateSQL(model, getAppCommandContext());
    }

    public static void start(Context context, DepartmentModel department){
        create(EditDepartmentCommand.class).arg(ARG_MODEL, department).queueUsing(context);
    }
}
