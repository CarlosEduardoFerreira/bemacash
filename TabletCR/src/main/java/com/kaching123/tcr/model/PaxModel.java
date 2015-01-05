package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore.PaxTable;

import java.io.Serializable;

public class PaxModel implements IValueModel, Serializable{

    public String guid;
    public String ip;
    public int port;
    public String mac;
    public String subNet;
    public String gateway;
    public boolean dhcp;


    public PaxModel(String guid, String ip, int port, String mac, String subNet, String gateway, boolean dhcp) {
        this.guid = guid;
        this.ip = ip;
        this.port = port;
        this.mac = mac;
        this.subNet = subNet;
        this.gateway = gateway;
        this.dhcp = dhcp;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues(4);
        values.put(PaxTable.GUID, guid);
        values.put(PaxTable.IP, ip);
        values.put(PaxTable.PORT, port);
        values.put(PaxTable.MAC, mac);
        values.put(PaxTable.SUBNET, subNet);
        values.put(PaxTable.GATEWAY, gateway);
        values.put(PaxTable.DHCP, dhcp);
        return values;
    }

    @Override
    public String toString() {
        return String.format("http://%s:%s", ip, port);
    }

    public static PaxModel get() {
        TcrApplication app = TcrApplication.get();
        return new PaxModel(null, app.getShopPref().paxUrl().get(), app.getShopPref().paxPort().get(), null, null, null, false);
    }
}
