package com.kaching123.tcr.commands.rest.sync.v1;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.kaching123.tcr.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder.formatApplicationVersion;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalWithScale;

/**
 * Created by gdubina on 19/03/14.
 */
public class SyncUploadRequestBuilderV1 {

    public static final String KEY_TYPE = "type";
    public static final String KEY_VALUE = "value";

    public static JSONObject createHeader(EmployeeModel employeeModel, TcrApplication app) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("login", employeeModel.login);
        request.put("pswd", employeeModel.password);
        request.put("register_serial", app.getRegisterSerial());

        return request;
    }

    public static JSONObject getUploadObject(EmployeeModel employeeModel, TcrApplication app, List<UploadCommandV1> commands) throws JSONException {
        JSONObject request = createHeader(employeeModel, app);

        JSONArray result = new JSONArray();
        for (UploadCommandV1 c : commands) {
            result.put(getCommand(c.id, c.cmd));
        }
        request.put("req", result);
        request.put("app_ver", formatApplicationVersion(app, Util.getApplicationVersion(app)));
        return request;
    }

    protected static JSONObject getCommand(long id, SingleSqlCommandV1 command) throws JSONException {
        return getCommand(true, id, command);
    }

    protected static JSONObject getCommand(boolean needId, long id, SingleSqlCommandV1 command) throws JSONException {
        JSONObject json = new JSONObject();
        if (needId) {
            json.put("id", id);
        }
        json.put("shop_id", command.shopId);
        json.put("sql", command.sql);

        JSONArray args = new JSONArray();
        for (QueryParamV1 p : command.params) {
            args.put(getParam(p));
        }
        json.put("args", args);
        return json;
    }

    private static JSONObject getParam(QueryParamV1 p) throws JSONException {
        JSONObject arg = new JSONObject();
        arg.put(KEY_TYPE, getType(p.type));
        if (p.value == null) {
            arg.put(KEY_VALUE, JSONObject.NULL);
        } else {
            switch (p.type) {
                case Types.DECIMAL:
                    arg.put(KEY_VALUE, p.value == null ? null : _decimalWithScale((BigDecimal) p.value));
                    break;
                case Types.BOOLEAN:
                    arg.put(KEY_VALUE, p.value != null && Boolean.TRUE == p.value ? 1 : 0);
                    break;
                case Types.TIMESTAMP:
                    if (p.value instanceof Timestamp)
                        arg.put(KEY_VALUE, getTimestamp((Timestamp) p.value));
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
    }

    private static String getTimestamp(Timestamp value) {
        if(value == null)
            return null;
        return value.toString();
    }

    public static class UploadCommandV1 {
        public final long id;
        public final SingleSqlCommandV1 cmd;

        public UploadCommandV1(long id, SingleSqlCommandV1 cmd) {
            this.id = id;
            this.cmd = cmd;
        }
    }
}
