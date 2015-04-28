package com.kaching123.tcr.commands.device;

import android.content.Context;

import com.kaching123.pos.PosPrinter;
import com.kaching123.pos.drawer.OpenDrawerAction;
import com.kaching123.tcr.Logger;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.IOException;

/**
 * Created by gdubina on 04.12.13.
 */
public class OpenDrawerCommand extends BaseDeviceCommand {

    @Override
    protected TaskResult executeInner(PosPrinter printer) throws IOException {
        Logger.d("PrinterCommand: try to open drawer");
        boolean needSync = getBooleanArg(ARG_NEED_SYNC);
        if (isEmulate()) {
            Logger.d("PrinterCommand: try to open drawer emulate mode!!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
            return succeeded().add(PrinterCommand.EXTRA_NEED_SYNC, needSync);
        }
        Logger.d("PrinterCommand: try to run OpenDrawerAction");
        new OpenDrawerAction().execute(printer);
        Logger.d("PrinterCommand: OpenDrawerAction was executed");
        return succeeded().add(PrinterCommand.EXTRA_NEED_SYNC, needSync);
    }

    public static void start(Context context, boolean searchByMac, BaseOpenDrawerCallback callback, boolean needSync) {
        create(OpenDrawerCommand.class).arg(ARG_SEARCH_BY_MAC, searchByMac).arg(ARG_NEED_SYNC, needSync).callback(callback).queueUsing(context);
    }

    public static abstract class BaseOpenDrawerCallback {

        @OnSuccess(OpenDrawerCommand.class)
        public void onSuccess(@Param(PrinterCommand.EXTRA_NEED_SYNC) boolean needSync) {
            onDrawerOpened(needSync);
        }

        @OnFailure(OpenDrawerCommand.class)
        public void onFailure(@Param(PrinterCommand.EXTRA_ERROR_PRINTER) PrinterError error) {
            if (error != null && error == PrinterError.NOT_CONFIGURED) {
                onDrawerOpened(true);
                return;
            }
            if (error != null && error == PrinterError.IP_NOT_FOUND) {
                onDrawerIPnoFound();
                return;
            }
            onDrawerOpenError(error);
        }

        protected abstract void onDrawerIPnoFound();

        protected abstract void onDrawerOpened(boolean needSync);

        protected abstract void onDrawerOpenError(PrinterError error);
    }
}
