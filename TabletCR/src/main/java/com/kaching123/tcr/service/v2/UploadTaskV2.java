package com.kaching123.tcr.service.v2;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder.UploadCommand;
import com.kaching123.tcr.commands.rest.sync.v1.UploadResponseV1;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SqlCommandTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UploadTaskV2 {

    protected static final Uri URI_SQL_COMMAND_NO_NOTIFY = ShopProvider.contentUriNoNotify(SqlCommandTable.URI_CONTENT);
    public static final int BATCH_SIZE = 100;
    public static final String CMD_START_TRANSACTION = "start_transaction";
    public static final String CMD_END_TRANSACTION = "end_transaction";
    public static final String CMD_START_EMPLOYEE = "start_employee";
    public static final String CMD_END_EMPLOYEE = "end_employee";
    private EmployeeModel employee;
    public static String ACTION_EMPLOYEE_UPLOAD_FAILED = "com.kaching123.tcr.service.ACTION_EMPLOYEE_UPLOAD_FAILED";
    public static String ACTION_EMPLOYEE_UPLOAD_COMPLETED = "com.kaching123.tcr.service.ACTION_EMPLOYEE_UPLOAD_COMPLETED";
    public static String EXTRA_SUCCESS = "success";
    public static String EXTRA_ERROR_CODE = "EXTRA_ERROR_CODE";
    static ContentValues sentValues = new ContentValues();

    static {
        sentValues.put(SqlCommandTable.IS_SENT, 1);
    }

    public UploadTaskV2() {

    }

    public UploadTaskV2(EmployeeModel employeeModel) {
        this.employee = employeeModel;
    }

    public boolean webApiUpload(ContentResolver cr, Context context) throws TransactionNotFinalizedException, SyncCommand.SyncLockedException {

        Log.d("BemaCarl","UploadTaskV2.webApiUpload.cr: " + cr);
        if(!new AtomicUpload().hasInternetConnection()){
            return false;
        }

        if (TcrApplication.get().isTrainingMode())
            return true;

        boolean errorsOccurred = false;

        Cursor c = cr.query(URI_SQL_COMMAND_NO_NOTIFY, new String[]{SqlCommandTable.ID, SqlCommandTable.SQL_COMMAND},
                SqlCommandTable.IS_SENT + " = ?", new String[]{"0"},
                SqlCommandTable.ID);
        ArrayList<UploadCommand> commands = new ArrayList<UploadCommand>(BATCH_SIZE);
        try {
            while (c.moveToNext()) {
                final long id = c.getLong(0);
                String json = c.getString(1);
                Log.d("BemaCarl","UploadTaskV2.employeeUpload.json: " + json);
                Logger.d("[CMD_TABLE] %d = %s", id, json);
                if (CMD_START_TRANSACTION.equals(json) || CMD_START_EMPLOYEE.equals(json)) {
                    Logger.d("START TRANSACTION");
                    ArrayList<Long> subIds = new ArrayList<Long>();
                    //read transaction
                    BatchSqlCommand batch = null;
                    long endTransactionId = 0;
                    while (c.moveToNext()) {
                        long subId = c.getLong(0);
                        subIds.add(subId);
                        String subJson = c.getString(1);
                        Logger.d("[CMD_TABLE] %d = %s", subId, subJson);
                        //TODO need to verify end
                        Log.d("BemaCarl", "UploadTask2.webApiUpload.json|subJson: |" + json +"|"+ subJson + "|");
                        if ((CMD_START_TRANSACTION.equals(json) && CMD_END_TRANSACTION.equals(subJson)) || (CMD_START_EMPLOYEE.equals(json) && CMD_END_EMPLOYEE.equals(subJson))) {
                            endTransactionId = subId;
                            Logger.d("END TRANSACTION");
                            break;
                        }
                        try {
                            if (batch == null) {
                                batch = BatchSqlCommand.fromJson(subJson);
                            } else {
                                batch.add(BatchSqlCommand.fromJson(subJson));
                            }
                        } catch (JSONException e) {
                            Logger.e("can't parse command: " + subJson, e);
                            throw new IllegalArgumentException("can't parse command: " + subJson, e);
                            // TODO send log
                        }
                    }
                    if (batch == null && endTransactionId != 0) {
                        //need to delete start and end transaction
                        cr.update(URI_SQL_COMMAND_NO_NOTIFY, sentValues, SqlCommandTable.ID + " = ? OR " + SqlCommandTable.ID + " = ?", new String[]{String.valueOf(id), String.valueOf(endTransactionId)});
                    } else if (batch == null || endTransactionId == 0) {
                        Logger.e("transaction is not finalized!");
                        throw new TransactionNotFinalizedException("transaction is not finalized!");
                    } else {
                        String transactionCmd = batch.toJson();
                        Logger.d("TRANSACTION_RESULT: %s", transactionCmd);
                        commands.add(new UploadCommand(id, transactionCmd, subIds));
                    }
                } else {
                    commands.add(new UploadCommand(id, json));
                }
                if (commands.size() == BATCH_SIZE) {
                    boolean uploaded = try2Upload(cr, commands, context);
                    commands.clear();
                    if (!uploaded) {
                        errorsOccurred = true;
                        break;
                    }
                }
            }
        } finally {
            c.close();
        }

        if (!commands.isEmpty()) {
            errorsOccurred = !try2Upload(cr, commands, context);
        }
        return !errorsOccurred;
    }

    public boolean employeeUpload(ContentResolver cr, Context context) throws SyncCommand.SyncLockedException {

        Log.d("BemaCarl","UploadTaskV2.employeeUpload.cr: " + cr);
        if(!new AtomicUpload().hasInternetConnection()){
            return false;
        }

        if (TcrApplication.get().isTrainingMode())
            return true;

        boolean errorsOccurred = false;

        Cursor c = cr.query(URI_SQL_COMMAND_NO_NOTIFY, new String[]{SqlCommandTable.ID, SqlCommandTable.SQL_COMMAND},
                SqlCommandTable.IS_SENT + " = ?", new String[]{"0"},
                SqlCommandTable.ID);
        ArrayList<UploadCommand> commands = new ArrayList<UploadCommand>(BATCH_SIZE);
        try {
            while (c.moveToNext()) {
                boolean emloyee_upload_end = false;
                final long id = c.getLong(0);
                String json = c.getString(1);
                Log.d("BemaCarl","UploadTaskV2.employeeUpload.json: " + json);
                Logger.d("[CMD_TABLE] %d = %s", id, json);
                if (CMD_START_EMPLOYEE.equals(json)) {
                    Logger.d("START EMPLOYEE UPLOAD");
                    ArrayList<Long> subIds = new ArrayList<Long>();
                    //read transaction
                    BatchSqlCommand batch = null;
                    long endTransactionId = 0;
                    while (c.moveToNext() && !emloyee_upload_end) {
                        long subId = c.getLong(0);
                        subIds.add(subId);
                        String subJson = c.getString(1);

                        Logger.d("[CMD_TABLE] %d = %s", subId, subJson);
                        Log.d("BemaCarl", "UploadTask2.employeeUpload.json|subJson: |" + json +"|"+ subJson + "|");
                        //TODO need to verify end
                        if ((CMD_START_TRANSACTION.equals(json)) || (CMD_END_TRANSACTION.equals(json))) {
                            break;
                        }

                        if ((CMD_END_EMPLOYEE.equals(subJson))) {
                            endTransactionId = subId;
                            emloyee_upload_end = true;
                            Logger.d("END EMPLOYEE UPLOAD");

                            if (batch == null && endTransactionId != 0) {
                                //need to delete start and end transaction
                                cr.update(URI_SQL_COMMAND_NO_NOTIFY, sentValues, SqlCommandTable.ID + " = ? OR " + SqlCommandTable.ID + " = ?", new String[]{String.valueOf(id), String.valueOf(endTransactionId)});
                            } else if (batch == null || endTransactionId == 0) {
//                        throw new TransactionNotFinalizedException("transaction is not finalized!");
                                Logger.e("transaction is not finalized!");
                            } else {
                                String transactionCmd = batch.toJson();
                                Log.d("BemaCarl","UploadTaskV2.employeeUpload.transactionCmd: " + transactionCmd);
                                Logger.d("TRANSACTION_RESULT: %s", transactionCmd);
                                commands.add(new UploadCommand(id, transactionCmd, subIds));
                            }

                            break;
                        }
                        try {
                            Log.d("BemaCarl","UploadTaskV2.employeeUpload.subJson: " + subJson);
                            if (batch == null) {
                                batch = BatchSqlCommand.fromJson(subJson);
                            } else {
                                batch.add(BatchSqlCommand.fromJson(subJson));
                            }
                        } catch (JSONException e) {
//                            throw new IllegalArgumentException("can't parse command: " + subJson, e);
                            Logger.e("can't parse command: " + subJson, e);
                            // TODO send log
                        }
                    }

                }
            }
        } finally {
            c.close();
        }

        if (!commands.isEmpty()) {
            errorsOccurred = !try2Upload(cr, commands, context);
        }

        fireUploadEmployeeCompleteEvent(context, !errorsOccurred, "errorCode");

        return !errorsOccurred;
    }

    private void fireUploadEmployeeCompleteEvent(Context context, boolean success, String errorCode) {
        Intent intent = new Intent(ACTION_EMPLOYEE_UPLOAD_COMPLETED);
        intent.putExtra(EXTRA_SUCCESS, success);
        intent.putExtra(EXTRA_ERROR_CODE, errorCode);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private boolean try2Upload(ContentResolver cr, ArrayList<UploadCommand> commands, Context context) throws SyncCommand.SyncLockedException {

        Log.d("BemaCarl","UploadTaskV2.try2Upload.cr: " + cr);

        TcrApplication app = TcrApplication.get();
        EmployeeModel employeeModel = app.getOperator() == null ? employee : app.getOperator();
        if (employeeModel == null) {
            Logger.e("[UploadWeb] user not logged in!");
            return false;
        }

        JSONObject req = null;
        try {
            req = SyncUploadRequestBuilder.getUploadObject(commands);
            Logger.d("[UploadWeb] req = %s", req.toString());
            Log.d("BemaCarl","UploadTaskV2.try2Upload.req.toString(): " + req.toString());
            SyncApi api = app.getRestAdapter().create(SyncApi.class);
            UploadResponseV1 resp = api.upload(app.emailApiKey, SyncUploadRequestBuilder.getReqCredentials(employeeModel, app), req);
            if (resp == null) {
                Logger.e("[UploadWeb] can not get response!");
                return false;
            }
            Logger.d("[UploadWeb] resp: %s", resp);
            if (resp.isSyncLockedError()) {
                throw new SyncCommand.SyncLockedException();
            }
            long skippId = -1L;
            if (!resp.isSuccess()) {
                //JSONArray requestArray = req.getJSONArray("req");
                Logger.e("[UploadWeb] error: request: " + req);
                Logger.e("[UploadWeb] error: response: " + resp);
                skippId = resp.optFailedId(-1L);
                if (resp.isCredentialsFial())
                    fireUploadEmployeeCompleteEvent(context, false, "400");
                if (skippId == -1L)
                    return false;
            }


            ArrayList<Long> currentTransactionsIds = new ArrayList<>();
            for (UploadCommand comm : commands) {
                currentTransactionsIds.add(comm.id);
            }
            final List<Long> failedIds = resp.getFailedTransactions(currentTransactionsIds);
            for (Long l : failedIds) {
                Logger.d("[UploadValidation] Transaction %d had been failed", l);
            }

            for (UploadCommand c : commands) {
                if (c.id == skippId) {
                    return false;
                }

                if (!failedIds.isEmpty() && failedIds.contains(c.id)) {
                    continue;
                }

                if (c.subIds == null) {
                    cr.update(URI_SQL_COMMAND_NO_NOTIFY, sentValues, SqlCommandTable.ID + " = ?", new String[]{String.valueOf(c.id)});
                    continue;
                }

                ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(c.subIds.size() + 1);
                operations.add(ContentProviderOperation.newUpdate(URI_SQL_COMMAND_NO_NOTIFY)
                        .withValues(sentValues)
                        .withSelection(SqlCommandTable.ID + " = ?", new String[]{String.valueOf(c.id)})
                        .build());
                for (Long id : c.subIds) {
                    operations.add(ContentProviderOperation.newUpdate(URI_SQL_COMMAND_NO_NOTIFY)
                            .withValues(sentValues)
                            .withSelection(SqlCommandTable.ID + " = ?", new String[]{String.valueOf(id)})
                            .build());
                }
                cr.applyBatch(ShopProvider.AUTHORITY, operations);
            }
            return true;
        } catch (SyncCommand.SyncLockedException e) {
            Logger.e("[UploadWeb] error: sync is locked", e);
            throw e;
        } catch (Exception e) {
            // TODO find out where does login catch this exception
            Logger.e("[UploadWeb] error", e);
            Logger.e("[UploadWeb] error, request: " + (req == null ? null : req.toString()));
            return false;
        }
    }

    public static class TransactionNotFinalizedException extends Exception {

        private TransactionNotFinalizedException() {
        }

        public TransactionNotFinalizedException(String detailMessage) {
            super(detailMessage);
        }

        private TransactionNotFinalizedException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        private TransactionNotFinalizedException(Throwable throwable) {
            super(throwable);
        }
    }

}
