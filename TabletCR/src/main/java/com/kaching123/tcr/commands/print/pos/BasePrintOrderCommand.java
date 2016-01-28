package com.kaching123.tcr.commands.print.pos;

import com.kaching123.tcr.print.printer.PosKitchenPrinter;
import com.kaching123.tcr.print.printer.PosOrderTextPrinter;

/**
 * Created by vkompaniets on 14.01.14.
 */
public abstract class BasePrintOrderCommand extends BasePrintCommand<PosOrderTextPrinter>{

    @Override
    protected PosOrderTextPrinter createTextPrinter() {
        return getPrinter().printerType.equalsIgnoreCase("Thermal") ? new PosOrderTextPrinter(): new PosKitchenPrinter();
    }
}
