package com.kaching123.tcr.countries.costarica;

import com.kaching123.tcr.countries.CountryFunctionality;
import com.kaching123.tcr.print.builder.DigitalOrderBuilder;
import com.kaching123.tcr.print.printer.PosKitchenPrinter;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;
import com.telly.groundy.PublicGroundyTask;

/**
 * Created by alboyko on 27.09.2016.
 */

public class CostaRicaSettings implements CountryFunctionality {

    @Override
    public PosOrderTextPrinter getOrderPrinter() {
        return new PosOrderTextPrinter();
    }

    @Override
    public PosOrderTextPrinter getPosKitchenPrinter() {
        return new PosKitchenPrinter();
    }

    @Override
    public DigitalOrderBuilder getDigitalOrderBuilder() {
        return new DigitalOrderBuilder();
    }

    @Override
    public boolean isMultiTaxGroup() {
        return false;
    }

    @Override
    public PrintOrderProcessor getOrderOrderProcessor(String orderGuid, PublicGroundyTask.IAppCommandContext appCommandContext) {
        return new PrintOrderProcessor(orderGuid, appCommandContext);
    }

    @Override
    public String currencySymbol() {
        return "CRC";
    }
}
