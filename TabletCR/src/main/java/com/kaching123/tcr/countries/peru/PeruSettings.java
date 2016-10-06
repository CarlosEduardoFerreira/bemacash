package com.kaching123.tcr.countries.peru;

import com.kaching123.tcr.countries.CountryFunctionality;
import com.kaching123.tcr.print.builder.DigitalOrderBuilder;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;
import com.telly.groundy.PublicGroundyTask;

/**
 * Created by alboyko on 27.09.2016.
 */

public class PeruSettings implements CountryFunctionality {
    @Override
    public PosOrderTextPrinter getOrderPrinter() {
        return new PosPeruOrderTextPrinter();
    }

    @Override
    public PosOrderTextPrinter getPosKitchenPrinter() {
        return new PosPeruKitchenPrinter();
    }

    @Override
    public DigitalOrderBuilder getDigitalOrderBuilder() {
        return new PeruDigitalOrderBuilder();
    }

    @Override
    public boolean isMultiTaxGroup() {
        return true;
    }

    @Override
    public PrintOrderProcessor getOrderOrderProcessor(String orderGuid, PublicGroundyTask.IAppCommandContext appCommandContext) {
        return new PeruPrintProcessor(orderGuid, appCommandContext);
    }
    @Override
    public String currencySymbol() {
        return "S/";
    }
}
