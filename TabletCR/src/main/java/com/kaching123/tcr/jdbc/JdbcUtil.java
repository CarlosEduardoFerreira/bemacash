package com.kaching123.tcr.jdbc;

import com.kaching123.tcr.model.DiscountType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class JdbcUtil {

    private static final String NULL = "NULL";

    private static DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
    static{
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
    }

	private static ThreadLocal<DecimalFormat> decimalFormat = new ThreadLocal<DecimalFormat>(){
		protected DecimalFormat initialValue() {
			DecimalFormat dcf = new DecimalFormat("0.00");
            dcf.setDecimalFormatSymbols(otherSymbols);
            return dcf;
		}
	};

	//'2008-06-13 13:00:00'
	private static ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>(){
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		}
	};

	public static String _jdbcDecimal(BigDecimal decimal){
		return decimal == null ? NULL : decimalFormat.get().format(decimal);
	}

    public static String _jdbcDate(Date date){
        if (date == null) {
            return NULL;
        }
        Date offsetDate = new Date(date.getTime());
        offsetDate.setTime(offsetDate.getTime() - Calendar.getInstance().get(Calendar.ZONE_OFFSET));
		return "'" + dateFormat.get().format(offsetDate) + "'";
	}

    public static Date _jdbcDate(Timestamp date){
        if (date == null) {
            return null;
        }
        Date offsetDate = new Date(date.getTime());
        offsetDate.setTime(offsetDate.getTime() + Calendar.getInstance().get(Calendar.ZONE_OFFSET));
        return offsetDate;
    }

    public static String _jdbcBool(boolean isDiscountable) {
		return isDiscountable ? "1" : "0";
	}

    public static String _jdbcDiscountType(DiscountType type){
        return type == null ? NULL : "'" + type.name() + "'";
    }

    public static String _jdbcEnum(Enum e){
        return e == null ? NULL : "'" + e.name() + "'";
    }

    private JdbcUtil(){}
}
