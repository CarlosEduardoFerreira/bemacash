/* Copyright 2011-2013 Google Inc.
 * Copyright 2013 mike wakerly <opensource@hoho.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: https://github.com/mik3y/usb-serial-for-android
 */

package com.hoho.android.usbserial.driver;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Developed by Carlos
 */

public class UsbScannerDriver implements UsbSerialDriver {

    private final String TAG = UsbScannerDriver.class.getSimpleName();

    private final UsbDevice mDevice;
    private final UsbSerialPort mPort;

    public UsbScannerDriver(UsbDevice device) {
        mDevice = device;
        mPort = new UsbScannerSerialPort(device, 0);
    }

    @Override
    public UsbDevice getDevice() {
        return mDevice;
    }

    @Override
    public List<UsbSerialPort> getPorts() {
        return Collections.singletonList(mPort);
    }

    class UsbScannerSerialPort extends CommonUsbSerialPort {

        private UsbInterface mControlInterface;
        private UsbInterface mDataInterface;
        private UsbEndpoint mControlEndpoint;

        private static final int USB_RECIP_INTERFACE = 0x01;
        private static final int USB_RT_ACM = UsbConstants.USB_TYPE_CLASS | USB_RECIP_INTERFACE;

        private static final int SET_LINE_CODING = 0x20;
        private static final int SET_CONTROL_LINE_STATE = 0x22;

        private final boolean mEnableAsyncReads;

        private UsbEndpoint mReadEndpoint;
        private UsbEndpoint mWriteEndpoint;

        private boolean mRts = false;
        private boolean mDtr = false;

        public UsbScannerSerialPort(UsbDevice device, int portNumber) {
            super(device, portNumber);
            Log.d("BemaCarl4","UsbScannerDriver.UsbScannerSerialPort.device: " + device);
            Log.d("BemaCarl4","UsbScannerDriver.UsbScannerSerialPort.portNumber: " + portNumber);
            mEnableAsyncReads = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1);
        }

        @Override
        public UsbSerialDriver getDriver() {
            return UsbScannerDriver.this;
        }

