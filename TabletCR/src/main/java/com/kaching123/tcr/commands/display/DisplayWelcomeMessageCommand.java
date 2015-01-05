package com.kaching123.tcr.commands.display;

import android.content.Context;

import com.kaching123.display.printers.DisplayPrinterWrapper;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;

/**
 * Created by pkabakov on 28.02.14.
 */
public class DisplayWelcomeMessageCommand extends BaseDisplayCommand<DisplayPrinterWrapper> {

    @Override
    protected DisplayPrinterWrapper getPrinterWrapper() {
        return new DisplayPrinterWrapper();
    }

    @Override
    protected void printBody(Context context, DisplayPrinterWrapper printerWrapper) {
        ShopInfo shopInfo = ((TcrApplication)context.getApplicationContext()).getShopInfo();

        printerWrapper.addLine(shopInfo.displayWelcomeMsg);
        printerWrapper.addLine(shopInfo.displayWelcomeMsgBottom);
    }

}
