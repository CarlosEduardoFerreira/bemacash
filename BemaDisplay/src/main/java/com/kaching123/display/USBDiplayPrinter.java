package com.kaching123.display;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public class USBDiplayPrinter implements DisplayPrinter {

    private SerialPort mSerialPort = null;
    protected OutputStream mOutputStream;

    public USBDiplayPrinter(String path, int baudrate, int databits, int parity, int stopbits, int flowctl) throws IOException {
        if (mSerialPort == null) {
            /* Read serial port parameters */
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

    @Override
    public void write(byte[] bytes) throws IOException {
        mOutputStream.write(bytes);
    }

    @Override
    public void close() throws IOException {
        mOutputStream.close();
    }

}

