package com.kaching123.tcr.service.broadcast;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.service.LocalSyncHelper;
import com.kaching123.tcr.service.broadcast.messages.BemaSocketMsg;
import com.kaching123.tcr.service.broadcast.messages.NotifyNewCommandMsg;
import com.kaching123.tcr.service.broadcast.messages.RequestCommandsMsg;
import com.kaching123.tcr.service.broadcast.messages.RunCallBack;
import com.kaching123.tcr.service.broadcast.messages.RunCommandsMsg;
import com.kaching123.tcr.util.ValueUtil;

public class WifiSocketService extends Service {

    public static final int TIMEOUT = 10 * 1000;

    private static WifiSocketService mInstance;
    private boolean started;

    public static WifiSocketService getInstance() {
        if (mInstance == null) mInstance = new WifiSocketService();
        return mInstance;
    }

    public static boolean checkDevice(BroadcastInfo deviceInfo) {
        Context context = TcrApplication.get().getApplicationContext();
        if (deviceInfo != null) {
            String error = null;
            int versionCode = ValueUtil.getApplicationVersion(context).code;
            if (deviceInfo.getShopId() != TcrApplication.get().getShopPref().shopId().get()) {
                error = context.getString(R.string.shop_host_is_diff, deviceInfo.getSerial());

            } else if (deviceInfo.getVersionCode() < versionCode) {
                error = context.getString(R.string.host_status_outdated_msg, deviceInfo.getSerial());

            } else if (deviceInfo.getVersionCode() > versionCode) {
                error = context.getString(R.string.host_status_more_updated_msg, deviceInfo.getSerial());
            }

            if (error != null) {
                Logger.d(LocalSyncHelper.TAG + " " + error);
                LocalSyncHelper.localSyncError(context, error, deviceInfo.getSerial());
                return false;
            }
        }
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new BroadcastDiscoverer(this).start();

        if (!started && TcrApplication.get().getShopPref().enabledLocalSync().get()) {
            started = true;
            LocalSyncHelper.setWifiSocketService(this);

            new Thread() {
                @Override
                public void run() {
                    try {
                        ServerSocket serverSocket = new ServerSocket(0);
                        BroadcastDiscoverer.sMySocketPort = serverSocket.getLocalPort();

                        while (!Thread.currentThread().isInterrupted()) {
                            hearSocket(serverSocket.accept());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();

                    } finally {
                        started = false;
                    }
                }
            }.start();
        }

        return Service.START_NOT_STICKY;
    }

    private void hearSocket(final Socket socket) throws IOException {
        final BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && socket.isConnected()) {
                    try {
                        String data = input.readLine();
                        if (!workDataSocket(data, socket)) break;

                    } catch (IOException e) {
                        if (e.getLocalizedMessage() != null && e.getLocalizedMessage().contains("Connection reset by peer")) {
                            Logger.d(LocalSyncHelper.TAG + ": Connection reset by peer on port " + socket.getPort(), e);
                        } else {
                            Logger.e(LocalSyncHelper.TAG_HEIGHT, e);
                        }
                    }
                }
            }
        }).start();
    }

    public boolean workDataSocket(String data, Socket socket) {
        if (data == null) {
            Logger.d(LocalSyncHelper.TAG + ": Ignoring receiver");
            closeSocket(socket);
            return false;
        }
        LocalSyncHelper.checkAndStartWorkers();
        try {
            BemaSocketMsg bemaSocketMsg = new BemaSocketMsg().fromJson(data);
            if (bemaSocketMsg == null) {
                Logger.d(LocalSyncHelper.TAG + ": MESSAGE NULL");
                closeSocket(socket);
                return false;
            }
            Logger.d(LocalSyncHelper.TAG_HEIGHT + ": " + bemaSocketMsg.action() + ": " + data);
            switch (bemaSocketMsg.action()) {
                case NOTIFY_NEW_COMMANDS:
                    NotifyNewCommandMsg notifyNewCommandMsg = (NotifyNewCommandMsg) bemaSocketMsg.toObject(NotifyNewCommandMsg.class);
                    LocalSyncHelper.addInQueue(notifyNewCommandMsg.serial);
                    sendMsg(socket, new RunCallBack(bemaSocketMsg.uuid()).toJson());
                    break;

                case REQUEST_COMMANDS:
                    LocalSyncHelper.getInstance().workRequestCommands(
                            ((RequestCommandsMsg) bemaSocketMsg.toObject(RequestCommandsMsg.class)).getSerial(), socket);
                    break;

                case RUN_COMMANDS:
                    LocalSyncHelper.getInstance().workRequestRunCommand(
                            (RunCommandsMsg) bemaSocketMsg.toObject(RunCommandsMsg.class), socket);
                    break;

                case RUN_CALLBACK:
                    RunCallBack runCommandsMsg = (RunCallBack) bemaSocketMsg.toObject(RunCallBack.class);
                    boolean finish = LocalSyncHelper.getInstance().workCallBack(runCommandsMsg, getContentResolver(), socket);
                    if (finish) {
                        closeSocket(socket);
                        return false;
                    }
                    break;

                default:
                    Logger.d(LocalSyncHelper.TAG + ": WITHOUT ACTION");
                    closeSocket(socket);
                    break;
            }

        } catch (IOException | JsonSyntaxException e) {
            Logger.e(LocalSyncHelper.TAG_HEIGHT, e);
            return false;
        }
        return true;
    }

    public void closeSocket(Socket socket) {
        try {
            Logger.d(LocalSyncHelper.TAG + ": Closing Socket on port " + socket.getPort());
            socket.close();
        } catch (IOException e) {
            Logger.e(LocalSyncHelper.TAG_HEIGHT, e);
        }
    }

    public void makeMsgAndSend(final String msg, final boolean hearSocket, String target) throws Exception {
        TcrApplication app = TcrApplication.get();
        for (int i = 0; i < app.getLanDevices().size(); i++) {
            final BroadcastInfo deviceInfo = app.getLanDevices().get(i);
            if (target != null && !target.equals(deviceInfo.getSerial())) continue;

            if (checkDevice(deviceInfo)) {
                if (hearSocket) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Socket socket = getSocket(deviceInfo.getAddress(), deviceInfo.getPort(), true);
                                sendMsg(socket, msg);

                            } catch (Exception e) {
                                LocalSyncHelper.localSyncError(TcrApplication.get(), TcrApplication.get().getString(R.string.local_sync_failed), deviceInfo.getSerial());
                                Logger.e(LocalSyncHelper.TAG_HEIGHT + " makeMsgAndSend", e);
                            }
                        }
                    }).start();
                } else {
                    Socket socket = getSocket(deviceInfo.getAddress(), deviceInfo.getPort(), false);
                    sendMsg(socket, msg);
                }

            } else {
                i--;
            }
        }
    }

    private Socket getSocket(String address, int port, boolean hearSocket) throws IOException {
        Socket socket = new Socket(address, port);
        if (hearSocket) hearSocket(socket);
        return socket;
    }

    public void sendMsg(Socket socket, String msg) throws IOException {
        if (socket == null) return;

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));

        PrintWriter out = new PrintWriter(writer, true);
        out.println(msg);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public ContentResolver getContentResolver() {
        return TcrApplication.get().getContentResolver();
    }
}