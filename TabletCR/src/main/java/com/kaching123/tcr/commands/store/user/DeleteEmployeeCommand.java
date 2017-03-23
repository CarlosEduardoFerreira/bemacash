package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CustomerTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

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
        BatchSqlCommand batch = batchDelete(EmployeeTable.TABLE_NAME);
        batch.add(JdbcFactory.delete(EmployeeTable.TABLE_NAME, guid, getAppCommandContext()));
        new AtomicUpload().upload(batch, AtomicUpload.UploadType.WEB);
        return batch;
    }

    public static void start(Context context, String employeeGuid, DeleteEmployeeCallback callback) {
        create(DeleteEmployeeCommand.class).arg(ARG_EMPLOYEE_GUID, employeeGuid).callback(callback).queueUsing(context);
    }

    public static abstract class DeleteEmployeeCallback {
        @OnSuccess(DeleteEmployeeCommand.class)
        public void handleSuccess() {
            onSuccess();
        }

        @OnFailure(DeleteEmployeeCommand.class)
        public void handleFailure() {
            onFailure();
        }

        protected abstract void onSuccess();

        protected abstract void onFailure();

    }
}
