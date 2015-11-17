package com.kaching123.tcr.print.printer;

import com.kaching123.pos.printer.PrintLineAction;
import com.kaching123.pos.util.IReportsPrinter;

/**
 * Created by vkompaniets on 27.01.14.
 */
public class PosReportsMatrixPrinter extends PosReportsPrinter implements IReportsPrinter {
    @Override
    public void header(String header) {
        boldString(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, header)));
    }
}
