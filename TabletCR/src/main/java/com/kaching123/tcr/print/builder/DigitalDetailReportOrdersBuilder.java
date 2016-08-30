package com.kaching123.tcr.print.builder;

import android.text.TextUtils;

import com.kaching123.pos.PosPrinter;
import com.kaching123.pos.util.ITextPrinter;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;

/**
 * Created by mboychenko on 25.08.2016.
 */
public class DigitalDetailReportOrdersBuilder extends BaseDigitalBuilder implements ITextPrinter {

    private static final DecimalFormat quantityFormat = new DecimalFormat("0.###");
    private static final DecimalFormat priceFormat = new DecimalFormat("0.00");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.US);
    private static final String SPACES_2 = "&nbsp;&nbsp;";

    static {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        priceFormat.setDecimalFormatSymbols(otherSymbols);
        quantityFormat.setDecimalFormatSymbols(otherSymbols);
    }

    private static final String HR_STYLE2 = "height: 2px;border: 0;background-color:#C0C0C0;";
    private static final String H1_STYLE = "text-align:center;font-style:italic;";
    private static final String PRICE_STYLE = "text-align:right;";
    private static final String BOLD_ITALIC_STYLE = "font-style:italic;font-weight:bold;";
    private static final String POWER_BY_STYLE = "font-style:italic;font-weight:bold;font-size:0.8em;";
    private static final String FOOTER_STYLE = "text-align:center;font-size:0.8em;";
    private static final String HEADER_STYLE = "font-size:0.8em;";
    private boolean hasHeader;

    private void add(String title, String qty, String price, List<String> units) {
        stringBuilder.append(_styled("table", TABLE_STYLE));
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(qty).append(' ').append(title);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(price);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        for (String serial : units) {
            stringBuilder.append("<tr>");
            stringBuilder.append("<td>");
            stringBuilder.append("Serial : ").append(serial);
            stringBuilder.append("</td>");
            stringBuilder.append("</tr>");
        }
        stringBuilder.append("</table>");
    }

    private void add(String title, String price) {
        stringBuilder.append(_styled("table", TABLE_STYLE));
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append("&nbsp;").append(' ').append(title);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(price);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>");
    }

    @Override
    public void add(String title, boolean bold, boolean crop) {
        stringBuilder.append(_styled("div", bold ? BOLD_STYLE : "", crop ? CROP_STYLE : ""));
        stringBuilder.append("<p>").append(title).append("</p>");
        stringBuilder.append("</div>");
    }

    @Override
    public void addAddsOn(String title, BigDecimal price) {
        stringBuilder.append(_styled("table", TABLE_STYLE));
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append("&nbsp;").append(' ').append(title);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(price);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>");
    }

    @Override
    public void addCashBack(String title, BigDecimal price) {
        addItemDiscount(title, price);
    }

    @Override
    public void add(String title, BigDecimal qty, BigDecimal totalPrice, BigDecimal itemPrice, List<String> units) {
        add(title, quantityFormat.format(qty), priceFormat.format(totalPrice), units);
    }

    @Override
    public void add(String title, BigDecimal qty, BigDecimal price, BigDecimal unitPrice, String unitsLabel, boolean isUntiPrice, List<String> units) {
        if (!isUntiPrice && qty.compareTo(BigDecimal.ONE) == 0)
            add(title, commaPriceFormat(price));
        else {
            add(title);
            stringBuilder.append(_styled("table", TABLE_STYLE));
            stringBuilder.append("<tr>");
            stringBuilder.append("<td>");
            stringBuilder.append("  ").append(qty).append(" " + unitsLabel).append(" @ ").append("1 " + unitsLabel).append(" /" + unitPrice);
            stringBuilder.append("</td>");

            stringBuilder.append(_styled("td", PRICE_STYLE));
            stringBuilder.append(price);
            stringBuilder.append("</td>");
            stringBuilder.append("</tr>");

        }
        for (String serial : units) {
            stringBuilder.append("<tr>");
            stringBuilder.append("<td>");
            stringBuilder.append("Serial : ").append(serial);
            stringBuilder.append("</td>");
            stringBuilder.append("</tr>");
        }
        stringBuilder.append("</table>");
    }

    @Override
    public void addItemDiscount(String title, BigDecimal discoutn) {
        add(SPACES_2 + title, priceFormat.format(discoutn));
    }

    @Override
    public void addWithTab(String left, String right, boolean fixedLeft, boolean bold) {
        stringBuilder.append(_styled("table", TABLE_FULL_STYLE, bold ? BOLD_STYLE : ""));
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(left);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", RIGHT_STYLE));
        stringBuilder.append(right);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>");
    }

    @Override
    public void addWithTab2(String left, String right, boolean fixedLeft, boolean bold) {
        stringBuilder.append(_styled("table", TABLE_STYLE, bold ? BOLD_STYLE : ""));
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(left);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", RIGHT_STYLE));
        stringBuilder.append(right);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>");
    }

    @Override
    public void header(String message) {
        if(!hasHeader) {
//        stringBuilder.append(_styled("div", H1_STYLE)).append(message).append("</div>");
            stringBuilder.append(_styled("h1", H1_STYLE)).append(message).append("</h1>").append("<br>");
            hasHeader = true;
        }
    }

    @Override
    public void header(String guest, String message) {
        if(!hasHeader) {
//        stringBuilder.append(_styled("div", H1_STYLE)).append(message).append("</div>");
            stringBuilder.append(_styled("h1", H1_STYLE)).append(message).append("</h1>").append("<br>");
            hasHeader = true;
        }

    }

    @Override
    public void header(String orderPrefix, String registerTitle, int orderSeqNum, Date date,
                       String operatorTitle, String operatorName,
                       String customerTitle, String customerIdentification) {
        stringBuilder.append(_styled("table", TABLE_STYLE));
        stringBuilder.append("<tr>");
        stringBuilder.append(_styled("td", HEADER_STYLE));
        stringBuilder.append(orderPrefix).append(' ').append(registerTitle).append('-').append(orderSeqNum);
        stringBuilder.append("</td>");
        stringBuilder.append(_styled("td", HEADER_STYLE, RIGHT_STYLE));
        stringBuilder.append(dateFormat.format(date));
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");

        stringBuilder.append("<tr>");
        stringBuilder.append(_styled("td", HEADER_STYLE));
        stringBuilder.append(operatorTitle);
        stringBuilder.append("</td>");
        stringBuilder.append(_styled("td", HEADER_STYLE, RIGHT_STYLE));
        stringBuilder.append(operatorName);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");

        if (!TextUtils.isEmpty(customerIdentification)) {
            stringBuilder.append("<tr>");
            stringBuilder.append(_styled("td", HEADER_STYLE));
            stringBuilder.append(customerTitle);
            stringBuilder.append("</td>");
            stringBuilder.append(_styled("td", HEADER_STYLE, RIGHT_STYLE));
            stringBuilder.append(customerIdentification);
            stringBuilder.append("</td>");
            stringBuilder.append("</tr>");
        }

//        stringBuilder.append(_styled("div", HEADER_STYLE, RIGHT_STYLE)).append(operatorName).append("</div>").append("<br>");
        stringBuilder.append("</table>");
    }

    @Override
    public void drawDoubleLine() {
        stringBuilder.append(_styled("hr", HR_STYLE2));
        stringBuilder.append(_styled("hr", HR_STYLE2));
    }

    public void orderFooter(String label, BigDecimal price) {
        orderFooter(label, price, false);
    }

    @Override
    public void orderFooter(String label, BigDecimal price, boolean bold) {
        stringBuilder.append(_styled("table", TABLE_STYLE));
        stringBuilder.append(bold ? _styled("tr", BOLD_STYLE) : "<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(label);
        stringBuilder.append("</td>");
        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(priceFormat.format(price));
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>");
    }

    @Override
    public void footer(String label) {
        footer(label, false);
    }

    @Override
    public void footer(String label, boolean bold) {
        stringBuilder.append(bold ? _styled("div", FOOTER_STYLE, BOLD_STYLE) : _styled("div", FOOTER_STYLE)).append(label).append("</div>");
    }

    public void powerBy(String label) {
        drawLine();
        stringBuilder.append(_styled("div", POWER_BY_STYLE)).append(label).append("</div>");
    }

    @Override
    public void emptyLine() {
        emptyLine(1);
    }

    @Override
    public void emptyLine(int c) {
        for (int i = 0; i < c; i++)
            stringBuilder.append("<br>");
    }

    @Override
    public void payment(String cardName, BigDecimal amount) {
        stringBuilder.append(_styled("table", TABLE_STYLE));
        stringBuilder.append(_styled("tr", BOLD_ITALIC_STYLE));
        stringBuilder.append("<td>");
        stringBuilder.append(cardName);
        stringBuilder.append("</td>");
        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(priceFormat.format(amount));
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>");
    }

    @Override
    public void add(String content) {
        stringBuilder.append(_styled("table", TABLE_STYLE));
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(content);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>");
    }

    @Override
    public void addNotes(String notes, String noteWords) {
        stringBuilder.append(_styled("table", TABLE_STYLE));
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append("&nbsp;").append(' ').append(noteWords);
        stringBuilder.append("</td>");
        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(notes);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>");
    }

    @Override
    public void change(String cardName, BigDecimal amount) {
        payment(cardName, amount);
    }

    @Override
    public void subTitle(String label) {
        stringBuilder.append(_styled("div", CENTER_STYLE)).append(label).append("</div>");

    }

    @Override
    public void print(PosPrinter posPrinter) throws IOException {
    }
}
