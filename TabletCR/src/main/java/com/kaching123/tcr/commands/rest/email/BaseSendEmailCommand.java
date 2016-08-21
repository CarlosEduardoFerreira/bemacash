package com.kaching123.tcr.commands.rest.email;

import android.widget.Toast;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.rest.RestCommand;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pkabakov on 12.02.14.
 */
public abstract class BaseSendEmailCommand extends RestCommand {

    protected static final String ARG_HARDCODE = "ARG_HARDCODE";

    protected boolean isHardcode;

    protected Response sendEmail(SyncApi api, String apiKey, String[] emailList, String subject, String html) {
        Response response = api.sendEmail(apiKey, getCredentials(), getEmailEntity(emailList, subject, html));
        Logger.d("BaseSendEmailCommand.Response: status: " + response.status + ", message: " + response.message);
        return response;
    }

    protected JSONObject getCredentials() {
        JSONObject credentials = null;
        try {
            credentials = SyncUploadRequestBuilder.getReqCredentials(getApp().getOperator(), getApp());
        } catch (JSONException ignore) {}
        return credentials;
    }

    private JSONObject getEmailEntity(String[] emailList, String subject, String html) {
        JSONObject entity = new JSONObject();
        try {
            JSONArray emailListJson = new JSONArray();
            for (String email: emailList)
                emailListJson.put(email);
            entity.put("emailList", emailListJson);
            entity.put("subject", subject);
            entity.put("html", html);
            entity.put("shop_id", isHardcode ? 67L : getAppCommandContext().getShopId());
        } catch (JSONException ignore) {}
        return entity;
    }

}
