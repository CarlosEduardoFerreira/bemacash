package com.kaching123.tcr.commands.display;

import android.content.Context;

import com.kaching123.display.printers.DisplayPrinterWrapper;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.fragment.settings.FindDeviceFragment;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler;
import com.kaching123.tcr.function.OrderTotalPriceCursorQuery;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 25.02.14.
 */
public class DisplayOrderCommand extends BaseDisplayCommand<DisplayPrinterWrapper> {

    private final String orderGuid;

    public DisplayOrderCommand(String orderGuid) {
        this.orderGuid = orderGuid;
    }

    @Override
    protected DisplayPrinterWrapper getPrinterWrapper(Context context) {
        return new DisplayPrinterWrapper(getSerialPortDisplaySet(context));
    }

    @Override
    protected void printBody(final Context context, final DisplayPrinterWrapper printerWrapper) {
        OrderTotalPriceCursorQuery.loadSync(context, orderGuid, new Handler() {

            @Override
            public void handleItem(String saleItemGuid, String description, BigDecimal qty, BigDecimal itemPriceWithAddons, BigDecimal itemSubTotal, BigDecimal itemTotal, BigDecimal itemFinalPrice, BigDecimal itemFinalDiscount, BigDecimal itemFinalTax) {
            }

            @Override
            public void handleTotal(BigDecimal totalDiscount, BigDecimal subTotalItemTotal, BigDecimal totalTaxVatValue, BigDecimal totalOrderPrice, BigDecimal tipsValue) {
                if (totalDiscount != null && totalDiscount.compareTo(BigDecimal.ZERO) != 0)
                    printerWrapper.add(context.getString(R.string.display_total_title), context.getString(R.string.display_discount_title), totalOrderPrice, totalDiscount);
                else
                    printerWrapper.add(context.getString(R.string.display_total_title), totalOrderPrice);
            }
        });
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
