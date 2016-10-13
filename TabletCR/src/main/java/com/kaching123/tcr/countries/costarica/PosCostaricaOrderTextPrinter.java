package com.kaching123.tcr.countries.costarica;

import com.kaching123.tcr.print.printer.PosOrderTextPrinter;

/**
 * Created by alboyko on 13.10.2016.
 */

public class PosCostaricaOrderTextPrinter extends PosOrderTextPrinter{
    public PosCostaricaOrderTextPrinter() {
        super();
        add(new DrawCurrencySignAction());
    }
}
