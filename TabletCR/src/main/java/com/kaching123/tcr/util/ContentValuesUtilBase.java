package com.kaching123.tcr.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.google.common.base.Optional;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.Unit;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import static com.google.common.base.Enums.getIfPresent;

/**
 * Created by gdubina on 06/11/13.
 */
public class ContentValuesUtilBase {
    public static final int DECIMAL_SCALE = 2;
    public static final int QUANTITY_SCALE = 3;

    private static DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);

    static {
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
    }

    private static ThreadLocal<DecimalFormat> decimalFormat = new ThreadLocal<DecimalFormat>() {
        protected DecimalFormat initialValue() {
            DecimalFormat format = new DecimalFormat("0.00");
            format.setDecimalFormatSymbols(otherSymbols);
            format.setParseBigDecimal(true);
            return format;
        }
    };
    private static ThreadLocal<DecimalFormat> quantityFormat = new ThreadLocal<DecimalFormat>() {
        protected DecimalFormat initialValue() {
            DecimalFormat format = new DecimalFormat("0.000");
            format.setDecimalFormatSymbols(otherSymbols);
            format.setParseBigDecimal(true);
            return format;
        }
    };

    public static BigDecimal _decimal(Cursor c, int columnIndex) {
        return _decimal(c.getString(columnIndex));
    }

    public static BigDecimal _decimal(Cursor c, int columnIndex, int scale) {
        return _decimal(c.getString(columnIndex), scale);
    }

    public static BigDecimal _decimalQty(Cursor c, int columnIndex) {
        return _decimal(c.getString(columnIndex), QUANTITY_SCALE);
    }

    public static String _decimal(BigDecimal decimal, int scale) {
        if (decimal == null) {
            return null;
        }

        return scale <= DECIMAL_SCALE ? decimalFormat.get().format(decimal) : quantityFormat.get().format(decimal);
    }

    public static String _decimalNullable(BigDecimal decimal) {
        if (decimal == null) {
            return null;
        }
        return decimalFormat.get().format(decimal);
    }

    public static BigDecimal _decimalNullable(String decimalValue) {
        if (TextUtils.isEmpty(decimalValue))
            return null;
        try {
            return (BigDecimal) decimalFormat.get().parse(decimalValue);
        } catch (ParseException e) {
            Logger.e("Parse number error", e);
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal _decimalNullable(Cursor c, int columnIndex) {
        return _decimalNullable(c.getString(columnIndex));
    }

    public static String _decimalQty(BigDecimal decimal) {
        return _decimal(decimal, QUANTITY_SCALE);
    }

    public static String _decimal(BigDecimal decimal) {
        if (decimal == null) {
            return null;
        }
        return decimalFormat.get().format(decimal);
    }

    public static String _decimalWithScale(BigDecimal decimal) {
        if (decimal == null) {
            return null;
        }
        int scale = decimal.scale();
        return scale <= DECIMAL_SCALE ? decimalFormat.get().format(decimal) : quantityFormat.get().format(decimal);
    }

    public static String _long(Long value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value.longValue());
    }

    public static long _long(String value, long def) {
        if (value == null) {
            return def;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return def;
        }
    }

    public static Long _long(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static String _date(Date value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value.getTime());
    }

    public static Date _date(String value) {
        long time = _long(value, 0);
        if (time == 0)
            return null;
        return new Date(time);
    }

    public static BigDecimal _decimalCheckScale(String decimalValue) {
        if (TextUtils.isEmpty(decimalValue))
            return BigDecimal.ZERO;
        int scale = getDecimalScale(decimalValue);
        try {
            return (BigDecimal) (scale <= DECIMAL_SCALE ? decimalFormat.get().parse(decimalValue) : quantityFormat.get().parse(decimalValue));
        } catch (ParseException e) {
            Logger.e("Parse number error", e);
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal _decimal(String decimalValue) {
        if (TextUtils.isEmpty(decimalValue))
            return BigDecimal.ZERO;
        try {
            return (BigDecimal) decimalFormat.get().parse(decimalValue);
        } catch (ParseException e) {
            Logger.e("Parse number error", e);
        }
        return BigDecimal.ZERO;
    }

    public static int getDecimalScale(String decimalValue) {
        if (TextUtils.isEmpty(decimalValue))
            return 0;
        int dotIndex = decimalValue.indexOf(decimalFormat.get().getDecimalFormatSymbols().getDecimalSeparator());
        return dotIndex < 0 ? 0 : decimalValue.length() - (dotIndex + 1);
    }

    public static BigDecimal _decimal(String decimalValue, int scale) {
        if (TextUtils.isEmpty(decimalValue))
            return BigDecimal.ZERO;
        try {
            return (BigDecimal) (scale <= DECIMAL_SCALE ? decimalFormat.get().parse(decimalValue) : quantityFormat.get().parse(decimalValue));
        } catch (ParseException e) {
            Logger.e("Parse number error", e);
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal _decimalQty(String decimalValue) {
        return _decimal(decimalValue, QUANTITY_SCALE);
    }

    public static OrderStatus _orderStatus(Cursor c, int index) {
        return c.isNull(index) ? null : OrderStatus.values()[(c.getInt(index))];
    }

    public static Unit.Status _statusType(Cursor c, int index) {
        return c.isNull(index) ? null : Unit.Status.values()[(c.getInt(index))];
    }

    public static String _unitLabelShortcut(Cursor c, int indexItemTable, int indexUnitLabelTable) {
        return !TextUtils.isEmpty(c.getString(indexItemTable)) ? c.getString(indexItemTable) : c.getString(indexUnitLabelTable);
    }

    public static Date _nullableDate(Cursor c, int index) {
        return c.isNull(index) ? null : new Date(c.getLong(index));
    }

    public static ContentValues _nullableDate(ContentValues v, String key, Date date) {
        if (date == null)
            v.putNull(key);
        else
            v.put(key, date.getTime());
        return v;
    }

    public static boolean _bool(Cursor c, int index) {
        return !c.isNull(index) && c.getInt(index) == 1;
    }

    public static int _enum(Enum e) {
        try {
            return e.ordinal();
        } catch (NullPointerException bunny) {
            return 0;
        }
    }

    public static <T extends Enum<T>> T _enum(Class<T> clazz, String name, T def) {
        if (TextUtils.isEmpty(name))
            return def;
        Optional<T> optional = getIfPresent(clazz, name);
        if (optional.isPresent())
            return optional.get();
        return def;
    }

    public static ContentValues _putDate(ContentValues v, String key, Date date) {
        if (date == null) {
            v.putNull(key);
        } else {
            v.put(key, date.getTime());
        }
        return v;
    }

    public static ContentValues _putEnum(ContentValues v, String key, Enum e) {
        if (e == null) {
            v.putNull(key);
        } else {
            v.put(key, e.ordinal());
        }
        return v;
    }

    public static String _count(String column, String as) {
        return String.format(Locale.US, " count(%s) as %s", column, as);
    }

    public static String _countDistinct(String column, String as) {
        return String.format(Locale.US, " count(distinct %s) as %s", column, as);
    }

    public static String _castToReal(String column) {
        return String.format(Locale.US, "cast(%s as real)", column);
    }

    public static DiscountType _discountType(Cursor c, int index) {
        return c.isNull(index) ? null : DiscountType.valueOf(c.getInt(index));
    }

    protected ContentValuesUtilBase() {
    }
}
