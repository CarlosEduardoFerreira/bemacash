package com.kaching123.display.scale;


import android.util.Log;

import com.bematechus.bemaUtils.CommunicationException;
import com.bematechus.bemaUtils.CommunicationPort;
import com.bematechus.bemaUtils.PortInfo;
import com.bematechus.bemaUtils.WatchDog;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by b1107005 on 5/23/2015.
 */

public class SerialPort extends CommunicationPort {

    private static final String TAG = "SerialPort";

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd = null;
    private InputStream mFileInputStream = null;
    private OutputStream mFileOutputStream = null;

    private final String pathCOM1 = "/dev/ttymxc0";
    private final String pathCOM2 = "/dev/ttymxc1";
    private final String pathCOM3 = "/dev/ttymxc2";

    private final String pathCOM1_3036 = "/dev/ttyS0";
    private final String pathCOM2_3036 = "/dev/ttyS1";
    private final String pathCOM3_3036 = "/dev/ttyS3";


    private int baudrate = 9600;
    private int databits = 8;
    private int parity = 0;
    private int stopbits = 1;
    private int flowctl = 0;
    private android_serialport_api.SerialPort nativePort = null;



    private String getPathFromPath(String portName)
    {
        String arch     = System.getProperty("os.arch","unknown");
        String name     = System.getProperty("os.name","unknown");
        String version  = System.getProperty("os.version","unknown");

        String upper = portName.toUpperCase();
        String path = portName;
        if ( upper.matches("COM1"))
        {
            path = version.equals("3.0.36+".toString()) ? pathCOM1_3036 : pathCOM1;
        }
        else if (upper.matches("COM2"))
        {
            path = version.equals("3.0.36+".toString()) ? pathCOM2_3036 : pathCOM2;
        }
        else if (upper.matches("COM3"))
        {
            path = version.equals("3.0.36+".toString()) ? pathCOM3_3036 : pathCOM3;
        }

        return path;
    }

    private File initialize (String portName) throws SecurityException
    {

        File device = new File( getPathFromPath(portName));

        String path1 = device.getAbsolutePath();

        if (!device.canRead() || !device.canWrite()) {

            try {
                Runtime.getRuntime().exec("su -c chmod 666 " + path1);
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }else{
            Log.d("TCR.bemacarl.yes", device.getAbsolutePath());
        }

        return device;

    }

    public boolean open(String portName,  int baud, int dataBits, int parity, int stopBits, int flow) throws CommunicationException {

        try {
            File device = initialize(portName);

            if ( nativePort != null)
            {
                nativePort.close();

            }
            nativePort = new android_serialport_api.SerialPort(device, baud, dataBits, parity, stopBits, flow);

            mFileInputStream = nativePort.getInputStream();
            mFileOutputStream = nativePort.getOutputStream();
        }
        catch ( SecurityException ex)
        {
            Log.d(TAG, "Access Denied");
            throw new CommunicationException(ex.getMessage(), CommunicationException.ErrorCode.AccessDenied);

        }
        catch (Exception ex)
        {
            Log.d(TAG, "Connection Refused");
            throw new CommunicationException(ex.getMessage(), CommunicationException.ErrorCode.ConnectionRefused);
        }


        return true;
    }

    @Override
    public boolean open(PortInfo info) throws CommunicationException {

        readTimeout = info.getReadTimeout();

        return open(info.getPortName(), info.getBaudRate(), info.getDataBits(),
                info.getParity().getValue(), info.getStopBits(), info.getFlow().getValue());


    }

    @Override
    public void close() throws CommunicationException {
        if ( isOpen())
        {
            try {
                if (mFileInputStream != null) {
                    mFileInputStream.close();

                }
                if (mFileOutputStream != null) {
                    mFileOutputStream.close();

                }

            }
            catch (IOException ex)
            {
                throw new CommunicationException(ex.getMessage(), CommunicationException.ErrorCode.CloseError);
            }
            finally {
                if (nativePort != null)
                {
                    nativePort.close();
                    nativePort = null;
                }
                mFd = null;
                mFileOutputStream = null;
                mFileInputStream = null;
            }

        }

    }



    @Override
    public Integer write(byte[] data, int sizeToWrite) throws CommunicationException {
        if ( isOpen() == false ||  mFileOutputStream == null)
            throw new CommunicationException("Port Not initialized", CommunicationException.ErrorCode.PortNotAvailable);

        try {
            mFileOutputStream.write(data, 0, sizeToWrite);
        }
        catch (IOException ex)
        {
            throw new CommunicationException(ex.getMessage(), CommunicationException.ErrorCode.WriteError);

        }
        //file outputstream does not inform number of bytes written
        //assume , if successful that everything has been written
        return  sizeToWrite;
    }

    @Override
    public Integer read(byte[] data, int sizeToRead) throws CommunicationException {
        if ( isOpen() == false ||  mFileInputStream == null)
            throw new CommunicationException("Port Not initialized", CommunicationException.ErrorCode.PortNotAvailable);

        int bytesRead = 0;
        int ret = 0;
        try {
            WatchDog wd = new WatchDog();
            wd.Start(readTimeout.longValue());
            while ( bytesRead < sizeToRead) {
                ret = mFileInputStream.read(data, bytesRead, sizeToRead-bytesRead);
                if (wd.isTimeOut() || ret < 0)
                    break;
                bytesRead += ret;
                Thread.yield();
            }
        }
        catch (IOException ex)
        {
            CommunicationException e =  new CommunicationException(ex.getMessage(), CommunicationException.ErrorCode.ReadError);
            e.setDataTransmitted(bytesRead);
        }


        return  bytesRead;
    }

    @Override
    public boolean isOpen() {
        return nativePort != null;
    }




    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }


}

