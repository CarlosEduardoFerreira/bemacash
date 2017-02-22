package com.kaching123.tcr.commands.rest.sync;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.ApplicationVersion;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gdubina on 19/03/14.
 */
public class SyncUploadRequestBuilder {

    public static final String KEY_TYPE = "type";
    public static final String KEY_VALUE = "value";

    public static JSONObject createHeader(EmployeeModel employeeModel, TcrApplication app) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("api_key", app.emailApiKey);
        JSONObject credentials = getReqCredentials(employeeModel, app);
        request.put("credentials", credentials);
        return request;
    }

    public static JSONObject getReqCredentials(EmployeeModel employeeModel, TcrApplication app) throws JSONException {
        return getReqCredentials(employeeModel.login, employeeModel.password, app.getRegisterSerial(), app);
    }

    public static JSONObject getReqCredentialsCheckable(String login, String password, TcrApplication app, boolean shouldCheck) throws JSONException {
        JSONObject credentials = getReqCredentials(login, password, app.getRegisterSerial(), app);
        credentials.put("should_check", shouldCheck);
        return credentials;
    }

    public static JSONObject getReqCredentials(String login, String password, String registerSerial, Context context) throws JSONException {
        JSONObject credentials = new JSONObject();
        credentials.put("login", login);
        credentials.put("pswd", password);
        credentials.put("register_serial", registerSerial);
        credentials.put("ver", formatApplicationVersion(context, Util.getApplicationVersion(context)));
        return credentials;
    }

    public static String formatApplicationVersion(Context context, ApplicationVersion version) {
        return context.getString(R.string.app_version_format, version.name, version.code);
    }

    public static boolean hasHtmlInCommand(String command){
        return command.toLowerCase().contains("sat_response")
                && command.toLowerCase().contains("sale_order")
                && command.toLowerCase().contains("<html>");
    }

    public static JSONObject getUploadObject(List<UploadCommand> commands) throws JSONException {
        //JSONObject request = createHeader(employeeModel, app);

        JSONObject request = new JSONObject();
        JSONArray transactions = new JSONArray();
        for (UploadCommand c : commands) {
            try {
                transactions.put(getCommand(c.id, c.json));
            } catch (JSONException ex) {
                Logger.e("SyncUploadRequestBuilder" + ex.toString());
            }
        }
        request.put("transactions", transactions);
        return request;
    }

    protected static JSONObject getCommand(long id, String jsonCmd) throws JSONException {
        return getCommand(true, id, jsonCmd);
    }

    protected static JSONObject getCommand(boolean needId, long id, String jsonCmd) throws JSONException {
        JSONObject json = new JSONObject(jsonCmd);
        if (needId) {
            json.put("id", id);
        }
/*        json.put("shop_id", command.shopId);
        json.put("sql", command.sql);

        JSONArray args = new JSONArray();
        for (QueryParam p : command.params) {
            args.put(getParam(p));
        }
        json.put("args", args);*/
        return json;
    }

    /*private static JSONObject getParam(QueryParam p) throws JSONException {
        JSONObject arg = new JSONObject();
        arg.put(KEY_TYPE, getType(p.type));
        if(p.value == null){
            arg.put(KEY_VALUE, JSONObject.NULL);
        }else{
            switch (p.type) {
                case Types.DECIMAL:
                    arg.put(KEY_VALUE, p.value == null ? null : _decimalWithScale((BigDecimal) p.value));
                    break;
                case Types.BOOLEAN:
                    arg.put(KEY_VALUE, p.value != null && Boolean.TRUE == p.value ? 1 : 0);
                    break;
                case Types.TIMESTAMP:
                    if (p.value instanceof Timestamp)
                        arg.put(KEY_VALUE, JdbcJSONObject.getTimestamp((Timestamp) p.value));
                    else
                        arg.put(KEY_VALUE, JdbcJSONObject.getDate((Date) p.value));
                    break;
                case Types.VARCHAR:
                case Types.INTEGER:
                case Types.BIGINT:
                    arg.put(KEY_VALUE, p.value);
                    break;
                default:
                    throw new IllegalArgumentException("unsupported type");
            }
        }
        return arg;
    }

    private static String getType(int type) {
        switch (type) {
            case Types.VARCHAR:
                return "VARCHAR";
            case Types.DECIMAL:
                return "DECIMAL";
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.TIMESTAMP:
                return "TIMESTAMP";
            case Types.INTEGER:
                return "INTEGER";
            case Types.BIGINT:
                return "BIGINT";
            default:
                throw new IllegalArgumentException("unsupported type");
        }
    }*/

    public static class UploadCommand {
        public final long id;
        public final String json;
        public final ArrayList<Long> subIds;

        public UploadCommand(long id, String json) {
            this(id, json, null);
        }

        public UploadCommand(long id, String json, ArrayList<Long> subIds) {
            this.id = id;
            this.json = json;
            this.subIds = subIds;
        }
    }
}
