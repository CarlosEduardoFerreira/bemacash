package com.kaching123.tcr.countries.puertorico;

import com.kaching123.pos.printer.PrintLineAction;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;

import java.util.Date;

/**
 * Created by alboyko on 23.09.2016.
 */

public class PosPuertoricoOrderTextPrinter extends PosOrderTextPrinter {

        public PosPuertoricoOrderTextPrinter() {
            super();
        }

    @Override
    public void header(String orderPrefix, String registerTitle, int orderSeqNum, Date date, String operatorTitle, String operatorName, String customerTitle, String customerIdentification) {
        if (orderPrefix != null && !orderPrefix.equalsIgnoreCase(""))
            add(new PrintLineAction(formatString_Header(PRINTER_MAX_TEXT_LEN, PRINTER_MAX_DATE_LEN, orderPrefix + " " + registerTitle + "-" + orderSeqNum, dateFormat.format(date))));
        header(operatorTitle, operatorName);
    }
}
