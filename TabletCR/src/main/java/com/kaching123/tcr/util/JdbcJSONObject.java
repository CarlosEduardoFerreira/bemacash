package com.kaching123.tcr.util;

import android.text.TextUtils;

import com.kaching123.tcr.commands.rest.sync.Sync2Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.Blob;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by gdubina on 20/03/14.
 */
public class JdbcJSONObject extends JSONObject{

    private static ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return format;
        }
    };

    private static ThreadLocal<SimpleDateFormat> dateSimpleFormat = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return format;
        }
    };

    public JdbcJSONObject(String json) throws JSONException {
        super(json);
    }

    public JdbcJSONObject(JSONObject json) throws JSONException {
        super(json, names(json));
    }

    private static String[] names(JSONObject json) throws JSONException {
        if(json == null)
            return new String[0];
        JSONArray jsonArray = json.names();
        if(jsonArray == null || jsonArray.length() == 0)
            return new String[0];
        String[] result = new String[jsonArray.length()];
        for(int i = 0; i < jsonArray.length(); i++){
            result[i] = jsonArray.getString(i);
        }
        return result;
    }

    @Override
    public boolean getBoolean(String name) throws JSONException {
        return optInt(name) == 1 || optBoolean(name);
    }

    public Date getDate(String column) throws JSONException {
        String datetime = getString(column);
        if (TextUtils.isEmpty(datetime))
            return null;
        try {
            return dateFormat.get().parse(datetime);
        } catch (ParseException e) {
            throw new IllegalArgumentException("unsupported format", e);
        }
    }

    public Date getSimpleDate(String column) throws JSONException {
        String datetime = getString(column);
        if (TextUtils.isEmpty(datetime))
            return null;
        try {
            return dateSimpleFormat.get().parse(datetime);
        } catch (ParseException e) {
            throw new IllegalArgumentException("unsupported format", e);
        }
    }

    public Date getTimestamp(String column) throws JSONException {
        String timestamp = getString(column);
        if (TextUtils.isEmpty(timestamp))
            return null;
        return Sync2Util.formatMillisec(timestamp);
    }

    public BigDecimal getBigDecimal(String column) throws JSONException {
        String decimal = getString(column);
        if (TextUtils.isEmpty(decimal))
            return null;
        return _decimal(decimal, BigDecimal.ZERO);
    }

    public BigDecimal getBigDecimal(String column, int scale) throws JSONException {
        String decimal = getString(column);
        if (TextUtils.isEmpty(decimal))
            return null;
        return _decimal(decimal, scale, BigDecimal.ZERO);
    }

    @Override
    public String getString(String column) throws JSONException {
        if(isNull(column)){
            return null;
        }
        return super.getString(column);
    }

    /**
     *
     * @param name - filed name
     * @return 0 if null
     * @throws JSONException
     */
    @Override
    public int getInt(String name) throws JSONException {
        if(isNull(name))
            return 0;
        return super.getInt(name);
    }

    @Override
    public JdbcJSONObject getJSONObject(String name) throws JSONException {
        JSONObject json = super.getJSONObject(name);
        if(json == null)
            return null;
        return new JdbcJSONObject(json);
    }

    @Override
    public JdbcJSONArray getJSONArray(String name) throws JSONException {
        JSONArray json = super.getJSONArray(name);
        if(json == null)
            return null;
        return new JdbcJSONArray(json);
    }

    public static String getDate(Date value) {
        if(value == null)
            return null;
        return dateFormat.get().format(value);
    }

    /*public static String getTimestamp(Timestamp value) {
        if(value == null)
            return null;
        return value.toString();
    }*/
}
