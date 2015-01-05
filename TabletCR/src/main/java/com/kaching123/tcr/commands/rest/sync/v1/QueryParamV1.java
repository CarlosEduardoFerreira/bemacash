package com.kaching123.tcr.commands.rest.sync.v1;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;

import static com.kaching123.tcr.model.ContentValuesUtil._date;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalCheckScale;
import static com.kaching123.tcr.model.ContentValuesUtil._long;

/**
 * Created by gdubina on 05/03/14.
 */
public class QueryParamV1 {

    public static final String JSON_TYPE = "TYPE";
    public static final String JSON_OBJECT = "OBJECT";
    public int type;
    public Object value;

    public QueryParamV1(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_TYPE, type);
        switch (type) {
            case Types.DECIMAL:
                jsonObject.put(JSON_OBJECT, _decimal((BigDecimal) value));
                break;
            case Types.BIGINT:
                jsonObject.put(JSON_OBJECT, _long((Long) value));
                break;
            case Types.TIMESTAMP:
                jsonObject.put(JSON_OBJECT, _date((Date) value));
                break;
            default:
                jsonObject.putOpt(JSON_OBJECT, value);
        }
        return jsonObject;
    }

    public static QueryParamV1 fromJson(JSONObject pJson) throws JSONException {
        int type = pJson.getInt(JSON_TYPE);
        Object value;
        switch (type) {
            case Types.DECIMAL:
                value = _decimalCheckScale(pJson.getString(JSON_OBJECT));
                break;
            case Types.BIGINT:
                value = _long(pJson.getString(JSON_OBJECT));
                break;
            case Types.TIMESTAMP:
                value = _date(pJson.getString(JSON_OBJECT));
                break;
            default:
                value = pJson.opt(JSON_OBJECT);
        }
        return new QueryParamV1(type, value);
    }
}
