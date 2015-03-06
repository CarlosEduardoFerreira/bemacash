package com.kaching123.display;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothSocketPrinter implements DisplayPrinter {

    private static final UUID SPP_SLAVE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

	private BluetoothSocket socket;

	private OutputStream out;

	public BluetoothSocketPrinter(BluetoothDevice device) throws IOException {
        socket = device.createRfcommSocketToServiceRecord(SPP_SLAVE_UUID);
        socket.connect();
        out = socket.getOutputStream();
	}
	
	@Override
	public void write(byte[] bytes) throws IOException {
		out.write(bytes);
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}

    @Override
    public boolean isUSBDisplayer() {
        return false;
    }

}
