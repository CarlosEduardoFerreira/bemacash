package com.kaching123.tcr.service;

/**
 * Created by long.jiao on 12/7/2015.
 */
public interface ScannerListener {

    public void onDisconnected();
    public void onBarcodeReceived(String barcode);
}
