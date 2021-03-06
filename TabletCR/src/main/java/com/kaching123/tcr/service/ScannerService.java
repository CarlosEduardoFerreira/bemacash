package com.kaching123.tcr.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by pkabakov on 12.03.14.
 */
public class ScannerService extends Service {

    private static final UUID SPP_SLAVE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private static final int DISCONNECTED_WHAT = 0;
    private static final int BARCODE_RECEIVED_WHAT = 1;

    private static final int RECONNECTIONS_COUNT = 2;


    private BluetoothScannerBinder binder = new BluetoothScannerBinder();

    private ExecutorService executor;

    private BluetoothSocket scannerSocket;

    private ScannerListener scannerListener;

    private volatile boolean shouldConnect;

    public AtomicBoolean isConnected = new AtomicBoolean();

    public static void bind(Context context, ServiceConnection connection) {
        Intent intent = new Intent(context, ScannerService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private static boolean isEmulate() {
        return !BuildConfig.SUPPORT_PRINTER;
    }

    @Override
    public void onCreate() {
        Logger.d("ScannerService: onCreate()");
        super.onCreate();

        if (isEmulate()) {
            Logger.d("ScannerService: onCreate(): emulating");
            return;
        }

        executor = Executors.newSingleThreadExecutor();
        startOpenConnection();
    }

    @Override
    public void onDestroy() {
        Logger.d("ScannerService: onDestroy()");
        super.onDestroy();

        if (isEmulate()) {
            Logger.d("ScannerService: onDestroy(): emulating");
            return;
        }

        startCloseConnection();
        executor.shutdown();
    }

    private void startOpenConnection() {
        Logger.d("ScannerService: startOpenConnection()");
        if (isConnected.get()) {
            if (!shouldConnect) {
                Logger.d("ScannerService: startOpenConnection(): ignore and exit - still disconnecting!");
                onDisconnected();
            } else {
                Logger.d("ScannerService: startOpenConnection(): ignore and exit - already connected!");
            }
            return;
        }

        shouldConnect = true;
        isConnected.set(true);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean connectionOpened = false;
                int i = 0;
                while (i < RECONNECTIONS_COUNT) {

                    if (!shouldConnect) {
                        Logger.d("ScannerService: OpenConnectionRunnable: ignore and exit - should connect flag is not set");
                        isConnected.set(false);
                        return;
                    }

                    Logger.d("ScannerService: OpenConnectionRunnable: trying to open connection: attempt " + (i + 1));
                    connectionOpened = openConnection();

                    if (connectionOpened) {
                        break;
                    }

                    Logger.d("ScannerService: OpenConnectionRunnable: trying to open connection: attempt " + (i + 1) + " failed!");
                    i++;
                }

                if (!connectionOpened) {
                    isConnected.set(false);
                    if (shouldConnect)
                        sendOnDisconnected();
                    return;
                }

                if (!read()) {
                    isConnected.set(false);
                    if (shouldConnect)
                        sendOnDisconnected();
                } else {
                    isConnected.set(false);
                }

                closeConnection();
            }
        });
    }

    private void startCloseConnection() {
        Logger.d("ScannerService: startCloseConnection()");
        shouldConnect = false;
        closeConnection();
    }

    private boolean openConnection() {
        Logger.d("ScannerService: openConnection()");
        BluetoothSocket scannerSocket = null;
        try {
            BluetoothDevice scanner = getScanner();
            if (scanner == null) {
                Logger.d("ScannerService: openConnection(): failed - scanner not found!");
                return false;
            }

            try {
                scannerSocket = scanner.createRfcommSocketToServiceRecord(SPP_SLAVE_UUID);
                Logger.d("ScannerService: openConnection(): uuid socket obtained - going to connect");
                scannerSocket.connect();
                Logger.d("ScannerService: openConnection(): just connected with uuid");
            } catch (IOException e){
                if (scannerSocket != null)
                    try {
                        scannerSocket.close();
                    } catch (IOException ignore) {}
                scannerSocket = null;
                Logger.e("ScannerService: openConnection(): failed to create socket using uuid - fallback to port method", e);
            }

            if (scannerSocket == null) {
                scannerSocket = createRfcommSocket(scanner);
                Logger.d("ScannerService: openConnection(): port socket obtained - going to connect");
                scannerSocket.connect();
                Logger.d("ScannerService: openConnection(): just connected with port");
            }

        } catch (IOException e) {
            if (scannerSocket != null)
                try {
                    scannerSocket.close();
                } catch (IOException ignore) {}
            Logger.e("ScannerService: openConnection(): failed - connection failed!", e);
            return false;
        }

        synchronized (this) {
            this.scannerSocket = scannerSocket;
        }

        Logger.d("ScannerService: openConnection(): successful");
        return true;
    }

    private BluetoothSocket createRfcommSocket(BluetoothDevice device) throws IOException {
        try {
            Class<?> clazz = device.getClass();
            Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
            Method m = clazz.getMethod("createRfcommSocket", paramTypes);
            Object[] params = new Object[] {Integer.valueOf(1)};
            return (BluetoothSocket) m.invoke(device, params);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private boolean read() {
        Logger.d("ScannerService: read()");
        try {
            BufferedReader reader = null;
            while (shouldConnect) {
                if (reader == null) {
                    if ((reader = getScannerReader()) == null) {
                        Logger.d("ScannerService: read(): failed - can not get reader!");
                        return false;
                    }
                }
                String barcode = reader.readLine();
                Logger.d("ScannerService: read(): sending barcode = " + barcode);
                if (shouldConnect)
                    sendOnBarcodeReceived(barcode);
            }
            Logger.d("ScannerService: read(): exiting - should connect flag cleared");
        } catch (IOException e) {
            Logger.e("ScannerService: read(): exiting with exception", e);
            return false;
        }
        return true;
    }

    private BufferedReader getScannerReader() throws IOException {
        Logger.d("ScannerService: getScannerReader()");
        InputStream in = null;

        synchronized (this) {
            if (scannerSocket != null)
                in = scannerSocket.getInputStream();
            else
                Logger.d("ScannerService: getScannerReader(): failed - scanner socket is null!");
        }

        if (in == null) {
            Logger.d("ScannerService: read(): failed - can not get input stream!");
            return null;
        }

        return new BufferedReader(new InputStreamReader(in));
    }

    private void closeConnection() {
        Logger.d("ScannerService: closeConnection()");
        synchronized (this) {
            if (scannerSocket == null) {
                Logger.d("ScannerService: closeConnection(): ignore and exit - no socket to close");
                return;
            }

            try {
                scannerSocket.close();
            } catch (IOException e) {
                Logger.e("ScannerService: closeConnection(): exiting with exception", e);
            }
            scannerSocket = null;
            Logger.d("ScannerService: closeConnection(): closed");
        }
    }

    private BluetoothDevice getScanner() {
        Logger.d("ScannerService: getScanner()");
        String scannerAddress = getApp().getShopPref().scannerAddress().get();
        Logger.d("ScannerService: getScanner(): scanner address = " + scannerAddress);

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Logger.d("ScannerService: getScanner(): failed - bluetooth adapter is disabled!");
            return null;
        }

        BluetoothDevice device = adapter.getRemoteDevice(scannerAddress);

        Logger.d("ScannerService: getScanner(): successful, device = " + device);
        return device;
    }

    private TcrApplication getApp(){
        return (TcrApplication)getApplicationContext();
    }

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

    private void onDisconnected() {
        Logger.d("ScannerService: onDisconnected()");
        if (scannerListener != null)
            scannerListener.onDisconnected();
        else
            Logger.d("ScannerService: onDisconnected(): listener is not set!");
    }

    private void onBarcodeReceived(String barcode) {
        Logger.d("ScannerService: onBarcodeReceived(): barcode = " + barcode);
        if (scannerListener != null)
            scannerListener.onBarcodeReceived(barcode);
        else
            Logger.d("ScannerService: onDisconnected(): listener is not set!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.d("ScannerService: onBind()");
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

    private Handler serviceHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (!shouldConnect)
                return;

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

    public class BluetoothScannerBinder extends ScannerBinder {
        @Override
        public void setScannerListener(ScannerListener scannerListener) {
            ScannerService.this.setScannerListener(scannerListener);
        }
        @Override
        public boolean tryReconnectScanner() {
            ScannerService.this.startOpenConnection();
            return true;
        }

        @Override
        public void disconnectScanner() {
            ScannerService.this.startCloseConnection();
        }
    }

}
