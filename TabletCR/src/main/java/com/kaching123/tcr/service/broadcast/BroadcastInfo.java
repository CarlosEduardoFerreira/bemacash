package com.kaching123.tcr.service.broadcast;

import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by Rodrigo Busata on 6/9/2016.
 */
public class BroadcastInfo {

    private String mAddress;
    private int mPort;
    private String mSerial;
    private int mVersionCode;
    private long mShopId;

    public BroadcastInfo() {
    }

    public BroadcastInfo(String mSerial) {
        this.mSerial = mSerial;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public String getSerial() {
        return mSerial;
    }

    public void setSerial(String mSerial) {
        this.mSerial = mSerial;
    }

    public int getVersionCode() {
        return mVersionCode;
    }

    public void setVersionCode(int mVersionCode) {
        this.mVersionCode = mVersionCode;
    }

    public long getShopId() {
        return mShopId;
    }

    public void setShopId(long mShopId) {
        this.mShopId = mShopId;
    }

    public int getPort() {
        return mPort;
    }

    public void setPort(int mPort) {
        this.mPort = mPort;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BroadcastInfo && this.mSerial.equals(((BroadcastInfo) other).getSerial());
    }

    public static BroadcastInfo fromJson(String deviceInfoString) {
        return new Gson().fromJson(deviceInfoString, BroadcastInfo.class);
    }
}