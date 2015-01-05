package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CustomerTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 20.02.14.
 */
public class DeleteEmployeeCommand extends AsyncCommand {

    private static final Uri EMPLOYEE_URI = ShopProvider.getContentUri(EmployeeTable.URI_CONTENT);

    private static final String ARG_EMPLOYEE_GUID = "ARG_EMPLOYEE_GUID";

    private String guid;

    @Override
    protected TaskResult doCommand() {
        guid = getStringArg(ARG_EMPLOYEE_GUID);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(EMPLOYEE_URI)
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(CustomerTable.GUID + " = ?", new String[]{guid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.delete(EmployeeTable.TABLE_NAME, guid, getAppCommandContext());
    }

    public static void start(Context context, String employeeGuid){
        create(DeleteEmployeeCommand.class).arg(ARG_EMPLOYEE_GUID, employeeGuid).queueUsing(context);
    }
}
