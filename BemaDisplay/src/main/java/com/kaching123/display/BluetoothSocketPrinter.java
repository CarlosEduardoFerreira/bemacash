package com.kaching123.display;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public class BluetoothSocketPrinter implements DisplayPrinter {

    private SerialPort mSerialPort = null;

    protected OutputStream mOutputStream;

    public BluetoothSocketPrinter(String path, int baudrate, int databits, int parity, int stopbits, int flowctl) throws IOException {
        try {
            System.out.println("trace--BluetoothSocketPrinter: 1");
            mSerialPort = new SerialPort(new File(path), baudrate, databits, parity, stopbits, flowctl);
            mOutputStream = mSerialPort.getOutputStream();
        } catch (SecurityException e) {
            System.out.println("trace--BluetoothSocketPrinter: 2" + e.toString());
        } catch (InvalidParameterException e) {
            System.out.println("trace--BluetoothSocketPrinter: 3" + e.toString());
        }
        catch (IOException e) {
            System.out.println("trace--BluetoothSocketPrinter: 4" + e.toString());
        }


    }

    @Override
    public void write(byte[] bytes) throws IOException {
        if (mOutputStream != null)
            mOutputStream.write(bytes);
    }

    @Override
    public void close() throws IOException {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

}
