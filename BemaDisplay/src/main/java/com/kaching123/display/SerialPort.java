/*
 * Copyright 2009 Cedric Priscal
 * Copyright 2009 Allen Hsu <allen@poslab.com.tw>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.kaching123.display;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {

	private static final String TAG = "SerialPort";

	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;

/* Serial port attribute.
 *      data: (7 == CS7, 8 == CS8)
 *      parity: (0 == none, 1 == odd, 2 == even)
 *      stop: (1 == 1 stop bit, 2 == 2 stop bits)
 *      flowctl: (0 == none, 1 == XON/XOFF, 2 == RTS/CTS)
 */
	public SerialPort(File device, int baudrate, int databit, int parity, int stopbit, int flowctl) throws SecurityException, IOException {

		/* Check access permission */

        System.out.println("trace--SerialPort: -1");
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Process su;
				su = Runtime.getRuntime().exec("/system/bin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
                System.out.println("trace--SerialPort: 0");
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}
                System.out.println("trace--SerialPort: 1");
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException();
			}
		}
        System.out.println("trace--SerialPort: 3");
		mFd = open(device.getAbsolutePath(), baudrate, databit, parity, stopbit, flowctl);
        System.out.println("trace--SerialPort: 4");
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}
        System.out.println("trace--SerialPort: 5");
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	// Getters and setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate, int databit, int parity, int stopbit, int flowctl);
	public native void close();
    static {
        System.loadLibrary("serial_port");
    }
}
