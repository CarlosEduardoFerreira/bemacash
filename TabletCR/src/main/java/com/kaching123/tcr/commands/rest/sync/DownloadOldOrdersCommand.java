package com.kaching123.tcr.commands.rest.sync;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by pkabakov on 03.02.2015.
 */
public class DownloadOldOrdersCommand extends PublicGroundyTask {

    private static final String EXTRA_GUIDS = "extra_guids";
    private static final String EXTRA_ERROR = "extra_error";

    private static final String ARG_REGISTER_TITLE = "arg_register_title";
    private static final String ARG_PRINT_SEQ_NUM = "arg_print_seq_num";
    private static final String ARG_FROM_DATE = "arg_from_date";
    private static final String ARG_TO_DATE = "arg_to_date";
    private static final String ARG_UNIT_SERIAL = "arg_unit_serial";

    private String registerTitle;
    private String printSeqNum;
    private Date from;
    private Date to;
    private String unitSerial;

    public static enum Error {
        NOT_FOUND, SYNC_LOCKED
    }

    @Override
    protected TaskResult doInBackground() {
        registerTitle = getStringArg(ARG_REGISTER_TITLE);
        printSeqNum = getStringArg(ARG_PRINT_SEQ_NUM);
        from = (Date) getArgs().getSerializable(ARG_FROM_DATE);
        to = (Date) getArgs().getSerializable(ARG_TO_DATE);
        unitSerial = getStringArg(ARG_UNIT_SERIAL);

        Logger.d("[SYNC HISTORY]DownloadOldOrdersCommand: setting loading orders flag");
        getApp().setLoadingOldOrders(true);
        Logger.d("[SYNC HISTORY]DownloadOldOrdersCommand: loading orders flag set");
        try {
            Logger.d("[SYNC HISTORY]DownloadOldOrdersCommand: acquiring history lock");
            getApp().lockOnSalesHistory();
            try {
                Logger.d("[SYNC HISTORY]DownloadOldOrdersCommand: history lock acquired");

                GetArrayResponse resp = getApi().downloadOldOrders(getApp().emailApiKey, SyncUploadRequestBuilder.getReqCredentials(getApp().getOperator(), getApp()),
                        getEntity(registerTitle, printSeqNum, from, to, unitSerial));
                Logger.d("DownloadOldOrdersCommand: response: " + resp);

                if (resp != null && resp.isSyncLockedError()) {
                    return failed().add(EXTRA_ERROR, Error.SYNC_LOCKED);
                }

                if (resp == null || !resp.isSuccess()) {
                    Logger.e("DownloadOldOrdersCommand: failed, response: " + resp);
                    return failed();
                }

                if (resp.getEntity() == null || resp.getEntity().length() == 0) {
                    Logger.e("DownloadOldOrdersCommand: empty response: " + resp);
                    return failed().add(EXTRA_ERROR, Error.NOT_FOUND);
                }

                String[] guids = new DownloadOldOrdersResponseHandler(getContext()).handleOrdersResponse(resp);

                return succeeded().add(EXTRA_GUIDS, guids);
            } catch (Exception e) {
                Logger.e("DownloadOldOrdersCommand: failed", e);
                return failed();
            } finally {
                Logger.d("[SYNC HISTORY]DownloadOldOrdersCommand: releasing history lock");
                getApp().unlockOnSalesHistory();
                Logger.d("[SYNC HISTORY]DownloadOldOrdersCommand: history lock released");
            }
        } finally {
            Logger.d("[SYNC HISTORY]DownloadOldOrdersCommand: dropping loading orders flag");
            getApp().setLoadingOldOrders(false);
            Logger.d("[SYNC HISTORY]DownloadOldOrdersCommand: loading orders flag dropped");
        }
    }

    private SyncApi2 getApi() {
        return getApp().getRestAdapter().create(SyncApi2.class);
    }

    private JSONObject getEntity(String registerTitle, String printSeqNum, Date from, Date to, String unitSerial) throws JSONException {
        JSONObject request = new JSONObject();

        if (!TextUtils.isEmpty(unitSerial)) {
            request.put("unit_serial", unitSerial);
        } else {
            request.put("register", registerTitle);
            request.put("print_num", printSeqNum);
        }

        request.put("from", Sync2Util.formatMillisec(from));
        request.put("to", Sync2Util.formatMillisec(to));

        return request;
    }

    public static void start(Context context, String registerTitle, String printSeqNum, Date from, Date to, String unitSerial, BaseDownloadOldOrdersCommandCallback callback) {
        create(DownloadOldOrdersCommand.class).arg(ARG_REGISTER_TITLE, registerTitle).arg(ARG_PRINT_SEQ_NUM, printSeqNum).arg(ARG_FROM_DATE, from).arg(ARG_TO_DATE, to)
                .arg(ARG_UNIT_SERIAL, unitSerial)
                .callback(callback).queueUsing(context);
    }

    public static abstract class BaseDownloadOldOrdersCommandCallback {

        @OnSuccess(DownloadOldOrdersCommand.class)
        public void handleSuccess(@Param(EXTRA_GUIDS) String[] guids) {
            onSuccess(guids);
        }

        @OnFailure(DownloadOldOrdersCommand.class)
        public void handleFailure(@Param(EXTRA_ERROR) Error error) {
            if (error == null) {
                onFailure();
                return;
            }
            switch (error) {
                case NOT_FOUND:
                    onNotFoundError();
                    break;
                case SYNC_LOCKED:
                    onSyncLockedError();
                    break;
                default:
                    onFailure();
            }
        }

        protected abstract void onSuccess(String[] guids);

        protected abstract void onNotFoundError();

        protected abstract void onSyncLockedError();

        protected abstract void onFailure();
    }

}
