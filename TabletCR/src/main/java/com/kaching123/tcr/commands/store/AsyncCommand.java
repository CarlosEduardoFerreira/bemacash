package com.kaching123.tcr.commands.store;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.saleorder.UpdateSaleOrderCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.model.SqlCommandHelper;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class AsyncCommand extends PublicGroundyTask {

    private boolean isStandalone = true;
    private SyncResult syncResult;

    protected AsyncCommand() {
        super();
    }

    protected AsyncCommand(Context context, Bundle args) {
        super(context, args);
    }

    protected AsyncCommand(Context context, Bundle args, IAppCommandContext appCommandContext) {
        super(context, args, appCommandContext);
    }
    protected boolean isStandalone() {
        return isStandalone;
    }

    public SyncResult syncDependent(Context context, IAppCommandContext appCommandContext) {
        return syncDependent(context, null, appCommandContext);
    }

    public SyncResult syncDependent(Context context, Bundle args, IAppCommandContext appCommandContext) {
		syncResult = new SyncResult();
        isStandalone = false;
        TaskResult result = super.sync(context, args, appCommandContext);
        isStandalone = true;
        return isFailed(result) ? null : syncResult;
    }

    public TaskResult syncStandalone(Context context, IAppCommandContext appCommandContext) {
        return syncStandalone(context, null, appCommandContext);
    }

    public TaskResult syncStandalone(Context context, Bundle args, IAppCommandContext appCommandContext) {
        return super.sync(context, args, appCommandContext);
    }

    public void afterDoCommand() {

    }

    @Override
    protected TaskResult doInBackground() {
        if (validateAppCommandContext() && !isAppCommandContextValid(getAppCommandContext()))
            return failed();

        TaskResult result = doCommand();
        if (isFailed(result)) {
            return result;
        }

        ContentProviderResult[] dbOperationResults = null;
        try {
            dbOperationResults = handleDbOperations(createDbOperations(), handleSqlCommand(createSqlCommand()));
            /*
            if (this instanceof UpdateSaleOrderCommand) {
                ContentProviderResult[] tempOperations = handleDbOperations(createDbOperations(),
                        handleSqlCommand(((UpdateSaleOrderCommand) this).createSqlAdditional()));

                if (dbOperationResults != null && tempOperations != null) {
                    ContentProviderResult[] newOperations = Arrays.copyOf(dbOperationResults, dbOperationResults.length + tempOperations.length);
                    System.arraycopy(tempOperations, 0, newOperations, dbOperationResults.length, tempOperations.length);
                    dbOperationResults = newOperations;

                } else if (dbOperationResults == null) {
                    dbOperationResults = tempOperations;
                }

            }
            /**/

        } catch (Exception e) {
            Logger.e("handleDbOperations exception", e);
            return failed();
        }

        afterCommand(dbOperationResults);

        return result;
    }

    protected boolean validateAppCommandContext() {
        return true;
    }

    protected boolean isAppCommandContextValid(IAppCommandContext appCommandContext) {
        if (appCommandContext.getShopId() == 0L) {
            Logger.e(this.getClass().getSimpleName() + ".isAppCommandContextValid(): failed, no SHOP_ID!", new RuntimeException());
            return false;
        }
        if (appCommandContext.getRegisterId() == 0L) {
            Logger.e(this.getClass().getSimpleName() + ".isAppCommandContextValid(): failed, no REGISTER_ID!", new RuntimeException());
            return false;
        }
        if (TextUtils.isEmpty(appCommandContext.getEmployeeGuid())) {
            Logger.e(this.getClass().getSimpleName() + ".isAppCommandContextValid(): failed, no EMPLOYEE_GUID!", new RuntimeException());
            return false;
        }

        return true;
    }

    /**
     * parse sql actions to store it to the local database
     *
     * @param sqlCmd
     */
    private ContentProviderOperation[] handleSqlCommand(ISqlCommand sqlCmd) {
        if (getApp().isTrainingMode())
            return null;

        if (sqlCmd == null)
            return null;

        if (isSync() && !isStandalone()) {
            syncResult.sqlCmd = sqlCmd;
            return null;
        }

        ContentProviderOperation[] operations;
        if (sqlCmd instanceof SingleSqlCommand) {
            operations = SqlCommandHelper.addSqlCommand((SingleSqlCommand) sqlCmd);
        } else {
            operations = SqlCommandHelper.addSqlCommands((BatchSqlCommand) sqlCmd);
        }

        return operations;
    }

    /**
     * Store local and external changes to the local database
     *
     * @param localDbOperations
     * @param externalDbOperations
     * @throws android.os.RemoteException
     * @throws android.content.OperationApplicationException
     */
    private ContentProviderResult[] handleDbOperations(ArrayList<ContentProviderOperation> localDbOperations, ContentProviderOperation[] externalDbOperations) throws RemoteException, OperationApplicationException {

        if (isSync() && !isStandalone()) {
            syncResult.localDbOperations = localDbOperations;
            return null;
        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        if (localDbOperations != null)
            operations.addAll(localDbOperations);

        if (externalDbOperations != null) {
            operations.add(externalDbOperations[0]);
            operations.add(externalDbOperations[1]);
        }

        if (operations.isEmpty()) {
            return null;
        }

        try {
            return getContext().getContentResolver().applyBatch(ShopProvider.AUTHORITY, operations);
        } catch (SQLiteConstraintException e) {
            Logger.e("AsyncCommand: failed to store the data, operations: " + operations, e);
            throw e;
        }
    }

    /**
     * Do something logic
     * @return
     */
    protected abstract TaskResult doCommand();

    /**
     * Do something after database operations
     * @return
     */
    protected void afterCommand(ContentProviderResult[] dbOperationResults) {

    }

    /**
     * prepare sql command to insert to external storage
     * @return
     */
    protected abstract ISqlCommand createSqlCommand();

    /**
     * prepare local changes to insert to local storage
     *
     * @return
     */
    protected abstract ArrayList<ContentProviderOperation> createDbOperations();


    protected BatchSqlCommand batchDelete(String table) {
        return new BatchSqlCommand(JdbcFactory.METHOD_DELETE + JdbcFactory.getApiMethod(table));
    }

    protected <T extends IValueModel> BatchSqlCommand batchDelete(T model) {
        return new BatchSqlCommand(JdbcFactory.METHOD_DELETE + JdbcFactory.getApiMethod(model));
    }

    protected <T extends IValueModel> BatchSqlCommand batchDelete(Class<T> clazz) {
        return new BatchSqlCommand(JdbcFactory.METHOD_DELETE + JdbcFactory.getApiMethod(clazz));
    }

    protected BatchSqlCommand batchUpdate(String tableName) {
        return new BatchSqlCommand(JdbcFactory.METHOD_UPDATE + JdbcFactory.getApiMethod(tableName));
    }

    protected <T extends IValueModel> BatchSqlCommand batchUpdate(T model) {
        return new BatchSqlCommand(JdbcFactory.METHOD_UPDATE + JdbcFactory.getApiMethod(model));
    }

    protected <T extends IValueModel> BatchSqlCommand batchUpdate(Class<T> clazz) {
        return new BatchSqlCommand(JdbcFactory.METHOD_UPDATE + JdbcFactory.getApiMethod(clazz));
    }

    protected BatchSqlCommand batchInsert(String tableName) {
        return new BatchSqlCommand(JdbcFactory.METHOD_ADD + JdbcFactory.getApiMethod(tableName));
    }

    protected <T extends IValueModel> BatchSqlCommand batchInsert(T model) {
        return new BatchSqlCommand(JdbcFactory.METHOD_ADD + JdbcFactory.getApiMethod(model));
    }

    protected <T extends IValueModel> BatchSqlCommand batchInsert(Class<T> clazz) {
        return new BatchSqlCommand(JdbcFactory.METHOD_ADD + JdbcFactory.getApiMethod(clazz));
    }

    /**
     * Represents result of sub-command
     */
    public static class SyncResult {

        private ISqlCommand sqlCmd;
        private ArrayList<ContentProviderOperation> localDbOperations;

        public SyncResult() {}

        protected SyncResult(ISqlCommand sqlCmd, ArrayList<ContentProviderOperation> localDbOperations) {
            this.sqlCmd = sqlCmd;
            this.localDbOperations = localDbOperations;
        }

        public ISqlCommand getSqlCmd() {
            return sqlCmd;
        }

        public ArrayList<ContentProviderOperation> getLocalDbOperations() {
            return localDbOperations;
        }

    }

}
