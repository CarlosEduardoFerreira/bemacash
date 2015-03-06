package com.kaching123.tcr.commands.display;

import android.content.Context;

import com.kaching123.display.printers.DisplayPrinterWrapper;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.fragment.settings.FindDeviceFragment;

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
    protected DisplayPrinterWrapper getPrinterWrapper(Context context) {
        return new DisplayPrinterWrapper(getSerialPortDisplaySet(context));
    }

    @Override
    protected void printBody(Context context, DisplayPrinterWrapper printerWrapper) {

        printerWrapper.add(context.getString(R.string.display_tender_title), context.getString(R.string.display_pending_title), tenderAmount, pendingAmount);
    }
    @Override
    protected boolean getSerialPortDisplaySet(Context context) {
        if (context == null)
            return false;
        String displayAddress = ((TcrApplication) context.getApplicationContext()).getShopPref().displayAddress().toString();
        if (displayAddress != null && displayAddress.equalsIgnoreCase(FindDeviceFragment.INTEGRATED_DISPLAYER))
            return true;
        return false;
    }
}
