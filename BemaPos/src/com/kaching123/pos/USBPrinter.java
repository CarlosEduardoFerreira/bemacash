package com.kaching123.pos;


import android.app.PendingIntent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;


public class USBPrinter implements PosPrinter {

    private static final int READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(3);
    private static final int WRITE_TIMEOUT = (int)TimeUnit.SECONDS.toMillis(5);
    private static final int MAX_NUM_ATTEMPTS = 3;

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
    public static final String USB_DESC = "USB";
    public static final String USB_MODELS = "LR2000";


    private int pid = 0;
    private int vid = 0;


    public USBPrinter( int pid, int vid, UsbManager manager, PendingIntent permissionIntent) throws IOException {
        super();
        this.pid = pid;
        this.vid = vid;
        this.manager = manager;
        this.permissionIntent = permissionIntent;
        if (this.manager == null) {
            throw new IOException(ERR_USB_SERVICE);
        }

    }
    public boolean findPrinter( boolean forceOpen) throws IOException {
        if ( manager == null)
        {
            throw new IOException(ERR_USB_SERVICE );
        }
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (device.getVendorId() == this.vid && device.getProductId() == this.pid  ) {
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

    private UsbEndpoint epIn = null;
    private UsbEndpoint epOut = null;
    private boolean closeConnection ()
    {
        mConnected = false;
        if ( mDeviceConnection == null) {
            return false;
        }
        mDeviceConnection.close();
        mDeviceConnection = null;
        mInterface = null;
        mUsbDevice = null;
        return true;

    }
    private boolean openConnection () throws IOException
    {
        if ( mConnected)
            return true;

        if (mUsbDevice == null) {
            return false;
        }

        for (int i = 0; i < mUsbDevice.getInterfaceCount(); ) {
            mInterface = mUsbDevice.getInterface(i);
            break;
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
                    throw new IOException("CLAIMED FAILED" );
                }
            }
            else
            {
                throw new IOException("PERMISSION FAILED" );
            }
        }

        return false;
    }

    @Override
    public boolean supportExtendedStatus()
    {
        return false;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        if ( mUsbDevice == null ) {
            boolean ret = findPrinter(false);
            if ( ret == false)
            {
                throw new IOException(ERR_USB_NOT_CONNECTED);
            }
        }

        if ( openConnection() == false)
        {
            throw new IOException(ERR_USB_CONNECTION);
        }
        int attempts = MAX_NUM_ATTEMPTS;
        int totalBytesWritten = 0;
        int totalLength = bytes.length;
        do {

            int ret = mDeviceConnection.bulkTransfer(epOut, bytes,bytes.length, WRITE_TIMEOUT);
            if (ret < 0) {
                closeConnection();
                throw new IOException(ERR_USB_WRITE);

            } else
                totalBytesWritten += ret;
            if ( totalBytesWritten >= totalLength)
                break;

            if ( ret < bytes.length)
                bytes = Arrays.copyOfRange(bytes,ret,bytes.length);
        }while ( attempts-- > 0);

    }

    @Override
    public byte[] read(int len) throws IOException {

        if ( mUsbDevice == null ) {
            boolean ret = findPrinter(false);
            if ( ret == false)
            {
                throw new IOException(ERR_USB_NOT_CONNECTED);
            }
        }
        if ( openConnection() == false)
        {
            throw new IOException(ERR_USB_CONNECTION);
        }
        byte[] bytes = new byte[len];
        int ret = mDeviceConnection.bulkTransfer(epIn, bytes,bytes.length, READ_TIMEOUT);

        if (ret < 0) {
            closeConnection();
            throw new IOException(ERR_USB_READ);

        }

        return bytes;
    }

    @Override
    public void close() throws IOException {
        closeConnection();
    }

    @Override
    public byte getBasicStatus() throws IOException{
        if ( mUsbDevice == null ) {
            boolean ret = findPrinter(false);
            if ( ret == false)
            {
                throw new IOException(ERR_USB_NOT_CONNECTED);
            }
        }

        if ( openConnection() == false)
        {
            throw new IOException(ERR_USB_CONNECTION);
        }


        byte [] buffer = new byte[1];
        int length = mDeviceConnection.controlTransfer(USB_GET_PORT_STATUS_REQ_TYPE,
                USB_GET_PORT_STATUS_REQ_ID,0,0,
                buffer, buffer.length,READ_TIMEOUT);
        if ( length == buffer.length) {
            return buffer[0];
        }
        return 0;
    }

}
