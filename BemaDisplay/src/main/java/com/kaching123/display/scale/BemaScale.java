package com.kaching123.display.scale;

import android.util.Log;

import com.bematechus.bemaUtils.CommunicationException;
import com.bematechus.bemaUtils.PortInfo;
import com.bematechus.bemaUtils.WatchDog;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Created by long.jiao on 7/16/2015.
 */
public class BemaScale {
    public static final String LB_UNIT_LABEL = "LB";
    public static final String LBS_UNIT_LABEL = "LBS";
    private static String TAG = "BemaScale";
    static public final int OK = 0;
    static public final int GENERIC_ERROR = -100;
    public static final byte[] INIT = {0x05}; // initiate communication
    //    public static final byte[] REQ = {0x12}; // request of weight data of type-1
    public static final byte[] REQ = {0x57,0x0D}; // request of weight data
    public static final byte[] ZERO = {0x38,0x38,0x38,0x38}; // request of weight data
    private static final int ScaleDataLength = 16;

    private PortInfo portInfo;

    public SerialPort getPort() {
        return port;
    }

    public void setPort(SerialPort port) {
        this.port = port;
    }

    private SerialPort port;
    public BemaScale(PortInfo portInfo){
        this.portInfo = portInfo;
    }

    static public PortInfo scalePortInfo() {
        PortInfo port = new PortInfo();
        port.setPortName("COM2");
        port.setBaudRate(9600);
        port.setDataBits(7);
        port.setParity(PortInfo.Parity.Even);
        port.setStopBits(1);
        port.setFlow(PortInfo.FlowControl.NoFlowControl);
        return port;
    }

    public int open ()
    {
        if ( portInfo != null) {
            try {
                if (port != null)
                    port.close();

                port = new SerialPort();

                if (port.open(portInfo)) {
                    return getStatus();
                }


            } catch (CommunicationException ex) {

                Log.d(TAG, ex.getMessage());
                return ex.getErr().getValue();
            }
        }
        else
        {
            return CommunicationException.ErrorCode.ServiceNotInitialized.getValue();
        }
        return GENERIC_ERROR;
    }

    public int open (PortInfo info)
    {
        portInfo = new PortInfo(info);
        return open();
    }


    public int close ()
    {
        try
        {
            if ( port != null)
                port.close();

        }
        catch (CommunicationException Ex)
        {
            return Ex.getErr().getValue();
        }

        return OK;
    }

    public int write (final byte [] data)
    {
        int result = GENERIC_ERROR;
        if ( port != null && data != null)
        {
            try
            {
                result = port.write(data.clone());

            }
            catch (CommunicationException ex)
            {
                Log.d(TAG, ex.getMessage());

            }
        }
        return result;
    }

    public int getStatus(){
        if ( port != null){
            try {
                if (port.write(REQ) > 0) {
                    Thread.sleep(200);
                    if (port.getInputStream().available() == 0)
                        return -1;
                    if (port.getInputStream().available() != ScaleDataLength)
                        return -2;
                    port.setReadTimeout(2000);
                    byte[] bytes = new byte[ScaleDataLength];
                    int bytesRead = 0;
                    WatchDog wd = new WatchDog();
                    wd.Start((long) port.getReadTimeout());
                    while (bytesRead < ScaleDataLength && !wd.isTimeOut()) {
                        int ret = port.read(bytes, bytesRead, ScaleDataLength - bytesRead);
                        if (ret < 0)
                            break;
                        bytesRead += ret;
                    }
                    formatBytes(bytes);
                    return Integer.parseInt(new String(Arrays.copyOfRange(bytes, 12, 14)));
                    //ToDo: Toast different error if necessary
                }
            } catch (NumberFormatException e){
                e.printStackTrace();
            } catch (CommunicationException ex) {
                Log.d(TAG, ex.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private void formatBytes(byte[] bytes) {
        for(int i = 0; i < bytes.length; i++){
            if(bytes[i]<0)
                bytes[i] += 128;
        }
    }

    public String readScale(){
        if ( port != null)
            try {
                if (port.write(REQ) > 0) {
                    Thread.sleep(200);
                    if (port.getInputStream().available() == 0)
                        return "0.00";
                    port.setReadTimeout(2000);
                    byte[] bytes = new byte[ScaleDataLength];
                    int bytesRead = 0;
                    WatchDog wd = new WatchDog();
                    wd.Start((long) port.getReadTimeout());
                    while (bytesRead < ScaleDataLength && wd.isTimeOut() == false) {
                        int ret = port.read(bytes, bytesRead, ScaleDataLength - bytesRead);
                        if (ret < 0)
                            break;
                        bytesRead += ret;
                    }
                    formatBytes(bytes);
                    Log.e(TAG, "serial port : " + Arrays.toString(bytes) + " string : " + new String(bytes));
//                    return Arrays.toString(bytes);
                    // check status
                    if (bytes[12] == 0x30 && bytes[13] == 0x30)
                        return covertBytesToScale(bytes);
                    //ToDo: Toast different error if necessary
                }
            } catch (CommunicationException ex) {
                Log.d(TAG, ex.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        return "0.00";
    }

    public String getUnitsLabel(){
        if ( port != null)
            try {
                if (port.write(REQ) > 0) {
                    Thread.sleep(200);
                    if (port.getInputStream().available() == 0)
                        return "";
                    port.setReadTimeout(2000);
                    byte[] bytes = new byte[ScaleDataLength];
                    int bytesRead = 0;
                    WatchDog wd = new WatchDog();
                    wd.Start((long) port.getReadTimeout());
                    while (bytesRead < ScaleDataLength && wd.isTimeOut() == false) {
                        int ret = port.read(bytes, bytesRead, ScaleDataLength - bytesRead);
                        if (ret < 0)
                            break;
                        bytesRead += ret;
                    }
                    formatBytes(bytes);
                    String unitLabel = new String(Arrays.copyOfRange(bytes,7,9), "UTF-8");
                    //unitLabel = unitLabel.equalsIgnoreCase(LB_UNIT_LABEL) ? LBS_UNIT_LABEL : unitLabel;
                    //unitLabel = unitLabel.equalsIgnoreCase("KG") ? "KGS" : unitLabel;
                    return unitLabel;
                    //ToDo: Toast different error if necessary
                }
            } catch (NumberFormatException e){
                e.printStackTrace();
            } catch (CommunicationException ex) {
                Log.d(TAG, ex.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        return "";
    }


    private String covertBytesToScale(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append(new String(bytes).substring(1,7));
        if(sb.indexOf(".") == -1)
            sb = sb.delete(4,6);
        return sb.toString().replaceAll("^0+(?!$)", ""); // remove leading 0
    }
}

