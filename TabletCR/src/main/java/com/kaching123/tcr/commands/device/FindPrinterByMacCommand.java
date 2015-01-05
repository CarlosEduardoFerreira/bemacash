package com.kaching123.tcr.commands.device;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.TaskResult;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Created by vkompaniets on 31.07.2014.
 */
public class FindPrinterByMacCommand extends PublicGroundyTask {

    private String mac;

    private DatagramSocket clientSocket;

    private PrinterInfo info;

    @Override
    protected TaskResult doInBackground() {
        try {
            clientSocket = new DatagramSocket(FindPrinterCommand.PC_PORT);
            clientSocket.setSoTimeout((int) FindPrinterCommand.FIND_TIMEOUT);
            sendMessage(clientSocket);
            info = waitAnswers(clientSocket);

            if (info != null){
                return succeeded();
            }
        } catch (Exception e) {
            Logger.e("Discovery printers ", e);
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
            clientSocket = null;
        }

        Logger.d("Discovery printers finished");
        return failed();
    }

    private void sendMessage(DatagramSocket clientSocket) throws IOException {
        byte[] sendData = FindPrinterCommand.DISCOVERY_PHRASE.getBytes();

        InetAddress mask = InetAddress.getByName(FindPrinterCommand.MASK);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, mask, FindPrinterCommand.PRINTER_PORT);
        clientSocket.setBroadcast(true);
        clientSocket.send(sendPacket);
        Logger.d("Discovery printers after send");
    }

    private PrinterInfo waitAnswers(DatagramSocket clientSocket) throws IOException {
        Logger.d("Discovery printers waitAnswers");
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < FindPrinterCommand.FIND_TIMEOUT) {
            byte[] receiveData = new byte[34];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                Logger.d("Discovery printers before received");
                clientSocket.receive(receivePacket);
                Logger.d("Discovery printers received");
            } catch (SocketTimeoutException tEx) {
                Logger.e("Discovery timeout2", tEx);
                return null;
            } catch (InterruptedIOException tEx) {
                Logger.e("Discovery timeout1", tEx);
                return null;
            }
            String mac = FindPrinterCommand.parseMac(receiveData);
            if (this.mac.equals(mac)){
                return FindPrinterCommand.parsePrinter(receiveData);
            }

        }
        return null;
    }

    /*package*/static PrinterInfo find(Context context, String mac, IAppCommandContext appCommandContext){
        FindPrinterByMacCommand cmd = new FindPrinterByMacCommand();
        cmd.mac = mac;
        cmd.sync(context, null, appCommandContext);
        return cmd.info;
    }
}
