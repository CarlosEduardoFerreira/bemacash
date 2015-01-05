package com.kaching123.tcr.commands.display;

import android.content.Context;

import com.kaching123.display.printers.DisplayPrinterWrapper;
import com.kaching123.tcr.R;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 25.02.14.
 */
public class DisplayPartialTenderCommand extends BaseDisplayCommand<DisplayPrinterWrapper> {

    private final BigDecimal tenderAmount;
    private final BigDecimal pendingAmount;

    public DisplayPartialTenderCommand(BigDecimal tenderAmount, BigDecimal pendingAmount) {
        this.tenderAmount = tenderAmount;
        this.pendingAmount = pendingAmount;
    }

    @Override
    protected DisplayPrinterWrapper getPrinterWrapper() {
        return new DisplayPrinterWrapper();
    }

    @Override
    protected void printBody(Context context, DisplayPrinterWrapper printerWrapper) {

        printerWrapper.add(context.getString(R.string.display_tender_title), context.getString(R.string.display_pending_title), tenderAmount, pendingAmount);
    }

}
