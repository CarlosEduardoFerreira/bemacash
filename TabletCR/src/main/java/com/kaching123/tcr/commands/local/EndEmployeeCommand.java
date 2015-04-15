package com.kaching123.tcr.commands.local;

import android.content.ContentValues;
import android.content.Context;

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
public class EndEmployeeCommand extends PublicGroundyTask {

    private static final String ARG_START_UPLOAD = "arg_start_upload";

    private boolean startUpload;

    @Override
    protected TaskResult doInBackground() {
        if (getApp().isTrainingMode())
            return succeeded();

        startUpload = getBooleanArg(ARG_START_UPLOAD);

        ContentValues values = new ContentValues();
        values.put(SqlCommandTable.CREATE_TIME, System.currentTimeMillis());
        values.put(SqlCommandTable.SQL_COMMAND, UploadTask.CMD_END_TRANSACTION);

        getContext().getContentResolver().insert(ShopProvider.contentUri(SqlCommandTable.URI_CONTENT), values);

        if (startUpload)
            OfflineCommandsService.startUpload(getContext());

        return succeeded();
    }

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean startUpload) {
        Groundy.create(EndEmployeeCommand.class).arg(ARG_START_UPLOAD, startUpload).queueUsing(context);
    }
}
