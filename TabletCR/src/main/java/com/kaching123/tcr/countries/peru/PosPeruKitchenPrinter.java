package com.kaching123.tcr.countries.peru;

import com.kaching123.pos.printer.BarcodeTextBelowPositionAction;
import com.kaching123.pos.printer.InitPrintAction;
import com.kaching123.pos.printer.PrintLineAction;
import com.kaching123.pos.util.IKitchenPrinter;

import java.math.BigDecimal;

/**
 * Created by alboyko on 23.09.2016.
 */

public class PosPeruKitchenPrinter extends PosPeruOrderTextPrinter implements IKitchenPrinter {
    public static final int PRINTER_KITCHEN_MAX_TEXT_LEN = 39;

    public PosPeruKitchenPrinter() {
        add(new InitPrintAction());
        //init barcode
        add(new BarcodeTextBelowPositionAction());
    }

    @Override
    public void header(String shopName, String registerTitle, String orderTypeLabel, String orderType, String orderNumLabel,
                       int orderSeqNum, String operatorLabel, String operatorName, String stationLabel, String station, String orderHolder, String orderTitle, String phoneLabel, String phone) {
        boldString(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, shopName)));
        if (orderType != null) {
            tabbed(orderTypeLabel, orderType);
        }
        tabbed(orderNumLabel, String.format("%s-%d", registerTitle, orderSeqNum));
        tabbed(stationLabel, station);
        tabbed(operatorLabel, operatorName);
        if (orderTitle != null) {
            tabbed(orderHolder, orderTitle);
        }
        if (phone != null) {
            tabbed(phoneLabel, phone);
        }
    }

    @Override
    public void header(String message) {
        boldString(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, message)));
    }

    @Override
    public void add(BigDecimal qty, String description) {
        add(new PrintLineAction(quantityFormat.format(qty) + " " + description));
    }


    @Override
    public void addModifier(String description) {
        splitRows(SPACES_2, description);
    }

    @Override
    public void addAddsOn(String description) {
        splitRows(SPACES_4, description);
    }

    @Override
    public void tabbed(String first, String second) {
        add(new PrintLineAction(formatHolderString(PRINTER_KITCHEN_MAX_TEXT_LEN, second.length(), first, second)));
    }

    @Override
    public void center(String message) {
        add(new PrintLineAction(centerString(PRINTER_KITCHEN_MAX_TEXT_LEN, message)));
    }


    private void splitRows(String prefix, String line) {
        int maxLineLen = PRINTER_KITCHEN_MAX_TEXT_LEN - prefix.length();

        if (maxLineLen <= 0)
            return;

        do {
            if (line.length() > maxLineLen) {
                String left = line.substring(0, maxLineLen);
                line = line.substring(maxLineLen);
                add(new PrintLineAction(prefix + left));
            } else {
                add(new PrintLineAction(prefix + line));
                break;
            }
        } while (true);

    }

    @Override
    public void barcode(String text) {
    }

}


