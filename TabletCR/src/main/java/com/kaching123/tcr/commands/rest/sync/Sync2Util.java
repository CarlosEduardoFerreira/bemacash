package com.kaching123.tcr.commands.rest.sync;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by hamsterksu on 09.09.2014.
 */
public final class Sync2Util {

    private Sync2Util(){}

    private static final int MILLISECONDS_FRACTIONAL_PART_LENGTH = 3;

    private static ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormatGmt;
        }
    };

    private static ThreadLocal<SimpleDateFormat> dateFormatMillisec = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormatGmt;
        }
    };

    public static Object format(Date date){
        if(date == null)
            return JSONObject.NULL;
        return dateFormat.get().format(date);
    }

    public static Object formatMillisec(Date date){
        if(date == null)
            return JSONObject.NULL;
        return dateFormatMillisec.get().format(date);
    }

    public static Date formatMillisec(String timestamp) {
        if (TextUtils.isEmpty(timestamp))
            return null;

        timestamp = truncateTimestampString(timestamp);

        try {
            return dateFormatMillisec.get().parse(timestamp);
        } catch (ParseException e) {
            throw new IllegalArgumentException("unsupported format", e);
        }
    }

    private static String truncateTimestampString(String timestamp) {
        int dotIndex = timestamp.indexOf('.');
        if (dotIndex == -1)
            throw new IllegalArgumentException("unsupported format");
        int fractionalPartLength = timestamp.length() - (dotIndex + 1);
        if (fractionalPartLength <= 0)
            throw new IllegalArgumentException("unsupported format");
        if (fractionalPartLength > MILLISECONDS_FRACTIONAL_PART_LENGTH) {
            String extraFractionalPart = timestamp.substring(dotIndex + 1 + MILLISECONDS_FRACTIONAL_PART_LENGTH, timestamp.length());
            int extraFractionalPartInt;
            try {
                extraFractionalPartInt = Integer.parseInt(extraFractionalPart);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("unsupported format");
            }
            if (extraFractionalPartInt != 0)
                throw new IllegalArgumentException("unsupported format");

            return timestamp.substring(0, timestamp.length() - (fractionalPartLength - MILLISECONDS_FRACTIONAL_PART_LENGTH));
        }
        return timestamp;
    }
}
