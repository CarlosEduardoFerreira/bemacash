package com.kaching123.tcr.commands.print;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.USBPrinter;
import com.kaching123.tcr.commands.device.PrinterInfo;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

/**
 * Created by teli.yin on 2/25/2015.
 */
public class CheckUSBPrinterCommand extends PublicGroundyTask {
    protected static final Uri URI_PRINTER = ShopProvider.getContentWithLimitUri(ShopStore.PrinterTable.URI_CONTENT, 1);
    private final static String EXTRA_PRINTER = "EXTRA_PRINTER";

    @Override
    protected TaskResult doInBackground() {
        if (getPrinter().equalsIgnoreCase(USBPrinter.USB_DESC))
            return succeeded();
        else
            return failed();
    }

    protected String getPrinter() {
        String st = null;
        Cursor c = ProviderAction.query(URI_PRINTER)
                .projection(
                        ShopStore.PrinterTable.IP
                )
                .where(ShopStore.PrinterTable.ALIAS_GUID + " IS NULL")
                .perform(getContext());
        PrinterInfo printerInfo = null;
        if (c.moveToFirst()) {

            st = c.getString(0);

        }
        c.close();
        return st;
    }

    public static TaskHandler start(Context context, CheckUSBFindPrinterCallback callback) {
        return create(CheckUSBPrinterCommand.class).callback(callback).queueUsing(context);
    }

    public static abstract class CheckUSBFindPrinterCallback {

        @OnSuccess(CheckUSBPrinterCommand.class)
        public void onSuccess() {
            onSearchFinished();
        }

        @OnFailure(CheckUSBPrinterCommand.class)
        public void onFailure() {
            onFailureFound();
        }

        protected abstract void onSearchFinished();

        protected abstract void onFailureFound();
    }
}
