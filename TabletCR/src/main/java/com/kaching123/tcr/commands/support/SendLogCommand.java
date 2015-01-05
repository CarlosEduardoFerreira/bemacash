package com.kaching123.tcr.commands.support;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.kaching123.tcr.commands.rest.email.BaseSendEmailCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder;
import com.kaching123.tcr.model.EmployeeModel;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import org.json.JSONException;
import org.json.JSONObject;

import static com.kaching123.tcr.util.LogUtils.getDebugLog;

/**
 * Created by hamsterksu on 20.03.14.
 */
public class SendLogCommand extends BaseSendEmailCommand {

    private static final String ARG_LOGIN = "ARG_LOGIN";
    private static final String ARG_PASSWORD = "ARG_PASSWORD";

    @Override
    protected Response execute(SyncApi restApi, String apiKey) {
        isHardcode = getBooleanArg(ARG_HARDCODE, false);
        String email = isHardcode ? "TabletCR.dev@dataart.com" : getApp().getShopInfo().supportEmail;
        if (TextUtils.isEmpty(email))
            email = "support_kaching@logiccontrols.com";

        String log = getDebugLog(getContext());
        return sendEmail(restApi, apiKey, new String[]{email}, "Tablet app log", log);
    }

    @Override
    protected JSONObject getCredentials() {
        JSONObject credentials = null;
        try {
            String login;
            String password;
            boolean shouldCheck;
            Bundle args = getArgs();
            if (args.containsKey(ARG_LOGIN) && args.containsKey(ARG_PASSWORD)){
                login = args.getString(ARG_LOGIN);
                password = args.getString(ARG_PASSWORD);
                shouldCheck = false;
            }else{
                EmployeeModel operator = getApp().getOperator();
                login = operator.login;
                password = operator.password;
                shouldCheck = true;
            }

            credentials = SyncUploadRequestBuilder.getReqCredentialsCheckable(login, password, getApp(), shouldCheck);
        } catch (JSONException ignore) {}
        return credentials;
    }

    public static void start(Context context) {
        create(SendLogCommand.class)
                .queueUsing(context);
    }

    public static void start(Context context, boolean isHardcode, BaseSendLogCallback callback) {
        create(SendLogCommand.class)
                .arg(ARG_HARDCODE, isHardcode)
                .callback(callback)
                .queueUsing(context);
    }

    public static void start(Context context, String login, String password, BaseSendLogCallback callback) {
        create(SendLogCommand.class)
                .arg(ARG_LOGIN, login)
                .arg(ARG_PASSWORD, password)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseSendLogCallback {

        @OnSuccess(SendLogCommand.class)
        public final void onSuccess() {
            handleOnSuccess();
        }

        protected abstract void handleOnSuccess();

        @OnFailure(SendLogCommand.class)
        public final void onFailure() {
            handleOnFailure();
        }

        protected abstract void handleOnFailure();

    }
}
