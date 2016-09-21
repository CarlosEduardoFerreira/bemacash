package com.kaching123.tcr.commands.payment.pax.processor;

import android.content.ContentProviderOperation;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.blackstone.pax.PaxTransaction;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.util.Validator;
import com.pax.poslink.BatchResponse;
import com.pax.poslink.CommSetting;
import com.pax.poslink.ManageResponse;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ReportResponse;
import com.telly.groundy.TaskResult;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


/**
 * Created by hamsterksu on 24.04.2014.
 */
public abstract class PaxProcessorBaseCommand extends AsyncCommand {
    public static final int TRANS_TYPE_UNKNOWN = 0;//    ask the terminal to select transaction type
    public static final int TRANS_TYPE_AUTH = 1;// Verify/Authorize a payment. Do not put in batch.
    public static final int TRANS_TYPE_SALE = 2;// To make a purchase with a card or Echeck/ACH with a check. Puts card payment in open batch
    public static final int TRANS_TYPE_RETURN = 3;// Return payment to card
    public static final int TRANS_TYPE_VOID = 4;//Removed a transaction from an unsettled batch
    public static final int TRANS_TYPE_POSTAUTH = 5;// Completes an Auth transaction
    public static final int TRANS_TYPE_FORCEAUTH = 6;//Forces transaction into open batch. Typically used for voice auths
    public static final int TRANS_TYPE_CAPTURE = 7;// –U
    public static final int TRANS_TYPE_REPEATSALE = 8;// –U Performs a repeat sale, using the PnRef, on a previously processed card
    public static final int TRANS_TYPE_CAPTUREALL = 9;// –U Performs a settlement or batch close
    public static final int TRANS_TYPE_ADJUST = 10;//Adjusts a previously processed transaction. Typically used for tip adjustment
    public static final int TRANS_TYPE_INQUIRY = 11;//Performs an inquiry to the host. Typically used to obtain the balance on a food stamp card or gift card
    public static final int TRANS_TYPE_ACTIVATE = 12;//Activates a payment card. Typically used for gift card activation
    public static final int TRANS_TYPE_DEACTIVATE = 13;// Deactivates an active card account. Typically used for gift cards
    public static final int TRANS_TYPE_RELOAD = 14;// Adds value to a card account. Typically used for gift cards
    public static final int TRANS_TYPE_VOID_SALE = 15;//
    public static final int TRANS_TYPE_VOID_RETURN = 16;
    public static final int TRANS_TYPE_VOID_AUTH = 17;
    public static final int TRANS_TYPE_VOID_POSTAUTH = 18;
    public static final int TRANS_TYPE_VOID_FORCEAUTH = 19;
    public static final int TRANS_TYPE_VOID_WITHDRAWAL = 20;
    public static final int TRANS_TYPE_INIT = 31;

    public static final int EDC_TYPE_ALL = 0;
    public static final int EDC_TYPE_CREDIT = 1;
    public static final int EDC_TYPE_DEBIT = 2;
    public static final int EDC_TYPE_CHECK = 3;
    public static final int EDC_TYPE_EBT = 4;
    public static final int EDC_TYPE_GIFT = 5;
    public static final int EDC_TYPE_LOYALTY = 6;
    public static final int EDC_TYPE_CASH = 7;

    public static final int LOCALTOTALREPORT = 1;
    public static final int LOCALDETAILREPORT = 2;
    public static final int LOCALFAILEDREPORT = 3;

    public static final int TRANSACTION_TYPE_BATCHCLOSE = 1;
    public static final int FORCEBATCHCLOSE = 2;
    public static final int BATCHCLEAR = 3;

    public static final int TRANSACTION_ID_ALL = 0;
    public static final int TRANSACTION_ID_CREDIT_SALE = 1;
    public static final int TRANSACTION_ID_DEBIT_SALE = 2;
    public static final int TRANSACTION_ID_CHECK = 3;
    public static final int TRANSACTION_ID_EBT_FOODSTAMP_SALE = 4;
    public static final int TRANSACTION_ID_EBT_CASH_SALE = 5;
    public static final int TRANSACTION_ID_GIFT = 6;

    public static final int MANAGE_TRANSACTION_TYPE_INIT = 1;
    public static final int MANAGE_SHOWMESSAGE = 6;
    public static final int MANAGE_CLEARMESSAGE = 7;

