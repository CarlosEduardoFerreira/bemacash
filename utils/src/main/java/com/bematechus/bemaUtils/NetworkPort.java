package com.bematechus.bemaUtils;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Created by b1107005 on 5/23/2015.
 */
public class NetworkPort extends CommunicationPort {


    private Socket socket = null;

    private InputStream is = null;
    private OutputStream out = null;
    private static String TAG = "NetworkPort";
    private PortInfo info = null;


    @Override
    public boolean open(final PortInfo info) throws CommunicationException {

        this.info = new PortInfo (info);
        if ( info != null) {
            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    try {
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(info.getPortName(), info.getPortNumber()), info.getConnectionTimeout());
                        socket.setSoTimeout(info.getReadTimeout());
                        is = socket.getInputStream();
                        out = socket.getOutputStream();


                        writeTimeout = info.getWriteTimeout();
                        readTimeout = info.getReadTimeout();
                        connectionTimeout = info.getConnectionTimeout();
                        return socket;
                    } catch (IOException ex) {

                    }

                    return null;

                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            try {
                if ( task.get() != null)
                    return true;
            } catch (Exception ex) {

                throw new CommunicationException(ex.getMessage(), CommunicationException.ErrorCode.ConnectionRefused);
            }

        }
        return false;


    }

    private void reopen ()
    {
        try {
            close();
            socket = new Socket();
            socket.connect(new InetSocketAddress(info.getPortName(), info.getPortNumber()), info.getConnectionTimeout());
            socket.setSoTimeout(info.getReadTimeout());
            is = socket.getInputStream();
            out = socket.getOutputStream();


            writeTimeout = info.getWriteTimeout();
            readTimeout = info.getReadTimeout();
            connectionTimeout = info.getConnectionTimeout();
        }
        catch (Exception ex)
        {
            //Log.d(TAG, ex.getMessage());
        }
    }



    @Override
    public void close() throws CommunicationException {

        try {
            if (socket != null)
                socket.close();

            socket = null;
            if (is != null)

                is.close();

            is = null;
            if (out != null)
                out.close();
            out = null;
        } catch (IOException ex) {
            throw new CommunicationException(ex.getMessage(), CommunicationException.ErrorCode.CloseError);
        }

    }


    @Override
    public void setReadTimeout(Integer timeout) {
        try {
            if (isOpen()) {
                this.socket.setSoTimeout(timeout);

            }
        } catch (IOException ex) {
            Log.d(TAG, ex.getMessage());
        }
    }


    @Override
    public Integer write(final byte[] data, final int sizeToWrite) throws CommunicationException {

        int bytesWritten = 0;
        AsyncTask task  = new AsyncTask<Void,Void,Integer>() {
            @Override
            protected Integer doInBackground(Void ... unused) {
                int totalBytesWritten = 0;
                int portReopenAttempt = 2;
                while ( portReopenAttempt-- > 0) {
                    if (isOpen() && out != null) {

                        try {

                            out.write(data, 0, sizeToWrite);
                            totalBytesWritten = sizeToWrite;
                            break;
                        } catch (IOException ex) {
                            //Log.d(TAG, ex.getMessage());
                            reopen();

                        }
                    }
                    else reopen();

                }

                return totalBytesWritten;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        try {
            bytesWritten = (Integer) task.get();
        }
        catch (Exception ex )
        {
            throw new CommunicationException(ex.getMessage(), CommunicationException.ErrorCode.WriteError);
        }

        return bytesWritten;



    }

    @Override
    public boolean isOpen()
    {
        return socket != null;
    }
    @Override
    public Integer read(byte[] data, int sizeToRead) throws CommunicationException {

        if ( data == null || sizeToRead <=0)
            return 0;

        AsyncTask readTask = new AsyncTask<Integer, Void, Byte[]>() {

            @Override
            protected Byte[] doInBackground(Integer[] objects) {
                if (isOpen() && objects != null && objects.length > 0) {

                    try {

                        int bytesRead = 0;
                        int bytesToRead = objects[0];
                        byte[] buffer = new byte[bytesToRead];
                        WatchDog wd = new WatchDog();
                        wd.Start(readTimeout.longValue() + 10);
                        do {
                            bytesRead += is.read(buffer, bytesRead, bytesToRead - bytesRead);
                            Thread.yield();


                        } while (!wd.isTimeOut() && bytesRead < bytesToRead && !isCancelled());

                        if ( bytesRead > 0 ) {
                            Byte[] result = new Byte[bytesRead];
                            int i = 0;
                            for (byte b : buffer) {
                                result[i++] = b;
                            }

                            return result;
                        }
                    } catch (IOException ex) {
                        //Log.d(TAG, ex.getMessage());
                    }

                }
                return null;

            }
        };
        readTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Integer[] { sizeToRead});
        int totalBytesRead = 0;
        try
        {
            Byte [] read = (Byte []) readTask.get(readTimeout.longValue()+500, TimeUnit.MILLISECONDS);
            if ( read != null ) {
                totalBytesRead = read.length;
                int j = 0;
                for (Byte b : read) {
                    data[j++] = b;
                }
            }
        }
        catch (Exception ex)
        {
            readTask.cancel(true);
            throw new CommunicationException(ex.getMessage() , CommunicationException.ErrorCode.ReadError);
        }

        return totalBytesRead;
    }
}


