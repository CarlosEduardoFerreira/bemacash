package com.kaching123.tcr.commands.device;

import android.content.Context;

import com.kaching123.pos.PosPrinter;
import com.kaching123.pos.drawer.WaitForCloseAction;
import com.kaching123.tcr.Logger;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by pkabakov on 09.12.13.
 */
public class WaitForCloseDrawerCommand extends BaseDeviceCommand {

    private static final long DRAWER_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

    private static final String RESULT_CLOSE_ERROR = "RESULT_CLOSE_ERROR";

    private WaitForCloseAction waitForCloseAction;

    @Override
    protected TaskResult executeInner(PosPrinter printer) throws IOException {
        Logger.d("PrinterCommand: WaitForCloseDrawerCommand execute");
        if (isEmulate()) {
            Logger.d("PrinterCommand: WaitForCloseDrawerCommand emulate mode!!!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
            if (isQuitting()) {
                return cancelled();
            }
            return succeeded();
        }

        boolean checkDrawerStatus = getApp().getShopInfo().drawerClosedForSale;
        if (!checkDrawerStatus){
            Logger.d("PrinterCommand: WaitForCloseDrawerCommand doesn't wait");
            return succeeded();
        }

        Logger.d("PrinterCommand: WaitForCloseDrawerCommand before WaitForCloseAction");
        waitForCloseAction = new WaitForCloseAction(getApp().getDrawerClosedValue(), DRAWER_TIMEOUT);
        boolean isClosed = waitForCloseAction.execute(printer);
        Logger.d("PrinterCommand: WaitForCloseDrawerCommand after WaitForCloseAction = %b", isClosed);
        if (isQuitting()) {
            return cancelled();
        }
        return isClosed ? succeeded() : failed().add(RESULT_CLOSE_ERROR, true);
    }

    @Override
    protected void onCancel() {
        if (waitForCloseAction != null)
            waitForCloseAction.cancel();
    }

    public static TaskHandler start(Context context, /*CallbacksManager manager,*/ BaseWaitForCloseDrawerCallback callback) {
        return create(WaitForCloseDrawerCommand.class)/*.callbackManager(manager)*/.callback(callback).queueUsing(context);
    }

    public static abstract class BaseWaitForCloseDrawerCallback {

        @OnSuccess(WaitForCloseDrawerCommand.class)
        public void onSuccess() {
            onDrawerClosed();
        }

        @OnFailure(WaitForCloseDrawerCommand.class)
        public void onFailure(@Param(PrinterCommand.EXTRA_ERROR_PRINTER) PrinterError error, @Param(RESULT_CLOSE_ERROR) boolean isCloseError) {
            if(error == PrinterError.NOT_CONFIGURED){
                onDrawerClosed();
                return;
            }
            if(isCloseError){
                onDrawerTimeoutError();
                return;
            }
            onDrawerCloseError(error);
        }

        protected abstract void onDrawerClosed();

        protected abstract void onDrawerTimeoutError();

        protected abstract void onDrawerCloseError(PrinterError error);
    }
}
