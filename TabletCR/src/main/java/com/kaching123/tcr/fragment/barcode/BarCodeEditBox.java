package com.kaching123.tcr.fragment.barcode;

import android.content.Context;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.component.CustomEditBox;
import com.kaching123.tcr.model.Unit.CodeType;
import com.kaching123.tcr.util.LuhnUtil;

/**
 * Created by gdubina on 29.11.13.
 */
public class BarCodeEditBox extends CustomEditBox {

    private StringBuilder value;

    public enum Formatter {
        BARCODE(new BCFormatter()),
        SN(new SNFormatter()),
        ICCID(new ICCIDFormatter()),
        IMEI(new IMEIFormatter());

        public final IFormatter formatter;

        Formatter(IFormatter formatter) {
            this.formatter = formatter;
        }
    }

    private IFormatter format;

    @Override
    public boolean valid() {
        return !TextUtils.isEmpty(getText().toString()) && getFormatter().valid(this.value);

    }

    private IFormatter getFormatter() {
        return format == null ? Formatter.BARCODE.formatter : format;
    }

    public BarCodeEditBox(Context context) {
        super(context);
        resetFormatter();
        setInputType(0);
    }

    public BarCodeEditBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        resetFormatter();
        setInputType(0);
    }

    public BarCodeEditBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        resetFormatter();
    }

    private void resetFormatter() {
        format = Formatter.BARCODE.formatter;
        value = format.value();
        updateView(true);
    }

    protected void updateView(boolean reload) {
        int maxLength = getFormatter().actualLength();
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        setFilters(fArray);

        value.setLength(Math.min(value.length(), getFormatter().length()));
        super.setText(getFormatter().format(value.toString()), BufferType.NORMAL);
    }

    @Override
    public void append(CharSequence text, int start, int end) {
        if(value.length() == getFormatter().length()){
            return;
        }
        value.append(text, start, end);
        super.setText(getFormatter().format(value.toString()), BufferType.EDITABLE);
    }

    @Override
        public void setText(CharSequence text, BufferType type) {
        if (value == null) {
            super.setText(getFormatter().format(text.toString()), type);
            return;
        }
        value.setLength(0);
        if (text != null) {
            value.append(text);
        }
        value.setLength(Math.min(value.length(), getFormatter().length()));
        super.setText(getFormatter().format(value.toString()), type);
    }

