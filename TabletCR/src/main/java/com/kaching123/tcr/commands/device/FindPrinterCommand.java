package com.kaching123.tcr.commands.device;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import com.kaching123.pos.USBPrinter;
import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.Logger;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnCallback;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class FindPrinterCommand extends PublicGroundyTask {

    private static final String CALLBACK_ADD_PRINTER = "CALLBACK_ADD_PRINTER";

    private static final String EXTRA_PRINTER = "extra_printer";

    protected static final String DISCOVERY_PHRASE = "MP4200FIND";
    protected static final long FIND_TIMEOUT = TimeUnit.SECONDS.toMillis(15);
    protected static final long SEARCHING_TIME = TimeUnit.MINUTES.toMillis(1);

    protected static final int PC_PORT = 5001;
    protected static final int PRINTER_PORT = 1460;

    protected static final String MASK = "255.255.255.255";

    private DatagramSocket clientSocket;

    private void searchForUsbPrinters()
    {
        UsbManager manager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);
        try
        {

            PendingIntent mPermissionIntent;

            mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(PrinterCommand.ACTION_USB_PERMISSION), 0);

            USBPrinter printer = new USBPrinter(USBPrinter.LR2000_PID,USBPrinter.LR2000_VID,manager,null);
            if ( printer.findPrinter(true)) {
                Logger.d("Printer USB found");
                firePrinterInfo(packPrinterData(new PrinterInfo(USBPrinter.USB_DESC, 0, "" , "", "", false)));
            }
            else
                Logger.d("Printer USB not found");

        }
        catch (Exception e) {
            Logger.e("Discovery USB printers ", e);
        }

    }
    @Override
    protected TaskResult doInBackground() {
        long time = System.currentTimeMillis();

        if(isEmulate()){
            return emulateCommand();
        }

        searchForUsbPrinters();
        try {
            clientSocket = new DatagramSocket(PC_PORT);
            clientSocket.setSoTimeout((int) FIND_TIMEOUT);
            while (!isQuitting() && (System.currentTimeMillis() - time) < SEARCHING_TIME) {
                Logger.d("Discovery printers findIteration");
                sendMessage(clientSocket);
                waitAnswers(clientSocket);
            }
        } catch (Exception e) {
            Logger.e("Discovery printers ", e);
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
            clientSocket = null;
        }

        if (isQuitting()) {
            Logger.d("Discovery printers cancelled");
            return cancelled();
        }
        Logger.d("Discovery printers finished");
        return succeeded();
    }

    protected boolean isEmulate() {
        return !BuildConfig.SUPPORT_PRINTER;
    }

    private TaskResult emulateCommand(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        for(int i = 1; i < 6; i++){
            firePrinterInfo(packPrinterData(new PrinterInfo("192.168.0." + i, 128, "10:11:12:13:14:" + i, "255.255.255.0", "192.168.0.1", false)));
            try {
                Thread.sleep(i * 150);
            } catch (InterruptedException e) {}
        }
        return succeeded();
    }

    private void sendMessage(DatagramSocket clientSocket) throws IOException {
        byte[] sendData = DISCOVERY_PHRASE.getBytes();

        InetAddress mask = InetAddress.getByName(MASK);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, mask, PRINTER_PORT);
        clientSocket.setBroadcast(true);
        clientSocket.send(sendPacket);
        Logger.d("Discovery printers after send");
    }

    private void waitAnswers(DatagramSocket clientSocket) throws IOException {
        Logger.d("Discovery printers waitAnswers");
        //int i = 0;
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < FIND_TIMEOUT) {
            byte[] receiveData = new byte[34];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                Logger.d("Discovery printers before received");
                clientSocket.receive(receivePacket);
                Logger.d("Discovery printers received");
            } catch (SocketTimeoutException tEx) {
                Logger.e("Discovery timeout2", tEx);
                return;
            } catch (InterruptedIOException tEx) {
                Logger.e("Discovery timeout1", tEx);
                return;
            }

            PrinterInfo info = parsePrinter(receiveData);
            if (info != null) {
                firePrinterInfo(packPrinterData(info));
            }
            if (isQuitting()) {
                Logger.d("FindPrinterCommand task was canceled");
                return;
            }
        }
    }

    @Override
    protected void onCancel() {
        Logger.d("FindPrinterCommand cancel task");
        if (clientSocket == null)
            return;
        clientSocket.close();
        clientSocket = null;
        Logger.d("FindPrinterCommand cancel task end");
    }

    private void firePrinterInfo(Bundle bundle) {

        Logger.d("Discovery printers send printer");
        callback(CALLBACK_ADD_PRINTER, bundle);

        /*LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());

        Intent intent = new Intent(ACTION_PRINTER);
        intent.replaceExtras(bundle);
        manager.sendBroadcast(intent);*/
    }

    private static Bundle packPrinterData(PrinterInfo info) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_PRINTER, info);
        return bundle;
    }

    public static PrinterInfo unpackPrinterInfo(Bundle bundle) {
        if (bundle == null)
            return null;
        return bundle.getParcelable(EXTRA_PRINTER);
    }

    protected static PrinterInfo parsePrinter(byte[] receiveData) {

        String answerPhrase = new String(receiveData, 0, 11);

        /*ByteBuffer macBuf = ByteBuffer.wrap(receiveData, 11, 6);
        String mac = toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get()));*/
        String mac = parseMac(receiveData);

        ByteBuffer ipBuf = ByteBuffer.wrap(receiveData, 19, 4);
        String ip = toShort(ipBuf.get()) + "." + toShort(ipBuf.get()) + "." + toShort(ipBuf.get()) + "." + toShort(ipBuf.get());

        ByteBuffer subNetBuf = ByteBuffer.wrap(receiveData, 23, 4);
        String subNet = toShort(subNetBuf.get()) + "." + toShort(subNetBuf.get()) + "." + toShort(subNetBuf.get()) + "." + toShort(subNetBuf.get());

        ByteBuffer gatewayBuf = ByteBuffer.wrap(receiveData, 27, 4);
        String gateway = toShort(gatewayBuf.get()) + "." + toShort(gatewayBuf.get()) + "." + toShort(gatewayBuf.get()) + "." + toShort(gatewayBuf.get());

        ByteBuffer portBuf = ByteBuffer.wrap(receiveData, 31, 2);

        byte portLByte = portBuf.get();
        byte portMByte = portBuf.get();

        int port = toInt(toShort(portLByte), toShort(portMByte));

        boolean dhcp = receiveData[33] == 1;

        return new PrinterInfo(ip, port, mac, subNet, gateway, dhcp);
    }

    protected static String toHex(int v){
        String result = Integer.toHexString(v);
        if(result.length() == 1)
            return "0" + result;
        return result;
    }

    protected static String parseMac(byte[] receiveData){
        ByteBuffer macBuf = ByteBuffer.wrap(receiveData, 11, 6);
        String mac = toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get()));
        return mac;
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

    public static TaskHandler start(Context context, BaseFindPrinterCallback callback) {
        return create(FindPrinterCommand.class).callback(callback).queueUsing(context);
    }

    public static abstract class BaseFindPrinterCallback {

        @OnSuccess(FindPrinterCommand.class)
        public void onSuccess() {
            onSearchFinished();
        }

        @OnCallback(value = FindPrinterCommand.class, name = CALLBACK_ADD_PRINTER)
        public void onAddPrinter(@Param(EXTRA_PRINTER) PrinterInfo printerInfo) {
            handleAddPrinter(printerInfo);
        }

        protected abstract void onSearchFinished();

        protected abstract void handleAddPrinter(PrinterInfo printerInfo);
    }

}
