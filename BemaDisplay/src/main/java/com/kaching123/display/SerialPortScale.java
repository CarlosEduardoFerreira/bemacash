package com.kaching123.display;

import android_serialport_api.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public class SerialPortScale implements DisplayPrinter {

    private SerialPort mSerialPort = null;
    protected OutputStream mOutputStream;
    protected InputStream mIntputStream;
    private final String path = "/dev/ttymxc1";
    private final int baudrate = 9600;
    private final int databits = 7;
    private final int parity = 2;
    private final int stopbits = 1;
    private final int flowctl = 0;

    public SerialPortScale(){
        if (mSerialPort == null) {
            /* Open the serial port */
            try {
                mSerialPort = new SerialPort(new File(path), baudrate, databits, parity, stopbits, flowctl);
                mOutputStream = mSerialPort.getOutputStream();
            } catch (SecurityException e) {
            } catch (IOException e) {
            } catch (InvalidParameterException e) {
            }
        }
    }

    public InputStream getInputStreamReader() {
        return mIntputStream;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        mOutputStream.write(bytes);
    }

    @Override
    public void close() throws IOException {
        mOutputStream.close();
    }

    @Override
    public boolean isUSBDisplayer() {
        return true;
    }

}

