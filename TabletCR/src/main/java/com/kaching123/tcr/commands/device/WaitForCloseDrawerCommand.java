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
        boolean needSync = getBooleanArg(PrinterCommand.ARG_NEED_SYNC);
        if (isEmulate()) {
            Logger.d("PrinterCommand: WaitForCloseDrawerCommand emulate mode!!!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
            if (isQuitting()) {
                return cancelled();
            }
            return succeeded().add(PrinterCommand.EXTRA_NEED_SYNC, needSync);
        }

        boolean checkDrawerStatus = getApp().getShopInfo().drawerClosedForSale;
        if (!checkDrawerStatus) {
            Logger.d("PrinterCommand: WaitForCloseDrawerCommand doesn't wait");
            return succeeded().add(PrinterCommand.EXTRA_NEED_SYNC, needSync);
        }

        Logger.d("PrinterCommand: WaitForCloseDrawerCommand before WaitForCloseAction");
        waitForCloseAction = new WaitForCloseAction(getApp().getDrawerClosedValue(), DRAWER_TIMEOUT);
        boolean isClosed = waitForCloseAction.execute(printer);
        Logger.d("PrinterCommand: WaitForCloseDrawerCommand after WaitForCloseAction = %b", isClosed);
        if (isQuitting()) {
            return cancelled();
        }
        return isClosed ? succeeded().add(PrinterCommand.EXTRA_NEED_SYNC, needSync) : failed().add(RESULT_CLOSE_ERROR, true);
    }

    @Override
    protected void onCancel() {
        if (waitForCloseAction != null)
            waitForCloseAction.cancel();
    }

    public static TaskHandler start(Context context, boolean needSync,/*CallbacksManager manager,*/ BaseWaitForCloseDrawerCallback callback) {
        return create(WaitForCloseDrawerCommand.class)/*.callbackManager(manager)*/.arg(PrinterCommand.ARG_NEED_SYNC, needSync).callback(callback).queueUsing(context);
    }

    public static abstract class BaseWaitForCloseDrawerCallback {

        @OnSuccess(WaitForCloseDrawerCommand.class)
        public void onSuccess(@Param(PrinterCommand.EXTRA_NEED_SYNC) boolean needSync) {
            onDrawerClosed(needSync);
        }

        @OnFailure(WaitForCloseDrawerCommand.class)
        public void onFailure(@Param(PrinterCommand.EXTRA_ERROR_PRINTER) PrinterError error, @Param(RESULT_CLOSE_ERROR) boolean isCloseError) {
            if (error == PrinterError.NOT_CONFIGURED) {
                onDrawerClosed(true);
                return;
            }
            if (isCloseError) {
                onDrawerTimeoutError();
                return;
            }
            onDrawerCloseError(error);
        }

        protected abstract void onDrawerClosed(boolean needSync);

        protected abstract void onDrawerTimeoutError();

        protected abstract void onDrawerCloseError(PrinterError error);
    }
}