        @Override
        public void open(UsbDeviceConnection connection) throws IOException {

            setReadBufferSize(8*1024);
            setWriteBufferSize(8*1024);

            Log.d("BemaCarl4","UsbScannerDriver.open");
            if (mConnection != null) {
                throw new IOException("Already open");
            }

            mConnection = connection;
            boolean opened = false;
            try {
                Log.d("BemaCarl4","UsbScannerDriver.open.mDevice.getInterfaceCount(): " + mDevice.getInterfaceCount());
                for (int i = 0; i < mDevice.getInterfaceCount(); i++) {
                    Log.d("BemaCarl4","UsbScannerDriver.open.mDevice.getInterface("+i+"): " + mDevice.getInterface(i));
                    Log.d("BemaCarl4","UsbScannerDriver.open.mDevice.getInterface("+i+").getInterfaceClass: " + mDevice.getInterface(i).getInterfaceClass());
                    if (connection.claimInterface(mDevice.getInterface(i), true)) {
                        if(i == 0){
                            mControlInterface = mDevice.getInterface(0);
                            Log.d("BemaCarl4","UsbScannerDriver.open.mControlInterface."+i+": " + mControlInterface);
                            mControlEndpoint = mControlInterface.getEndpoint(0);
                            Log.d("BemaCarl4","UsbScannerDriver.open.mControlEndpoint."+i+": " + mControlEndpoint);

                            mDataInterface = mDevice.getInterface(0);
                            Log.d("BemaCarl4","UsbScannerDriver.open.mDataInterface."+i+": " + mDataInterface);

                            int endPointsQty = mDataInterface.getEndpointCount();
                            Log.d("BemaCarl4","UsbScannerDriver.open."+i+".endPointsQty: " + endPointsQty);

                            int dir_1_ep = endPointsQty == 1 ? 0 : 1;

                            int direction_0 = mDataInterface.getEndpoint(0).getDirection();
                            int direction_1 = mDataInterface.getEndpoint(dir_1_ep).getDirection();
                            int readIndex = direction_0 == 128 ? 0 : 1;
                            int writIndex = direction_1 ==   0 ? 1 : 0;

                            Log.d("BemaCarl4","UsbScannerDriver.open."+i+".direction_0: " + direction_0);
                            Log.d("BemaCarl4","UsbScannerDriver.open."+i+".direction_1: " + direction_1);
                            Log.d("BemaCarl4","UsbScannerDriver.open."+i+".readIndex: " + readIndex);
                            Log.d("BemaCarl4","UsbScannerDriver.open."+i+".writIndex: " + writIndex);

                            mReadEndpoint = mDataInterface.getEndpoint(readIndex);
                            Log.d("BemaCarl4","UsbScannerDriver.open.mReadEndpoint."+i+": " + mReadEndpoint);
                            mWriteEndpoint = mDataInterface.getEndpoint(writIndex);
                            Log.d("BemaCarl4","UsbScannerDriver.open.mWriteEndpoint."+i+": " + mWriteEndpoint);
                        }
                        if(i == 1){
                            mDataInterface = mDevice.getInterface(1);
                            Log.d("BemaCarl4","UsbScannerDriver.open.mDataInterface."+i+": " + mDataInterface);

                            int endPointsQty = mDataInterface.getEndpointCount();
                            Log.d("BemaCarl4","UsbScannerDriver.open."+i+".endPointsQty: " + endPointsQty);

                            int dir_1_ep = endPointsQty == 1 ? 0 : 1;

                            int direction_0 = mDataInterface.getEndpoint(0).getDirection();
                            int direction_1 = mDataInterface.getEndpoint(dir_1_ep).getDirection();
                            int readIndex = direction_0 == 128 ? 0 : 1;
                            int writIndex = direction_1 ==   0 ? 1 : 0;

                            Log.d("BemaCarl4","UsbScannerDriver.open."+i+".direction_0: " + direction_0);
                            Log.d("BemaCarl4","UsbScannerDriver.open."+i+".direction_1: " + direction_1);
                            Log.d("BemaCarl4","UsbScannerDriver.open."+i+".readIndex: " + readIndex);
                            Log.d("BemaCarl4","UsbScannerDriver.open."+i+".writIndex: " + writIndex);

                            mReadEndpoint = mDataInterface.getEndpoint(readIndex);
                            Log.d("BemaCarl4","UsbScannerDriver.open.mReadEndpoint."+i+" " + mReadEndpoint);
                            mWriteEndpoint = mDataInterface.getEndpoint(writIndex);
                            Log.d("BemaCarl4","UsbScannerDriver.open.mWriteEndpoint."+i+": " + mWriteEndpoint);
                        }
                        Log.d("BemaCarl4","UsbScannerDriver.open.connection.claimInterface."+i+".SUCCESS: " + mDevice.getInterface(i));
                    } else {
                        Log.d("BemaCarl4","UsbScannerDriver.open.connection.claimInterface."+i+".ERROR: " + mDevice.getInterface(i));
                    }
                }
                opened = true;
            } finally {
                if (!opened) {
                    close();
                    mConnection = null;
                }
            }
        }

        private int sendAcmControlMessage(int request, int value, byte[] buf) {
            return mConnection.controlTransfer(
                    USB_RT_ACM, request, value, 0, buf, buf != null ? buf.length : 0, 5000);
        }

        @Override
        public void close() throws IOException {
            if (mConnection == null) {
                throw new IOException("Already closed");
            }
            mConnection.close();
            mConnection = null;
        }

        @Override
        public int read(byte[] dest, int timeoutMillis) throws IOException {
            if (mEnableAsyncReads) {
              final UsbRequest request = new UsbRequest();
              try {
                request.initialize(mConnection, mReadEndpoint);
                final ByteBuffer buf = ByteBuffer.wrap(dest);
                if (!request.queue(buf, dest.length)) {
                  throw new IOException("Error queueing request.");
                }

                final UsbRequest response = mConnection.requestWait();
                if (response == null) {
                  throw new IOException("Null response");
                }

                final int nread = buf.position();
                if (nread > 0) {
                  return nread;
                } else {
                  return 0;
                }
              } finally {
                request.close();
              }
            }

            final int numBytesRead;
            synchronized (mReadBufferLock) {
                int readAmt = Math.min(dest.length, mReadBuffer.length);
                numBytesRead = mConnection.bulkTransfer(mReadEndpoint, mReadBuffer, readAmt,
                        timeoutMillis);
                if (numBytesRead < 0) {
                    if (timeoutMillis == Integer.MAX_VALUE) {
                        return -1;
                    }
                    return 0;
                }
                System.arraycopy(mReadBuffer, 0, dest, 0, numBytesRead);
            }
            return numBytesRead;
        }


