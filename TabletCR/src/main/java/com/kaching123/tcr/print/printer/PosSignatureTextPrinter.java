package com.kaching123.tcr.print.printer;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.pos.printer.PrintLineAction;
import com.kaching123.pos.util.ISignaturePrinter;
import com.kaching123.tcr.R;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by vkompaniets on 13.01.14.
 */
public class PosSignatureTextPrinter extends BasePosTextPrinter implements ISignaturePrinter {

    protected static final int CARD_NAME_LENGTH = 20;
    protected static final int SHIFTED_NUMBER_LENGTH = 19;

    private Context context;

    public PosSignatureTextPrinter(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

    @Override
    public void date(Date date) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_DATE_LEN, getContext().getString(R.string.printer_date), dateFormat.format(date))));
    }

    @Override
    public void cardName(String cardName) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, CARD_NAME_LENGTH, getContext().getString(R.string.printer_card_name), cardName)));
    }

    @Override
    public void authNumber(String authNumber) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, authNumber.length(), getContext().getString(R.string.printer_auth_number), authNumber)));
    }

    @Override
    public void shiftedNumber(String shiftedNumber) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, SHIFTED_NUMBER_LENGTH, getContext().getString(R.string.printer_shifted_number), shiftedNumber)));
    }

    @Override
    public void addWithTab(String left, String right) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, SHIFTED_NUMBER_LENGTH, left, right)));
    }

    @Override
    public void amount(BigDecimal amount) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, getContext().getString(R.string.printer_amount), priceFormat.format(amount))));
    }

    @Override
    public void cashBack(BigDecimal amount) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, getContext().getString(R.string.printer_cashback), priceFormat.format(amount))));
    }

    @Override
    public void total(BigDecimal amount) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_PRICE_LEN, getContext().getString(R.string.printer_total_colon), priceFormat.format(amount))));
    }

    @Override
    public void cropLine(String line) {
        add(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, line)));
    }

    @Override
    public void header(String message) {
        boldDoubleString(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN / 2, message)));//because double
    }

    @Override
    public void header(String guest, String message) {

    }

    @Override
    public void header(String orderPrefix, String registerTitle, int orderSeqNum, Date date, String operatorTitle, String operatorName, String customer, String ci) {
        add(new PrintLineAction(""));
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_DATE_LEN, orderPrefix + " " + registerTitle + "-" + orderSeqNum, dateFormat.format(date))));
        header(operatorTitle, operatorName);
        if (!TextUtils.isEmpty(ci)) {
            header(customer, ci);
        }
    }

    @Override
    public void footer(String label) {
        add(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, label)));
    }

    @Override
    public void footer(String label, boolean bold) {
        footer(label);
    }

    @Override
    public void subTitle(String label) {
        add(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, label)));
    }
}
