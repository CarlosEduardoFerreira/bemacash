package com.bematechus.bemaLibrary.SB8010A;
import android.serialport.SerialPort;
import android.util.Log;

import com.bematechus.bemaUtils.CodePage850Converter;

import java.io.OutputStream;


/**
 * Created by b1107005 on 6/7/2015.
 */
public class BemaDisplay {
    private static final String TAG = "BemaDisplay";
    private static final byte[] NEW_LINE = new byte [] {0x0a} ;
    private static final byte[] MOVE_RIGHT = new byte[] { 0x09};
    private SerialPort port;
    private final String pathCOM5 = "/dev/ttymxc4";


    protected OutputStream outputStream;
    private final int baudrate = 9600;
    private final int databits = 8;
    private final int parity = 0;
    private final int stopbits = 1;
    private final int flowctl = 0;



    private static final byte E_MOVE_TO_HOME_POSITION = 0x0B;


    private static final byte[] MOVE_HOME = {E_MOVE_TO_HOME_POSITION};
    private static final byte[] INITIALIZE_DISPLAY = {0x1b, 0x40, 0x1b,0x74,0x02, E_MOVE_TO_HOME_POSITION};
    private byte [] MOVE_CURSOR = {0x1f, 0x24, 0x00, 0x00 }; //Y_X
    private static final byte [] CLEAR_DISPLAY = { 0x0c};
    private byte [] ENABLE_CURSOR = { 0x1F, 0x43, 0x00};
    private static final byte [] SCROLL_HORIZONTAL = { 0x1F, 0x03};


    public BemaDisplay ()
    {

    }

    public synchronized boolean reset()
    {
        if ( port != null && outputStream != null)
        {
            try {
                outputStream.write(INITIALIZE_DISPLAY);
                return true;

            }
            catch (Exception ex)
            {
                Log.d(TAG, ex.getMessage());
                return false;
            }

        }
        return false;


    }
    public synchronized boolean open ()
    {
        if ( port == null)
        {
            try {
                port = new SerialPort(pathCOM5,baudrate,databits,parity,stopbits,flowctl);
                outputStream = port.getOutputStream();

                return true;

            }
            catch (Exception ex)
            {
                Log.d(TAG, ex.getMessage());
                return false;
            }

        }

        return false;

    }
    public synchronized boolean clear()
    {
        if ( outputStream != null)
        {
            try {
                outputStream.write(CLEAR_DISPLAY);
                return true;
            }
            catch ( Exception ex)
            {
                Log.d(TAG, ex.getMessage());
            }


        }

        return false;
    }
    public synchronized boolean close ()
    {
        if ( port != null)
        {
            try {
                port.close();
                outputStream = null;
                port = null;
            }
            catch (Exception ex)
            {

            }
            return true;
        }
        return false;
    }
    public synchronized boolean goHome()
    {
        if ( outputStream != null)
        {
            try {
                outputStream.write(MOVE_HOME);
                return true;
            }
            catch ( Exception ex)
            {
                Log.d(TAG, ex.getMessage());
            }


        }
        return false;
    }
    public synchronized  boolean moveToLine(int line)
    {
        if ( line >=1 && line <=2)
        {
            if ( outputStream != null)
            {
                try {

                    goHome();
                    if ( line == 2)
                        outputStream.write(NEW_LINE);
                    return true;

                }
                catch ( Exception ex)
                {
                    Log.d(TAG, ex.getMessage());
                }


            }
            return false;

        }
        return false;

    }
    public synchronized boolean moveCursor (int x , int y)
    {
        if ( x >=1 && x <= 20 && y >=1 && y <= 2)
        {
            if ( outputStream != null)
            {
                try {
                    moveToLine(y);

                    for (int i=1; i<x; i++ )
                        outputStream.write(MOVE_RIGHT);
                    return true;
                }
                catch ( Exception ex)
                {
                    Log.d(TAG, ex.getMessage());
                }


            }
            return false;

        }
        return false;
    }
    public boolean writeText (String text)
    {
        if ( outputStream != null && text !=null)
        {
            try {


                outputStream.write(new CodePage850Converter().convert(text));
                return true;
            }
            catch ( Exception ex)
            {
                Log.d(TAG, ex.getMessage());
            }


        }
        return false;

    }
    public boolean showCursor (boolean show)
    {
        if (outputStream != null) {
            try {
                ENABLE_CURSOR[2] = show ? (byte) 0x01 : (byte) 0x00;
                outputStream.write(ENABLE_CURSOR);
                return true;
            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            }


        }


        return false;


    }
    public boolean scrollHorizontal() {
        if (outputStream != null) {
            try {
                outputStream.write(SCROLL_HORIZONTAL);
                return true;
            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            }


        }


        return false;

    }

}
