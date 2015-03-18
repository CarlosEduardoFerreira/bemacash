package com.kaching123.tcr.commands.device;

import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.pos.PosPrinter;
import com.kaching123.pos.USBPrinter;
import com.kaching123.pos.drawer.OpenDrawerAction;
import com.kaching123.pos.drawer.WaitForCloseAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnCallback;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by gdubina on 04.12.13.
 */
public class WaitForCashInDrawerCommand extends BaseDeviceCommand {

    private static final long DRAWER_TIMEOUT = TimeUnit.MINUTES.toMillis(3);

    private static final String CALLBACK_OPENED = "DRAWER_OPENED";
    private static final String RESULT_CLOSE_ERROR = "RESULT_CLOSE_ERROR";

    private WaitForCloseAction waitForCloseAction;

    @Override
    protected TaskResult executeInner(PosPrinter printer) throws IOException {
        Logger.d("PrinterCommand: WaitForCashInDrawerCommand execute");
        if (isEmulate()) {
            Logger.d("PrinterCommand: WaitForCashInDrawerCommand emulate mode!!!");
            callback(CALLBACK_OPENED);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
            return succeeded();
        }

        Logger.d("PrinterCommand: WaitForCashInDrawerCommand before OpenDrawerAction");
        new OpenDrawerAction().execute(printer);
        Logger.d("PrinterCommand: WaitForCashInDrawerCommand after OpenDrawerAction");
        if (getPrinterID().equalsIgnoreCase(USBPrinter.USB_DESC))
            return succeeded();
        callback(CALLBACK_OPENED);

        boolean checkDrawerStatus = getApp().getShopInfo().drawerClosedForSale;
        if (!checkDrawerStatus){
            Logger.d("PrinterCommand: WaitForCashInDrawerCommand doesn't wait");
            return succeeded();
        }
        Logger.d("PrinterCommand: WaitForCashInDrawerCommand before WaitForCloseAction");
        waitForCloseAction = new WaitForCloseAction(getApp().getDrawerClosedValue(), DRAWER_TIMEOUT);
        boolean isClosed = waitForCloseAction.execute(printer);
        Logger.d("PrinterCommand: WaitForCashInDrawerCommand after WaitForCloseAction = %b", isClosed);
        if (isQuitting()) {
            Logger.d("PrinterCommand: WaitForCashInDrawerCommand cancelled!");
            return cancelled();
        }

        return isClosed ? succeeded() : failed().add(RESULT_CLOSE_ERROR, true);
    }

    @Override
    protected void onCancel() {
        Logger.d("WaitForCashInDrawerCommand:  onCancel()");
        if (waitForCloseAction != null) {
            waitForCloseAction.cancel();
        }
    }

    public static TaskHandler start(Context context, boolean searchByMac, BaseWaitForCashInDrawerCallback callback) {
        Logger.d("WaitForCashInDrawerCommand: start(): callback: " + callback);
        return create(WaitForCashInDrawerCommand.class).arg(ARG_SEARCH_BY_MAC, searchByMac).callback(callback).queueUsing(context);
    }

    public static abstract class BaseWaitForCashInDrawerCallback {

        @OnSuccess(WaitForCashInDrawerCommand.class)
        public void onSuccess() {
            Logger.d("BaseWaitForCashInDrawerCallback: onSuccess()");
            onDrawerClosed();
        }

        @OnFailure(WaitForCashInDrawerCommand.class)
        public void onFailure(@Param(PrinterCommand.EXTRA_ERROR_PRINTER) PrinterError error, @Param(RESULT_CLOSE_ERROR) boolean isCloseError) {
            if(error == PrinterError.NOT_CONFIGURED){
                Logger.d("BaseWaitForCashInDrawerCallback: onFailure() NOT_CONFIGURED ignore error");
                onDrawerOpened();
                onDrawerClosed();
                return;
            }
            if(error!= null && error == PrinterError.IP_NOT_FOUND){
                onDrawerIPnotFound();
                return;
            }
            if(isCloseError){
                onDrawerTimeoutError();
                return;
            }
            Logger.d("BaseWaitForCashInDrawerCallback: onFailure()");
            onDrawerCloseError(error);
        }

        @OnCallback(value = WaitForCashInDrawerCommand.class, name = WaitForCashInDrawerCommand.CALLBACK_OPENED)
        public void onCallback() {
            Logger.d("BaseWaitForCashInDrawerCallback: onDrawerOpened()");
            onDrawerOpened();
        }

        protected abstract void onDrawerIPnotFound();
        protected abstract void onDrawerOpened();
        protected abstract void onDrawerClosed();
        protected abstract void onDrawerTimeoutError();
        protected abstract void onDrawerCloseError(PrinterError error);
    }
    protected String getPrinterID() {
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
}
