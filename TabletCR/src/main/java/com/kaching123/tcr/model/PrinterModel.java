package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.PrinterTable;

import java.io.Serializable;

/**
 * Created by gdubina on 11.02.14.
 */
public class PrinterModel implements IValueModel, Serializable{

    public String guid;
    public String ip;
    public int port;
    public String mac;
    public String subNet;
    public String gateway;
    public boolean dhcp;

    public String aliasGuid;

    public PrinterModel(String guid, String ip, int port, String mac, String subNet, String gateway, boolean dhcp, String aliasGuid) {
        this.guid = guid;
        this.ip = ip;
        this.port = port;
        this.mac = mac;
        this.subNet = subNet;
        this.gateway = gateway;
        this.dhcp = dhcp;
        this.aliasGuid = aliasGuid;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(PrinterTable.GUID, guid);
        values.put(PrinterTable.IP, ip);
        values.put(PrinterTable.PORT, port);
        values.put(PrinterTable.MAC, mac);
        values.put(PrinterTable.SUBNET, subNet);
        values.put(PrinterTable.GATEWAY, gateway);
        values.put(PrinterTable.DHCP, dhcp);
        values.put(PrinterTable.ALIAS_GUID, aliasGuid);
        return values;
    }
}
