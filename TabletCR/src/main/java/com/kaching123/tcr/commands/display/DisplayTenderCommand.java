package com.kaching123.tcr.commands.display;

import android.content.Context;

import com.kaching123.display.printers.DisplayPrinterWrapper;
import com.kaching123.tcr.R;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 25.02.14.
 */
public class DisplayTenderCommand extends BaseDisplayCommand<DisplayPrinterWrapper> {

    private final BigDecimal tenderAmount;
    private final BigDecimal changeAmount;

    public DisplayTenderCommand(BigDecimal tenderAmount, BigDecimal changeAmount) {
        this.tenderAmount = tenderAmount;
        this.changeAmount = changeAmount == null ? BigDecimal.ZERO : changeAmount;
    }

    @Override
    protected DisplayPrinterWrapper getPrinterWrapper() {
        return new DisplayPrinterWrapper();
    }

    @Override
    protected void printBody(Context context, DisplayPrinterWrapper printerWrapper) {

        printerWrapper.add(context.getString(R.string.display_tender_title), context.getString(R.string.display_change_title), tenderAmount, changeAmount);
    }

}
