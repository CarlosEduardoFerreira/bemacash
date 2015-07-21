package com.bematechus.bemaLibrary;

import com.bematechus.bemaUtils.PortInfo;

/**
 * Created by b1107005 on 5/24/2015.
 */
public class PrinterInfo {

    public enum PrinterType {
        USB, TCPIP, SERIAL, NONE;
    }
    public enum PrinterModel {
        LR2000, LR2000E, LR1000, MP200, NONE
    }

    private PrinterType printerType = PrinterType.NONE;
    private PrinterModel printerModel = PrinterModel.NONE ;
    private String portName=null;
    private int portNumber = -1;
    private String MAC = null;
    private boolean DHCP = false;


    public PortInfo getPortInfo ()
    {
        PortInfo port =  new PortInfo (portName,portNumber);
        if ( printerType == PrinterType.USB) {
            port.setType(PortInfo.PortType.USB);
        }

        return port;

    }
    public String getGW() {
        return GW;
    }

    @Override
    public String toString ()
    {
        return portName + ":" + portNumber;
    }

    void setGW(String GW) {
        this.GW = GW;
    }

    private String GW = null;



    private boolean drawerOpenHigh = true;

    public boolean isDrawerOpenHigh() {
        return drawerOpenHigh;
    }

    public void setDrawerOpenHigh(boolean drawerOpenHigh) {
        this.drawerOpenHigh = drawerOpenHigh;
    }


    public PrinterInfo () {

    }
    public PrinterInfo (PrinterType type, PrinterModel model, String portName, int portNumber )
    {
        this.printerType = type;
        this.printerModel = model;
        this.portName = portName;
        this.portNumber = portNumber;

    }
    public PrinterType getPrinterType() {
        return printerType;
    }

     public PrinterModel getPrinterModel() {
        return printerModel;
    }


    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getMAC() {
        return MAC;
    }
    void setMAC(String MAC) {
        this.MAC = MAC;
    }


    public boolean isDHCP() {
        return DHCP;
    }

    public void setDHCP(boolean DHCP) {
        this.DHCP = DHCP;
    }




}
