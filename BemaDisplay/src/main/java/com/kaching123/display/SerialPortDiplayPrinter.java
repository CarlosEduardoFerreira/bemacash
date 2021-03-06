package com.kaching123.display;

import android.os.Build;

import android_serialport_api.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public class SerialPortDiplayPrinter implements DisplayPrinter {

    public static final String INTEGRATED_DISPLAYER = "Integrated Customer Display";
    private SerialPort mSerialPort = null;
    protected OutputStream mOutputStream;
    private final String path = "/dev/ttymxc4";
    private final String path2 = "/dev/ttymxc3";
    private final String COM1 = "/dev/ttymxc0";
    private final String COM2 = "/dev/ttymxc1";
    private final int baudrate = 9600;
    private final int databits = 8;
    private final int parity = 0;
    private final int stopbits = 1;
    private final int flowctl = 0;

    public String getPort() {
        return port;
    }

    private String port;

    public SerialPortDiplayPrinter(String port) throws IOException {
        this.port = port;
        if (mSerialPort == null) {
            /* Open the serial port */
            try {
                if(port.equalsIgnoreCase(INTEGRATED_DISPLAYER)) {
                    if (Build.MANUFACTURER.toUpperCase().contains("FREESCALE")) {
                        mSerialPort = new SerialPort(new File(path2), baudrate, databits, parity, stopbits, flowctl);
                    } else
                        mSerialPort = new SerialPort(new File(path), baudrate, databits, parity, stopbits, flowctl);
                }else{
                    if (port.equalsIgnoreCase("COM1"))
                        mSerialPort = new SerialPort(new File(COM1), baudrate, databits, parity, stopbits, flowctl);
                    else
                        mSerialPort = new SerialPort(new File(COM2), baudrate, databits, parity, stopbits, flowctl);
                }
                mOutputStream = mSerialPort.getOutputStream();
            } catch (SecurityException e) {
            } catch (IOException e) {
                throw new IOException(e);
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

    @Override
    public boolean isUSBDisplayer() {
        return true;
    }

}

