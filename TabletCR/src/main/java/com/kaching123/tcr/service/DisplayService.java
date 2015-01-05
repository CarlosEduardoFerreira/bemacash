package com.kaching123.tcr.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.kaching123.display.BluetoothSocketPrinter;
import com.kaching123.display.DisplayPrinter;
import com.kaching123.display.actions.InitDisplayAction;
import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.TcrApplication;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by pkabakov on 26.02.14.
 */
public class DisplayService extends Service {

    private static final int DISCONNECTED_WHAT = 0;
    private static final int ERROR_WHAT = 1;


    private DisplayBinder binder = new DisplayBinder();

    private ExecutorService executor;

    private BluetoothSocketPrinter displayPrinter;

    private DisplayListener displayListener;

    private boolean isConnected;

    public static void bind(Context context, ServiceConnection connection) {
        Intent intent = new Intent(context, DisplayService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private static boolean isEmulate() {
        return !BuildConfig.SUPPORT_PRINTER;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        executor = Executors.newSingleThreadExecutor();
        startOpenDisplayPrinter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        startCloseDisplayPrinter();
        executor.shutdown();
    }

    private void startOpenDisplayPrinter() {
        if (isEmulate())
            return;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                isConnected = openDisplayPrinter();
                if (!isConnected) {
                    sendOnDisconnected();
                }
            }
        });
    }

    private void startCloseDisplayPrinter() {
        if (isEmulate())
            return;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                closeDisplayPrinter();
                isConnected = false;
            }
        });
    }

    private boolean openDisplayPrinter() {
        BluetoothSocketPrinter displayPrinter = null;
        try {
            BluetoothDevice display = getDisplay();
            if (display == null)
                return false;

            if (this.displayPrinter != null) {
                try {
                    this.displayPrinter.close();
                } catch (IOException ignore) {}
                this.displayPrinter = null;
            }

            displayPrinter = new BluetoothSocketPrinter(display);
            initDisplayPrinter(displayPrinter);
        } catch (IOException e) {
            try {
                if (displayPrinter != null)
                    displayPrinter.close();
            } catch (IOException ignore) {}
            return false;
        }

        this.displayPrinter = displayPrinter;
        return true;
    }

    private void initDisplayPrinter(BluetoothSocketPrinter displayPrinter) throws IOException{
        new InitDisplayAction().execute(displayPrinter);
    }

    private void closeDisplayPrinter() {
        if (displayPrinter == null)
            return;

        try {
            displayPrinter.close();
        } catch (IOException ignore) {}
    }

    private BluetoothDevice getDisplay() {
        String displayAddress = getApp().getShopPref().displayAddress().get();

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled())
            return null;

        BluetoothDevice device = adapter.getRemoteDevice(displayAddress);

        return device;
    }

    private TcrApplication getApp(){
        return (TcrApplication)getApplicationContext();
    }

    private void sendOnDisconnected() {
        serviceHandler.sendEmptyMessage(DISCONNECTED_WHAT);
    }

    private void sendOnError() {
        serviceHandler.sendEmptyMessage(ERROR_WHAT);
    }

    private void onDisconnected() {
        if (displayListener != null)
            displayListener.onDisconnected();
    }

    private void onError() {
        if (displayListener != null)
            displayListener.onError();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        setDisplayListener(null);
        return super.onUnbind(intent);
    }

    private void setDisplayListener(DisplayListener displayListener) {
        this.displayListener = displayListener;
    }

    private void startCommand(final Command command) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (!isEmulate() && !isConnected) {
                    return;
                }

                try {
                    command.execute(DisplayService.this, displayPrinter);
                } catch (IOException e) {
                    isConnected = false;
                    sendOnDisconnected();
                } catch (Exception e) {
                    sendOnError();
                }
            }
        });
    }

    private Handler serviceHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISCONNECTED_WHAT:
                    onDisconnected();
                    break;
                case ERROR_WHAT:
                    onError();
                    break;
            }
        }

    };

    public class DisplayBinder extends Binder implements IDisplayBinder {

        public void startCommand(Command displayCommand) {
            DisplayService.this.startCommand(displayCommand);
        }

        public void setDisplayListener(DisplayListener displayListener) {
            DisplayService.this.setDisplayListener(displayListener);
        }

        public void tryReconnectDisplay() {
            DisplayService.this.startOpenDisplayPrinter();
        }
    }

    public interface DisplayListener {

        public void onDisconnected();
        public void onError();

    }

    public interface Command {

        public void execute(Context context, DisplayPrinter printer) throws IOException;

    }

    public interface IDisplayBinder {

        public void startCommand(Command displayCommand);

        public void setDisplayListener(DisplayListener displayListener);

        public void tryReconnectDisplay();

    }

}
