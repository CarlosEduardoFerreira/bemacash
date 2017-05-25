package com.kaching123.display;

import android.app.PendingIntent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class USBDisplayPrinter implements DisplayPrinter {

    private static final String ERR_USB_SERVICE = "USB Service Not Initialized";
    private static final String ERR_USB_NOT_CONNECTED = "USB not Connected";
    private static final String ERR_USB_CONNECTION = "Unable to connect to USB Device ";
    private static final String ERR_USB_WRITE = "Unable to write to USB Device";
    private static final String ERR_USB_READ = "Unable to read from USB Device";

    public static final int LDX1000_PID = 0xa010;
    public static final int LDX1000_VID = 0x0fa8;

    private final String USB_DISPLAY = "LDX1000";
    private int pid,vid;
    private static final int READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(3);
    private static final int WRITE_TIMEOUT = (int)TimeUnit.SECONDS.toMillis(5);
    private static final int MAX_NUM_ATTEMPTS = 3;

    private UsbManager manager = null;
    private UsbDevice mUsbDevice = null;
    private UsbInterface mInterface = null;
    private UsbDeviceConnection mDeviceConnection = null;
    private boolean mConnected = false;
    private PendingIntent permissionIntent = null;

    public USBDisplayPrinter(int pid, int vid, UsbManager manager, PendingIntent permissionIntent )throws IOException{
        this.pid = pid;
        this.vid = vid;
        this.manager = manager;
        this.permissionIntent = permissionIntent;
        if (this.manager == null) {
            throw new IOException(ERR_USB_SERVICE);
        }

    }
    public boolean findPrinter( boolean forceOpen) throws IOException {
        Log.d("BemaCarl25", "USBDisplayPrinter.findPrinter.forceOpen: " + forceOpen);
        if ( manager == null)
        {
            throw new IOException(ERR_USB_SERVICE );
        }
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Log.d("BemaCarl25", "USBDisplayPrinter.findPrinter.deviceList.size: " + deviceList.size());
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        Log.d("BemaCarl25", "USBDisplayPrinter.findPrinter.deviceIterator.hasNext: " + deviceIterator.hasNext());

        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            Log.d("BemaCarl25", "USBDisplayPrinter.findPrinter.device.getVendorId()  : " + device.getVendorId());
            Log.d("BemaCarl25", "USBDisplayPrinter.findPrinter.device.getProductId() : " + device.getProductId());
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
    private UsbEndpoint epIn  = null;
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
                bytes = Arrays.copyOfRange(bytes, ret, bytes.length);
        }while ( attempts-- > 0);
    }

    @Override
    public void close() throws IOException {
        closeConnection();
    }

    @Override
    public boolean isUSBDisplayer() {
        return true;
    }

}

