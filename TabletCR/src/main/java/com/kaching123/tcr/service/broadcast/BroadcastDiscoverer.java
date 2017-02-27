package com.kaching123.tcr.service.broadcast;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Enumeration;
import java.util.Objects;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.ApplicationVersion;
import com.kaching123.tcr.service.LocalSyncHelper;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.util.ValueUtil;

import retrofit.Server;

/**
 * Created by Rodrigo Busata on 6/9/2016.
 */

public class BroadcastDiscoverer extends Thread {

    private final String TAG = "MAP_SYNC";

    private static final int DISCOVERY_PORT = 24768;
    private static final int SEND_INTERVAL = 5000;
    private static final int LOG_INTERVAL = 60000;
    public static int sMySocketPort;
    private static boolean startedSend;
    private static boolean startedListener;
    private WifiManager mWifi;
    private Service mContext;
    private int mVersionCode;

    public BroadcastDiscoverer(Service context) {
        if (startedSend && startedListener) return;

        mContext = context;
        mWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        ApplicationVersion version = ValueUtil.getApplicationVersion(mContext);
        if (version != null) {
            mVersionCode = version.code;
        }
    }

    public void run() {
        try {
            if (mContext == null) {
                return;
            }

            startedSend = true;
            TcrApplication app = (TcrApplication) mContext.getApplication();
            DatagramSocket socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            socket.bind(new InetSocketAddress(DISCOVERY_PORT));

            listenForResponses(app, socket);

            int logInterval = LOG_INTERVAL;
            while (!Thread.currentThread().isInterrupted() && startedSend) {
                logInterval -= SEND_INTERVAL;
                Thread.sleep(SEND_INTERVAL);

                if (sMySocketPort == 0) continue;

                BroadcastInfo info = new BroadcastInfo();
                info.setAddress(getIpAddress());
                info.setPort(sMySocketPort);
                info.setVersionCode(mVersionCode);
                info.setSerial(app.getRegisterSerial());
                info.setShopId(app.getShopPref().shopId().get());

                String data = sendDiscoveryRequest(info, socket);

                if (logInterval <= 0) {
                    Logger.d(TAG + ": Sending device: " + data);
                    logInterval = LOG_INTERVAL;
                }
            }

        } catch (IOException e) {
            Logger.d(TAG + ": Could not send discovery request");
            startedSend = false;

        } catch (InterruptedException e) {
            e.printStackTrace();
            startedSend = false;
        }
    }

