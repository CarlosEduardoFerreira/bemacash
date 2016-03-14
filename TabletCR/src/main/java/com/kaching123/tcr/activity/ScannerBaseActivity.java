package com.kaching123.tcr.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.settings.FindDeviceFragment;
import com.kaching123.tcr.service.ScannerService;
import com.kaching123.tcr.service.ScannerService.IScannerBinder;
import com.kaching123.tcr.service.ScannerService.ScannerBinder;
import com.kaching123.tcr.service.ScannerService.ScannerListener;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by pkabakov on 14.03.14.
 */
public abstract class ScannerBaseActivity extends SuperBaseActivity implements IScannerBinder {

    private ScannerBinder scannerBinder;

    protected abstract void onBarcodeReceived(String barcode);

    private boolean isUSBScanner;
    protected static final String EXTRA_ACTION = "ACTION";
    protected static final String EXTRA_ERROR = "ERROR";
    protected static final String EXTRA_ERRORMSG = "ERRORMSG";
    protected static final String EXTRA_TRANSACTIONID = "TRANSACTIONID";
    protected static final String EXTRA_ITEMNAME = "ITEMNAME";
    protected static final String EXTRA_ITEMDETAILS = "ITEMDETAILS";
    protected static final String EXTRA_ITEMQTY = "ITEMQTY";
    protected static final String EXTRA_ITEMPRICE = "ITEMPRICE";
    protected static final String EXTRA_ITEMTAXABLE = "ITEMTAXABLE";
    protected static final String EXTRA_SERVICEFEE = "SERVICEFEE";
    protected static final String EXTRA_RECEIPT = "RECEIPT";


    @Override
    protected void onStart() {
        super.onStart();

        bindToScannerService();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindFromScannerService();
    }

    private void bindToScannerService() {
        Logger.d("ScannerBaseActivity: bindToScannerService()");
        boolean scannerConfigured = !TextUtils.isEmpty(getApp().getShopPref().scannerAddress().get());

        if (scannerConfigured) {
            if (getApp().getShopPref().scannerAddress().get().equalsIgnoreCase(FindDeviceFragment.USB_SCANNER_ADDRESS))
                setUSBScanner(true);
            else if (!getApp().getShopPref().scannerAddress().get().equalsIgnoreCase(FindDeviceFragment.SEARIL_PORT_SCANNER_ADDRESS))
                ScannerService.bind(this, scannerServiceConnection);
        } else
            Logger.d("ScannerBaseActivity: bindToScannerService(): failed - scanner is not configured!");
    }

    private void setUSBScanner(boolean flag) {
        isUSBScanner = flag;
    }

    private boolean getUSBScanner() {
        return isUSBScanner;
    }

    private void unbindFromScannerService() {
        Logger.d("ScannerBaseActivity: unbindFromScannerService()");
        if (scannerBinder != null) {
            scannerBinder = null;
            unbindService(scannerServiceConnection);
        } else {
            Logger.d("ScannerBaseActivity: unbindFromScannerService(): ignore and exit - not binded");
        }
    }

    @Override
    public void setScannerListener(ScannerListener scannerListener) {
        Logger.d("ScannerBaseActivity: setScannerListener()");
        if (scannerBinder != null)
            scannerBinder.setScannerListener(scannerListener);
        else
            Logger.d("ScannerBaseActivity: setScannerListener(): failed - not binded!");
    }

    @Override
    public void tryReconnectScanner() {
        Logger.d("ScannerBaseActivity: tryReconnectScanner()");
        if (scannerBinder != null)
            scannerBinder.tryReconnectScanner();
        else
            Logger.d("ScannerBaseActivity: tryReconnectScanner(): failed - not binded!");
    }

    @Override
    public void disconnectScanner() {
        Logger.d("ScannerBaseActivity: disconnectScanner()");
        if (scannerBinder != null)
            scannerBinder.disconnectScanner();
        else
            Logger.d("ScannerBaseActivity: disconnectScanner(): failed - not binded!");
    }

    private ServiceConnection scannerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Logger.d("ScannerBaseActivity: scannerServiceConnection: onServiceConnected()");
            scannerBinder = (ScannerBinder) binder;
            setScannerListener(scannerListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Logger.d("ScannerBaseActivity: scannerServiceConnection: onServiceDisconnected()");
            scannerBinder = null;
        }
    };

    private ScannerListener scannerListener = new ScannerListener() {

        @Override
        public void onDisconnected() {
            Logger.d("ScannerBaseActivity: scannerListener: onDisconnected()");
            if (isFinishing() || isDestroyed()) {
                Logger.d("ScannerBaseActivity: scannerListener: onDisconnected(): ignore and exit - activity is finishing");
                return;
            }

            AlertDialogFragment.showAlert(
                    ScannerBaseActivity.this,
                    R.string.error_dialog_title,
                    getString(R.string.error_message_scanner_disconnected),
                    R.string.btn_try_again,
                    new OnDialogClickListener() {

                        @Override
                        public boolean onClick() {
                            tryReconnectScanner();
                            return true;
                        }

                    }
            );
        }

        @Override
        public void onBarcodeReceived(String barcode) {
            Logger.d("ScannerBaseActivity: scannerListener: onBarcodeReceived()");
            if (isFinishing() || isDestroyed()) {
                Logger.d("ScannerBaseActivity: scannerListener: onBarcodeReceived(): ignore and exit - activity is finishing");
                return;
            }

            ScannerBaseActivity.this.onBarcodeReceived(barcode);
        }

    };




}
