package com.kaching123.tcr.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by gdubina on 13/11/13.
 */
public final class DateUtils {

    private static final String INTERVAL_FORMAT = "%02d:%02d:%02d";

    private static final ThreadLocal<DateFormat> dateFormatThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MMM d HH:mm:ss");
        }
    };

    private static final ThreadLocal<DateFormat> dateFormatThreadLocalEcuador = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        }
    };

    private static final ThreadLocal<DateFormat> dateFormatThreadLocalFull = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MMM d yyyy HH:mm:ss");
        }
    };

    private static final ThreadLocal<DateFormat> dateOnlyFormatThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MMM dd yyyy");
        }
    };

    private static final ThreadLocal<DateFormat> timeOnlyFormatThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("HH:mm");
        }
    };

    private static final ThreadLocal<DateFormat> timeWithSecondsOnlyFormatThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("hh:mm:ss aa");
        }
    };

    private static final ThreadLocal<DecimalFormat> hrsFormatThreadLocal = new ThreadLocal<DecimalFormat>() {
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("0");
        }
    };

    private static final ThreadLocal<DecimalFormat> minutesFormatThreadLocal = new ThreadLocal<DecimalFormat>() {
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("00");
        }
    };

    private static final ThreadLocal<DateFormat> dateAndTimeShortFormatThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MMM dd HH:mm");
        }
    };

    private static final ThreadLocal<DateFormat> dateAndTimeShortAttendanceFormatThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MMM dd hh:mm aa");
        }
    };

    private static final ThreadLocal<DateFormat> dateAndTimeAttendanceFormatThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MMM dd yyyy hh:mm aa");
        }
    };

    private static final ThreadLocal<DateFormat> timeOnlyAttendanceFormatThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("hh:mm aa");
        }
    };

    private static final ThreadLocal<DateFormat> dateFullAttendanceFormatThreadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("hh:mm aa (MMM dd yyyy)");
        }
    };

    private static BigDecimal SIXTY = new BigDecimal(60);

    public static String format(Date date) {
        if (date == null)
            return null;
        return dateFormatThreadLocal.get().format(date);
    }

    public static Date valueOf(String date) throws ParseException {
        if (date == null)
            return null;
        return dateFormatThreadLocal.get().parse(date);
    }

    public static String formatFull(Date date) {
        if (date == null)
            return null;
        return dateFormatThreadLocalFull.get().format(date);
    }

    public static String formatEcuador(Date date) {
        if (date == null)
            return null;
        return dateFormatThreadLocalEcuador.get().format(date);
    }

    public static String formatInterval(final long timestamp) {
        final long hr = TimeUnit.MILLISECONDS.toHours(timestamp);
        final long min = TimeUnit.MILLISECONDS.toMinutes(timestamp - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(timestamp - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        return String.format(INTERVAL_FORMAT, hr, min, sec);
    }


    public static String dateOnlyFormat(Date date) {
        if (date == null)
            return null;
        return dateOnlyFormatThreadLocal.get().format(date);
    }

    public static String dateOnlyFormat(long date) {
        if (date == 0)
            return null;
        return dateOnlyFormatThreadLocal.get().format(new Date(date));
    }

    public static String dateAndTimeShortFormat(Date date){
        if (date == null)
            return null;
        return dateAndTimeShortFormatThreadLocal.get().format(date);

    }

    public static String dateAndTimeShortAttendanceFormat(Date date){
        if (date == null)
            return null;
        return dateAndTimeShortAttendanceFormatThreadLocal.get().format(date);

    }

    public static String dateAndTimeAttendanceFormat(Date date){
        if (date == null)
            return null;
        return dateAndTimeAttendanceFormatThreadLocal.get().format(date);

    }

    public static String timeOnlyFormat(Date date) {
        if (date == null)
            return null;
        return timeOnlyFormatThreadLocal.get().format(date);
    }

    public static String timeWithSecondsOnlyFormat(Date date) {
        if (date == null)
            return null;
        return timeWithSecondsOnlyFormatThreadLocal.get().format(date);
    }

    public static String formatMins(BigDecimal minutes) {
        BigDecimal h = minutes.divide(SIXTY, RoundingMode.DOWN);
        BigDecimal m = minutes.subtract(h.multiply(SIXTY));

        return hrsFormatThreadLocal.get().format(h) + ':' + minutesFormatThreadLocal.get().format(m);
    }

    public static String timeOnlyAttendanceFormat(Date date) {
        if (date == null)
            return null;
        return timeOnlyAttendanceFormatThreadLocal.get().format(date);
    }

    public static String formatFullAttendance(Date date) {
        if (date == null)
            return null;
        return dateFullAttendanceFormatThreadLocal.get().format(date);
    }

    public static String formatMilisec(long milisec) {

        long min = milisec / 1000 / 60;

        long h = min / 60;
        long m = min % 60;

        return hrsFormatThreadLocal.get().format(h) + ':' + minutesFormatThreadLocal.get().format(m);
    }

    public static String formatMilisecWithSecond(long milisec) {

        long sec = milisec / 1000;
        long min = sec / 60;
        sec = sec % 60;

        long h = min / 60;
        long m = min % 60;

        return hrsFormatThreadLocal.get().format(h) + ':' + minutesFormatThreadLocal.get().format(m) + ':' + minutesFormatThreadLocal.get().format(sec) ;
    }

    public static Date nowTime() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Calendar cutTime(Calendar c){
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    public static boolean isSameDay(long first, long second){
        Date f = first != 0 ? new Date(first) : null;
        Date s = second != 0 ? new Date(second) : null;

        return isSameDay(f, s);
    }

    public static boolean isSameDay(Date first, Date second) {
        if (first == null || second == null)
            return true;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(first);
        int firstYear = calendar.get(Calendar.YEAR);
        int firstDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.setTime(second);
        int secondYear = calendar.get(Calendar.YEAR);
        int secondDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        return firstYear == secondYear && firstDayOfYear == secondDayOfYear;
    }

    public static Date getStartOfDay() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE), 0, 0, 0);
        return calendar.getTime();
    }

    public static Date getEndOfDay() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE), 23, 59, 59);
        return calendar.getTime();
    }
}

