package com.kaching123.tcr.service;

/**
 * Created by long.jiao on 12/7/2015.
 */
public interface IScannerBinder {

    public void setScannerListener(ScannerListener displayListener);

    public boolean tryReconnectScanner();

    public void disconnectScanner();

}
