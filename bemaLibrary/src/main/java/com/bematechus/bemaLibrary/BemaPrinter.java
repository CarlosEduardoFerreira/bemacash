/**
 * Created by b1107005 on 5/23/2015.
 */
package com.bematechus.bemaLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;

import com.bematechus.bemaLibrary.SB8010A.SerialPort;
import com.bematechus.bemaUtils.CommunicationException;
import com.bematechus.bemaUtils.CommunicationPort;
import com.bematechus.bemaUtils.NetworkPort;
import com.bematechus.bemaUtils.PortInfo;
import com.bematechus.bemaUtils.UsbPort;
import com.bematechus.bemaUtils.WatchDog;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class BemaPrinter {

    public enum CodePage {
        CP437(0), CP850(2),  CP860(3), CP863(4), CP865(5),  CP866(7) , CP862(15);//ESCPOS
        CodePage (int value)
        {
            this.value = value;
        }
        int value;
        public byte getValue()
        {
            return (byte) value;
        }
    }
    public static <T> List<T> copyList(List<T> source) {
        List<T> dest = new ArrayList<T>();
        for (T item : source) { dest.add(item); }
        return dest;
    }
    public enum DitherMethod {
        Steinbert, Bayer, Floyd
    };

    static final int MAX_PAPER_FEED = 250;
    static final int MAX_DRAWER_PULSE = 800;

    static Map<PrinterInfo.PrinterModel,List<CodePage>> initializeCodePages()
    {
        Map<PrinterInfo.PrinterModel,List<CodePage>> map = new HashMap<PrinterInfo.PrinterModel,List<CodePage>>();
        ArrayList<CodePage> list = new ArrayList<CodePage>(10);
        //TODO: LR2000, LR2000E, MP200 may support different code pages. need to check
        list.addAll (Arrays.asList (CodePage.CP437, CodePage.CP850, CodePage.CP860, CodePage.CP862,
                    CodePage.CP863,CodePage.CP865, CodePage.CP866));

        //each printer should be ultimately checked and only those CP supported added
        map.put(PrinterInfo.PrinterModel.LR1000, copyList(list) );

        //each printer should be ultimately checked and only those CP supported added
        map.put(PrinterInfo.PrinterModel.LR2000, copyList(list) );

        //each printer should be ultimately checked and only those CP supported added
        map.put(PrinterInfo.PrinterModel.LR2000E, copyList(list) );

        //each printer should be ultimately checked and only those CP supported added
        map.put(PrinterInfo.PrinterModel.MP200, copyList(list) );

        return map;


    }
    Map<PrinterInfo.PrinterModel,List<CodePage>> supportedCodePages = initializeCodePages();

    public List<CodePage> getSupportedCodePage (PrinterInfo.PrinterModel model)
    {
        return copyList ( supportedCodePages.get(model));
    }


    private AsyncTask taskFindPrinter = null;

    private CodePage codePage = CodePage.CP437;

    static public final int OK = 0;
    static public final int GENERIC_ERROR = -100;


    private static String TAG = "BemaPrinter";
    private UsbManager manager = null;
    private PendingIntent permissionIntent = null;
    private CommunicationPort port = null;
    private boolean drawerOpenHigh = true;
    private PortInfo portInfo = null;


    public BemaPrinter(PortInfo info) {
        this.portInfo = new PortInfo(info);

    }
    public BemaPrinter() {

    }


    public BemaPrinter(PortInfo info,UsbManager manager, PendingIntent permissionIntent) {

        this.portInfo = new PortInfo(info);
        this.manager = manager;
        this.permissionIntent = permissionIntent;

    }


    public List<PrinterInfo> findPrinters(PrinterInfo.PrinterType typeToSearch) {

        List<PrinterInfo> list = new ArrayList<PrinterInfo>();
        if ( taskFindPrinter != null)
        {
            //task already running
            try {
                taskFindPrinter.cancel(true);
                taskFindPrinter.wait(1000);

            }
            catch (Exception ex )
            {
                Log.d( TAG, ex.getMessage());
            }
            finally {
                taskFindPrinter = null;
            }


        }

        try {

            final long  MAX_SEARCH_TIME =60; //seconds

            taskFindPrinter = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {


                    PrinterInfo.PrinterType filter = (PrinterInfo.PrinterType) objects[0];
                    List<PrinterInfo> printers = new ArrayList<PrinterInfo> ();

                    if (filter == null || filter == PrinterInfo.PrinterType.USB) {
                        printers.addAll(searchForUsbPrinters());

                    }
                    if (filter == null || filter == PrinterInfo.PrinterType.TCPIP) {
                        printers.addAll(searchForNetworkPrinters());
                    }
                    //TODO: Add search to serial printer int the future (how?) extended status
                    return printers;

                }
            };
            taskFindPrinter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,typeToSearch );

            list =  (List<PrinterInfo>)  taskFindPrinter.get (MAX_SEARCH_TIME,TimeUnit.SECONDS);


        }
        catch (TimeoutException ex)
        {
            taskFindPrinter.cancel(true);
        }
        catch (Exception ex)
        {

        }


        return list;

    }

    public synchronized int open ()
    {
        if ( portInfo != null) {
            try {
                if (port != null)
                    port.close();

                switch (portInfo.getType()) {
                    case TCP:
                        port = new NetworkPort();
                        break;
                    case SERIAL:
                        port = new SerialPort();
                        break;
                    case USB:
                        port = new UsbPort();
                        break;
                    default:
                        return CommunicationException.ErrorCode.PortNotAvailable.getValue();

                }
                if (port.open(portInfo)) {

                    return OK;
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

    public synchronized int open (PortInfo info)
    {
        portInfo = new PortInfo(info);
        return open();
    }


    public synchronized int close ()
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

    public synchronized CodePage getCodePage() {
        return codePage;
    }

    private boolean codePagePending = true;
    public synchronized void setCodePage(CodePage codePage) {
        this.codePage = codePage;
        codePagePending = true;
        if ( port != null)
        {
            try
            {
                port.write(new CodePageCommand(codePage.getValue()).getBytes());
                codePagePending = false;
            }
            catch (CommunicationException ex)
            {
                Log.d(TAG, ex.getMessage());

            }
        }

    }

    public synchronized int write (final byte [] data)
    {
        if ( codePagePending)
            setCodePage(this.codePage);

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
    public synchronized int write (final byte [] data, int offset, int size)
    {
        if ( data != null && offset >=0 && size >=0) {
            byte[] buffer = new byte[size];
            System.arraycopy(data,offset,buffer,0,size);
            return write(buffer);

        }
        return 0;



    }
    public synchronized int printText (String text)
    {
        if ( codePagePending)
            setCodePage(this.codePage);

        int result = OK;
        if ( port != null)
        {
            try
            {
                result = port.write(CodePageCommand.convertFromUnicode(codePage, text));

            }
            catch (CommunicationException ex)
            {
                Log.d(TAG, ex.getMessage());

            }
        }
        return result;



    }
    public synchronized int paperCut (int feedPaper)
    {
        int result = GENERIC_ERROR;
        if ( feedPaper > MAX_PAPER_FEED)
            feedPaper = MAX_PAPER_FEED;

        if ( port != null)
        {
            try
            {
                port.write(new PaperCutCommand((byte) feedPaper).getBytes());
                result = OK;

            }
            catch (CommunicationException ex)
            {
                Log.d(TAG, ex.getMessage());

            }
        }
        return result;
    }
    public synchronized int openDrawer (int pulse) //pulse in miliseconds
    {
        int result = GENERIC_ERROR;
        pulse /= 100;

        if ( pulse > MAX_DRAWER_PULSE)
            pulse = MAX_DRAWER_PULSE;

        if ( port != null)
        {
            try
            {
                port.write( new OpenDrawerCommand((byte) pulse).getBytes());
                result = OK;

            }
            catch (CommunicationException ex)
            {
                Log.d(TAG, ex.getMessage());

            }
        }
        return result;
    }
    public synchronized int getDrawerStatus ()
    {
        int result = GENERIC_ERROR;
        if ( port != null)
        {
            try
            {
                if ( port.write( new DrawerStatusCommand().getBytes()) > 0 ) {
                    port.setReadTimeout(1000);
                    byte[] status = new byte[1];
                    if ( port.read(status, 1) == 1)
                    {
                        result = (int) (0x01 & status[0]);
                    }
                }



            }
            catch (CommunicationException ex)
            {
                Log.d(TAG, ex.getMessage());
                result = ex.getErr().getValue();

            }
        }
        return result;
    }

    public  synchronized  int printBmp (Bitmap bmp )
    {
        return printBmp(bmp, DitherMethod.Steinbert);
    }
    public synchronized  int printBmp (Bitmap bmp, DitherMethod method)
    {
        int result = GENERIC_ERROR;


        if ( port != null) {

            byte [] motion = new byte[] { 0x1d, 0x50, (byte) 0xb4, (byte) 0xb4};
            if ( write( motion) == motion.length ) {
                BemaBmp rasterBmp = new BemaBmp(bmp, method);
                byte[] buffer = rasterBmp.getRasterImage();
                int offset = 0;

                int lineSize = 5* (rasterBmp.width * 3 + 5 + 3);

                while (offset < buffer.length) {
                    int sizeToWrite = (buffer.length - offset) > lineSize ? lineSize :
                            (buffer.length - offset);

                    result = write(buffer, offset, sizeToWrite);
                    if (result <= 0)
                        break;

                    offset += result;
                }
            }

        }
        return result;

    }
    public synchronized PrinterStatus geStatus ()
    {
        PrinterStatus pStatus = new PrinterStatus((byte)0x10);

        if ( port != null)
        {
            try
            {
                StatusCommand st = new StatusCommand();

                if ( port.write( st.getBytes()) > 0 ) {
                    port.setReadTimeout(4000);
                    byte[] status = new byte[st.getReturnSize()];
                    int bytesRead = 0;
                    WatchDog wd = new WatchDog();
                    wd.Start((long) port.getReadTimeout());
                    while ( bytesRead < st.getReturnSize() && wd.isTimeOut() == false ) {

                        int ret = port.read( status, bytesRead, st.getReturnSize()-bytesRead);
                        if ( ret < 0 )
                            break;
                        bytesRead += ret;

                    }
                    if (bytesRead == st.getReturnSize() )
                    {
                        byte result = 0;
                        result |=  (0x0008 & ~status[0]) >>3; //off-line  (move to bit 0)
                        result |=  (0x0004 & status[0]) >>1; //drawer     (move to bit 1)
                        result |=  (0x0004 & status[1]);    //cover       (bit 2 already)
                        result |=  (0x0008 & status[2]); //cutter         (bit 3 already)
                        result |=  (0x0020 & status[3]) >> 1; //paper end  (move to bit 4)

                        pStatus = new PrinterStatus(result);
                    }
                    else if ( portInfo.getType() == PortInfo.PortType.USB)
                    {
                        //maybe old firmware without support to bulk read
                        pStatus = new PrinterStatus(port.getBasicStatus());

                    }

                }

            }
            catch (CommunicationException ex)
            {
                int result = ex.getErr().getValue();
                Log.d(TAG, ex.getMessage()+result);


            }
        }
        return pStatus;
    }

    private ArrayList<PrinterInfo> searchForUsbPrinters() {
        ArrayList<PrinterInfo> list = new ArrayList<PrinterInfo>();
        //UsbManager manager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);
        //mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        if (manager != null) {
            try {


                PortInfo info = new PortInfo();
                info.setUsbManager(manager);
                info.setIntent(permissionIntent);


                UsbPort port = new UsbPort();

                //search for LR2000
                info.setUSB_PID(UsbPort.LR2000_PID);
                info.setUSB_VID(UsbPort.LR2000_VID);

                if (port.open(info)) {
                    PrinterInfo pInfo = new PrinterInfo(PrinterInfo.PrinterType.USB,
                            PrinterInfo.PrinterModel.LR2000, "USB", port.getUsbDeviceId());
                    list.add(pInfo);
                    port.close();

                }

                //TODO: search for new future printers here


            } catch (Exception e) {
                Log.d("Discovery USB printers", e.getMessage());
            }
        }
        return list;

    }

    private void quitSearchThread() {

    }

    private boolean isQuittingSearch() {
        return false;
    }

    final static String DISCOVERY_PHRASE = "MP4200FIND";
    final static long FIND_TIMEOUT = TimeUnit.SECONDS.toMillis(15);
    final static long SEARCHING_TIME = TimeUnit.SECONDS.toMillis(35);

    final static int PC_PORT = 5001;
    final static int PRINTER_PORT = 1460;

    final static String MASK = "255.255.255.255";

    private ArrayList<PrinterInfo> searchForNetworkPrinters() {

        DatagramSocket clientSocket=null;
        ArrayList<PrinterInfo> list = new ArrayList<PrinterInfo>();

        long time = System.currentTimeMillis();
        HashMap<String,PrinterInfo> map = new HashMap<String, PrinterInfo>(5);

        try {
            clientSocket = new DatagramSocket(PC_PORT);


            clientSocket.setSoTimeout((int) FIND_TIMEOUT);
            while (!isQuittingSearch() && (System.currentTimeMillis() - time) < SEARCHING_TIME) {
                Log.d(TAG, "Discovery printers findIteration");
                sendMessage(clientSocket);
                waitAnswers(clientSocket, map);
            }
        } catch (Exception e) {
            Log.d(TAG, "Discovery printers ");
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
            clientSocket = null;
        }

      list.addAll(map.values());


        return list;

    }


    private void sendMessage(DatagramSocket clientSocket) throws IOException {
        byte[] sendData = DISCOVERY_PHRASE.getBytes();

        InetAddress mask = InetAddress.getByName(MASK);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, mask, PRINTER_PORT);
        clientSocket.setBroadcast(true);
        clientSocket.send(sendPacket);
        Log.d(TAG, "Discovery printers after send");
    }


    private void waitAnswers(DatagramSocket clientSocket, HashMap<String,PrinterInfo> map ) throws IOException {

        Log.d(TAG, "Discovery printers waitAnswers");
        //int i = 0;
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < FIND_TIMEOUT) {
            byte[] receiveData = new byte[34];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                Log.d(TAG, "Discovery printers before received");

                clientSocket.receive(receivePacket);
                Log.d(TAG, "Discovery printers received");
            } catch (SocketTimeoutException tEx) {
                Log.d(TAG, "Discovery timeout2", tEx);
                return;
            } catch (InterruptedIOException tEx) {
                Log.d(TAG, "Discovery timeout1", tEx);
                return;
            }

            PrinterInfo info = parsePrinter(receiveData);
            if (info != null) {
                if ( !map.containsKey(info.getMAC()  ))
                {

                   map.put(info.getMAC(),info);
                }
            }


        }
    }


    protected static PrinterInfo parsePrinter(byte[] receiveData) {

        String answerPhrase = new String(receiveData, 0, 11);

        /*ByteBuffer macBuf = ByteBuffer.wrap(receiveData, 11, 6);
        String mac = toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get()));*/
        String mac = parseMac(receiveData);

        ByteBuffer ipBuf = ByteBuffer.wrap(receiveData, 19, 4);
        String ip = toShort(ipBuf.get()) + "." + toShort(ipBuf.get()) + "." + toShort(ipBuf.get()) + "." + toShort(ipBuf.get());

        ByteBuffer subNetBuf = ByteBuffer.wrap(receiveData, 23, 4);
        String subNet = toShort(subNetBuf.get()) + "." + toShort(subNetBuf.get()) + "." + toShort(subNetBuf.get()) + "." + toShort(subNetBuf.get());

        ByteBuffer gatewayBuf = ByteBuffer.wrap(receiveData, 27, 4);
        String gateway = toShort(gatewayBuf.get()) + "." + toShort(gatewayBuf.get()) + "." + toShort(gatewayBuf.get()) + "." + toShort(gatewayBuf.get());

        ByteBuffer portBuf = ByteBuffer.wrap(receiveData, 31, 2);

        byte portLByte = portBuf.get();
        byte portMByte = portBuf.get();

        int port = toInt(toShort(portLByte), toShort(portMByte));

        boolean dhcp = receiveData[33] == 1;

        PrinterInfo info = new PrinterInfo(PrinterInfo.PrinterType.TCPIP, PrinterInfo.PrinterModel.LR2000E, ip, port);
        info.setGW(gateway);
        info.setMAC(mac);

        info.setDHCP(dhcp);
        return info;

    }

    protected static String toHex(int v) {
        String result = Integer.toHexString(v);
        if (result.length() == 1)
            return "0" + result;
        return result;
    }

    protected static String parseMac(byte[] receiveData) {
        ByteBuffer macBuf = ByteBuffer.wrap(receiveData, 11, 6);
        String mac = toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get())) + ":" + toHex(toShort(macBuf.get()));
        return mac;
    }

    public static int toShort(byte b) {
        return b & 0xFF;
    }

    public static int toInt(byte lb, byte hb) {
        return ((int) hb << 8) | ((int) lb & 0xFF);
    }

    public static int toInt(int lb, int hb) {
        return (hb << 8) | (lb & 0xFF);
    }



}
