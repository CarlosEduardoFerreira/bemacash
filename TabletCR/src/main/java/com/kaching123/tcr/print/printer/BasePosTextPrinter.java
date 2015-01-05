package com.kaching123.tcr.print.printer;

import com.kaching123.pos.Action;
import com.kaching123.pos.PosPrinter;
import com.kaching123.pos.printer.BarcodeAction;
import com.kaching123.pos.printer.BarcodeHeightAction;
import com.kaching123.pos.printer.BarcodeTextBelowPositionAction;
import com.kaching123.pos.printer.CenterAlignment;
import com.kaching123.pos.printer.EmphasizedModeAction;
import com.kaching123.pos.printer.FullPaperCutAction2;
import com.kaching123.pos.printer.InitPrintAction;
import com.kaching123.pos.printer.LeftAlignment;
import com.kaching123.pos.printer.LogoAction;
import com.kaching123.pos.printer.PrintLineAction;
import com.kaching123.pos.printer.SelectDoublePrintModeAction;
import com.kaching123.pos.util.IPrinter;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;

/**
 * Created by vkompaniets on 14.01.14.
 */
public class BasePosTextPrinter implements IPrinter {

    public static final DecimalFormat quantityFormat = new DecimalFormat("0.###");
    public static final DecimalFormat priceFormat = new DecimalFormat("0.00");
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.US);
    public static final String SPACES_2 = "  ";

    static {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        priceFormat.setDecimalFormatSymbols(otherSymbols);
        quantityFormat.setDecimalFormatSymbols(otherSymbols);
    }

    public static final int PRINTER_MAX_TEXT_LEN = 41;

    protected static final int PRINTER_MAX_PRICE_LEN = 8;
    protected static final int PRINTER_MAX_QTY_LEN = 6;
    protected static final int PRINTER_FOUR_QTY_LEN = 4;
    protected static final int PRINTER_MAX_DATE_LEN = 22;

    private ArrayList<Action> commands = new ArrayList<Action>();

    protected BasePosTextPrinter() {
        add(new InitPrintAction());
        //add(new SelectPOSAction());
        //add(new SelectPOSUtf8Action());

        //init barcode
        add(new BarcodeTextBelowPositionAction());
        add(new BarcodeHeightAction(96));
    }

    protected void add(Action action) {
        if (action == null)
            return;
        commands.add(action);
    }

    protected void boldDoubleString(PrintLineAction line) {
        add(new SelectDoublePrintModeAction(true));
        add(new EmphasizedModeAction(true));
        add(line);

        add(new EmphasizedModeAction(false));
        add(new SelectDoublePrintModeAction(false));
    }

    protected void boldString(PrintLineAction line) {
        add(new EmphasizedModeAction(true));
        add(line);
        add(new EmphasizedModeAction(false));
    }

    @Override
    public void print(PosPrinter posPrinter) throws IOException {
        for (Action s : commands) {
            System.out.println("|" + s + "|");
            if (posPrinter != null) {
                s.execute(posPrinter);
            }
        }
        if (posPrinter != null) {
            new FullPaperCutAction2().execute(posPrinter);
        }
        //new PrintAndPaperFeedAction().execute(posPrinter);
        //new FullPaperCutAction().execute(posPrinter);
        //TODO need to cut paper
    }

    @Override
    public void emptyLine(int c) {
        for (int i = 0; i < c; i++) {
            add(new PrintLineAction(""));
        }
    }

    @Override
    public void emptyLine() {
        emptyLine(1);
    }

    @Override
    public void drawLine() {
        add(new PrintLineAction(fillString(PRINTER_MAX_TEXT_LEN, '-')));
    }

    @Override
    public void drawDoubleLine() {
        commands.add(new PrintLineAction(fillString(PRINTER_MAX_TEXT_LEN, '=')));
    }

    @Override
    public void barcode(String text) {
        add(new CenterAlignment());
        add(new BarcodeAction(text));
        add(new LeftAlignment());
    }

    @Override
    public void logo() {
        add(new CenterAlignment());
        add(new LogoAction());
        add(new LeftAlignment());
    }

    public static String crop(int maxTextLen, String label) {
        if (label == null)
            label = "";

        if (label.length() > maxTextLen) {
            return label.substring(0, maxTextLen);
        }
        return label;
    }

    public static String fillString(int maxLen, char ch) {
        StringBuilder builder = new StringBuilder(maxLen);
        for (int i = 0; i < maxLen; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }

    public static String formatString(int maxLen, int priceLen, int qtyLen, String title, String qty, String price) {
        if (title == null)
            title = "";

        StringBuilder printTitle = new StringBuilder();

//        for (int i = qtyLen - 1; i > qty.length(); i--) {
//            printTitle.append(' ');
//        }
        printTitle.append(qty);

        int maxLeftPart = maxLen - priceLen;
        printTitle.append(' ');

        int maxTitleLen = maxLeftPart - qtyLen;
        if (title.length() > maxTitleLen) {
            printTitle.append(crop(maxTitleLen, title));
        } else {
            printTitle.append(title);
        }

        if (printTitle.length() < maxLeftPart) {
            for (int i = printTitle.length(); i < maxLeftPart; i++) {
                printTitle.append(' ');
            }
        }
        for (int i = 0; i < priceLen - price.length(); i++) {
            printTitle.append(' ');
        }
//        printTitle.append("$");
        printTitle.append(price);
        return printTitle.toString();
    }

    public static String formatString(int maxLen, int priceLen, int qtyLen, String title, BigDecimal price) {
        if (title == null)
            title = "";

        StringBuilder printTitle = new StringBuilder();

        for (int i = qtyLen - 1; i > 0; i--) {
            printTitle.append(" ");
        }

        int maxLeftPart = maxLen - priceLen;
        printTitle.append(' ');

        int maxTitleLen = maxLeftPart - qtyLen;
        if (title.length() > maxTitleLen) {
            printTitle.append(crop(maxTitleLen, title));
        } else {
            printTitle.append(title);
        }

        if (printTitle.length() < maxLeftPart) {
            for (int i = printTitle.length(); i < maxLeftPart; i++) {
                printTitle.append(' ');
            }
        }
        int dolloarSignLen = (price.compareTo(BigDecimal.ZERO)) > 0 ? 1 : 0;
        String priceStr = commaPriceFormat(price);
        String[] parts = title.split(" ");
        int partsSpace = parts.length == 0 ? 0 : parts.length - 1;
        for (int i = 1; i < priceLen - price.toString().length() - dolloarSignLen - partsSpace; i++) {
            printTitle.append(' ');
        }
        if (dolloarSignLen > 0) {
            printTitle.append(priceStr);
        }
        return printTitle.toString();
    }

    public static String formatString_Header(int maxLen, int rightBlockLen, String left, String fixedRight) {
        if (left == null)
            left = "";
        if (fixedRight == null)
            fixedRight = "";

        StringBuilder printTitle = new StringBuilder(left);
        int maxTitleLen = maxLen - rightBlockLen;
        if (printTitle.length() > maxTitleLen) {
            printTitle.setLength(maxTitleLen);
        } else {
            for (int i = printTitle.length(); i < maxTitleLen; i++) {
                printTitle.append(' ');
            }
        }
        for (int i = 0; i < rightBlockLen - fixedRight.length(); i++) {
            printTitle.append(' ');
        }
        printTitle.append(fixedRight);
        return printTitle.toString();
    }

    public static String formatString(int maxLen, int rightBlockLen, String left, String fixedRight) {
        if (left == null)
            left = "";
        if (fixedRight == null)
            fixedRight = "";

        StringBuilder printTitle = new StringBuilder(left);
        int maxTitleLen = maxLen - rightBlockLen;
        if (printTitle.length() > maxTitleLen) {
            printTitle.setLength(maxTitleLen);
        } else {
            for (int i = printTitle.length(); i < maxTitleLen; i++) {
                printTitle.append(' ');
            }
        }
        for (int i = 0; i < rightBlockLen - fixedRight.length(); i++) {
            printTitle.append(' ');
        }
        printTitle.append(fixedRight);
        return printTitle.toString();
    }

    public static String formatStringTitle(String left) {
        if (left == null)
            left = "";

        StringBuilder printTitle = new StringBuilder(left);

        return printTitle.toString();
    }

    public static String formatHolderString(int maxLen, int rightBlockLen, String left, String fixedRight) {
        if (left == null)
            left = "";
        if (fixedRight == null)
            fixedRight = "";

        StringBuilder printTitle = new StringBuilder(left);
        int maxTitleLen = maxLen - rightBlockLen;
        if (printTitle.length() > maxTitleLen) {

        } else {
            for (int i = printTitle.length(); i < maxTitleLen; i++) {
                printTitle.append(' ');
            }
        }
        for (int i = 0; i < rightBlockLen - fixedRight.length(); i++) {
            printTitle.append(' ');
        }
        printTitle.append(fixedRight);
        return printTitle.toString();
    }

    public static String formatRightString(int maxLen, String fixedRight) {
        if (fixedRight == null)
            fixedRight = "";
        return formatString_Header(maxLen, fixedRight.length(), "", fixedRight);
    }

    public static String centerString(int maxLen, String title) {
        if (title == null)
            title = "";
        StringBuilder printTitle = new StringBuilder();
        if (printTitle.length() > maxLen) {
            printTitle.append(title);
            printTitle.setLength(maxLen);
        } else {
            int spaceCount = maxLen - title.length();
            for (int i = 0; i < spaceCount / 2; i++) {
                printTitle.append(' ');
            }
            printTitle.append(title);
            for (int i = 0; i < spaceCount / 2; i++) {
                printTitle.append(' ');
            }
        }
        return printTitle.toString();
    }

    public static String formatString(int maxLen, String left, String right, boolean fixedLeft, int fixedLen) {
        if (fixedLen > maxLen)
            return null;

        StringBuilder line = new StringBuilder();

        int floatLen = maxLen - fixedLen - 1;
        int leftLen = fixedLeft ? fixedLen : floatLen;
        int rightLen = fixedLeft ? floatLen : fixedLen;

        line.append(cropOrFillString(leftLen, left));
        line.append(' ');
        line.append(cropOrFillString(rightLen, right));

        return line.toString();
    }

    public static String cropOrFillString(int len, String string) {
        if (string == null)
            string = "";
        StringBuilder line = new StringBuilder();
        if (string.length() > len) {
            line.append(crop(len, string));
        } else {
            for (int i = string.length(); i < len; i++) {
                line.append(' ');
            }
            line.append(string);
        }

        return line.toString();
    }

}
