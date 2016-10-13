package com.kaching123.tcr.countries.puertorico;

import com.kaching123.tcr.countries.CountryFunctionality;
import com.kaching123.tcr.print.builder.DigitalOrderBuilder;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;
import com.kaching123.tcr.print.processor.PrintOrderProcessor;
import com.telly.groundy.PublicGroundyTask;

/**
 * Created by alboyko on 27.09.2016.
 */

public class PuertoRicoSettings implements CountryFunctionality {
    @Override
    public PosOrderTextPrinter getOrderPrinter() {
        return new PosPuertoricoOrderTextPrinter();
    }

    @Override
    public PosOrderTextPrinter getPosKitchenPrinter() {
        return new PosPuertoricoKitchenPrinter();
    }

    @Override
    public DigitalOrderBuilder getDigitalOrderBuilder() {
        return new DigitalOrderBuilder();
    }

    @Override
    public boolean isMultiTaxGroup() {
        return true;
    }

    @Override
    public PrintOrderProcessor getOrderOrderProcessor(String orderGuid, PublicGroundyTask.IAppCommandContext appCommandContext) {
        return new PuertoricoPrintProcessor(orderGuid, appCommandContext);
    }
    @Override
    public String currencySymbol() {
        return "$";
    }

    @Override
    public String currencySymbolUTF() {
        return "$";
    }
}
