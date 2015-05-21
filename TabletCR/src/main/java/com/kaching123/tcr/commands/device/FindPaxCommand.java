package com.kaching123.tcr.commands.device;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.PaxModel;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnCallback;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FindPaxCommand extends PublicGroundyTask {

    private static final String CALLBACK_ADD_PAX = "CALLBACK_ADD_PAX";

    private static final String EXTRA_PAX = "EXTRA_PAX";

    protected static final long SEARCHING_TIME = TimeUnit.MINUTES.toMillis(1);

    private static final int READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);
    private static final int CONNECTION_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);
    private Socket socket;

    @Override
    protected TaskResult doInBackground() {
        long time = System.currentTimeMillis();
        String ipHead = logLocalIpAddresses();
        PaxModel model = new PaxModel(null, "", 6911, "", null, null, false, null);

        try {
            Logger.d("Discovery Pax findIteration");
            create(model, ipHead);

        } catch (Exception e) {
            Logger.e("Discovery printers ", e);
            return failed();

        }
        return succeeded();

    }

    private Socket create(PaxModel model, String ipHead) {
        {
            int ipBottom = -1;

            for (int i = 0; i < 255; i++) {
                try {
                    socket = new Socket();
                    socket.setSoTimeout(READ_TIMEOUT);
                    ipBottom++;
                    model.ip = String.format(ipHead + "%d", ipBottom);
                    Logger.d("trace socket ip: " + model.ip);
                    socket.connect(new InetSocketAddress(model.ip, model.port), 25);
                    if (socket.isConnected()) {
                        if (model != null) {
                            firePrinterInfo(packPrinterData(model));
                        }
                        break;
                    }

                } catch (IOException ex) {
                    continue;
                }
            }
        }

        if (isQuitting()) {
            Logger.d("Discovery printers cancelled");
            cancelled();
        }
        return socket;
    }

    public String logLocalIpAddresses() {
        WifiManager wm = (WifiManager) getContext().getSystemService(getContext().WIFI_SERVICE);
        String ipFull = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        String[] dots = ipFull.split("\\.");
        String ip = dots[0] + "." + dots[1] + "." + dots[2] + ".";
        return ip;
    }

    private void firePrinterInfo(Bundle bundle) {

        Logger.d("Discovery printers send printer");
        callback(CALLBACK_ADD_PAX, bundle);

        /*LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());

        Intent intent = new Intent(ACTION_PRINTER);
        intent.replaceExtras(bundle);
        manager.sendBroadcast(intent);*/
    }

    private static Bundle packPrinterData(PaxModel info) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_PAX, info);
        return bundle;
    }

    public static PrinterInfo unpackPrinterInfo(Bundle bundle) {
        if (bundle == null)
            return null;
        return bundle.getParcelable(EXTRA_PAX);
    }

    public static int toShort(byte b) {
        return b & 0xFF;
    }

    public static int toInt(byte lb, byte hb) {
        return ((int) hb << 8) | ((int) lb & 0xFF);
    }

    public static int toInt(int lb, int hb) {
        return (hb << 8) | (lb & 0xFF);
    }

    public static TaskHandler start(Context context, BaseFindPaxCallback callback) {
        return create(FindPaxCommand.class).callback(callback).queueUsing(context);
    }

    public static abstract class BaseFindPaxCallback {

        @OnSuccess(FindPaxCommand.class)
        public void onSuccess() {
            onSearchFinished();
        }

        @OnCallback(value = FindPaxCommand.class, name = CALLBACK_ADD_PAX)
        public void onAddPax(@Param(EXTRA_PAX) PaxModel info) {
            handleAddPax(info);
        }

        protected abstract void onSearchFinished();

        protected abstract void handleAddPax(PaxModel info);
    }

}