    /**
     * Send a broadcast UDP packet containing a request for boxee services to
     * announce themselves.
     *
     * @throws IOException
     */
    private String sendDiscoveryRequest(BroadcastInfo broadcastInfo, DatagramSocket socket) throws IOException {
        String data = new Gson().toJson(broadcastInfo);

        String  ip   = broadcastInfo.getAddress();
        Log.d(TAG, "BroadcastDiscover.sendDiscoveryRequest.ip: " + ip);
        int     lio  = ip.lastIndexOf('.');
        Log.d(TAG, "BroadcastDiscover.sendDiscoveryRequest.lio: " + lio);
        String  bc   = ip.substring(0,lio) + ".255";
        Log.d(TAG, "BroadcastDiscover.sendDiscoveryRequest.bc: " + bc);
        InetAddress broadcastAddress = InetAddress.getByName(bc);
        Log.d(TAG, "BroadcastDiscover.sendDiscoveryRequest.broadcastAddress: " + broadcastAddress);
        /**/

        //InetAddress broadcastAddress = intToInetAddress(ipStringToInt(ip));

        //InetAddress broadcastAddress = socket.getInetAddress();
        //InetAddress broadcastAddress = getBroadcastAddress();
        if (broadcastAddress == null) return null;

        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), broadcastAddress, DISCOVERY_PORT);
        socket.send(packet);

        return data;
    }



    public static int ipStringToInt(String str) {
        int result = 0;
        String[] array = str.split("\\.");
        if (array.length != 4) return 0;
        try {
            result = Integer.parseInt(array[3]);
            result = (result << 8) + Integer.parseInt(array[2]);
            result = (result << 8) + Integer.parseInt(array[1]);
            result = (result << 8) + Integer.parseInt(array[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
        return result;
    }

    public static InetAddress intToInetAddress(int hostAddress) {
        InetAddress inetAddress;
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24)) };
        try {
            inetAddress = InetAddress.getByAddress(addressBytes);
        } catch(UnknownHostException e) {
            return null;
        }
        return inetAddress;
    }

    /**
     * Calculate the broadcast IP we need to send the packet along. If we send it
     * to 255.255.255.255, it never gets sent. I guess this has something to do
     * with the mobile network not wanting to do broadcast.
     */
    private InetAddress getBroadcastAddress() throws IOException {
        DhcpInfo dhcp = mWifi.getDhcpInfo();
        if (dhcp == null) {
            Logger.d(TAG + ": Could not get dhcp info");
            return null;
        }

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    /**
     * Listen on socket for responses, timing out after TIMEOUT_MS
     *
     * @param socket socket on which the announcement request was sent
     */
    private void listenForResponses(final TcrApplication app, final DatagramSocket socket) {
        if (startedListener) return;
        startedListener = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buf = new byte[128];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    int logInterval = LOG_INTERVAL;
                    while (!Thread.currentThread().isInterrupted() && startedListener) {
                        logInterval -= SEND_INTERVAL;

                        socket.receive(packet);
                        String s = new String(packet.getData(), 0, packet.getLength());

                        BroadcastInfo info = new Gson().fromJson(s, BroadcastInfo.class);

                        Log.d(TAG, "BroadcastDiscover.listenForResponses.info.getShopId(): " + info.getShopId() + "=" + app.getShopPref().shopId().get());
                        Log.d(TAG, "BroadcastDiscover.listenForResponses.info.getVersionCode(): " + info.getVersionCode() + "=" + ValueUtil.getApplicationVersion(mContext).code);
                        Log.d(TAG, "BroadcastDiscover.listenForResponses.info.getSerial(): " + info.getSerial() + "=" + app.getRegisterSerial());
                        Log.d(TAG, "BroadcastDiscover.listenForResponses.info.getAddress(): " + info.getAddress());

                        if(info.getShopId() != 0 && info.getAddress() != null) {
                            if (!info.getSerial().equals(app.getRegisterSerial())
                                    && (info.getVersionCode() == ValueUtil.getApplicationVersion(mContext).code)
                                    && Long.valueOf(app.getShopPref().shopId().get()).compareTo(info.getShopId()) == 0)
                            {

                                if (!app.getLanDevices().contains(info)) {
                                    Intent intent = new Intent(LocalSyncHelper.LOCAL_SYNC);
                                    intent.putExtra(LocalSyncHelper.MESSAGE, mContext.getString(R.string.new_bema_found, info.getSerial()));
                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                                }

                                int index = app.getLanDevices().indexOf(info);
                                if (index >= 0){
                                    if(app.getLanDevices().get(index).getAddress() != null){
                                        if(!app.getLanDevices().get(index).getAddress().equals(info.getAddress())){
                                            OfflineCommandsService.doDownloadLocal(mContext, info.getSerial());
                                        }
                                    }
                                }

                                app.getLanDevices().remove(info);
                                app.getLanDevices().add(info);
                            }
                        }

                        if (logInterval <= 0) {
                            Logger.d(TAG + ": Received device: " + s);
                            logInterval = LOG_INTERVAL;
                        }
                    }
                    Logger.d(TAG + ": Stop Receive devices");

                } catch (SocketTimeoutException e) {
                    Logger.d(TAG + ": Receive timed out");
                    startedListener = false;

                } catch (IOException e) {
                    e.printStackTrace();
                    startedListener = false;

                }
            }
        }).start();
    }

    public static String getIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
                        String ipAddress=inetAddress.getHostAddress().toString();
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("BemaCarl", "Socket exception in GetIP Address of Utilities" + ex.toString());
        }
        return null;
    }
}