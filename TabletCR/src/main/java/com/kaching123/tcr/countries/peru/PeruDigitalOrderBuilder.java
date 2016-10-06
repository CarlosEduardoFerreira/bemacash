package com.kaching123.tcr.countries.peru;

import com.kaching123.tcr.print.builder.DigitalOrderBuilder;

import java.math.BigDecimal;
import java.util.List;

import static com.kaching123.tcr.print.FormatterUtil.commaFormat;
import static com.kaching123.tcr.print.printer.BasePosTextPrinter.crop;

/**
 * Created by alboyko on 04.10.2016.
 */

public class PeruDigitalOrderBuilder extends DigitalOrderBuilder {
    public static final int PRINTER_MAX_TEXT_LEN = 41;
    protected static final int PRINTER_MAX_PRICE_LEN = 12;
    protected static final int PRINTER_MAX_QTY_LEN = 6;
    protected static final int PRINTER_FOUR_QTY_LEN = 4;
    protected static final int PRINTER_MAX_DATE_LEN = 22;


    public PeruDigitalOrderBuilder() {
        super();
    }

    public void addHeader(String description, String qty, String discount, String price, String unitPrice){
        stringBuilder.append(_styled("table", TABLE_FULL_STYLE));
        stringBuilder.append("<tr>");

        stringBuilder.append("<td>");
        stringBuilder.append(qty);
        stringBuilder.append("</td>");

        stringBuilder.append("<td>");
        stringBuilder.append(description);
        stringBuilder.append("</td>");


        stringBuilder.append("<td>");
        stringBuilder.append(unitPrice);
        stringBuilder.append("</td>");

        stringBuilder.append("<td>");
        stringBuilder.append(discount);
        stringBuilder.append("</td>");

        stringBuilder.append("<td>");
        stringBuilder.append(price);
        stringBuilder.append("</td>");

        stringBuilder.append("</tr>");
        stringBuilder.append("</table>");
    }

    public void add(String title, String qty, BigDecimal discount, BigDecimal price, BigDecimal itemPrice, List<String> units) {

        int maxLen = PRINTER_MAX_TEXT_LEN;
        int priceLen =   PRINTER_MAX_PRICE_LEN;
        int qtyLen =  PRINTER_MAX_QTY_LEN;

        int maxLeftPart = maxLen - priceLen * 2;
        int maxTitleLen = maxLeftPart - qtyLen;
        String printTitle;
        String printQty = qty;
        String printItemPrice = commaFormat(itemPrice);
        String printDiscount = commaFormat(discount);
        String printPrice = commaFormat(price);

        if (printQty.length() < 8 - qty.length()) {
            printQty+=' ';
        }

        if (title.length() > maxTitleLen) {
            printTitle= crop(maxTitleLen, title);
        } else {
            printTitle = title;
        }

     /*   if (printTitle.length() < maxLeftPart) {
            for (int i = printTitle.length(); i < maxLeftPart; i++) {
                spacePrintTitle+=' ';
            }
        }

        for (int i = 0; i < priceLen - printItemPrice.length() - 4; i++) {
            spacePrintItemPrice += ' ';
        }

        for (int i = 0; i < 5 - printDiscount.length() ; i++) {
            spacePrintDiscount += ' ';
        }*/


        stringBuilder.append(_styled("table", TABLE_FULL_STYLE));
        stringBuilder.append("<tr>");

        stringBuilder.append("<td>");
        stringBuilder.append(printQty);
        stringBuilder.append("</td>");

        stringBuilder.append("<td>");
        stringBuilder.append(printTitle);
        stringBuilder.append("</td>");


        stringBuilder.append("<td>");
        stringBuilder.append(printItemPrice);
        stringBuilder.append("</td>");

        stringBuilder.append("<td>");
        stringBuilder.append(printDiscount);
        stringBuilder.append("</td>");

        stringBuilder.append("<td>");
        stringBuilder.append(printPrice);
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
}
