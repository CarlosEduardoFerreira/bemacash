package com.kaching123.tcr.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by gdubina on 04.12.13.
 */
public class PrinterService extends Service {

    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class PrinterBinder extends Binder {

        public void printOrder() {

        }

        public void checkDrawer(IFunctionCallback<Boolean> callback) {
            //executor
        }

        public void openDrawer() {

        }

        public void waitForClose() {

        }


    }

    public static interface IFunctionCallback<T> {
        void onResult(T result);
    }

    /*public static interface IPrinterListener{

    }*/
}
