package com.kaching123.tcr.commands.device;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Splitter;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.PrinterModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PrinterTable;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by gdubina on 17.02.14.
 */
public class ConfigurePrinterCommand extends PublicGroundyTask {

    private static final Uri URI_PRINTER = ShopProvider.getContentUri(PrinterTable.URI_CONTENT);

    private static final int PC_PORT = 5001;
    private static final int PRINTER_PORT = 1460;
    private static final String MASK = "255.255.255.255";

    private static final String UPDATE_CONFIG = "MP4200SAVE";

    private static final String ARG_MODEL = "ARG_MODEL";

    private PrinterModel model;

    @Override
    protected TaskResult doInBackground() {
        model = (PrinterModel)getArgs().getSerializable(ARG_MODEL);

        if(sendMessage()){
            updateDatabase();
            return succeeded();
        }
        return failed();
    }

    private void updateDatabase(){

        ContentValues v = model.toValues();
        v.remove(PrinterTable.GUID);

        ProviderAction
                .update(URI_PRINTER)
                .values(v)
                .where(PrinterTable.GUID + " = ?", model.guid)
                .perform(getContext());
    }

    private boolean sendMessage() {
        DatagramSocket clientSocket = null;
        try {
            clientSocket = new DatagramSocket(PC_PORT);

            ByteBuffer buffer = ByteBuffer.allocate(34);
            buffer.put(UPDATE_CONFIG.getBytes());
            for (String a : Splitter.on(':').split(model.mac)) {
                buffer.put(toByte(Integer.parseInt(a, 16)));
            }
            buffer.put(toByte2(15));
            Splitter dotSplitter = Splitter.on('.');
            for (String a : dotSplitter.split(model.ip)) {
                buffer.put(toByte(Integer.parseInt(a)));
            }
            for (String a : dotSplitter.split(model.subNet)) {
                buffer.put(toByte(Integer.parseInt(a)));
            }
            for (String a : dotSplitter.split(model.gateway)) {
                buffer.put(toByte(Integer.parseInt(a)));
            }
            buffer.put(toByte2(model.port));
            buffer.put(model.dhcp ? toByte2(1) : toByte2(0));

            byte[] sendData = buffer.array();

            InetAddress mask = InetAddress.getByName(MASK);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, mask, PRINTER_PORT);
            clientSocket.setBroadcast(true);
            clientSocket.send(sendPacket);

            return true;
        } catch (SocketException e) {
            Logger.e("Socket exception", e);
        } catch (UnknownHostException e) {
            Logger.e("Socket exception", e);
        } catch (IOException e) {
            Logger.e("Socket exception", e);
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
        return false;
    }

    public static byte toByte(int v) {
        return (byte) (v & 0xFF);
    }

    public static byte[] toByte2(int v) {
        return new byte[]{(byte)(v & 0xFF), (byte)(v >> 8)};
    }

    public static void start(Context context, PrinterModel model, PrinterConfigureBaseCallback callback) {
        create(ConfigurePrinterCommand.class)
                .arg(ARG_MODEL, model)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class PrinterConfigureBaseCallback {

        @OnSuccess(ConfigurePrinterCommand.class)
        public final void onSuccess(){
            handleSuccess();
        }

        @OnFailure(ConfigurePrinterCommand.class)
        public final void onFailure(){
            handleFailure();
        }

        protected abstract void handleSuccess();
        protected abstract void handleFailure();
    }
}
