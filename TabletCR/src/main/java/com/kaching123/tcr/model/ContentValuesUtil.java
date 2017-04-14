package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.print.digital.PrintOrderToKdsCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;
import com.kaching123.tcr.model.BillPaymentDescriptionModel.PrepaidType;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.Unit.CodeType;
import com.kaching123.tcr.model.Unit.Status;
import com.kaching123.tcr.model.payment.MovementType;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import static com.google.common.base.Enums.getIfPresent;

/**
 * Created by gdubina on 06/11/13.
 */
public final class ContentValuesUtil {
    public static final int DECIMAL_SCALE = 2;
    public static final int QUANTITY_SCALE = 3;

    private static DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
    static{
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
    }

    private static ThreadLocal<DecimalFormat> decimalFormat = new ThreadLocal<DecimalFormat>() {
        protected DecimalFormat initialValue() {
            DecimalFormat format = new DecimalFormat("0.000");
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

    public static BigDecimal _decimal(Cursor c, int columnIndex, BigDecimal def) {
        return _decimal(c.getString(columnIndex), def);
    }

    public static BigDecimal _decimal(Cursor c, int columnIndex, int scale, BigDecimal def) {
        return _decimal(c.getString(columnIndex), scale, def);
    }

    public static BigDecimal _decimalQty(Cursor c, int columnIndex, BigDecimal def) {
        return _decimal(c.getString(columnIndex), QUANTITY_SCALE, def);
    }

    public static BigDecimal _decimalQty(Cursor c, int columnIndex) {
        return _decimal(c.getString(columnIndex), QUANTITY_SCALE);
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

    public static ItemRefType _itemRefType(Cursor c, int index) {
        return c.isNull(index) ? null : ItemRefType.values()[(c.getInt(index))];
    }

    public static String _decimal(BigDecimal decimal, int scale) {
        if (decimal == null) {
            return null;
        }

        return scale <= DECIMAL_SCALE ? decimalFormat.get().format(decimal) : quantityFormat.get().format(decimal);
    }

    public static String _decimalQty(BigDecimal decimal) {
        return _decimal(decimal, QUANTITY_SCALE);
    }

    public static String _decimal(BigDecimal decimal) {
        if (decimal == null) {
            return "";
        }
        return decimalFormat.get().format(decimal);
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
        try{
            return Long.parseLong(value);
        }catch (Exception e){
            return def;
        }
    }

    public static Long _long(String value) {
        if (value == null) {
            return null;
        }
        try{
            return Long.parseLong(value);
        }catch (Exception e){
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
        if(time == 0)
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

    public static BigDecimal _decimal(String decimalValue, BigDecimal def) {
        if (TextUtils.isEmpty(decimalValue))
            return def;
        try {
            return (BigDecimal) decimalFormat.get().parse(decimalValue);
        } catch (ParseException e) {
            Logger.e("Parse number error", e);
        }
        return def;
    }

    public static int getDecimalScale(String decimalValue) {
        if (TextUtils.isEmpty(decimalValue))
            return 0;
        int dotIndex = decimalValue.indexOf(decimalFormat.get().getDecimalFormatSymbols().getDecimalSeparator());
        return dotIndex < 0 ? 0 : decimalValue.length() - (dotIndex + 1);
    }

    public static BigDecimal _decimal(String decimalValue, int scale, BigDecimal def) {
        if (TextUtils.isEmpty(decimalValue))
            return def;
        try {
            return (BigDecimal) (scale <= DECIMAL_SCALE ? decimalFormat.get().parse(decimalValue) : quantityFormat.get().parse(decimalValue));
        } catch (ParseException e) {
            Logger.e("Parse number error", e);
        }
        return def;
    }

    public static BigDecimal _decimal2(Cursor c, int columnIndex, int scale, BigDecimal def){
        String value = c.getString(columnIndex);
        if (TextUtils.isEmpty(value))
            return def;
        MathContext mc = new MathContext(scale, RoundingMode.HALF_UP);
        try {
            return new BigDecimal(value, mc);
        } catch (Exception e) {
            return def;
        }
    }

    public static ContentValues _putItemRefType(ContentValues v, String key, ItemRefType itemRefType) {
        if (itemRefType == null) {
            v.putNull(key);
        } else {
            v.put(key, itemRefType.ordinal());
        }
        return v;
    }

    public static BigDecimal _decimalQty(String decimalValue) {
        return _decimal(decimalValue, QUANTITY_SCALE, BigDecimal.ZERO);
    }

    public static DiscountType _discountType(Cursor c, int index) {
        return c.isNull(index) ? null : DiscountType.valueOf(c.getInt(index));
    }

    public static CustomerStatus _customerStatus(Cursor c, int index) {
        return c.isNull(index) ? null : CustomerStatus.values()[(c.getInt(index))];
    }

    public static OrderStatus _orderStatus(Cursor c, int index) {
        return c.isNull(index) ? null : OrderStatus.values()[(c.getInt(index))];
    }

    public static OnHoldStatus _onHoldStatus(Cursor c, int index) {
        return c.isNull(index) ? null : OnHoldStatus.values()[(c.getInt(index))];
    }

    public static Status _statusType(Cursor c, int index) {
        return c.isNull(index) ? null : Status.values()[(c.getInt(index))];
    }

    public static CodeType _codeType(Cursor c, int index) {
        return c.isNull(index) ? null : CodeType.values()[(c.getInt(index))];
    }

    public static OrderType _orderType(Cursor c, int index) {
        return c.isNull(index) ? null : OrderType.values()[(c.getInt(index))];
    }

    public static PriceType _priceType(Cursor c, int index) {
        return c.isNull(index) ? null : PriceType.values()[(c.getInt(index))];
    }

    public static PaymentType _paymentType(Cursor c, int index) {
        return c.isNull(index) ? null : PaymentType.values()[(c.getInt(index))];
    }

    public static PaymentStatus _paymentStatus(Cursor c, int index) {
        return c.isNull(index) ? null : PaymentStatus.values()[(c.getInt(index))];
    }

    public static PaymentGateway _paymentGateway(Cursor c, int index) {
        return c.isNull(index) ? null : PaymentGateway.values()[(c.getInt(index))];
    }

    public static ModifierType _modifierType(Cursor c, int index) {
        return c.isNull(index) ? null : ModifierType.values()[(c.getInt(index))];
    }

    public static EmployeeStatus _employeeStatus(Cursor c, int index) {
        return c.isNull(index) ? null : EmployeeStatus.values()[c.getInt(index)];
    }

    public static PrepaidType _prepaidType(Cursor c, int index) {
        return c.isNull(index) ? null : PrepaidType.values()[(c.getInt(index))];
    }

    public static TipsModel.PaymentType _tipsPaymentType(Cursor c, int index) {
        return c.isNull(index) ? null : TipsModel.PaymentType.values()[(c.getInt(index))];
    }

    public static MovementType _movementType(Cursor c, int index) {
        return c.isNull(index) ? null : MovementType.values()[(c.getInt(index))];
    }

    public static KitchenPrintStatus _kitchenPrintStatus(Cursor c, int index) {
        return c.isNull(index) ? null : KitchenPrintStatus.values()[(c.getInt(index))];
    }

    public static PrintOrderToKdsCommand.KDSSendStatus _kdsSendStatus(Cursor c, int index) {
        return c.isNull(index) ? null : PrintOrderToKdsCommand.KDSSendStatus.values()[(c.getInt(index))];
    }

    public static Date _nullableDate(Cursor c, int index) {
        return c.isNull(index) ? null : new Date(c.getLong(index));
    }

    public static ContentValues _nullableDate(ContentValues v, String key, Date date) {
        if(date == null)
            v.putNull(key);
        else
            v.put(key, date.getTime());
        return v;
    }

    public static boolean _bool(Cursor c, int index) {
        return !c.isNull(index) && c.getInt(index) == 1;
    }

    public static int _enum(Enum e) {
        return e.ordinal();
    }

    public static <T extends Enum<T>> T _enum(Class<T> clazz, String name, T def) {
        if (TextUtils.isEmpty(name))
            return def;
        Optional<T> optional = getIfPresent(clazz, name);
        if (optional.isPresent())
            return optional.get();
        return def;
    }

    public static String _unitLabelShortcut(Cursor c, int indexItemTable, int indexUnitLabelTable) {
        String unitLabel = c.getString(indexItemTable);
        String unitLabelShortcut = c.getString(indexUnitLabelTable);
        String defaultUnitLabel = TcrApplication.get().getShopInfo().defUnitLabelShortcut;

        if(!TextUtils.isEmpty(unitLabel)) {
            return unitLabel;
        } else if(!TextUtils.isEmpty(unitLabelShortcut)) {
            return unitLabelShortcut;
        } else{
            return defaultUnitLabel;
        }
    }

    public static ContentValues _putDiscount(ContentValues v, String key, DiscountType discountType) {
        if (discountType == null) {
            v.putNull(key);
        } else {
            v.put(key, discountType.ordinal());
        }
        return v;
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

    public static String _caseCount(String column, String value, String as) {
        return String.format(Locale.US, "sum(case when %s = %s then 1 else 0 end) as %s", column, value, as);
    }

    public static String _caseCount(String column, String[] values, String as) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++){
            if (i > 0)
                builder.append(" or ");
            builder.append("%1$s = %" + (i + 3) + "$s");
        }
        String selection = "sum(case when(" + builder + ") then 1 else 0 end) as %2$s";
        String[] selectionArgs = new String[values.length + 2];
        selectionArgs[0] = column;
        selectionArgs[1] = as;
        for (int i = 0; i < values.length; i++){
            selectionArgs[i + 2] = values[i];
        }

        return String.format(Locale.US, selection, selectionArgs);
    }

    public static String _castAsReal(String column){
        return String.format(Locale.US, "cast(%s as real)", column);
    }

    public static String _sum(String column){
        return String.format(Locale.US, "sum(%s)", column);
    }

    public static String _sum(String column, String as){
        return String.format(Locale.US, "sum(%s) as %s", column, as);
    }

    public static String _max(String column){
        return String.format(Locale.US, "max(%s)", column);
    }

    public static String _in(String column, int count){
        return column + " IN (" + Joiner.on(",").join(Collections.nCopies(count, "?")) + ")";
    }

    public static String _notIn(String column, int count){
        return column + " NOT IN (" + Joiner.on(",").join(count, "?") + ")";
    }

    public static String _lower(String column){
        return String.format(Locale.US, "lower(%s)", column);
    }

    public static String _coalesce(String column, String defVal){
        return String.format(Locale.US, "coalesce(%s, %s)", column, defVal);
    }

    private ContentValuesUtil() {
    }
}
