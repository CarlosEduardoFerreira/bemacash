package com.kaching123.tcr.service.v2;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder.UploadCommand;
import com.kaching123.tcr.commands.rest.sync.v1.UploadResponseV1;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SqlCommandTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UploadTaskV2 {

    protected static final Uri URI_SQL_COMMAND_NO_NOTIFY = ShopProvider.contentUriNoNotify(SqlCommandTable.URI_CONTENT);
    private static final int BATCH_SIZE = 20;
    public static final String CMD_START_TRANSACTION = "start_transaction";
    public static final String CMD_END_TRANSACTION = "end_transaction";

    static ContentValues sentValues = new ContentValues();

    static {
        sentValues.put(SqlCommandTable.IS_SENT, 1);
    }

    private OfflineCommandsService service;

    public UploadTaskV2(OfflineCommandsService service) {
        this.service = service;
    }

    public boolean webApiUpload(ContentResolver cr) throws TransactionNotFinalizedException {
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
                Logger.d("[CMD_TABLE] %d = %s", id, json);
                if (CMD_START_TRANSACTION.equals(json)) {
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
                        if (CMD_END_TRANSACTION.equals(subJson)) {
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
                            throw new IllegalArgumentException("can't parse command: " + subJson, e);
                        }
                    }
                    if (batch == null && endTransactionId != 0) {
                        //need to delete start and end transaction
                        cr.update(URI_SQL_COMMAND_NO_NOTIFY, sentValues, SqlCommandTable.ID + " = ? OR " + SqlCommandTable.ID + " = ?", new String[]{String.valueOf(id), String.valueOf(endTransactionId)});
                    } else if (batch == null || endTransactionId == 0) {
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
                    boolean uploaded = try2Upload(cr, commands);
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
            errorsOccurred = !try2Upload(cr, commands);
        }
        return !errorsOccurred;
    }

    private boolean try2Upload(ContentResolver cr, ArrayList<UploadCommand> commands) {
        TcrApplication app = TcrApplication.get();
        EmployeeModel employeeModel = app.getOperator();
        if (employeeModel == null) {
            Logger.e("[UploadWeb] user not logged in!");
            return false;
        }

        JSONObject req = null;
        try {
            req = SyncUploadRequestBuilder.getUploadObject(commands);
            Logger.d("[UploadWeb] req = %s", req.toString());
            SyncApi api = app.getRestAdapter().create(SyncApi.class);
            UploadResponseV1 resp = api.upload(app.emailApiKey, SyncUploadRequestBuilder.getReqCredentials(employeeModel, app), req);
            if (resp == null) {
                Logger.e("[UploadWeb] can not get response!");
                return false;
            }
            Logger.d("[UploadWeb] resp: %s", resp);
            long skippId = -1L;
            if (!resp.isSuccess()) {
                //JSONArray requestArray = req.getJSONArray("req");
                Logger.e("[UploadWeb] error: request: " + req);
                Logger.e("[UploadWeb] error: response: " + resp);
                skippId = resp.optFailedId(-1L);
                if (skippId == -1L)
                    return false;
            }
            for (UploadCommand c : commands) {
                if (c.id == skippId) {
                    return false;
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
        } catch (Exception e) {
            Logger.e("[UploadWeb] error", e);
            Logger.e("[UploadWeb] error, request: " + (req == null ? null : req.toString()));
            return false;
        }
    }

    public static class TransactionNotFinalizedException extends Exception {

        private TransactionNotFinalizedException() {
        }

        private TransactionNotFinalizedException(String detailMessage) {
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