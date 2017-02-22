package com.kaching123.tcr.commands;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Map;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.activity.SuperBaseActivity;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.model.SqlCommandHelper;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;

import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by Rodrigo Busata on 07/10/16.
 */
public abstract class AsyncTaskCommand<T extends IValueModel> extends AsyncTask<T, Integer, Boolean> {

    private SuperBaseActivity mContext;
    private T mModel;
    private Uri mUri;
    private boolean mIsInsert;
    private boolean mIsDeleting;
    private CallBackTask mCallBack;
    private ContentValues mLocalValues;
    private Map<String, Object> mValues;

    public AsyncTaskCommand(SuperBaseActivity context, Uri uri, boolean isInsert, CallBackTask callBack) {
        this(context, uri, isInsert, callBack, false, null, null);
    }

    public AsyncTaskCommand(SuperBaseActivity context, Uri uri, boolean isInsert, CallBackTask callBack, boolean isDeleting) {
        this(context, uri, isInsert, callBack, isDeleting, null, null);
    }

    public AsyncTaskCommand(SuperBaseActivity context, Uri uri, boolean isInsert, CallBackTask callBack,
                            boolean isDeleting, ContentValues localValues, Map<String, Object> values) {
        mContext = context;
        mUri = uri;
        mCallBack = callBack;
        mIsDeleting = isDeleting;
        mIsInsert = isInsert;
        mLocalValues = localValues;
        mValues = values;
    }

    @Override
    protected Boolean doInBackground(T... params) {
        mModel = params[0];

        try {
            handleDbOperations(createDbOperations(), handleSqlCommand(createSqlCommand()));

        } catch (Exception e) {
            Logger.e("handleDbOperations exception", e);

            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCallBack != null) mCallBack.failed();
                }
            });
            return false;
        }

        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCallBack != null) mCallBack.success(mModel, mIsInsert);
            }
        });
        return true;
    }

    private ContentProviderOperation[] handleSqlCommand(ISqlCommand sqlCmd) {
        if (mContext.getApp().isTrainingMode())
            return null;

        if (sqlCmd == null)
            return null;

        ContentProviderOperation[] operations;
        if (sqlCmd instanceof SingleSqlCommand) {
            operations = SqlCommandHelper.addSqlCommand((SingleSqlCommand) sqlCmd);
        } else {
            operations = SqlCommandHelper.addSqlCommands((BatchSqlCommand) sqlCmd);
        }

        return operations;
    }

    private ContentProviderResult[] handleDbOperations(ArrayList<ContentProviderOperation> localDbOperations, ContentProviderOperation[] externalDbOperations) throws RemoteException, OperationApplicationException {

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
            return mContext.getContentResolver().applyBatch(ShopProvider.AUTHORITY, operations);

        } catch (SQLiteConstraintException e) {
            Logger.e("AsyncCommand: failed to store the data, operations: " + operations, e);
            throw e;
        }
    }

    protected ISqlCommand createSqlCommand() {
        if (mIsDeleting) {
            return JdbcFactory.getConverter(mModel).deleteSQL(mModel, null);

        } else {
            if (mIsInsert) {
                return JdbcFactory.getConverter(mModel).insertSQL(mModel, null);

            } else {
                if (mValues != null){
                    JdbcBuilder builder = _update(JdbcFactory.getConverter(mModel).getTableName(), null);
                    for (String key : mValues.keySet()){
                        builder.add(key, mValues.get(key));
                    }
                    return builder.where(JdbcFactory.getConverter(mModel).getGuidColumn(), mModel.getGuid())
                            .build(JdbcFactory.getApiMethod(mModel));

                }
                return JdbcFactory.getConverter(mModel).updateSQL(mModel, null);
            }
        }
    }

    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        if (mIsDeleting) {
            ContentValues v = ShopStore.DELETE_VALUES;

            if (JdbcFactory.getConverter(mModel).supportUpdateTimeLocalFlag()) {
                v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, mContext.getApp().getCurrentServerTimestamp());
            }

            operations.add(ContentProviderOperation.newUpdate(mUri)
                    .withValues(v)
                    .withSelection(JdbcFactory.getConverter(mModel).getLocalGuidColumn() + " = ?", new String[]{mModel.getGuid()})
                    .build());

        } else {
            if (mIsInsert) {
                ContentValues values = mModel.toValues();
                operations.add(ContentProviderOperation.newInsert(mUri)
                        .withValues(values).build());

            } else {
                operations.add(ContentProviderOperation.newUpdate(mUri)
                        .withValues(mLocalValues != null ? mLocalValues : mModel.toValues())
                        .withSelection(JdbcFactory.getConverter(mModel).getLocalGuidColumn() + " = ?", new String[]{mModel.getGuid()})
                        .build());
            }
        }

        return operations;
    }

    static public abstract class CallBackTask<T extends IValueModel> {

        protected abstract void success(T model, boolean isInsert);

        protected abstract void failed();
    }
}
