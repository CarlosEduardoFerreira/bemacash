package com.kaching123.display.printers;

import android.util.Log;

import com.kaching123.display.Action;
import com.kaching123.display.DisplayPrinter;
import com.kaching123.display.actions.PrintTextAction;
import com.kaching123.display.actions.SelectFirstDigitAction;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by pkabakov on 25.02.14.
 */
public class DisplayPrinterWrapper implements IDisplayPrinterWrapper {

    private static final String TAG = DisplayPrinterWrapper.class.getSimpleName();

    private static final DecimalFormat decimalFormat = new DecimalFormat("0.##");
    private static final DecimalFormat priceFormat = new DecimalFormat("0.00");
    private boolean isSerialPortDisplay;
    static {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        priceFormat.setDecimalFormatSymbols(otherSymbols);
        decimalFormat.setDecimalFormatSymbols(otherSymbols);
    }
    public DisplayPrinterWrapper(boolean isSerialPortDisplay)
    {
        this.isSerialPortDisplay = isSerialPortDisplay;
    }

    private static final char QUANTITY_SEPARATOR = 'x';

    private static final int MAX_TEXT_LEN = 40;
    private static final int MAX_TEXT_LINE_LEN = 20;

    private ArrayList<Action> commands = new ArrayList<Action>();

    @Override
    public void print(DisplayPrinter displayPrinter) throws IOException {
        for (Action command : commands) {
            if (displayPrinter != null)
                command.execute(displayPrinter);
            else
                Log.d(TAG, "|" + command + "|");
        }
    }

    @Override
    public void clear() {
        commands.add(new PrintTextAction(fill(MAX_TEXT_LEN, ' '), isSerialPortDisplay));
        commands.add(new SelectFirstDigitAction(isSerialPortDisplay));
    }

    public void add(String text) {
        commands.add(new PrintTextAction(cropText(MAX_TEXT_LEN, text), isSerialPortDisplay));
    }

    public void addLine(String text) {
        commands.add(new PrintTextAction(cropFillText(MAX_TEXT_LINE_LEN, text), isSerialPortDisplay));
    }
    public void addLine() {
        commands.add(new PrintTextAction(fill(MAX_TEXT_LEN, ' '), isSerialPortDisplay));
    }

    public void add(BigDecimal quantity, String description, BigDecimal total) {
        add(decimalFormat.format(quantity), description, priceFormat.format(total));
    }

    public void add(String quantity, String description, String total) {
        commands.add(new PrintTextAction(formatText(MAX_TEXT_LINE_LEN, quantity, description), isSerialPortDisplay));
        commands.add(new PrintTextAction(formatTextRight(MAX_TEXT_LINE_LEN, total), isSerialPortDisplay));
    }

    public void add(String totalLabel, String discountLabel, BigDecimal orderTotalPrice, BigDecimal orderTotalDiscount) {
        add(totalLabel, discountLabel, priceFormat.format(orderTotalPrice), priceFormat.format(orderTotalDiscount));
    }

    public void add(String totalLabel, BigDecimal orderTotalPrice) {
        add(totalLabel, priceFormat.format(orderTotalPrice));
    }

    public void add(String totalLabel, String orderTotalPrice) {
        commands.add(new PrintTextAction(formatText(MAX_TEXT_LINE_LEN, orderTotalPrice.length(), totalLabel, orderTotalPrice), isSerialPortDisplay));
    }

    public void add(String totalLabel, String discountLabel, String orderTotalPrice, String orderTotalDiscount) {
        commands.add(new PrintTextAction(formatText(MAX_TEXT_LINE_LEN, orderTotalPrice.length(), totalLabel, orderTotalPrice), isSerialPortDisplay));
        commands.add(new PrintTextAction(formatText(MAX_TEXT_LINE_LEN, orderTotalDiscount.length(), discountLabel, orderTotalDiscount), isSerialPortDisplay));
    }

    public static String formatTextRight(int maxTextLen, String text) {
        if (text == null) {
            return fill(maxTextLen, ' ');
        }

        return formatText(maxTextLen, text.length(), "", text);
    }

    public static String formatText(int maxTextLen, int rightTextLen, String leftText, String rightText) {
        if (leftText == null || rightText == null) {
            return fill(maxTextLen, ' ');
        }

        StringBuilder builder = new StringBuilder(leftText);
        int maxLeftTextLen = maxTextLen - rightTextLen;
        if (builder.length() > maxLeftTextLen) {
            builder.setLength(maxLeftTextLen);
        } else {
            for (int i = builder.length(); i < maxLeftTextLen; i++) {
                builder.append(' ');
            }
        }
        for (int i = 0; i < rightTextLen - rightText.length(); i++) {
            builder.append(' ');
        }
        builder.append(rightText);
        return builder.toString();
    }

    public static String formatText(int maxTextLen, String quantity, String description) {
        StringBuilder builder = new StringBuilder();

        builder.append(quantity).append(' ').append(QUANTITY_SEPARATOR).append(' ');

        int maxTitleLen = maxTextLen - builder.length();
        builder.append(cropFillText(maxTitleLen, description));

        return builder.toString();
    }

    public static String fill(int maxTextLen, char symbol) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < maxTextLen; i++)
            builder.append(symbol);
        return builder.toString();
    }

    public static String cropText(int maxTextLen, String text) {
        if (text == null) {
            return "";
        }

        if (text.length() > maxTextLen) {
            text = text.substring(0, maxTextLen);
        }
        return text;
    }

    public static String cropFillText(int maxTextLen, String text) {
        if (text == null) {
            return fill(maxTextLen, ' ');
        }

        StringBuilder builder = new StringBuilder(text);
        if (builder.length() > maxTextLen) {
            builder.setLength(maxTextLen);
        } else {
            for (int i = text.length(); i < maxTextLen; i++)
                builder.append(' ');
        }
        return builder.toString();
    }

    public static String centerText(int maxTextLen, String text) {
        if (text.length() > maxTextLen) {
            return cropText(maxTextLen, text);
        }

        StringBuilder builder = new StringBuilder();
        int spaceCount = maxTextLen - text.length();
        for (int i = 0; i < spaceCount / 2; i++) {
            builder.append(' ');
        }
        builder.append(text);
        for (int i = 0; i < spaceCount / 2; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

}