        @Override
        public int write(byte[] src, int timeoutMillis) throws IOException {
            int offset = 0;

            while (offset < src.length) {
                final int writeLength;
                final int amtWritten;
                synchronized (mWriteBufferLock) {
                    final byte[] writeBuffer;
                    writeLength = Math.min(src.length - offset, mWriteBuffer.length);
                    if (offset == 0) {
                        writeBuffer = src;
                    } else {
                        System.arraycopy(src, offset, mWriteBuffer, 0, writeLength);
                        writeBuffer = mWriteBuffer;
                    }

                    amtWritten = mConnection.bulkTransfer(mWriteEndpoint, writeBuffer, writeLength,
                            timeoutMillis);
                }
                if (amtWritten <= 0) {
                    throw new IOException("Error writing " + writeLength
                            + " bytes at offset " + offset + " length=" + src.length);
                }
                Log.d(TAG, "Wrote amt=" + amtWritten + " attempted=" + writeLength);
                offset += amtWritten;
            }
            return offset;
        }

        @Override
        public void setParameters(int baudRate, int dataBits, int stopBits, int parity) {
            byte stopBitsByte;
            switch (stopBits) {
                case STOPBITS_1: stopBitsByte = 0; break;
                case STOPBITS_1_5: stopBitsByte = 1; break;
                case STOPBITS_2: stopBitsByte = 2; break;
                default: throw new IllegalArgumentException("Bad value for stopBits: " + stopBits);
            }

            byte parityBitesByte;
            switch (parity) {
                case PARITY_NONE: parityBitesByte = 0; break;
                case PARITY_ODD: parityBitesByte = 1; break;
                case PARITY_EVEN: parityBitesByte = 2; break;
                case PARITY_MARK: parityBitesByte = 3; break;
                case PARITY_SPACE: parityBitesByte = 4; break;
                default: throw new IllegalArgumentException("Bad value for parity: " + parity);
            }

            byte[] msg = {
                    (byte) ( baudRate & 0xff),
                    (byte) ((baudRate >> 8 ) & 0xff),
                    (byte) ((baudRate >> 16) & 0xff),
                    (byte) ((baudRate >> 24) & 0xff),
                    stopBitsByte,
                    parityBitesByte,
                    (byte) dataBits};
            sendAcmControlMessage(SET_LINE_CODING, 0, msg);
        }

        @Override
        public boolean getCD() throws IOException {
            return false;  // TODO
        }

        @Override
        public boolean getCTS() throws IOException {
            return false;  // TODO
        }

        @Override
        public boolean getDSR() throws IOException {
            return false;  // TODO
        }

        @Override
        public boolean getDTR() throws IOException {
            return mDtr;
        }

        @Override
        public void setDTR(boolean value) throws IOException {
            mDtr = value;
            setDtrRts();
        }

        @Override
        public boolean getRI() throws IOException {
            return false;  // TODO
        }

        @Override
        public boolean getRTS() throws IOException {
            return mRts;
        }

        @Override
        public void setRTS(boolean value) throws IOException {
            mRts = value;
            setDtrRts();
        }

        private void setDtrRts() {
            int value = (mRts ? 0x2 : 0) | (mDtr ? 0x1 : 0);
            sendAcmControlMessage(SET_CONTROL_LINE_STATE, value, null);
        }

    }


    public static Map<Integer, int[]> getSupportedDevices() {
        final Map<Integer, int[]> supportedDevices = new LinkedHashMap<Integer, int[]>();
        supportedDevices.put(com.hoho.android.usbserial.driver.UsbId.VENDOR_BEMA,
                new int[] {
                        com.hoho.android.usbserial.driver.UsbId.D_7500_1,
                        com.hoho.android.usbserial.driver.UsbId.D_7500_2
                });
        return supportedDevices;
    }

}
