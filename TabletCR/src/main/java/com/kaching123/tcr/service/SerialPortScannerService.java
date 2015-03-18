package com.kaching123.tcr.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;

import java.io.IOException;
import java.io.InputStream;

public class SerialPortScannerService extends Service {
    private final byte terminator = 0x0d;
    private final int maxBarCodeSize = 128;
    public static String ACTION_SERIAL_PORT_SCANNER = "com.kaching123.tcr.service.ACTION_SERIAL_PORT_SCANNER";
    public static String EXTRA_BARCODE = "barcode";
    private Intent intent;
    private Reader reader;

    public SerialPortScannerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (isEmulate()) {
            Logger.d("ScannerService: onCreate(): emulating");
            return;
        }
        intent = new Intent(ACTION_SERIAL_PORT_SCANNER);
        reader = new Reader();
        reader.start();

    }

    private class Reader extends Thread {
        public void run() {
            try {
                InputStream inputStream = getInputStream();
                int size;
                byte[] buffer = new byte[maxBarCodeSize];
                String input = "";
                while (!Thread.currentThread().isInterrupted()) {

                    if (inputStream == null)
                        return;
                    size = inputStream.read(buffer);

                    if (size > 0) {

                        input = input + new String(buffer, 0, size);
                        Logger.e("ScannerService: read() barcode: " + input + ", thread:" + Thread.currentThread().getId());

                        if (buffer[size - 1] == terminator) {
                            String barcode = input.replace("\r", "").replace(" ", "").replace("\n", "");
                            intent.putExtra(EXTRA_BARCODE, barcode);
                            LocalBroadcastManager.getInstance(SerialPortScannerService.this).sendBroadcast(intent);
                            input = "";
                        }

                    }
                    if (input.length() >= maxBarCodeSize)
                        input = "";
                }

            } catch (IOException e) {
                Logger.e("ScannerService: Serial Port Scanner read(): exiting with exception", e);
            } catch (Exception e) {
                Logger.e("ScannerService: Serial Port Scanner read(): exiting with exception", e);
            }
        }
    }

    private static boolean isEmulate() {
        return !BuildConfig.SUPPORT_PRINTER;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeSerialScanner();
        reader.interrupt();
    }

    private InputStream getInputStream() {
        return getApp().getScannerIS();
    }

    private void closeSerialScanner() {
        getApp().closeSerialScanner();
    }

    private TcrApplication getApp() {
        return (TcrApplication) getApplicationContext();
    }
}