//    @Override
//    public CharSequence getText() {
//        return value;
//    }


    public void setCodeType(CodeType type) {
        if (type != null) switch (type) {
            case SN: {
                this.format = Formatter.SN.formatter;
                break;
            }
            case IMEI: {
                this.format = Formatter.IMEI.formatter;
                break;
            }
            case ICCID: {
                this.format = Formatter.ICCID.formatter;
                break;
            }
            default: this.format = Formatter.BARCODE.formatter;
        } else this.format = Formatter.BARCODE.formatter;
        updateView(false);
    }

    public static class SNFormatter implements IFormatter {

        protected final int SN_SIZE = 20;

        @Override
        public int minLength() {
            return 1;
        }


        @Override
        public int actualLength() {
            return length();
        }

        @Override
        public int length() {
            return SN_SIZE;
        }

        @Override
        public StringBuilder value() {
            return new StringBuilder(length());
        }

        @Override
        public String format(String str) {
            if (str.length() > length()) {
                str = str.substring(0, length());
            }
            StringBuilder builder = new StringBuilder(actualLength());
            for(int i = 0; i < length() - str.length(); i++){
                builder.append("#");
            }
            for(int i = 0; i < str.length(); i++){
                builder.append(str.charAt(i));
            }
            return builder.toString();
        }

        @Override
        public boolean valid(StringBuilder sb) {
            return sb.length() >= minLength() && sb.length() <= length();
        }
    }

    public static class ICCIDFormatter implements IFormatter {

        private static final String ICCID_FILLER = "F";


        @Override
        public int minLength() {
            return length();
        }

        @Override
        public int actualLength() {
            return length() + 5;
        }

        @Override
        public int length() {
            return LuhnUtil.ICCID_LUHN;
        }

        @Override
        public StringBuilder value() {
            return new StringBuilder(length());
        }

        @Override
        public String format(String str) {
            StringBuilder builder = new StringBuilder(actualLength());
            for(int i = 0; i < length() - str.length(); i++){
                builder.append("#");
            }
            for(int i = 0; i < str.length(); i++){
                builder.append(str.charAt(i));
            }
            builder.insert(4, '-');
            builder.insert(9, '-');
            builder.insert(14, '-');
            builder.insert(19, '-');
            builder.insert(22, '-');
            return builder.toString();
        }

        @Override
        public boolean valid(StringBuilder sb) {
            String validationString = sb.toString();
            if (validationString.toUpperCase().endsWith(ICCID_FILLER))
                validationString = validationString.substring(0, validationString.length() - ICCID_FILLER.length());
            return sb.length() >= minLength() && sb.length() <= length() && LuhnUtil.isLuhnValid(validationString);
        }
    }

    public static class IMEIFormatter implements IFormatter {


        @Override
        public int actualLength() {
            return length() + 3;
        }

        @Override
        public int length() {
            return LuhnUtil.IMEI_LUHN;
        }

        @Override
        public int minLength() {
            return length();
        }

        @Override
        public StringBuilder value() {
            return new StringBuilder(length());
        }

        @Override
        public String format(String str) {
            StringBuilder builder = new StringBuilder(actualLength());
            for(int i = 0; i < length() - str.length(); i++){
                builder.append("#");
            }
            for(int i = 0; i < str.length(); i++){
                builder.append(str.charAt(i));
            }
            builder.insert(2, '-');
            builder.insert(9, '-');
            builder.insert(16, '-');
            return builder.toString();
        }

        @Override
        public boolean valid(StringBuilder sb) {
            return sb.length() >= minLength() && sb.length() <= length() && LuhnUtil.isLuhnValid(sb.toString());
        }
    }

    public static class BCFormatter implements IFormatter {

        protected final StringBuilder value = new StringBuilder(length());

        @Override
        public int actualLength() {
            return length() + 2;
        }

        @Override
        public int length() {
            return TcrApplication.BARCODE_MAX_LEN;
        }

        @Override
        public int minLength() {
            return TcrApplication.BARCODE_MIN_LEN;
        }

        @Override
        public StringBuilder value() {
            return new StringBuilder(length());
        }

        @Override
        public String format(String str) {
            StringBuilder builder = new StringBuilder(actualLength());
            for(int i = 0; i < length() - str.length(); i++){
                builder.append("#");
            }
            for(int i = 0; i < str.length(); i++){
                builder.append(str.charAt(i));
            }
            builder.insert(1, '-');
            builder.insert(8, '-');
            return builder.toString();
        }

        @Override
        public boolean valid(StringBuilder sb) {
            return sb.length() >= minLength() && sb.length() <= length();
        }
    }

    public interface IFormatter {
        int actualLength();
        int length();
        int minLength();
        StringBuilder value();

        String format(String str);
        boolean valid(StringBuilder sb);
    }


    public static class IccidInputFilter implements InputFilter {

        private static final char ICCID_FILLER = 'F';

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            boolean isAttachingToEnd = dest.length() == 0 || dstart == dest.length() || dend == dest.length();
            boolean isDeleting = (end - start == 0) && (dend - dstart != 0);
            boolean keepOriginal = true;

            StringBuilder sb;
            if (isDeleting) {
                sb = new StringBuilder(dend - dstart);
                int pos = 0;
                for (int i = 0; i < dest.length(); i++) {
                    char c = dest.charAt(i);
                    if (i >= dstart && i < dend) {
                        sb.append(c);
                        continue;
                    }
                    if (isCharAllowed(c, true, pos)) {
                        pos++;
                    } else {
                        keepOriginal = false;
                    }
                }
            } else {
                sb = new StringBuilder(end - start);
                int pos = dstart;
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (keepOriginal && Character.isLowerCase(c))
                        keepOriginal = false;
                    if (isCharAllowed(c, isAttachingToEnd, pos)) {
                        sb.append(Character.toUpperCase(c));
                        pos++;
                    } else {
                        keepOriginal = false;
                    }
                }
            }

            if (keepOriginal)
                return null;

            if (source instanceof Spanned) {
                SpannableString sp = new SpannableString(sb);
                TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                return sp;
            }

            return sb;
        }

        private boolean isCharAllowed(char c, boolean isAttachingToEnd, int pos) {
            return Character.isDigit(c) || (isAttachingToEnd && pos == LuhnUtil.ICCID_LUHN - 1 && Character.toUpperCase(c) == ICCID_FILLER);
        }

    }
}