    public static final String TCP_INT = "TCP";
    public static final String COM_INT = "UART";

    public static final int CARD_TYPE_VISA = 1;
    public static final int CARD_TYPE_MASTERCARD = 2;
    public static final int CARD_TYPE_AMEX = 3;
    public static final int CARD_TYPE_DISCOVER = 4;
    public static final int CARD_TYPE_DINERCLUB = 5;
    public static final int CARD_TYPE_ENROUTE = 6;
    public static final int CARD_TYPE_JCB = 7;
    public static final int CARD_TYPE_REVOLUTIONCARD = 8;
    public static final int CARD_TYPE_OTHER = 9;

    public static final String RESULT_CODE_SUCCESS = "000000";
    public static final String RESULT_CODE_DECLINE = "000100";
    public static final String RESULT_CODE_TIMEOUT = "100001";
    public static final String RESULT_CODE_ABORTED = "100002";
    public static final String RESULT_CODE_ERROR = "100003";
    public static final String RESULT_CODE_CANNOT_TIP = "100020";

    public static final String ECRREFNUM_DEFAULT = "1";

    private static final int Half_Min = 30000;
    public static final int CONNECTION_TIMEOUT = 4 * Half_Min;

    protected static final String ARG_DATA_PAX = "ARG_DATA_PAX";

    protected static final String RESULT_DATA = "RESULT_DATA";
    protected static final String RESULT_ERROR = "RESULT_ERROR";
    protected static final String RESULT_ERROR_CODE = "RESULT_ERROR_CODE";

    protected PaxModel paxTerminal;

    protected String getTimeStamp() {
        java.util.Date date = new java.util.Date();
        return new SimpleDateFormat("yyyymmddhhmmss").format(date);
    }

    protected void preFillRequest(PaymentRequest request, PaxTransaction transaction) {

        switch (transaction.getType()) {
            case PAX:
            case CREDIT:
            case CREDIT_WITH_TOKEN:
            case PRE_AUTHORIZATION:
                request.TenderType = TRANSACTION_ID_CREDIT_SALE;
                request.TransType = TRANS_TYPE_INQUIRY;
                break;
            case SALEDEBIT:
            case PAX_DEBIT:
                request.TenderType = TRANSACTION_ID_DEBIT_SALE;
                request.TransType = TRANS_TYPE_INQUIRY;
                break;
            case PAX_EBT_FOODSTAMP:
                request.TenderType = TRANSACTION_ID_EBT_FOODSTAMP_SALE;
                request.TransType = TRANS_TYPE_INQUIRY;
                break;

            case PAX_EBT_CASH:
                request.TenderType = TRANSACTION_ID_EBT_CASH_SALE;
                request.TransType = TRANS_TYPE_INQUIRY;
                break;
            case PAX_GIFT_CARD:
                request.TenderType = TRANSACTION_ID_GIFT;
                request.TransType = TRANS_TYPE_SALE;
                break;
            default:
                request.TenderType = 0;

        }
//        if (transaction.getType() == TransactionType.PRE_AUTHORIZATION) {
//            request.TransType = TRANS_TYPE_AUTH;
//        } else {
        request.TransType = TRANS_TYPE_SALE;
//        }
//        request.CashBackAmt = transaction.getCashBack().toPlainString();
//        request.ClerkID = transaction.getOperatorId();
//        request.AuthCode = transaction.getAuthorizationNumber();

    }
    public final static String FILENAME = "setting.ini";

    protected PosLink createPosLink() {
        paxTerminal = getPaxModel();
        PosLink posLink = new PosLink();
        String path = TcrApplication.get().getApplicationContext().getFilesDir().getAbsolutePath()+ "/" + FILENAME;
        CommSetting settings =getCommSettingFromFile(path);
        if (Validator.isIp(paxTerminal.ip)) {
            settings.setType(TCP_INT);
            settings.setDestIP(paxTerminal.ip);
            settings.setDestPort(String.valueOf(paxTerminal.port));
        } else {
            settings.setType(COM_INT);
            settings.setSerialPort(paxTerminal.ip);
        }
        settings.setTimeOut(String.valueOf(CONNECTION_TIMEOUT));


        posLink.appDataFolder = path;
        posLink.SetCommSetting(settings);
        saveCommSettingToFile(path, settings);
        return posLink;

    }

