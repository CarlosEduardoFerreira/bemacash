package com.kaching123.tcr.print.printer;

import com.kaching123.pos.printer.PrintLineAction;
import com.kaching123.pos.util.IReportsPrinter;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;
import static com.kaching123.tcr.util.DateUtils.dateOnlyFormat;
import static com.kaching123.tcr.util.DateUtils.timeOnlyAttendanceFormat;

/**
 * Created by vkompaniets on 27.01.14.
 */
public class PosReportsPrinter extends BasePosTextPrinter implements IReportsPrinter {

    protected static final SimpleDateFormat shortDateFormat = new SimpleDateFormat("MMM d, HH:mm", Locale.US);
    protected static final SimpleDateFormat superShortDateFormat = new SimpleDateFormat("M/d/yy  HH:mm", Locale.US);

    protected static final int PRINTER_MAX_QTY_LEN = 7;
    protected static final int SHORT_DATE_LEN = 13;
    protected static final int TIME_LEN = 5;
    protected static final int ATTENDANCE_TIME_LEN = 8;

    @Override
    public void header(String header) {
        boldDoubleString(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN / 2, header)));
    }

    @Override
    public void header(String guest, String message) {

    }

    @Override
    public void startBody() {
        //do nothing
    }

    @Override
    public void add(String title) {
        add(new PrintLineAction(title));
    }

    @Override
    public void add(String title, BigDecimal qty, BigDecimal price) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, quantityFormat.format(qty),commaPriceFormat(price))));
    }

    @Override
    public void add(String title, BigDecimal value) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, title,commaPriceFormat(value))));
    }

    @Override
    public void add(Date clockIn, Date clockOut, boolean isSameDay) {
        if (isSameDay) {
            add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, ATTENDANCE_TIME_LEN + 3, ATTENDANCE_TIME_LEN + 3, "  " + dateOnlyFormat(clockIn), timeOnlyAttendanceFormat(clockIn), timeOnlyAttendanceFormat(clockOut) == null ? "-  " : timeOnlyAttendanceFormat(clockOut))));
        } else {
            add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, ATTENDANCE_TIME_LEN + 3, ATTENDANCE_TIME_LEN + 3, "  " + dateOnlyFormat(clockIn), timeOnlyAttendanceFormat(clockIn), "")));
            add(new PrintLineAction(formatRightString(PRINTER_MAX_TEXT_LEN, "  (" + dateOnlyFormat(clockOut) + ") " + timeOnlyAttendanceFormat(clockOut))));
        }
    }

    @Override
    public void add(Date date, String title, BigDecimal qty) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_QTY_LEN, SHORT_DATE_LEN, title, shortDateFormat.format(date), quantityFormat.format(qty))));
    }

    @Override
    public void add(String title, BigDecimal onHand, BigDecimal unitCost, BigDecimal totalCost, boolean active) {

    }

    @Override
    public void add(String title, String ean, String productCode, BigDecimal onHand, BigDecimal unitCost, BigDecimal totalCost, boolean active) {

    }

    @Override
    public void add5Columns(String title, String ean, String productCode, BigDecimal qty, BigDecimal revenue) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, quantityFormat.format(qty), commaPriceFormat(revenue))));
    }

    @Override
    public void add4Columns(String title, BigDecimal qty1, BigDecimal qty2, BigDecimal qty3) {
        add(new PrintLineAction(format4ColumnsString(PRINTER_MAX_TEXT_LEN, 9, 9, 9, title, quantityFormat.format(qty1), quantityFormat.format(qty2), commaPriceFormat(qty3))));
    }

    @Override
    public void addHourly(String title, BigDecimal value) {
        final String appendix = "/hour";
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN + appendix.length(), title, commaPriceFormat(value) + appendix)));
    }

    @Override
    public void addBold(String title, BigDecimal value) {
        boldString(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, title, commaPriceFormat(value))));
    }

    @Override
    public void addBold(String title) {
        boldString(new PrintLineAction(title));
    }

    @Override
    public void addWithTab(String title, BigDecimal value) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, SPACES_2 + title,commaPriceFormat(value))));
    }

    @Override
    public void addWithTab(String left, String right, boolean bold) {
        if (right.length() > PRINTER_MAX_TEXT_LEN - 2)
            return;

        PrintLineAction line = new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, right.length(), left, right));
        if(bold){
            boldString(line);
        }else{
            add(line);
        }
    }

    @Override
    public void addShiftHrs(String label, String hrs) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, ATTENDANCE_TIME_LEN + 3, ATTENDANCE_TIME_LEN + 3, "", label, hrs)));
    }

    @Override
    public void endBody() {
        //do nothing
    }

    @Override
    public void total(String label, BigDecimal total) {
        boldString(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, label, commaPriceFormat(total))));
    }

    @Override
    public void total2(String label, BigDecimal total) {
        boldString(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, label, quantityFormat.format(total), "")));
    }


    @Override
    public void dateRange(String label, Date start, Date end) {
        String dateRange = String.format("%s - %s", superShortDateFormat.format(start), superShortDateFormat.format(end));
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, dateRange.length(), label, dateRange)));
    }

    @Override
    public void time(String label, Date time) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_DATE_LEN, label, dateFormat.format(time))));
    }

    @Override
    public void subHeader(String itemLabel, String qtyLabel, String revenueLabel) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, itemLabel, qtyLabel, revenueLabel)));
    }

    @Override
    public void subHeader2(String dateLabel, String titleLabel, String qtyLabel) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_QTY_LEN, SHORT_DATE_LEN, titleLabel, dateLabel, qtyLabel)));
    }

    @Override
    public void subHeader(String itemLabel, String revenueLabel) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, itemLabel, revenueLabel)));
    }

    @Override
    public void subHeader4Columns(String col1, String col2, String col3, String col4) {
        add(new PrintLineAction(format4ColumnsString(PRINTER_MAX_TEXT_LEN, 9, 9, 9, col1, col2, col3, col4)));
    }

    @Override
    public void subHeader5Columns(String title, String ean, String code, String qty, String revenue) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, qty, revenue)));
    }

    @Override
    public void subHeader7Columns(String col1, String col2, String col3, String col4, String col5, String col6, String col7) {

    }

    @Override
    public void header(String orderPrefix, String registerTitle, int orderSeqNum, Date date, String operatorName) {

    }

    @Override
    public void footer(String label) {
        footer(label, false);
    }

    @Override
    public void footer(String label, boolean bold) {
        add(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, label)));
    }

    @Override
    public void subTitle(String label) {
        add(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, label)));
    }

    public static String formatString(int maxLen, int priceLen, int qtyLen, String title, String qty, String price) {
        StringBuilder printTitle = new StringBuilder(title);

        int maxTitleLen = maxLen - qtyLen - priceLen - 2; //2 spaces
        if (printTitle.length() > maxTitleLen){
            printTitle.setLength(maxTitleLen);
        }else{
            for (int i = printTitle.length(); i < maxTitleLen; i++){
                printTitle.append(' ');
            }
        }

        printTitle.append(' ');

        if (qty.length() < qtyLen){
            for (int i = qty.length(); i < qtyLen; i++){
                printTitle.append(' ');
            }
        }
        printTitle.append(qty);

        printTitle.append(' ');

        if (price.length() < priceLen){
            for (int i = price.length(); i < priceLen; i++){
                printTitle.append(' ');
            }
        }
        printTitle.append(price);

        return printTitle.toString();
    }

    public static String formatDateRange(int maxLen, int dateLength, String leftDate, String rightDate){
        StringBuilder printTitle = new StringBuilder(leftDate);

        int spaceCnt = (maxLen - 2 * dateLength - 2)/2;
        for (int i = 0; i < spaceCnt + 1; i++){
            printTitle.append(' ');
        }
        printTitle.append("--");
        for (int i = 0; i < spaceCnt + 1; i++){
            printTitle.append(' ');
        }

        printTitle.append(rightDate);

        return printTitle.toString();
    }

    public static String format4ColumnsString(int maxLen, int len2, int len3, int len4, String float1, String fixed2, String fixed3, String fixed4){
        StringBuilder printLine = new StringBuilder(float1);

        int maxLen1 = maxLen - len2 - len3 - len4 - 3; //3 spaces between columns
        if (maxLen1 < 0)
            return "";

        if (float1.length() > maxLen1){
            printLine.setLength(maxLen1);
        }else{
            for (int i = float1.length(); i < maxLen1; i++){
                printLine.append(' ');
            }
        }

        int[] lengths = new int[]{len2, len3, len4};
        String[] strings = new String[]{fixed2, fixed3, fixed4};

        for (int i = 0; i < 3; i++){
            printLine.append(' ');
            for (int k = strings[i].length(); k < lengths[i]; k++){
                printLine.append(' ');
            }
            printLine.append(strings[i]);
        }

        return printLine.toString();
    }
}
