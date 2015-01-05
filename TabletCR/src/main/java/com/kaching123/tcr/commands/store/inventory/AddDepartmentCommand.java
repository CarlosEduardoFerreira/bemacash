package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Bundle;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.DepartmentModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.UUID;

public class AddDepartmentCommand extends AsyncCommand {

	private static final String ARG_TITLE = "arg_title";
	private static final String RESULT_GUID = "result_guid";
	
	private DepartmentModel model;

	@Override
	protected TaskResult doCommand() {
        model = new DepartmentModel(
                UUID.randomUUID().toString(),
                getArgs().getString(ARG_TITLE));

		return succeeded().add(RESULT_GUID, model.guid);
	}

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newInsert(ShopProvider.getContentUri(DepartmentTable.URI_CONTENT))
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
	protected ISqlCommand createSqlCommand() {
		return JdbcFactory.insert(model, getAppCommandContext());
	}

	public static void start(Context context, String title){
		create(AddDepartmentCommand.class).arg(ARG_TITLE, title).queueUsing(context);
	}

    /** will be used in import. can be standalone **/
    public String sync(Context context, String title, IAppCommandContext appCommandContext){
        TaskResult result = syncStandalone(context, arg(title), appCommandContext);
        return isFailed(result) ? null : model.guid;
    }

    private static Bundle arg(String title){
        Bundle arg = new Bundle(1);
        arg.putString(ARG_TITLE, title);
        return arg;
    }
}