    private final static String Deft = "";

    public CommSetting getCommSettingFromFile(final String fileName)
    {
        IniFile ini;
        ini = new IniFile(fileName);
        ini.setSection(SectionComm);

        CommSetting commsetting = new CommSetting();
        commsetting.setTimeOut(ini.read(TagTimeout, Deft));
        commsetting.setType(ini.read(TagComm, Deft));
        commsetting.setSerialPort(ini.read(TagPortnum, Deft));
        commsetting.setBaudRate(ini.read(TagBaudrate, Deft));
        commsetting.setDestIP(ini.read(TagIp, Deft));
        commsetting.setDestPort(ini.read(TagPort, Deft));
        commsetting.setMacAddr(ini.read(TagMacAddr, Deft));
        return commsetting;
    }
    private final static String SectionComm = "COMMUNICATE";
    private final static String TagComm = "CommType";
    private final static String TagIp = "IP";
    private final static String TagPortnum = "SERIALPORT";
    private final static String TagBaudrate = "BAUDRATE";
    private final static String TagPort = "PORT";
    private final static String TagTimeout = "TIMEOUT";
    private final static String TagMacAddr = "MACADDR";

    public  boolean saveCommSettingToFile(final String fileName, final CommSetting commsetting)
    {
        IniFile ini;
        ini = new IniFile(fileName);
        ini.setSection(SectionComm);

        boolean bDone = ini.write(TagComm, commsetting.getType());
        bDone &= ini.write(TagTimeout, commsetting.getTimeOut());
        bDone &= ini.write(TagPortnum, commsetting.getSerialPort());
        bDone &= ini.write(TagBaudrate, commsetting.getBaudRate());
        bDone &= ini.write(TagIp, commsetting.getDestIP());
        bDone &= ini.write(TagPort, commsetting.getDestPort());
        bDone &= ini.write(TagMacAddr, commsetting.getMacAddr());
        return bDone;
    }
    protected abstract PaxModel getPaxModel();

