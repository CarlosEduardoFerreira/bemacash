package com.kaching123.tcr.commands.display;

import android.content.Context;

import com.kaching123.display.printers.DisplayPrinterWrapper;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.fragment.settings.FindDeviceFragment;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;

/**
 * Created by pkabakov on 28.02.14.
 */
public class DisplayWelcomeMessageCommand extends BaseDisplayCommand<DisplayPrinterWrapper> {

    @Override
    protected DisplayPrinterWrapper getPrinterWrapper(Context context) {
        return new DisplayPrinterWrapper(getSerialPortDisplaySet(context));
    }

    @Override
    protected void printBody(Context context, DisplayPrinterWrapper printerWrapper) {
        ShopInfo shopInfo = ((TcrApplication) context.getApplicationContext()).getShopInfo();
        Logger.d(shopInfo.displayWelcomeMsg);
        printerWrapper.addLine(shopInfo.displayWelcomeMsg);
        printerWrapper.addLine(shopInfo.displayWelcomeMsgBottom);
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
