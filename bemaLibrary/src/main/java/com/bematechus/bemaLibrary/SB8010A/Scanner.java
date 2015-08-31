package com.bematechus.bemaLibrary.SB8010A;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.serialport.*;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;



public class Scanner  {

    public interface OnDataReadyListner {
        void onDataReady(String tmp);

    }

    private static final String INTENT_NAME = "SCANNER_SB8010_FILTER";
    private static final String INTENT_KEY = "SCANNED_STRING";

    private static final String TAG = "BemaDisplay";
    private android.serialport.SerialPort port;
    private final String pathCOM3 = "/dev/ttymxc2";


    protected OutputStream outputStream;
    protected InputStream inputStream;
    private final int baudrate = 9600;
    private final int databits = 8;
    private final int parity = 0;
    private final int stopbits = 1;
    private final int flowctl = 0;
    private final int terminator = 0x0a;

    private static Scanner instance = null;
    boolean active = false;
    Thread readingThread = null;
    OnDataReadyListner listner = null;
    private final Object listnerLock = new Object(); // listener set/reset
    private final Object bufferLock = new Object(); //buffer
    private Queue<Character> buffer = new LinkedList<Character>();
    BroadcastReceiver broadcastReceiver = null;
    Context context = null;
    IntentFilter intentFilter = null;
    boolean alive = true;



    public void setDataReadyListner (OnDataReadyListner listner)
    {
        synchronized (listnerLock) {
            this.listner = listner;
        }
    }

    private Scanner ()
    {
        intentFilter=new IntentFilter();
        intentFilter.addAction(INTENT_NAME);
        broadcastReceiver = new BroadcastReceiver() {
            /** Receives the broadcast that has been fired */
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(INTENT_NAME)){

                    String receivedValue=intent.getStringExtra(INTENT_KEY);
                    synchronized (listnerLock) {
                        if ( listner != null)
                            listner.onDataReady(receivedValue);
                    }
                }
            }
        };

    }

    synchronized public static Scanner getInstance(Context context)
    {
        if ( instance == null)
        {
            instance = new Scanner();
        }
        instance.context = context;
        return instance;
    }
    String readBuffer()
    {

        String scanned="";

        synchronized (bufferLock) {
            Character[] tmp = new Character[buffer.size()];
            tmp = buffer.toArray(tmp);

            for (char c :tmp)
                scanned+=c;

            buffer.clear();

        }
        return  scanned;

    }
    public String read () {
        return readBuffer();
    }
    public int charactersAvailable()
    {
        int size;
        synchronized (bufferLock) {
            size = buffer.size();
        }

        return size;
    }


    public boolean open()
    {
        try
        {
            port = new android.serialport.SerialPort(pathCOM3,baudrate,databits,parity,stopbits,flowctl);
            inputStream = port.getInputStream();
            outputStream = port.getOutputStream();
            active = false;
            alive = true;
            readingThread = new Thread () {


                void flushBuffer()
                {
                    try {
                        int flushSize = inputStream.available();
                        if (flushSize > 0) {
                            byte[] flush = new byte[flushSize];
                            inputStream.read(flush);
                        }
                    }
                    catch ( Exception ex)
                    {
                        Log.d(TAG, ex.getMessage());
                    }

                }

                public void run()
                {
                    try {

                        flushBuffer();
                        while (alive) {
                            if (port != null && inputStream != null) {
                                if ( inputStream.available() <= 0 )
                                {
                                    Thread.sleep(300);
                                    continue;
                                }

                                int ch = inputStream.read();
                                while (  !active   && inputStream.available() >0) {
                                    ch = inputStream.read();
                                    Thread.sleep(5);
                                }

                                if ( ch == -1 ||!active ) {
                                    Thread.sleep(300);
                                    continue;
                                }
                                else
                                {
                                    if ( context != null) {
                                        if (ch == terminator ) {
                                            if (buffer.size() > 0) {

                                                String scanned = readBuffer();
                                                Intent i = new Intent();
                                                i.setAction(INTENT_NAME);
                                                i.putExtra(INTENT_KEY, scanned);
                                                context.sendBroadcast(i);


                                            }


                                        } else if (ch >= 0x20 && ch <= 0x7e) {
                                            synchronized (bufferLock) {
                                                buffer.add((char) ch);
                                            }
                                        }
                                    }
                                    else {
                                        synchronized (bufferLock) {
                                            buffer.add((char) ch);
                                        }
                                    }

                                }

                            }
                        }
                        synchronized (bufferLock) {
                            buffer.clear();
                        }
                    }
                    catch ( Exception ex)
                    {
                        Log.d(TAG, ex.getMessage());
                    }

                }

            };

            return true;
        }
        catch (Exception ex)
        {
            Log.d(TAG, ex.getMessage());
        }
        return false;
    }
    public void close()
    {

        try {
            alive = false;
            active = false;
            readingThread.join();

        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
        if (port != null)
            port.close();
        try {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
        }
        catch (Exception ex)
        {
            Log.d(TAG, ex.getMessage());
        }

    }
    public void enable(boolean run)
    {
        if ( readingThread != null ) {
            if (this.active) {
                if (!run) {

                    this.active = false;


                    if (context != null)
                        context.unregisterReceiver(broadcastReceiver);


                    //stop thread

                }
            } else {
                if (run) {
                    this.active = true;
                    try {

                        readingThread.start();
                    } catch (Exception ex) {
                        Log.d(TAG, ex.getMessage());
                    }


                    if (context != null)
                     context.registerReceiver(broadcastReceiver, intentFilter);
                    //enable thread

                }
            }
        }

    }


}