    @Override
    protected abstract TaskResult doCommand();

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return null;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return null;
    }


    public static class PaxProcessorException extends RuntimeException {

        public PaxProcessorException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }

    public class PaxProcessorResponse {

        private TransactionStatusCode code;
        private PaymentResponse inner;
        private ManageResponse manageResponse;
        private ReportResponse reportResponse;
        private BatchResponse batchResponse;

        private void setPaymentResponseCode() {
            if (inner != null) {
                if (inner.ResultCode.compareTo(RESULT_CODE_SUCCESS) == 0) {
                    code = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY;
                } else if (inner.ResultCode.compareTo(RESULT_CODE_TIMEOUT) == 0) {
                    code = TransactionStatusCode.SYSTEM_ERROR;
                } else if (inner.ResultCode.compareTo(RESULT_CODE_ERROR) == 0) {
                    code = TransactionStatusCode.SYSTEM_ERROR;
                } else {
                    code = TransactionStatusCode.SYSTEM_ERROR;
                }
            } else {
                code = TransactionStatusCode.SYSTEM_ERROR;
            }
        }

        private void setManageResponseCode() {
            if (manageResponse != null) {
                if (manageResponse.ResultCode.compareTo(RESULT_CODE_SUCCESS) == 0) {
                    code = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY;
                } else if (manageResponse.ResultCode.compareTo(RESULT_CODE_TIMEOUT) == 0) {
                    code = TransactionStatusCode.SYSTEM_ERROR;
                } else if (manageResponse.ResultCode.compareTo(RESULT_CODE_ERROR) == 0) {
                    code = TransactionStatusCode.SYSTEM_ERROR;
                } else {
                    code = TransactionStatusCode.SYSTEM_ERROR;
                }
            } else {
                code = TransactionStatusCode.SYSTEM_ERROR;
            }
        }

        private void setReportResponseCode() {
            if (reportResponse != null) {
                if (reportResponse.ResultCode.compareTo(RESULT_CODE_SUCCESS) == 0) {
                    code = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY;
                } else if (reportResponse.ResultCode.compareTo(RESULT_CODE_TIMEOUT) == 0) {
                    code = TransactionStatusCode.SYSTEM_ERROR;
                } else if (reportResponse.ResultCode.compareTo(RESULT_CODE_ERROR) == 0) {
                    code = TransactionStatusCode.SYSTEM_ERROR;
                } else {
                    code = TransactionStatusCode.SYSTEM_ERROR;
                }
            } else {
                code = TransactionStatusCode.SYSTEM_ERROR;
            }
        }

        private void setBatchResponseCode() {
            if (batchResponse != null) {
                if (batchResponse.ResultCode.compareTo(RESULT_CODE_SUCCESS) == 0) {
                    code = TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY;
                } else if (batchResponse.ResultCode.compareTo(RESULT_CODE_TIMEOUT) == 0) {
                    code = TransactionStatusCode.SYSTEM_ERROR;
                } else if (batchResponse.ResultCode.compareTo(RESULT_CODE_ERROR) == 0) {
                    code = TransactionStatusCode.SYSTEM_ERROR;
                } else {
                    code = TransactionStatusCode.SYSTEM_ERROR;
                }
            } else {
                code = TransactionStatusCode.SYSTEM_ERROR;
            }
        }

        public PaxProcessorResponse(PaymentResponse response) {
            this.inner = response;
            setPaymentResponseCode();

        }

        public PaxProcessorResponse(ManageResponse response) {
            this.manageResponse = response;
            setManageResponseCode();

        }

        public PaxProcessorResponse(ReportResponse response) {
            this.reportResponse = response;
            setReportResponseCode();

        }

        public PaxProcessorResponse(BatchResponse response) {
            this.batchResponse = response;
            setBatchResponseCode();

        }

        public TransactionStatusCode getStatusCode() {
            return code;
        }

        public final PaymentResponse getResponse() {
            return this.inner;
        }

        public final ManageResponse getManageResponse() {
            return this.manageResponse;
        }

        public final ReportResponse getReportResponse() {
            return this.reportResponse;
        }

        public final BatchResponse getBatchResponse() {
            return this.batchResponse;
        }

    }

    class IniFile {

        public static final int MAX_INI_FILE_SIZE  = 1024*16;

        private String m_fileName;
        private String m_section;


        public IniFile(final String fileName)
        {
            m_fileName=fileName;

            File fconfig = new File(m_fileName);
            if (fconfig.exists())
            {
                //System.out.println("file is exist!");
                try{
                    String command = "chmod 666 " + m_fileName;
                    Runtime runtime = Runtime.getRuntime();
                    runtime.exec(command);
                }catch(IOException e)
                {
                    System.out.println("chmod 666 failed!");
                }
            }
            else
            {
                try {
                    if (fconfig.createNewFile())
                    {
                        //System.out.println("create successful!");
                        try{
                            String command = "chmod 666 " + m_fileName;
                            Runtime runtime = Runtime.getRuntime();
                            runtime.exec(command);
                        }catch(IOException e)
                        {
                            System.out.println("chmod 666 failed!");
                        }
                    }
                }
                catch (IOException e)
                {
                    //e.printStackTrace();
                }
            }
        }

        public final String getFileName()
        {
            return m_fileName;
        }

        public final String getSection()
        {
            return m_section;
        }
        public void setSection(final String section)
        {
            m_section = section;
        }

        public boolean write(final String key, final String value)
        {
            return (write_profile_string(m_section,key,value,m_fileName)==1);
        }

        public boolean write(final String key, int value)
        {
            StringBuffer  tmp = new StringBuffer(64);
            tmp.delete(0, tmp.capacity());
            tmp.append(value);
            return write(key, tmp.toString());
        }

        public String read(final String key,final String default_value)
        {
            StringBuffer buf=new StringBuffer(4096);
            read_profile_string(m_section,key,buf,buf.capacity(),default_value,m_fileName);
            return buf.toString();
        }
        public int read(final String key, int default_value)
        {
            return read_profile_int(m_section,key,default_value,m_fileName);
        }

        private  int load_ini_file(final String file, StringBuffer buf,int file_size[])
        {

            try {
                File fconfig = new File(file);
                if (!fconfig.exists())
                {
                    //System.out.println("file is not exist!");
                    return 0;
                }
                FileReader in=new FileReader(file);
                file_size[0] =0;

                char data[]=new char[MAX_INI_FILE_SIZE];

                int num = in.read(data);
                if(num>0)
                {
                    String str=new String(data,0,num);
                    buf.delete(0, buf.capacity());
                    buf.append(str);
                    file_size[0]=num;
                }
                in.close();

            } catch (IOException e) {
                //e.printStackTrace();
                return 0;
            }

            return 1;
        }
        private int newline(char c)
        {
            return ('\n' == c ||  '\r' == c )? 1 : 0;
        }
        /*//java not supported endchar
        private static int end_of_string(char c)
        {
            return '\0'==c? 1 : 0;
        }
        */
        private int left_barce(char c)
        {
            return '[' == c? 1 : 0;
        }
        private int right_brace(char c )
        {
            return ']' == c? 1 : 0;
        }
        private int parse_file(final String section, final String key, final String buf,int sec_s[],int sec_e[],
                                      int key_s[],int key_e[], int value_s[], int value_e[])
        {
            final String p = buf;
            int i=0;

            sec_s[0]=sec_e[0] = key_e[0] = key_s[0] = value_s[0] = value_e[0] = -1;

            while(i<p.length()){
                //find the section

                if(( 0==i || newline(p.charAt(i-1))==1) && left_barce(p.charAt(i))==1)
                {
                    int section_start=i+1;

                    //find the ']'
                    do {
                        i++;
                    } while( right_brace(p.charAt(i))==0 && i<p.length());

                    //System.out.println("section_start  " + section_start);
                    //System.out.println("i-section_start  " + (i-section_start));
                    //System.out.println("write section is   " + section);
                    //System.out.println("file section is   " + p.substring(section_start,i-section_start));
                    //System.out.println("file content is   " + p);
                    if(section.equals(p.substring(section_start,i))) {
                        int newline_start=0;

                        i++;

                        //Skip over space char after ']'
                        while(p.charAt(i)==' ') {
                            i++;
                        }

                        //find the section
                        sec_s[0] = section_start;
                        sec_e[0] = i;

                        //System.out.println("sec_s[0] is " + sec_s[0]);
                        //System.out.println("sec_e[0] is " + sec_e[0]);
                        while( i<p.length()&&(newline(p.charAt(i-1))==0 || left_barce(p.charAt(i))==0) )
                        {
                            //System.out.println("j char is   " + p.charAt(j));
                            //get a new line
                            newline_start = i;

                            while( newline(p.charAt(i))==0 &&  i<p.length() ) {
                                i++;
                            }

                            //now i  is equal to end of the line
                            int j = newline_start;

                            if(';' != p.charAt(j)) //skip over comment
                            {
                                while(j < i && p.charAt(j)!='=') {
                                    //System.out.println("j char is   " + p.charAt(j));
                                    j++;
                                    //System.out.println("j+1 char is " + p.charAt(j));
                                    if('=' == p.charAt(j)) {
                                        //System.out.println("newline_start  " + newline_start);
                                        //System.out.println("j is   " + j);
                                        //System.out.println("key is   " + key);
                                        //System.out.println("file key is   " + p.substring(newline_start,j));
                                        //System.out.println("file content is   " + p);

                                        if(key.equals(p.substring(newline_start,j)))
                                        {
                                            //find the key ok
                                            //System.out.println("not find the key ");
                                            key_s[0] = newline_start;
                                            key_e[0] = j-1;

                                            value_s[0] = j+1;
                                            value_e[0] = i;
                                            //System.out.println("the key_s is  "+key_s[0]);
                                            return 1;
                                        }
                                    }
                                }
                            }

                            i++;
                        }
                    }
                }
                else
                {
                    i++;
                }
            }
            return 0;
        }

        public int read_profile_string( final String section, final String key,StringBuffer value,
                                               int size, final String default_value, final String file)
        {
            StringBuffer buf=new StringBuffer(MAX_INI_FILE_SIZE);

            int file_size[]=new int[1];
            int sec_s[]=new int[1];
            int sec_e[]=new int[1];
            int key_s[]=new int[1];
            int key_e[]=new int[1];
            int value_s[]=new int[1];
            int value_e[]=new int[1];

            file_size[0]=sec_s[0]=sec_e[0]=key_s[0]=key_e[0]=value_s[0]=value_e[0]=0;
            //check parameters


            if(load_ini_file(file,buf,file_size)==0)
            {
                if(default_value!=null)
                {
                    value.delete(0, value.length());
                    value.append(default_value);
                }
                return 0;
            }

            if(parse_file(section,key,buf.toString(),sec_s,sec_e,key_s,key_e,value_s,value_e)==0)
            {
                if(default_value!=null)
                {
                    value.delete(0, value.length());
                    value.append(default_value);
                }
                return 0; //not find the key
            }
            else
            {
                int cpcount = value_e[0] -value_s[0];

                if( size-1 < cpcount)
                {
                    cpcount =  size-1;
                }

                value.delete(0, value.length());
                value.append(buf.toString().substring(value_s[0], value_s[0]+cpcount));

                return 1;
            }
        }
        public int read_profile_int( final String section, final String key,int default_value,
                                            final String file)
        {
            StringBuffer value =new StringBuffer(32);

            if(read_profile_string(section,key,value, value.capacity(),null,file)==0)
            {
                return default_value;
            }
            else
            {
                return Integer.parseInt(value.toString());
            }
        }

        /**
         * write a profile string to a ini file
         * @param section [in] name of the section,can't be NULL and empty string
         * @param key [in] name of the key pairs to value, can't be NULL and empty string
         * @param value [in] profile string value
         * @param file [in] path of ini file
         * @return 1 : success\n 0 : failure
         */
        public int write_profile_string(final String section, final String key,
                                               final String value, final String file)
        {
            StringBuffer buf=new StringBuffer(MAX_INI_FILE_SIZE);
            StringBuffer w_buf=new StringBuffer(MAX_INI_FILE_SIZE);
            int file_size[]=new int[1];
            int sec_s[]= new int[1];
            int sec_e[]= new int[1];
            int key_s[]= new int[1];
            int key_e[]= new int[1];
            int value_s[]= new int[1];
            int value_e[]= new int[1];
            file_size[0]=sec_s[0]=sec_e[0]=key_s[0]=key_e[0]=value_s[0]=value_e[0]=0;


            //check parameters

            if(load_ini_file(file,buf,file_size)==0)
            {
                sec_s[0] = -1;
            }
            else
            {
                //System.out.println("file content is "+buf.toString());
                parse_file(section,key,buf.toString(),sec_s,sec_e,key_s,key_e,value_s,value_e);
            }
            //System.out.println("sec_s[0] is "+sec_s[0]);
            //System.out.println("key_s[0] is "+key_s[0]);
            if( -1 == sec_s[0])
            {

                if(0==file_size[0])
                {
                    //sprintf(w_buf+file_size,"[%s]\n%s=%s\n",section,key,value);
                    w_buf.insert(file_size[0], "["+section+"]"+"\n"+key+"="+value+"\n");

                }
                else
                {
                    //not find the section, then add the new section at end of the file
                    w_buf.delete(0, w_buf.capacity());
                    w_buf.append(buf.toString().substring(0,file_size[0]));
                    w_buf.insert(file_size[0], "\n"+"["+section+"]"+"\n"+key+"="+value+"\n");
                }
            }
            else if(-1 == key_s[0])
            {
                //not find the key, then add the new key=value at end of the section

                w_buf.delete(0, w_buf.capacity());
                w_buf.append(buf.toString().substring(0,sec_e[0]+1));
                w_buf.append(key+"="+value+"\n");
                w_buf.append(buf.toString().substring(sec_e[0]+1));
            }
            else
            {
                //update value with new value
                w_buf.delete(0, w_buf.capacity());

                //System.out.println("djk buf is "+buf.toString());
                //System.out.println("value_s[0] is "+value_s[0]);
                //System.out.println("djk buf key is "+buf.toString().substring(0, value_s[0]));
                //System.out.println("value_len is "+value_len);
                //System.out.println("value_e[0] is "+value_e[0]);

                w_buf.append(buf.toString().substring(0, value_s[0]));

                //System.out.println("value is "+value);
                w_buf.append(value);
                //System.out.println("file_size[0] is "+file_size[0]);
                if(value_e[0]<file_size[0])
                {
                    w_buf.append(buf.toString().substring(value_e[0]));
                }

            }

            try {
                FileWriter out = new FileWriter(file);

                //System.out.println("write file content is "+w_buf.toString());
                out.write(w_buf.toString());
                out.flush();
                out.close();
            } catch (Exception e) {
                //e.printStackTrace();
                return 0;
                //e.printStackTrace();
            }

            return 1;
        }
    }
}
