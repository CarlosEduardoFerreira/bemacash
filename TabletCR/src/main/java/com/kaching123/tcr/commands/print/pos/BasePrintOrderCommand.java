package com.kaching123.tcr.commands.print.pos;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.ecuador.PosEcuadorKitchenPrinter;
import com.kaching123.tcr.ecuador.PosEcuadorOrderTextPrinter;
import com.kaching123.tcr.print.printer.PosKitchenPrinter;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;

/**
 * Created by vkompaniets on 14.01.14.
 */
public abstract class BasePrintOrderCommand extends BasePrintCommand<PosOrderTextPrinter> {

    @Override
    protected PosOrderTextPrinter createTextPrinter() {
        if (getPrinter() != null) {
            return getPrinter().printerType.equalsIgnoreCase("Thermal") ?
                    (TcrApplication.isEcuadorVersion() ? new PosEcuadorOrderTextPrinter() : new PosOrderTextPrinter())
                    : (TcrApplication.isEcuadorVersion() ? new PosEcuadorKitchenPrinter() : new PosKitchenPrinter());
        }
        return TcrApplication.isEcuadorVersion() ? new PosEcuadorOrderTextPrinter() : new PosOrderTextPrinter();
    }
}
