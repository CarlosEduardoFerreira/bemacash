package com.bematechus.bemaUtils;

import android.app.PendingIntent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.util.HashMap;


/**
 * Created by b1107005 on 5/23/2015.
 */
public class UsbPort extends CommunicationPort {

     private static final int MAX_NUM_ATTEMPTS = 3;
    private static final int USB_WRITE_TIMEOUT = 500;


    private UsbManager manager = null;
    private UsbDevice mUsbDevice = null;
    private UsbInterface mInterface = null;
    private UsbDeviceConnection mDeviceConnection = null;
    private boolean mConnected = false;
    private PendingIntent permissionIntent = null;



    private static final String ERR_USB_SERVICE = "USB Service Not Initialized";
    private static final String ERR_USB_NOT_CONNECTED = "USB not Connected";
    private static final String ERR_USB_CONNECTION = "Unable to connect to USB Device ";
    private static final String ERR_USB_WRITE = "Unable to write to USB Device";
    private static final String ERR_USB_READ = "Unable to read from USB Device";

    private static final int USB_GET_PORT_STATUS_REQ_TYPE = 161;
    private static final int USB_GET_PORT_STATUS_REQ_ID = 1;

    public static final int LR2000_PID = 0x811e;
    public static final int LR2000_VID = 0x0fe6;
    public static final int LDX1000_PID = 0xa010;
    public static final int LDX1000_VID = 0x0fa8;
    public static final String USB_DESC = "USB";
    public static final String USB_MODELS = "LR2000";

    private UsbEndpoint epIn = null;
    private UsbEndpoint epOut = null;

    private int pid = 0;
    private int vid = 0;
    private int deviceId = -1;



    @Override
    public boolean open(PortInfo info) throws CommunicationException {

        if(info.getPortName().equalsIgnoreCase("LDX1000")){
            info.setUSB_PID(LDX1000_PID);
            info.setUSB_VID(LDX1000_VID);
        }else{
            info.setUSB_PID(LR2000_PID);
            info.setUSB_VID(LR2000_VID);
        }
        this.pid = info.getUSB_PID();
        this.vid = info.getUSB_VID();
        this.deviceId = info.getPortNumber();
        this.manager = info.getUsbManager();

        this.permissionIntent = info.getIntent();

        writeTimeout = info.getWriteTimeout();
        readTimeout = info.getReadTimeout();


        return findPrinter(true);

    }

    @Override
    public void close() throws CommunicationException {
        closeConnection();

    }

    public int getUsbDeviceId ()
    {
        if ( isOpen() && mUsbDevice != null)
            return mUsbDevice.getDeviceId();

        return -1;
    }


    private void transactionInitialize() throws CommunicationException
    {
        if ( mUsbDevice == null ) {

            if ( !findPrinter(false) )
            {
                throw new CommunicationException(ERR_USB_NOT_CONNECTED, CommunicationException.ErrorCode.PortNotAvailable);
            }
        }

        if (!openConnection())
        {
            throw new CommunicationException(ERR_USB_CONNECTION, CommunicationException.ErrorCode.ServiceNotInitialized);
        }
    }

    @Override
    public Integer write(final byte[] data, int sizeToWrite) throws CommunicationException {

        transactionInitialize();

        int ret = mDeviceConnection.bulkTransfer(epOut, data,sizeToWrite, writeTimeout);
        if (ret < 0) {
            closeConnection();
            throw new CommunicationException(ERR_USB_WRITE, CommunicationException.ErrorCode.WriteError);

        }

        return ret;
    }

    @Override
    public Integer read( byte[] data, int sizeToRead) throws CommunicationException {

        transactionInitialize();


        int ret = mDeviceConnection.bulkTransfer(epIn, data,sizeToRead, readTimeout);

        if (ret < 0) {
            closeConnection();
            throw new CommunicationException(ERR_USB_READ, CommunicationException.ErrorCode.ReadError);

        }

       return ret;
    }

    @Override
    public boolean isOpen() {
        return mConnected;
    }


    public boolean findPrinter( boolean forceOpen) throws CommunicationException {
        if ( manager == null)
        {
            throw new CommunicationException(ERR_USB_SERVICE, CommunicationException.ErrorCode.ServiceNotInitialized );
        }
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();


        for (UsbDevice device : deviceList.values()) {
            if (device.getVendorId() == this.vid && device.getProductId() == this.pid) {
               //  TODO: support different USB printers int the future
               mUsbDevice = device;

               break;

            }
        }
        if ( mUsbDevice == null) {
            return false;
        }
        if (forceOpen )
        {
            try {
                openConnection();
            }
            catch (Exception ex) {
                ex = ex;

            }
            finally {
                closeConnection();
            }

        }

        return true;
    }


    private boolean closeConnection ()
    {

        if ( mDeviceConnection != null) {

            mDeviceConnection.close();
            mDeviceConnection = null;
        }
        mInterface = null;
        mUsbDevice = null;
        mConnected = false;
        return true;

    }
    private boolean openConnection () throws CommunicationException
    {
        if ( mConnected)
            return true;

        if (mUsbDevice == null) {
            return false;
        }

        if  (mUsbDevice.getInterfaceCount() > 0 ) {
                mInterface = mUsbDevice.getInterface(0);

        }
        if (mInterface != null) {
            UsbDeviceConnection connection = null;
            // check permission

            if ( permissionIntent != null)
                manager.requestPermission(mUsbDevice, permissionIntent);



            if (manager.hasPermission(mUsbDevice)) {
                // open and connect to the selected device
                connection = manager.openDevice(mUsbDevice);

                if (connection == null) {
                    return false;
                }
                if (connection.claimInterface(mInterface, true))
                {

                    mDeviceConnection = connection;

                    int numEndPoints = mInterface.getEndpointCount();
                    for ( int i=0; i < numEndPoints; i++) {
                        UsbEndpoint endpoint = mInterface.getEndpoint(i);

                        if  ( endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK ) {
                            if ( endpoint.getDirection() == UsbConstants.USB_DIR_IN ) {
                                epIn = endpoint;
                            }
                            else if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT){
                                epOut = endpoint;
                            }
                        }
                    }

                    if ( epIn != null && epOut != null) {
                        mConnected = true;
                        return true;
                    }

                }
                else
                {
                    connection.close();
                    throw new CommunicationException("USB CLAIMED FAILED", CommunicationException.ErrorCode.AccessDenied );
                }
            }
            else
            {
                throw new CommunicationException("USB PERMISSION FAILED", CommunicationException.ErrorCode.AccessDenied  );
            }
        }

        return false;
    }


    byte convertBasicStatus(byte input)
    {
        //check GET_PORT_STATUS from CLASS 7 USB.ORG. Parallel port emulation
        byte status = 0x00 ;
        if ( (input & 0x10)>0) //online
            status |= 0x01;
        if ( (input & 0x20) >0) //no paper
            status |= 0x10;
        return status;




    }

    @Override
    public byte getBasicStatus() throws CommunicationException{

        transactionInitialize();


        byte [] buffer = new byte[1];
        int length = mDeviceConnection.controlTransfer( USB_GET_PORT_STATUS_REQ_TYPE,
                USB_GET_PORT_STATUS_REQ_ID,0,0,
                buffer, buffer.length,readTimeout);
        if ( length == buffer.length) {

            return convertBasicStatus(buffer[0]);

        }
        return 0x10;
    }

}