package com.kaching123.tcr.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by BemaCarl on 04/05/2017.
 */

public class DeviceUtil extends ShopOpenHelper {

    private String _ipAddress;
    public boolean _alive = true;
    public boolean _running;

    public DeviceUtil(Context context) {
        super(context);
    }

    public void checkConnection(String ipAddress){
        _ipAddress = ipAddress;
        _running = true;
        Thread t = new Thread(new Runnable() {
            public void run()
            {
                Log.d("BemaCarl9", "DeviceUtil.checkConnection.ipAddress: " + _ipAddress);
                stillAlive2(_ipAddress);
            }
        });
        t.start();
    }

    private void stillAlive(String deviceIpAddress){
        Log.d("BemaCarl9", "DeviceUtil.stillAlive.deviceIpAddress: " + deviceIpAddress);
        HttpGet httpGet = new HttpGet("http://" + deviceIpAddress);
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 2000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 3000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        try{
            Log.d("BemaCarl9", "DeviceUtil.stillAlive: Checking Connection to " + deviceIpAddress);
            httpClient.execute(httpGet);
            Log.d("BemaCarl9", "DeviceUtil.stillAlive: Connection to " + deviceIpAddress + " is ok.");
            _alive = true;
            if(_running) stillAlive(deviceIpAddress);
        }
        catch(ClientProtocolException e){
            Log.e("BemaCarl9", "DeviceUtil.stillAlive: ClientProtocolException: Connection to " + deviceIpAddress + " is NOT ok.");
            _alive = false;
            //e.printStackTrace();
            //return;
        }
        catch(IOException e){
            Log.e("BemaCarl9", "DeviceUtil.stillAlive: IOException: Connection to " + deviceIpAddress + " is NOT ok.");
            _alive = false;
            //e.printStackTrace();
            //return;
        }
        Log.e("BemaCarl9", "DeviceUtil.stillAlive: Connection to " + deviceIpAddress + " is NOT ok.");
    }


    public void stillAlive2(String deviceIpAddress){
        try{
            InetAddress address = InetAddress.getByName(deviceIpAddress);
            boolean reachable = address.isReachable(2000);

            Log.d("BemaCarl9", "DeviceUtil.stillAlive2._running: " + _running);
            Log.d("BemaCarl9", "DeviceUtil.stillAlive2.reachable: " + reachable);

            if(reachable){
                _alive = true;
                Thread.sleep(1000);
                if(_running) stillAlive2(deviceIpAddress);
            }else{
                _alive = false;
            }

            Log.d("BemaCarl9", "DeviceUtil.stillAlive2: Connection to " + deviceIpAddress + " is ok.");
        } catch (Exception e){
            Log.e("BemaCarl9", "DeviceUtil.stillAlive2: Connection to " + deviceIpAddress + " is NOT ok.");
            _alive = false;
            //e.printStackTrace();
        }
    }


}
