package com.kaching123.tcr.model;

/**
 * Created by pkabakov on 12.03.14.
 */
public interface BarcodeListenerHolder {

    public void setBarcodeListener(BarcodeListener barcodeListener);

    public void setDefaultBarcodeListener();

    public interface BarcodeListener {
        public void onBarcodeReceived(String barcode);
    }

}
