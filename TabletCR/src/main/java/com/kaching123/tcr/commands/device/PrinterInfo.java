package com.kaching123.tcr.commands.device;

import android.os.Parcel;
import android.os.Parcelable;

import com.kaching123.pos.USBPrinter;

/**
 * Created by gdubina on 11.02.14.
 */
public class PrinterInfo implements Parcelable {

    public String ip;
    public final int port;
    public final String macAddress;
    public final String subNet;
    public final String gateway;
    public final boolean dhcp;

    public final String fullAddress;

    public PrinterInfo(String ip, int port, String macAddress, String subNet, String gateway, boolean dhcp) {
        this.ip = ip;
        this.port = port;
        this.macAddress = macAddress;
        this.subNet = subNet;
        this.gateway = gateway;
        this.dhcp = dhcp;
        if ( ip.compareTo(USBPrinter.USB_DESC)==0)
            this.fullAddress = USBPrinter.USB_DESC + " (" + USBPrinter.USB_MODELS + ")";
        else
            this.fullAddress = ip + ":" + port;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ip);
        dest.writeInt(port);
        dest.writeString(macAddress);
        dest.writeString(subNet);
        dest.writeString(gateway);
        dest.writeInt(dhcp ? 1 : 0);
    }

    public static Creator<PrinterInfo> CREATOR = new Creator<PrinterInfo>() {

        @Override
        public PrinterInfo createFromParcel(Parcel source) {
            return new PrinterInfo(
                    source.readString(),
                    source.readInt(),
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readInt() == 1
            );
        }

        @Override
        public PrinterInfo[] newArray(int size) {
            return new PrinterInfo[size];
        }
    };


}
