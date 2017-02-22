package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore.PaxTable;

public class PaxModel implements IValueModel, Parcelable {

    public String guid;
    public String ip;
    public int port;
    public String mac;
    public String subNet;
    public String gateway;
    public boolean dhcp;
    public String serial;


    public PaxModel(String guid, String ip, int port, String mac, String subNet, String gateway, boolean dhcp, String serial) {
        this.guid = guid;
        this.ip = ip;
        this.port = port;
        this.mac = mac;
        this.subNet = subNet;
        this.gateway = gateway;
        this.dhcp = dhcp;
        this.serial = serial;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    public String getSerial() {
        return serial;
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
        values.put(PaxTable.SERIAL, serial);
        return values;
    }

    @Override
    public String getIdColumn() {
        return null;
    }

    @Override
    public String toString() {
        return String.format("http://%s:%s", ip, port);
    }

    public static PaxModel get() {
        TcrApplication app = TcrApplication.get();
        return new PaxModel(null, app.getShopPref().paxUrl().get(), app.getShopPref().paxPort().get(), null, null, null, false, null);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(guid);
        dest.writeString(ip);
        dest.writeInt(port);
        dest.writeString(mac);
        dest.writeString(subNet);
        dest.writeString(gateway);
        dest.writeInt(dhcp ? 1 : 0);
        dest.writeString(serial);
    }

    public static Creator<PaxModel> CREATOR = new Creator<PaxModel>() {

        @Override
        public PaxModel createFromParcel(Parcel source) {
            return new PaxModel(
                    source.readString(),
                    source.readString(),
                    source.readInt(),
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readInt() == 1,
                    source.readString()
            );
        }

        @Override
        public PaxModel[] newArray(int size) {
            return new PaxModel[size];
        }
    };
}
