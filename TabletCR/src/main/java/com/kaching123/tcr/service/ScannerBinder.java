package com.kaching123.tcr.service;

import android.os.Binder;

/**
 * Created by long.jiao on 12/7/2015.
 */
public abstract class ScannerBinder extends Binder implements IScannerBinder {
    @Override
    public void setScannerListener(ScannerListener scannerListener) {
        this.setScannerListener(scannerListener);
    }
    @Override
    public boolean tryReconnectScanner() {
        return false;
    }

    @Override
    public void disconnectScanner() {
    }
}
