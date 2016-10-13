package com.kaching123.tcr.countries.peru;

import com.kaching123.pos.printer.PrintLineAction;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.print.FormatterUtil.commaFormat;
import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;

public class PosPeruOrderTextPrinter extends PosOrderTextPrinter {

        public PosPeruOrderTextPrinter() {
            super();
        }

        public void add(String title, String qty, /*String Iva,*/ String discount,String totalPrice, String itemPrice, List<String> units) {
            add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, qty, /*Iva,*/ "", discount,totalPrice, itemPrice)));

            if (units == null || units.isEmpty())
                return;
            for (String unit : units) {
                add(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, "Serial : " + unit)));
            }
        }

    @Override
    public void header(String orderPrefix, String registerTitle, int orderSeqNum, Date date, String operatorTitle, String operatorName, String customerTitle, String customerIdentification) {
        if (orderPrefix != null && !orderPrefix.equalsIgnoreCase(""))
            add(new PrintLineAction(formatString_Header(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_DATE_LEN, orderPrefix + " " + registerTitle + "-" + orderSeqNum, dateFormat.format(date))));
        header(operatorTitle, operatorName);
    }

        public void add(String title, BigDecimal qty, String discount, BigDecimal totalPrice, BigDecimal itemPrice, List<String> units) {
            add(title, quantityFormat.format(qty), discount,commaFormat(totalPrice), commaFormat(itemPrice), units);
        }

        @Override
        public void add(String title, BigDecimal qty, BigDecimal price, BigDecimal unitPrice, String unitsLabel, boolean isUnitPrice, List<String> units) {
            if (!isUnitPrice && qty.compareTo(BigDecimal.ONE) == 0)
                add(title, commaFormat(price));
            else {
                add(new PrintLineAction(formatStringTitle(title)));
                add(new PrintLineAction(formatLabelString(PRINTER_MAX_TEXT_LEN, quantityFormat.format(qty), commaPriceFormat(unitPrice), unitsLabel, commaPriceFormat(price))));
            }
            if (units == null || units.isEmpty())
                return;
            for (String unit : units) {
                add(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, "Serial : " + unit)));
            }
        }

        void addHeaderTitle(String title, String qty, String discount, String price, String unitPrice) {
            add(new PrintLineAction(formatStringPeruHeader(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, qty, discount, price, unitPrice)));
        }

        void addPeru(String title, String qty, BigDecimal discount, BigDecimal price, BigDecimal itemPrice, List<String> units) {
            add(new PrintLineAction(formatStringPeruItem(42, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, qty, commaFormat(itemPrice),  commaFormat(discount),  commaFormat(price))));

            if (units == null || units.isEmpty())
                return;
            for (String unit : units) {
                add(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, "Serial : " + unit)));
            }
        }

    void printLoyalty(String name, String value) {
        add(name, value);
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
