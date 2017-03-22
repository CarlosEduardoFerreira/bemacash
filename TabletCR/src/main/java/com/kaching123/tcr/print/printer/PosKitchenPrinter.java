package com.kaching123.tcr.print.printer;

import android.text.TextUtils;

import com.kaching123.pos.printer.BarcodeTextBelowPositionAction;
import com.kaching123.pos.printer.InitPrintAction;
import com.kaching123.pos.printer.PrintLineAction;
import com.kaching123.pos.util.IKitchenPrinter;

import java.math.BigDecimal;

/**
 * Created by vkompaniets on 14.02.14.
 */
public class PosKitchenPrinter extends PosOrderTextPrinter implements IKitchenPrinter {
    public static final int PRINTER_KITCHEN_MAX_TEXT_LEN = 39;

    public boolean isVoidOrder() {
        return isVoidOrder;
    }

    public void setVoidOrder(boolean voidOrder) {
        isVoidOrder = voidOrder;
    }

    private boolean isVoidOrder;

    public PosKitchenPrinter() {
        add(new InitPrintAction());
        //add(new SelectPOSAction());
        //add(new SelectPOSUtf8Action());

        //init barcode
        add(new BarcodeTextBelowPositionAction());
    }

    @Override
    public void header(String shopName, String registerTitle, String orderTypeLabel, String orderType, String orderNumLabel,
                       int orderSeqNum, String operatorLabel, String operatorName, String stationLabel, String station, String orderHolder, String orderTitle, String phoneLabel, String phone) {
        boldString(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, shopName)));
        if (!TextUtils.isEmpty(orderType)) {
            tabbed(orderTypeLabel, orderType);
        }
        tabbed(orderNumLabel, String.format("%s-%d", registerTitle, orderSeqNum));
        tabbed(stationLabel, station);
        tabbed(operatorLabel, operatorName);
        if (!TextUtils.isEmpty(orderTitle)) {
            tabbed(orderHolder, orderTitle);
        }
        if (!TextUtils.isEmpty(phone)) {
            tabbed(phoneLabel, phone);
        }
        if(isVoidOrder){
            boldString(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, "****VOID****")));
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
