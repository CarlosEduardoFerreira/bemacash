package com.kaching123.tcr.activity;

import android.support.v4.app.FragmentActivity;

public abstract class SerialPortScannerBaseActivity extends FragmentActivity {

    public abstract void barcodeReceivedFromSerialPort(String barcode);

}
