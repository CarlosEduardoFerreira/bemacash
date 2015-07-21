package com.bematechus.bemaUtils;

import android.app.PendingIntent;
import android.hardware.usb.UsbManager;


/**
 * Created by b1107005 on 5/23/2015.
 */
public class PortInfo {


    public enum FlowControl
    {
        NoFlowControl(0),
        XONXOFF(1),
        RTSCTS(2);
        private int value =0;
        FlowControl (int value)
        {
            this.value = value;
        }
        public int getValue()
        {
            return value;
        }
    }
    public enum Parity
    {
        None(0),
        Odd(1),
        Even(2);
        private int value =0;
        Parity (int value)
        {
            this.value = value;
        }
        public int getValue()
        {
            return value;
        }
    }
    public static final Integer READ_TIMEOUT = 3000; //miliseconds
    public static final Integer WRITE_TIMEOUT = 2000;
    public static final Integer CONNECTION_TIMEOUT = 100; //miliseconds
    private static final int DEFAULT_SERIAL_BAUD_RATE = 9600;
    private static final int DEFAULT_SERIAL_DATA_BITS = 8 ;
    private static final int DEFAULT_STOP_BITS = 1;

    private Integer USB_PID = UsbPort.LR2000_PID;

    public Integer getUSB_VID() {
        return USB_VID;
    }

    public void setUSB_VID(Integer USB_VID) {
        this.USB_VID = USB_VID;
    }

    public Integer getUSB_PID() {
        return USB_PID;
    }

    public void setUSB_PID(Integer USB_PID) {
        this.USB_PID = USB_PID;
    }

    private Integer USB_VID = UsbPort.LR2000_VID;

    private String portName = null;

    private Integer portNumber = -1; //for socket and USB device Id

    private Integer readTimeout = READ_TIMEOUT ;  //miliseconds
    private Integer writeTimeout = WRITE_TIMEOUT;
    private Integer connectionTimeout = CONNECTION_TIMEOUT;

    private UsbManager usbManager = null;
    private PendingIntent permissionIntent = null;




    private FlowControl flow = FlowControl.NoFlowControl;
    private Parity parity = Parity.None;

    private int baudRate = DEFAULT_SERIAL_BAUD_RATE;
    private int dataBits = DEFAULT_SERIAL_DATA_BITS;
    private int stopBits = DEFAULT_STOP_BITS;

    public PendingIntent getIntent() {
        return permissionIntent;
    }

    public void setIntent(PendingIntent intent) {
        this.permissionIntent = intent;
    }

    public FlowControl getFlow() {
        return flow;
    }

    public void setFlow(FlowControl flow) {
        this.flow = flow;
    }

    public Parity getParity() {
        return parity;
    }

    public void setParity(Parity parity) {
        this.parity = parity;
    }



    public void SetDefaultTimeouts()
    {
        setReadTimeout(READ_TIMEOUT);
        setWriteTimeout(WRITE_TIMEOUT);
        setConnectionTimeout(CONNECTION_TIMEOUT);
    }
    public PortInfo ()
    {
        SetDefaultTimeouts();
    }

    public PortInfo (String portName)
    {
        this.portName = portName;
        SetDefaultTimeouts();

    }
    public PortInfo (PortInfo info)
    {
        if ( info != null) {
            USB_PID = info.USB_PID;
            USB_VID = info.USB_VID;
            portName = info.portName;
            portNumber = info.portNumber;
            flow = info.flow;
            parity = info.parity;
            baudRate = info.baudRate;
            dataBits = info.dataBits;
            stopBits = info.stopBits;
            usbManager = info.usbManager;
            permissionIntent = info.permissionIntent;
            readTimeout = info.readTimeout;
            writeTimeout = info.writeTimeout;
            connectionTimeout = info.connectionTimeout;
        }



    }
    public PortInfo (String portName, Integer portNumber)
    {
        this.portNumber = portNumber;
        this.portName = portName;
        SetDefaultTimeouts();
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Integer getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Integer writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public int getStopBits() {
        return stopBits;
    }

    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    public int getDataBits() {
        return dataBits;
    }

    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    public UsbManager getUsbManager() {
        return usbManager;
    }

    public void setUsbManager(UsbManager usbManager) {
        this.usbManager = usbManager;
    }


    public enum PortType
    {
        SERIAL, USB, TCP, UNDEFINED;

    }
    public void setType (PortType type)
    {
        if (type == PortType.USB && portName.length() == 0)
        {
            portName = "USB";
        }
    }
    public PortType getType ()
    {
        PortType type = PortType.UNDEFINED;
        if ( portName != null)
        {
            String portUpper = portName.toUpperCase();
            if (portUpper.startsWith("COM") || portUpper.contains("TTYS")  )
                type = PortType.SERIAL;
            else if (portUpper.contains("USB"))
                type = PortType.USB;
            else if ( new IPAddressValidator().validate(portName))
                type = PortType.TCP;

        }

        return type;

    }



}
