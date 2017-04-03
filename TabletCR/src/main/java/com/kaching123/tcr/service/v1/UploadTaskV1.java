package com.kaching123.tcr.service.v1;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.rest.sync.v1.SingleSqlCommandV1;
import com.kaching123.tcr.commands.rest.sync.v1.SyncApiV1;
import com.kaching123.tcr.commands.rest.sync.v1.SyncUploadRequestBuilderV1;
import com.kaching123.tcr.commands.rest.sync.v1.SyncUploadRequestBuilderV1.UploadCommandV1;
import com.kaching123.tcr.commands.rest.sync.v1.UploadResponseV1;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SqlCommandTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UploadTaskV1 {

    protected static final Uri URI_SQL_COMMAND_NO_NOTIFY = ShopProvider.contentUriNoNotify(SqlCommandTable.URI_CONTENT);

    private static final int BATCH_SIZE = 20;

    static ContentValues sentValues = new ContentValues();

    static {
        sentValues.put(SqlCommandTable.IS_SENT, 1);
    }

    public boolean webApiUpload(ContentResolver cr) {

        Log.d("BemaCarl","UploadTaskV1.webApiUpload.cr: " + cr);
        if(!new AtomicUpload().hasInternetConnection()){
            return false;
        }


        if (TcrApplication.get().isTrainingMode())
            return true;

        TcrApplication app = TcrApplication.get();
        SyncApiV1 api = app.getRestAdapter().create(SyncApiV1.class);
        boolean errorsOccurred = false;
        //test
        Cursor c = cr.query(URI_SQL_COMMAND_NO_NOTIFY,
                new String[]{SqlCommandTable.ID, SqlCommandTable.SQL_COMMAND},
                SqlCommandTable.IS_SENT + " = ? and " + SqlCommandTable.API_VERSION + " = ?", new String[]{"0", "1"},
                SqlCommandTable.ID);

        ArrayList<UploadCommandV1> commands = new ArrayList<UploadCommandV1>(BATCH_SIZE);
        while (c.moveToNext()) {
            long id = c.getLong(0);
            String sql = c.getString(1);
            SingleSqlCommandV1 command = null;
            try {
                command = SingleSqlCommandV1.fromJson(sql);
            } catch (JSONException e) {
                Logger.e("[UploadWebV1] error", e);
                errorsOccurred = true;
                break;
            }
            commands.add(new UploadCommandV1(id, command));
            if (commands.size() == BATCH_SIZE) {
                boolean uploaded = try2Upload(api, cr, commands);
                commands.clear();
                if (!uploaded) {
                    errorsOccurred = true;
                    break;
                }
            }
        }
        c.close();
        if (!commands.isEmpty()) {
            errorsOccurred = !try2Upload(api, cr, commands);
        }
        return !errorsOccurred;
    }

    private boolean try2Upload(SyncApiV1 api, ContentResolver cr, ArrayList<UploadCommandV1> commands) {

        Log.d("BemaCarl","UploadTaskV1.try2Upload.cr: " + cr);

        TcrApplication app = TcrApplication.get();
        EmployeeModel employeeModel = app.getOperator();
        if (employeeModel == null) {
            Logger.e("[UploadWebV1] user not logged in!");
            return false;
        }

        JSONObject req = null;
        try {
            req = SyncUploadRequestBuilderV1.getUploadObject(employeeModel, app, commands);
            Logger.d("[UploadWebV1] %s", req.toString());

            UploadResponseV1 resp = api.upload(app.emailApiKey, req);
            if (resp == null) {
                Logger.e("[UploadWebV1] can not get response!");
                return false;
            }
            Logger.d("[UploadWebV1] resp: %s", resp);
            long skippId = -1L;
            if (!resp.isSuccess()) {
                try {
                    JSONArray requestArray = req.getJSONArray("req");
                    Logger.e("[UploadWebV1] error: request: " + requestArray);
                } catch (JSONException ignore) {
                }
                Logger.e("[UploadWebV1] error: response: " + resp);
                skippId = resp.optFailedId(-1L);
                if (skippId == -1L)
                    return false;
            }
            for (UploadCommandV1 c : commands) {
                if (c.id == skippId) {
                    return false;
                }
                cr.update(URI_SQL_COMMAND_NO_NOTIFY, sentValues, SqlCommandTable.ID + " = ?", new String[]{String.valueOf(c.id)});
            }
            return true;
        } catch (Exception e) {
            Logger.e("[UploadWebV1] error, request: " + (req == null ? null : req.toString()), e);
            return false;
        }
    }

    public int getV1CommandsCount(ContentResolver cr) {
        Cursor c = cr.query(URI_SQL_COMMAND_NO_NOTIFY,
                new String[]{SqlCommandTable.ID, SqlCommandTable.SQL_COMMAND},
                SqlCommandTable.IS_SENT + " = ? and " + SqlCommandTable.API_VERSION + " = ?", new String[]{"0", "1"},
                null);
        int count = c.getCount();
        c.close();
        return count;
    }
}
