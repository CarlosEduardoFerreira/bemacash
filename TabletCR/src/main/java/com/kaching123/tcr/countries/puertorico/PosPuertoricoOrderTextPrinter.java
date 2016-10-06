package com.kaching123.tcr.countries.puertorico;

import com.kaching123.tcr.print.printer.PosOrderTextPrinter;

/**
 * Created by alboyko on 23.09.2016.
 */

public class PosPuertoricoOrderTextPrinter extends PosOrderTextPrinter {

        public PosPuertoricoOrderTextPrinter() {
            super();
        }
/*
    public void add(String title1, String qty, String price, List<String> units) {
        String title = qty + " " + title1;

        int maxLeftPart = PRINTER_MAX_TEXT_LEN - PRINTER_MAX_PRICE_LEN;

        int maxTitleLen = maxLeftPart - PRINTER_MAX_QTY_LEN;

        int maxLineLen = maxTitleLen - title.length();

        if (maxLineLen < 0) {
            do {
                if (title.length() > maxTitleLen) {
                    String subString = title.substring(0, maxTitleLen);
                    title = title.substring(maxTitleLen);
                    add(new PrintLineAction(subString));
                } else {
                    add(new PrintLineAction(formatStringMultiLineLastPart(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, price)));
                    break;
                }
            } while (true);
        } else {
            add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, qty, price)));
        }

        if (units == null || units.isEmpty())
            return;
        for (String unit : units) {
            add(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, "Serial : " + unit)));
        }
    }


        public void add(String title, String qty, String Iva, String discount,String totalPrice, String itemPrice, List<String> units) {
            add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, qty, Iva, discount,totalPrice, itemPrice)));

            if (units == null || units.isEmpty())
                return;
            for (String unit : units) {
                add(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, "Serial :" + unit)));
            }
        }

        public void add(String title, BigDecimal qty, String Iva, String discount, BigDecimal totalPrice, BigDecimal itemPrice, List<String> units) {
            add(title, quantityFormat.format(qty), Iva, discount,commaFormat(totalPrice), commaFormat(itemPrice), units);
        }

        @Override
        public void add(String title, BigDecimal qty, BigDecimal price, BigDecimal unitPrice, String unitsLabel, boolean isUnitPrice, List<String> units) {
            if (!isUnitPrice && qty.compareTo(BigDecimal.ONE) == 0)
                add(title, commaFormat(price));
            else {
                add(new PrintLineAction(formatStringTitle(title)));
                add(new PrintLineAction(formatLabelString(PRINTER_MAX_TEXT_LEN, quantityFormat.format(qty), commaFormat(unitPrice), unitsLabel, commaFormat(price))));
            }
            if (units == null || units.isEmpty())
                return;
            for (String unit : units) {
                add(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, "Serial : " + unit)));
            }
        }

        void addHeaderTitle(String title, String qty, String Iva, String discount, String price, String unitPrice) {
            add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, qty, Iva, discount, price, unitPrice)));
        }

        public void addBodyItem(String title, String qty, String Iva, String discount, String price, String unitPrice, List<String> units) {
            add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, qty, Iva, discount, price, unitPrice)));
        }

        void addPeru(String title, String qty, String isVal, BigDecimal discount, BigDecimal price, BigDecimal itemPrice, List<String> units) {
            add(new PrintLineAction(formatStringEcua(42, PRINTER_MAX_PRICE_LEN, PRINTER_MAX_QTY_LEN, title, qty, isVal, commaFormat(itemPrice),  commaFormat(discount),  commaFormat(price))));

            if (units == null || units.isEmpty())
                return;
            for (String unit : units) {
                add(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, "Serial : " + unit)));
            }
        }

        @Override
        public void addAddsOn(String title, BigDecimal price) {
            add(new PrintLineAction(formatEcString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, PRINTER_FOUR_QTY_LEN, title, commaFormat(price))));
        }

        @Override
        public void payment(String label, BigDecimal amount) {
            add(new PrintLineAction(formatEcCash(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, label, commaFormat(amount))));
        }

        public void add(String title, String price) {
            add(new PrintLineAction(formatEcCash(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, title, price)));
        }

        public void addItemDiscount(String title, BigDecimal discount) {
            add(title, commaFormat(discount));
        }

        public void addCashBack(String title, BigDecimal discount) {
            addItemDiscount(title, discount);
        }

        @Override
        public void orderFooter(String label, BigDecimal price) {
           // add(label, commaPriceFormat(price));
            add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, label, commaPriceFormat(price == null ? BigDecimal.ZERO : price))));
        }*/

    }
