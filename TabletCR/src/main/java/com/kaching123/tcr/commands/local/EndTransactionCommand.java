package com.kaching123.tcr.commands.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.service.UploadTask;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SqlCommandTable;
import com.telly.groundy.Groundy;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

/**
 * Created by hamsterksu on 28.08.2014.
 */
public class EndTransactionCommand extends PublicGroundyTask {

    private static final String ARG_START_UPLOAD = "arg_start_upload";

    private static final Uri URI_SQL_COMMAND = ShopProvider.contentUriWithLimit(SqlCommandTable.URI_CONTENT, 1);

    private boolean startUpload;

    @Override
    protected TaskResult doInBackground() {
        if (getApp().isTrainingMode())
            return succeeded();

        startUpload = getBooleanArg(ARG_START_UPLOAD);

        if (checkLastTransactionIsOpened()) {
            ContentValues values = new ContentValues();
            values.put(SqlCommandTable.CREATE_TIME, System.currentTimeMillis());
            values.put(SqlCommandTable.SQL_COMMAND, UploadTask.CMD_END_TRANSACTION);

            getContext().getContentResolver().insert(ShopProvider.contentUri(SqlCommandTable.URI_CONTENT), values);
        }

        getApp().getShopPref().lastUncompletedSaleOrderGuid().put(null);

        if (startUpload)
            OfflineCommandsService.startUpload(getContext());

        return succeeded();
    }

    private boolean checkLastTransactionIsOpened() {
        Cursor cursor = ProviderAction.query(URI_SQL_COMMAND)
                .projection(SqlCommandTable.SQL_COMMAND + " = '" + UploadTask.CMD_START_TRANSACTION + "'")
                .where(SqlCommandTable.IS_SENT + " = ?", "0")
                .where("(" + SqlCommandTable.SQL_COMMAND + " = ? OR " + SqlCommandTable.SQL_COMMAND + " = ?)", new String[]{UploadTask.CMD_START_TRANSACTION, UploadTask.CMD_END_TRANSACTION})
                .orderBy(SqlCommandTable.ID + " DESC")
                .perform(getContext());

        boolean result = false;
        if (cursor.moveToFirst())
            result = cursor.getInt(0) > 0;

        cursor.close();

        if (!result)
            Logger.e("EndTransactionCommand: failed to write end transaction - no opened transactions found!");

        return result;
    }

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean startUpload) {
        Groundy.create(EndTransactionCommand.class).arg(ARG_START_UPLOAD, startUpload).queueUsing(context);
    }
}
