package com.kaching123.tcr.print.printer;

import com.kaching123.pos.printer.EmphasizedModeAction;
import com.kaching123.pos.printer.PrintLineAction;
import com.kaching123.pos.util.IXReportPrinter;

import java.math.BigDecimal;
import java.util.Date;

import static com.kaching123.tcr.print.FormatterUtil.commaBracketsPriceFormat;
import static com.kaching123.tcr.print.FormatterUtil.commaPriceFormat;
import static com.kaching123.tcr.print.FormatterUtil.percentFormat;

/**
 * Created by vkompaniets on 21.01.14.
 */
public class PosXReportTextPrinter extends BasePosTextPrinter implements IXReportPrinter {

    @Override
    public void header(String label) {
        boldDoubleString(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN / 2, label)));
    }

    @Override
    public void titledDate(String title, Date date) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_DATE_LEN, title, dateFormat.format(date))));
    }

    @Override
    public void pair(String name, BigDecimal cost, boolean brackets) {
        String formattedCost = brackets ? commaBracketsPriceFormat(cost) : commaPriceFormat(cost);
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, formattedCost.length(), name, formattedCost)));
    }

    @Override
    public void pair(String name, BigDecimal cost) {
        pair(name, cost, false);
    }
    /**
     * @param tabSize
     * Should be from 1 to 3 tabulations.
     */
    @Override
    public void subPair(String name, BigDecimal cost, int tabSize, boolean bold) {
        StringBuilder sb = new StringBuilder();
        sb.ensureCapacity(40);

        if (tabSize <= 1)
            tabSize = 1;
        if (tabSize > 3)
            tabSize = 3;

        for (int i = 0; i < tabSize; i++) {
            sb.append("  ");
        }
        sb.append(name);

        if (bold) {
            boldPair(sb.toString(), cost, false);
        } else {
            pair(sb.toString(), cost);
        }
    }

    @Override
    public void pair(String left, String right) {
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, right.length(), left, right)));
    }

    @Override
    public void boldPair(String name, BigDecimal cost, boolean brackets) {
        add(new EmphasizedModeAction(true));
        pair(name, cost, brackets);
        add(new EmphasizedModeAction(false));
    }

    @Override
    public void percent(BigDecimal percent) {
        String right = percentFormat(percent);
        add(new PrintLineAction(formatRightString(PRINTER_MAX_TEXT_LEN, right)));
    }

    @Override
    public void percent(String name, BigDecimal percent) {
        String right = percentFormat(percent);
        add(new PrintLineAction(formatString(PRINTER_MAX_TEXT_LEN, right.length(), name, right)));
    }

    @Override
    public void subtitle(String name, boolean bold) {
        if (bold) {
            boldString(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, name)));
        } else {
            add(new PrintLineAction(crop(PRINTER_MAX_TEXT_LEN, name)));
        }
    }

    @Override
    public void footer(String footer) {
        add(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, footer)));
    }

    @Override
    public void center(String label) {
        add(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, label)));
    }

}
