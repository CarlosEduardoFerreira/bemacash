package com.kaching123.tcr.print.printer;

import com.kaching123.pos.printer.PrintLineAction;
import com.kaching123.pos.util.IXReportPrinter;

/**
 * Created by vkompaniets on 21.01.14.
 */
public class PosXReportTextMatrixPrinter extends PosXReportTextPrinter implements IXReportPrinter {

    @Override
    public void header(String label) {
        boldString(new PrintLineAction(centerString(PRINTER_MAX_TEXT_LEN, label)));
    }

}
