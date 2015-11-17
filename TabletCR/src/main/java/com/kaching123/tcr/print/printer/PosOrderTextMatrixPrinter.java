package com.kaching123.tcr.print.printer;

import com.kaching123.pos.printer.PrintLineAction;
import com.kaching123.pos.util.ITextPrinter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;

/**
 * Created by gdubina on 03.12.13.
 */
public class PosOrderTextMatrixPrinter extends PosOrderTextPrinter implements ITextPrinter {

    protected String qtyHolderText;
    protected static final int SHIFTED_NUMBER_LENGTH = 19;

    public PosOrderTextMatrixPrinter() {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < PRINTER_MAX_QTY_LEN + 2; i++) {//need add 2 extra spaces between label
            builder.append(' ');
        }
        qtyHolderText = builder.toString();
    }

    public void add(String title, String qty, String price, List<String> units) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, qty, price)));

        if (units == null || units.isEmpty())
            return;
        for (String unit : units) {
            add(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, "Serial : " + unit)));
        }
    }

    public void add(String title, String price) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, title, price)));
    }

    public void addItemDiscount(String title, BigDecimal discount) {
//        add(qtyHolderText + title, priceFormat.format(discount));
        add(title, commaPriceFormat(discount));
    }

    public void addCashBack(String title, BigDecimal discount) {
//        add(qtyHolderText + title, priceFormat.format(discount));
        addItemDiscount(title, discount);
    }

    @Override
    public void addWithTab(String left, String right, boolean fixedLeft, boolean bold) {
        if (left == null)
            left = "";
        if (right == null)
            right = "";
        PrintLineAction printLineAction = new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, left, right, fixedLeft, fixedLeft ? left.length() : right.length()));
        if (bold) {
            boldString(printLineAction);
        } else {
            add(printLineAction);
        }
    }

    @Override
    public void addWithTab2(String left, String right, boolean fixedLeft, boolean bold) {
        addWithTab(left, right, fixedLeft, bold);
    }

    @Override
    public void add(String title, boolean bold, boolean crop) {
        PrintLineAction printLineAction = new PrintLineAction(crop ? crop(PRINTER_MAX_TEXT_LEN, title) : title);

        if (bold)
            boldString(printLineAction);
        else
            add(printLineAction);
    }

    @Override
    public void addAddsOn(String title, BigDecimal price) {

        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_FOUR_QTY_LEN, title, price)));

    }

    @Override
    public void add(String title, BigDecimal qty, BigDecimal price, List<String> units) {
        add(title, quantityFormat.format(qty), commaPriceFormat(price), units);
    }


    @Override
    public void payment(String label, BigDecimal amount) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, label, commaPriceFormat(amount))));
    }

    @Override
    public void add(String lastFourDidits) {
        add(new PrintLineAction(formatStringTitle(lastFourDidits)));
    }

    @Override
    public void addNotes(String notes) {
        add(new PrintLineAction(formatStirng_notes(PRINTER_MAX_TEXT_LEN, PRINTER_FOUR_QTY_LEN, notes)));
    }

    @Override
    public void change(String label, BigDecimal amount) {
        payment(label, amount);
    }

    @Override
    public void subTitle(String label) {
        add(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, label)));
    }

    @Override
    public void header(String message) {
        boldDoubleString(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN / 2, message)));//because double
    }

    @Override
    public void header(String guest, String message) {
        add(new PrintLineAction(formatHolderString(PRINTER_MAX_TEXT_LEN, message.length(), guest, message)));
    }

    @Override
    public void header(String orderPrefix, String registerTitle, int orderSeqNum, Date date, String operatorTitle, String operatorName) {
        if (orderPrefix != null && !orderPrefix.equalsIgnoreCase(""))
            add(new PrintLineAction(formatString_Header(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_DATE_LEN, orderPrefix + " " + registerTitle + "-" + orderSeqNum, dateFormat.format(date))));
        header(operatorTitle,operatorName);
    }

    @Override
    public void orderFooter(String label, BigDecimal price) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, label, commaPriceFormat(price))));
    }

    @Override
    public void orderFooter(String label, BigDecimal price, boolean bold) {
        orderFooter(label, price);
    }

    @Override
    public void footer(String label) {
        add(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, label)));
    }

    @Override
    public void footer(String label, boolean bold) {
        footer(label);
    }

    public void powerBy(String label) {
        drawLine();
        boldString(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, label)));//double
    }

}
