package com.kaching123.tcr.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class USBScannerService extends Service {
    private static final int DISCONNECTED_WHAT = 0;
    private static final int BARCODE_RECEIVED_WHAT = 1;
    private static final String TAG = "USBScannerService";

    private ExecutorService mExecutor;
    private ScannerListener scannerListener;
    private SerialInputOutputManager mSerialIoManager;
    private static UsbSerialPort sPort = null;
    private USBScannerBinder binder;
    private UsbManager mUsbManager;
    public boolean isConnected;

    public static void bind(Context context, ServiceConnection connection) {
        Log.d("BemaCarl4","USBScannerService.bind");

        Intent intent = new Intent(context, USBScannerService.class);
        Log.d("BemaCarl4","USBScannerService.bind.intent: " + intent);
        Log.d("BemaCarl4","USBScannerService.bind.connection: " + connection);
        Log.d("BemaCarl4","USBScannerService.bind.Context.BIND_AUTO_CREATE: " + Context.BIND_AUTO_CREATE);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BemaCarl4","USBScannerService.onCreate");
        //sPort = getPort();
        isConnected = false;
        mExecutor = Executors.newSingleThreadExecutor();
        startOpenConnection();
    }

    public UsbSerialPort getPort(){
        Log.d("BemaCarl4","USBScannerService.getPort");
        mUsbManager = (UsbManager) TcrApplication.get().getSystemService(Context.USB_SERVICE);
        final List<UsbSerialDriver> drivers =
                UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        Log.d("BemaCarl4","USBScannerService.getPort.drivers: " + drivers.size());
        final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
        for (final UsbSerialDriver driver : drivers) {
            final List<UsbSerialPort> ports = driver.getPorts();
            Log.d("BemaCarl4","USBScannerService.getPort: |driver|ports.size()|: |" + driver +"|"+ ports.size() +"|");
            result.addAll(ports);
        }
        UsbSerialPort usbSerialPort = null;
        for(UsbSerialPort port: result){
            final UsbSerialDriver driver = port.getDriver();
            final UsbDevice device = driver.getDevice();

            Log.d("BemaCarl4","USBScannerService.getPort.device.getInterfaceCount(): " + device.getInterfaceCount());
            for (int i = 0; i < device.getInterfaceCount(); i++) {
                Log.d("BemaCarl4","USBScannerService.getPort.device.getInterface.i: " + i);
                Log.d("BemaCarl4","USBScannerService.getPort.device.getInterface("+i+").getInterfaceClass(): " + device.getInterface(i).getInterfaceClass());
                Log.d("BemaCarl4","USBScannerService.getPort.device.port: " + port);
            }
            if(device.getInterface(0).getInterfaceClass() == 2){
                usbSerialPort = port;
            }
        }
        return usbSerialPort;
    }

    private Handler serviceHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISCONNECTED_WHAT:
                    onDisconnected();
                    break;
                case BARCODE_RECEIVED_WHAT:
                    String barcode = (String)msg.obj;
                    onBarcodeReceived(barcode);
                    break;
            }
        }

    };

    private void sendOnDisconnected() {
        Logger.d("ScannerService: sendOnDisconnected()");
        serviceHandler.sendEmptyMessage(DISCONNECTED_WHAT);
    }

    private void sendOnBarcodeReceived(String barcode) {
        Logger.d("ScannerService: sendOnBarcodeReceived(): barcode = " + barcode);
        Message msg = serviceHandler.obtainMessage();
        msg.what = BARCODE_RECEIVED_WHAT;
        msg.obj = barcode;
        serviceHandler.sendMessage(msg);
    }

    private void onBarcodeReceived(String barcode) {
        Logger.d("ScannerService: onBarcodeReceived(): barcode = " + barcode);
        if (scannerListener != null)
            scannerListener.onBarcodeReceived(barcode);
        else
            Logger.d("ScannerService: onDisconnected(): listener is not set!");
    }

    private void onDisconnected() {
        Logger.d("ScannerService: onDisconnected()");
        if (scannerListener != null)
            scannerListener.onDisconnected();
        else
            Logger.d("ScannerService: onDisconnected(): listener is not set!");
    }

    private void startOpenConnection() {
        if(isConnected)
            return;
        if (sPort == null) {
            Logger.e("No serial device.");
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            Log.d("BemaCarl4","USBScannerService.startOpenConnection.usbManager: " + usbManager);
            UsbDeviceConnection connection = usbManager.openDevice(sPort.getDriver().getDevice());
            Log.d("BemaCarl4","USBScannerService.startOpenConnection.connection: " + connection);
            if (connection == null) {
                Logger.e("Opening device failed");
                return;
            }

            try {
                Log.d("BemaCarl4","USBScannerService.startOpenConnection.sPort.getDriver().getPorts().get(0): " + sPort.getDriver().getPorts().get(0));
                //sPort.getDriver().getPorts().get(0).open(connection);
                sPort.open(connection);
                Log.d("BemaCarl4","USBScannerService.startOpenConnection.sPort1: " + sPort);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                Log.d("BemaCarl4","USBScannerService.startOpenConnection.catch.e.getMessage(): " + e.getMessage());
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                try {
                    sPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sPort = null;
                return;
            }
            Log.d("BemaCarl4","USBScannerService.startOpenConnection.sPort2: " + sPort);
        }
        onDeviceStateChange();
        isConnected = true;
    }

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                    onDisconnected();
                    startCloseConnection();
                    mExecutor.shutdownNow();
                }

                @Override
                public void onNewData(final byte[] data) {
                        USBScannerService.this.updateReceivedData(data);
                }
            };

    private void updateReceivedData(byte[] data) {
        final String message = new String(data);
        sendOnBarcodeReceived(message);
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            if(mExecutor.isShutdown())
                mExecutor = Executors.newSingleThreadExecutor();
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (binder == null)
            binder = new USBScannerBinder();
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.d("ScannerService: onUnbind()");
        setScannerListener(null);
        return super.onUnbind(intent);
    }

    private void setScannerListener(ScannerListener scannerListener) {
        Logger.d("ScannerService: setScannerListener(): scannerListener = " + scannerListener);
        this.scannerListener = scannerListener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        startCloseConnection();
        mExecutor.shutdown();
    }

    private void startCloseConnection() {
        if(!isConnected)
            return;
        stopIoManager();
        if (sPort != null) {
            try {
                sPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            sPort = null;
        }
        isConnected = false;
    }

    public class USBScannerBinder extends ScannerBinder {
        @Override
        public void setScannerListener(ScannerListener scannerListener) {
            USBScannerService.this.setScannerListener(scannerListener);
        }
        @Override
        public boolean tryReconnectScanner() {
            if(sPort == null) {
                UsbSerialPort newPort = getPort();
                if(newPort != null) {
                    sPort = newPort;
                    USBScannerService.this.startOpenConnection();
                    return true;
                }
            }else{
                USBScannerService.this.startOpenConnection();
                return true;
            }
            return false;
        }

        @Override
        public void disconnectScanner() {
            USBScannerService.this.startCloseConnection();
        }
    }
}
