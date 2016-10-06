package com.kaching123.tcr.commands.print.pos;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;

/**
 * Created by vkompaniets on 14.01.14.
 */
public abstract class BasePrintOrderCommand extends BasePrintCommand<PosOrderTextPrinter> {


    /*
    protected PosOrderTextPrinter createTextPrinter() {
        if (getPrinter() != null) {
            return getPrinter().printerType.equalsIgnoreCase("Thermal") ?
                    (TcrApplication.isEcuadorVersion() ? new PosEcuadorOrderTextPrinter() : new PosOrderTextPrinter())
                    : (TcrApplication.isEcuadorVersion() ? new PosEcuadorKitchenPrinter() : new PosKitchenPrinter());
        }
        return TcrApplication.isEcuadorVersion() ? new PosEcuadorOrderTextPrinter() : new PosOrderTextPrinter();
    }*/
    @Override
    protected PosOrderTextPrinter createTextPrinter() {

        if (getPrinter() != null) {
            if (getPrinter().printerType.equalsIgnoreCase("Thermal")) {
                /*if (TcrApplication.isEcuadorVersion()) {
                    return new PosEcuadorOrderTextPrinter();
                } else if (TcrApplication.isPeruVersion()) {
                    return new PosPeruOrderTextPrinter();
                } else {
                    return new PosOrderTextPrinter();
                }*/
                return TcrApplication.getCountryFunctionality().getOrderPrinter();
            } else {
                /*if (TcrApplication.isEcuadorVersion()) {
                    return new PosEcuadorKitchenPrinter();
                } else if (TcrApplication.isPeruVersion()) {
                    return new PosPeruKitchenPrinter();
                } else {
                    return new PosKitchenPrinter();
                }*/
                return TcrApplication.getCountryFunctionality().getPosKitchenPrinter();
            }
        }
        return TcrApplication.getCountryFunctionality().getOrderPrinter();
            /*if(TcrApplication.isEcuadorVersion()) {
                return new PosEcuadorOrderTextPrinter();
            } else if(TcrApplication.isPeruVersion()){
                return new PosPeruOrderTextPrinter();
            } else {
                return new PosOrderTextPrinter();
            }*/
    }
}
