package com.kaching123.tcr.countries;

import com.kaching123.tcr.print.builder.DigitalOrderBuilder;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;
import com.telly.groundy.PublicGroundyTask;

/**
 * Created by alboyko on 27.09.2016.
 */

public interface CountryFunctionality {
    PosOrderTextPrinter getOrderPrinter();
    PosOrderTextPrinter getPosKitchenPrinter();
    DigitalOrderBuilder getDigitalOrderBuilder();
    boolean isMultiTaxGroup();
    PrintOrderProcessor getOrderOrderProcessor(String orderGuid, PublicGroundyTask.IAppCommandContext appCommandContext);
    String currencySymbol();
    String currencySymbolUTF();
}
