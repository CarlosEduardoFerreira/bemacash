package com.kaching123.tcr.commands.rest.sync;

import com.kaching123.tcr.service.QueryParam;

import java.util.ArrayList;

/**
 * Created by gdubina on 19/03/14.
 */
public class SyncUploadRawRequestBuilder {

    /*public static final String KEY_TYPE = "type";
    public static final String KEY_VALUE = "value";*/

    /*public static JSONObject createHeader(EmployeeModel employeeModel, TcrApplication app) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("login", employeeModel.login);
        request.put("pswd", employeeModel.password);
        request.put("register_serial", app.getRegisterSerial());

        return request;
    }*/

    /*public static JSONObject getUploadObject(EmployeeModel employeeModel, TcrApplication app, List<UploadCommand> commands) throws JSONException {
        JSONObject request = createHeader(employeeModel, app);

        JSONArray result = new JSONArray();
        for (UploadCommand c : commands) {
            result.put(getCommand(c.id, c.cmd));
        }
        request.put("req", result);
        request.put("app_ver", formatApplicationVersion(app, Util.getApplicationVersion(app)));
        return request;
    }*/

    /*protected static JSONObject getCommand(long id, RawSqlCommand command) throws JSONException {
        return getCommand(true, id, command);
    }*/

    /*protected static JSONObject getCommand(boolean needId, long id, RawSqlCommand command) throws JSONException {
        JSONObject json = new JSONObject();
        if (needId) {
            json.put("id", id);
        }
        json.put("shop_id", command.shopId);
        json.put("sql", command.sql);

        JSONArray args = new JSONArray();
        for (QueryParam p : command.params) {
            args.put(getParam(p));
        }
        json.put("args", args);
        return json;
    }*/

    /*private static JSONObject getParam(QueryParam p) throws JSONException {
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
    }*/

    /*private static String getType(int type) {
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
        public final RawSqlCommand cmd;

        public UploadCommand(long id, RawSqlCommand cmd) {
            this.id = id;
            this.cmd = cmd;
        }
    }

    public static class RawSqlCommand {
        public final long shopId;
        public final String sql;
        public final ArrayList<QueryParam> params = new ArrayList<QueryParam>();

        public RawSqlCommand(long shopId, String sql, ArrayList<QueryParam> params) {
            this.shopId = shopId;
            this.sql = sql;
            this.params.addAll(params);
        }

        public RawSqlCommand(long shopId, String sql, Iterable<QueryParam>... params) {
            this.shopId = shopId;
            this.sql = sql;
            if (params != null) {
                for (Iterable<QueryParam> it : params) {
                    for (QueryParam p : it) {
                        this.params.add(p);
                    }
                }
            }
        }

        public void add(int type, Object value) {
            params.add(new QueryParam(type, value));
        }

        public void add(QueryParam p) {
            params.add(p);
        }
    }
}
