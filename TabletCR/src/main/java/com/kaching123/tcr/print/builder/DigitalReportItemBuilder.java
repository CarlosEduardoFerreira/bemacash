package com.kaching123.tcr.print.builder;

import android.text.TextUtils;

import com.kaching123.pos.util.IReportsPrinter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;
import static com.kaching123.tcr.util.DateUtils.dateOnlyFormat;
import static com.kaching123.tcr.util.DateUtils.timeOnlyAttendanceFormat;

/**
 * Created by vkompaniets on 24.01.14.
 */
public class DigitalReportItemBuilder extends BaseDigitalBuilder implements IReportsPrinter {

    private static final DecimalFormat priceFormat = new DecimalFormat("0.00");
    private static final DecimalFormat quantityFormat = new DecimalFormat("0.###");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.US);
    private static final SimpleDateFormat dateRangeFormat = new SimpleDateFormat("MMM d, yyyy, HH:mm", Locale.US);
    protected static final SimpleDateFormat shortDateFormat = new SimpleDateFormat("MMM d, HH:mm", Locale.US);

    static {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        priceFormat.setDecimalFormatSymbols(otherSymbols);
        quantityFormat.setDecimalFormatSymbols(otherSymbols);
    }

    private static final String H1_STYLE = "text-align:center;font-style:italic;";
    private static final String QTY_STYLE = "text-align:right; width: 30px;";
    private static final String PRICE_STYLE = "text-align:right; width: 60px;";
    private static final String DATE_STYLE = "text-align:left; width: 90px;";
    private static final String CODE_STYLE = "text-align:right; width: 130px;";
    private static final String CLOCK_IN_OUT_STYLE = "text-align:right; width: 80px;";
    private static final String FLOAT_LEFT = "float:left";
    private static final String FOOTER_STYLE = "text-align:center;font-size:0.8em;";

    public static final String NBSP = "&nbsp;";

    @Override
    public void add(String title) {
        addStyled(title, null);
    }

    @Override
    public void add(String title, BigDecimal qty, BigDecimal price) {
        add(title, quantityFormat.format(qty), commaPriceFormat(price));
    }

    @Override
    public void add(String title, BigDecimal value) {
        addStyled(title, commaPriceFormat(value), null);
    }

    @Override
    public void add(Date clockIn, Date clockOut, boolean isSameDay) {
        if (isSameDay) {
            addClockInOut("  " + dateOnlyFormat(clockIn), timeOnlyAttendanceFormat(clockIn), clockOut == null ? "-" + NBSP + NBSP : timeOnlyAttendanceFormat(clockOut));
        } else {
            addClockInOut("  " + dateOnlyFormat(clockIn), timeOnlyAttendanceFormat(clockIn), "");
            addClockInOut("", "(" + dateOnlyFormat(clockOut) + ")", timeOnlyAttendanceFormat(clockOut));
        }
    }

    @Override
    public void add(Date date, String title, BigDecimal qty) {
        add2(shortDateFormat.format(date), title, quantityFormat.format(qty));
    }

    @Override
    public void add(String title, BigDecimal onHand, BigDecimal unitCost, BigDecimal totalCost, boolean active) {
        add5Columns(title, quantityFormat.format(onHand), priceFormat.format(unitCost), commaPriceFormat(totalCost), active ? "yes" : "no");
    }

    @Override
    public void add5Columns(String title, String ean, String productCode, BigDecimal qty, BigDecimal revenue) {
        add5Columns(title, ean, productCode, quantityFormat.format(qty), commaPriceFormat(revenue));
    }

    @Override
    public void add(String title, String ean, String productCode, BigDecimal onHand, BigDecimal unitCost, BigDecimal totalCost, boolean active) {
        add7Columns(title, ean, productCode, quantityFormat.format(onHand), commaPriceFormat(unitCost), commaPriceFormat(totalCost), active ? "yes" : "no");
    }

    @Override
    public void add4Columns(String title, BigDecimal qty1, BigDecimal qty2, BigDecimal qty3) {
        add4Columns(title, quantityFormat.format(qty1), quantityFormat.format(qty2), commaPriceFormat(qty3));
    }

    @Override
    public void addHourly(String title, BigDecimal value) {
        add3Columns(title, "", commaPriceFormat(value) + "/hour", false);
    }

    @Override
    public void addBold(String title, BigDecimal value) {
        addStyled(title, commaPriceFormat(value), BOLD_STYLE);
    }

    @Override
    public void addBold(String title) {
        addStyled(title, BOLD_STYLE);
    }

    @Override
    public void addWithTab(String title, BigDecimal value) {
        addStyled(NBSP + title, commaPriceFormat(value), null);
    }

    @Override
    public void addWithTab(String left, String right, boolean bold) {
        add3Columns(left, "", right, bold);
    }

    @Override
    public void addShiftHrs(String label, String hrs) {
        addClockInOut("", label, hrs);
    }

    public void addStyled(String title, String value, String style) {
        stringBuilder.append(style != null ? _styled("tr", style) : "<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(title);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(value);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
    }

    public void addStyled(String title, String style) {
        stringBuilder.append(style != null ? _styled("div", style) : "<div>");
        stringBuilder.append(title);
        stringBuilder.append("</div>");
    }

    public void add3Columns(String left, String center, String right, boolean bold) {
        stringBuilder.append("<tr>");
        stringBuilder.append(bold ? _styled("td", BOLD_STYLE) : "<td>");
        stringBuilder.append(left);
        stringBuilder.append("</td>");

        stringBuilder.append(bold ? _styled("td", BOLD_STYLE) : "<td>");
        stringBuilder.append(center);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", bold ? new String[]{RIGHT_STYLE, BOLD_STYLE} : new String[]{RIGHT_STYLE}));
        stringBuilder.append(right);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
    }

    @Override
    public void header(String header) {
        stringBuilder.append(_styled("h1", H1_STYLE)).append(header).append("</h1>").append("<br>");
    }

    @Override
    public void header(String guest, String message) {

    }

    @Override
    public void startBody() {
        stringBuilder.append(_styled("table", TABLE_STYLE));
    }

    private void add(String title, String qty, String price) {
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(title);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", QTY_STYLE));
        stringBuilder.append(qty);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(price);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
    }

    private void add2(String date, String title, String qty) {
        stringBuilder.append("<tr>");
        stringBuilder.append(_styled("td", DATE_STYLE));
        stringBuilder.append(date);
        stringBuilder.append("</td>");

        stringBuilder.append("<td>");
        stringBuilder.append(title);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", QTY_STYLE));
        stringBuilder.append(qty);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
    }

    private void add4Columns(String float1, String fixed2, String fixed3, String fixed4) {
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(float1);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(fixed2);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(fixed3);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(fixed4);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
    }

    private void add5Columns(String float1, String ean, String prodCode, String fixed4, String fixed5) {
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(float1);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", CODE_STYLE));
        if (!TextUtils.isEmpty(ean)) {
            stringBuilder.append(ean);
        }
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", CODE_STYLE));
        if (!TextUtils.isEmpty(prodCode)) {
            stringBuilder.append(prodCode);
        }
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(fixed4);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(fixed5);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
    }

    private void add7Columns(String float1, String ean, String prodCode, String fixed4, String fixed5, String fixed6, String fixed7) {
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(float1);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", CODE_STYLE));
        if (!TextUtils.isEmpty(ean)) {
            stringBuilder.append(ean);
        }
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", CODE_STYLE));
        if (!TextUtils.isEmpty(prodCode)) {
            stringBuilder.append(prodCode);
        }
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(fixed4);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(fixed5);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(fixed6);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(fixed7);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
    }

    private void addClockInOut(String date, String clockIn, String clockOut) {
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(NBSP + date);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", CLOCK_IN_OUT_STYLE));
        stringBuilder.append(clockIn);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", CLOCK_IN_OUT_STYLE));
        stringBuilder.append(clockOut);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
    }

    @Override
    public void endBody() {
        stringBuilder.append("</table>");
    }

    @Override
    public void total(String label, BigDecimal total) {
        startBody();
        stringBuilder.append("<tr>");
        stringBuilder.append(_styled("td", BOLD_STYLE));
        stringBuilder.append(label);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", QTY_STYLE));
        stringBuilder.append(NBSP);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE, BOLD_STYLE));
        stringBuilder.append(total);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        endBody();
    }

    @Override
    public void total2(String label, BigDecimal total) {
        startBody();
        stringBuilder.append("<tr>");
        stringBuilder.append(_styled("td", BOLD_STYLE));
        stringBuilder.append(label);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", QTY_STYLE, BOLD_STYLE));
        stringBuilder.append(total);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE, BOLD_STYLE));
        stringBuilder.append(NBSP);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        endBody();
    }

    @Override
    public void dateRange(String label, Date start, Date end) {
        stringBuilder.append(_styled("div", FLOAT_LEFT));
        stringBuilder.append(label);
        stringBuilder.append("</div>");

        stringBuilder.append(_styled("div", RIGHT_STYLE));
        stringBuilder.append(String.format("%s - %s", dateRangeFormat.format(start), dateRangeFormat.format(end)));
        stringBuilder.append("</div>");
    }

    @Override
    public void time(String label, Date time) {
        stringBuilder.append(_styled("div", FLOAT_LEFT));
        stringBuilder.append(label);
        stringBuilder.append("</div>");

        stringBuilder.append(_styled("div", RIGHT_STYLE));
        stringBuilder.append(dateFormat.format(time));
        stringBuilder.append("</div>");
    }

    @Override
    public void subTitle(String subtitle) {
        stringBuilder.append(_styled("div", CENTER_STYLE));
        stringBuilder.append(subtitle);
        stringBuilder.append("</div>");
    }

    @Override
    public void subHeader(String itemLabel, String qtyLabel, String revenueLabel) {
        startBody();
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(itemLabel);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", QTY_STYLE));
        stringBuilder.append(qtyLabel);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(revenueLabel);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        endBody();
    }

    @Override
    public void subHeader2(String dateLabel, String titleLabel, String qtyLabel) {
        startBody();
        add2(dateLabel, titleLabel, qtyLabel);
        endBody();
    }

    @Override
    public void subHeader(String itemLabel, String revenueLabel) {
        startBody();
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(itemLabel);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(revenueLabel);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        endBody();
    }

    @Override
    public void subHeader4Columns(String col1, String col2, String col3, String col4) {
        startBody();
        add4Columns(col1, col2, col3, col4);
        endBody();
    }

    @Override
    public void subHeader5Columns(String col1, String col2, String col3, String col4, String col5) {
        add5Columns(col1, col2, col3, col4, col5);
    }

    @Override
    public void subHeader7Columns(String col1, String col2, String col3, String col4, String col5, String col6, String col7) {
        add7Columns(col1, col2, col3, col4, col5, col6, col7);
    }

    @Override
    public void footer(String label, boolean bold) {
        stringBuilder.append(bold ? _styled("div", FOOTER_STYLE, BOLD_STYLE) : _styled("div", FOOTER_STYLE)).append(label).append("</div>");
    }

    @Override
    public void lotoTitle(String label) {

    }

    @Override
    public void footer(String label) {
        footer(label, false);
    }

    @Override
    public void header(String orderPrefix, String registerTitle, int orderSeqNum, Date date, String operatorName) {

    }
}
