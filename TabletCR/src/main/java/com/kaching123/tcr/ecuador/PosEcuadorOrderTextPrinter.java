package com.kaching123.tcr.ecuador;

import com.kaching123.pos.printer.PrintLineAction;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;

import java.math.BigDecimal;
import java.util.List;

import static com.kaching123.tcr.print.FormatterUtil.commaFormat;
import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;

public class PosEcuadorOrderTextPrinter extends PosOrderTextPrinter {

    public PosEcuadorOrderTextPrinter() {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < PRINTER_MAX_QTY_LEN + 2; i++) {//need add 2 extra spaces between label
            builder.append(' ');
        }
        qtyHolderText = builder.toString();
    }

    public void add(String title, String qty, String totalPrice, String itemPrice, List<String> units) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, qty, totalPrice, itemPrice)));

        if (units == null || units.isEmpty())
            return;
        for (String unit : units) {
            add(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, "Serial : " + unit)));
        }
    }

    public void add(String title, BigDecimal qty, BigDecimal totalPrice, BigDecimal itemPrice, List<String> units) {
        add(title, quantityFormat.format(qty), commaPriceFormat(totalPrice), commaPriceFormat(itemPrice), units);
    }

    @Override
    public void add(String title, BigDecimal qty, BigDecimal price, BigDecimal unitPrice, String unitsLabel, boolean isUnitPrice, List<String> units) {
        if (!isUnitPrice && qty.compareTo(BigDecimal.ONE) == 0)
            add(title, commaPriceFormat(price));
        else {
            add(new PrintLineAction(formatStringTitle(title)));
            add(new PrintLineAction(formatLabelString(PRINTER_MAX_TEXT_LEN, quantityFormat.format(qty), commaFormat(unitPrice), unitsLabel, commaPriceFormat(price))));
        }
        if (units == null || units.isEmpty())
            return;
        for (String unit : units) {
            add(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, "Serial : " + unit)));
        }
    }

    public void addHeaderTitle(String title, String qty, String price, String unitPrice) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, qty, price, unitPrice)));
    }

    @Override
    public void addAddsOn(String title, BigDecimal price) {
        add(new PrintLineAction(formatEcString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_FOUR_QTY_LEN, title, commaPriceFormat(price))));
    }

    @Override
    public void payment(String label, BigDecimal amount) {
        add(new PrintLineAction(formatEcCash(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, label, commaPriceFormat(amount))));
    }

    public void add(String title, String price) {
        add(new PrintLineAction(formatEcCash(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, title, price)));
    }

    public void addItemDiscount(String title, BigDecimal discount) {
        add(title, commaPriceFormat(discount));
    }

    public void addCashBack(String title, BigDecimal discount) {
        addItemDiscount(title, discount);
    }

    @Override
    public void orderFooter(String label, BigDecimal price) {
        add(label, commaPriceFormat(price));
    }

}
