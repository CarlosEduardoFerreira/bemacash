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
        if(isEmulate()){
            Logger.d("PrinterCommand: try to open drawer emulate mode!!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
            return succeeded();
        }
        Logger.d("PrinterCommand: try to run OpenDrawerAction");
        new OpenDrawerAction().execute(printer);
        Logger.d("PrinterCommand: OpenDrawerAction was executed");
        return succeeded();
    }

    public static void start(Context context, boolean searchByMac, BaseOpenDrawerCallback callback) {
        create(OpenDrawerCommand.class).arg(ARG_SEARCH_BY_MAC, searchByMac).callback(callback).queueUsing(context);
    }

    public static abstract class BaseOpenDrawerCallback{

        @OnSuccess(OpenDrawerCommand.class)
        public void onSuccess(){
            onDrawerOpened();
        }

        @OnFailure(OpenDrawerCommand.class)
        public void onFailure(@Param(PrinterCommand.EXTRA_ERROR_PRINTER) PrinterError error){
            if(error!= null && error == PrinterError.NOT_CONFIGURED){
                onDrawerOpened();
                return;
            }
            if(error!= null && error == PrinterError.IP_NOT_FOUND){
                onDrawerIPnoFound();
                return;
            }
            onDrawerOpenError(error);
        }

        protected abstract void onDrawerIPnoFound();
        protected abstract void onDrawerOpened();
        protected abstract void onDrawerOpenError(PrinterError error);
    }
}
