package com.kaching123.tcr.print.builder;

import com.kaching123.pos.util.IXReportPrinter;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.kaching123.tcr.print.FormatterUtil.commaBracketsPriceFormat;
import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;
import static com.kaching123.tcr.print.FormatterUtil.percentFormat;

/**
 * Created by vkompaniets on 23.01.14.
 */
public class DigitalXReportBuilder extends BaseDigitalBuilder implements IXReportPrinter {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.US);

    private static final String H1_STYLE = "text-align:center;font-style:italic;";
    private static final String PRICE_STYLE = "text-align:right;";

    @Override
    public void header(String label) {
        stringBuilder.append(_styled("h1", H1_STYLE)).append(label).append("</h1>").append("<br>");
    }

    @Override
    public void titledDate(String title, Date date) {
        titledDate(title, dateFormat.format(date));
    }

    @Override
    public void pair(String name, BigDecimal cost, boolean brackets) {
        String formattedCost = brackets ? commaBracketsPriceFormat(cost) : commaPriceFormat(cost);
        pair(name, formattedCost, false);
    }

    @Override
    public void pair(String name, BigDecimal cost) {
        pair(name, cost, false);
    }

    @Override
    public void pair(String left, String right) {
        pair(left, right, false);
    }

    @Override
    public void boldPair(String name, BigDecimal cost, boolean brackets) {
        pair(name, commaPriceFormat(cost), true);
    }

    @Override
    public void percent(BigDecimal percent) {
        percent(percentFormat(percent));
    }

    @Override
    public void subtitle(String name, boolean bold) {
        stringBuilder.append(bold ? _styled("div", BOLD_STYLE) : "<div>").append(name).append("</div>");
    }

    @Override
    public void footer(String footer) {
        stringBuilder.append(_styled("div", CENTER_STYLE)).append(footer).append("</div>");
    }

    @Override
    public void center(String label) {
        stringBuilder.append(_styled("div", CENTER_STYLE)).append(label).append("</div>");
    }

    private void titledDate(String title, String date){
        stringBuilder.append(_styled("table", TABLE_STYLE));
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(title);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", RIGHT_STYLE));
        stringBuilder.append(date);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>");
    }

    public void pair(String name, String cost, boolean bold) {
        String style = bold ? TABLE_STYLE + BOLD_STYLE : TABLE_STYLE;
        stringBuilder.append(_styled("table", style));
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append(name);
        stringBuilder.append("</td>");

        stringBuilder.append(_styled("td", PRICE_STYLE));
        stringBuilder.append(cost);
        stringBuilder.append("</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>");
    }

    private void percent(String percent){
        stringBuilder.append(_styled("div", RIGHT_STYLE)).append(percent).append("</div>");
    }
}
