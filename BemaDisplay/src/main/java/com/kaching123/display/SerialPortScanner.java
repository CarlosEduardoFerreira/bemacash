package com.kaching123.display;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public class SerialPortScanner implements DisplayPrinter {

    private SerialPort mSerialPort = null;
    protected OutputStream mOutputStream;
    protected InputStream mIntputStream;
    private final String path = "/dev/ttymxc2";
    private final int baudrate = 9600;
    private final int databits = 8;
    private final int parity = 0;
    private final int stopbits = 1;
    private final int flowctl = 0;

    public SerialPortScanner() {
        if (mSerialPort == null) {
            /* Open the serial port */
            try {
                mSerialPort = new SerialPort(new File(path), baudrate, databits, parity, stopbits, flowctl);
                mOutputStream = mSerialPort.getOutputStream();
                mIntputStream = mSerialPort.getInputStream();
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
        if (mSerialPort != null) {
            mOutputStream.close();
            mSerialPort = null;
        }

    }

    @Override
    public boolean isUSBDisplayer() {
        return true;
    }

}

