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
                break;
            case SALEDEBIT:
            case PAX_DEBIT:
                request.TenderType = TRANSACTION_ID_DEBIT_SALE;
                break;
            case PAX_EBT_FOODSTAMP:
                request.TenderType = TRANSACTION_ID_EBT_FOODSTAMP_SALE;
                break;

            case PAX_EBT_CASH:
                request.TenderType = TRANSACTION_ID_EBT_CASH_SALE;
                break;
            case PAX_GIFT_CARD:
                request.TransType = TRANSACTION_ID_GIFT;
                break;
            default:
                request.TenderType = TRANS_TYPE_SALE;

        }
//        if (transaction.getType() == TransactionType.PRE_AUTHORIZATION) {
//            request.TransType = TRANS_TYPE_AUTH;
//        } else {
//        request.TransType = TRANS_TYPE_SALE;
//        }
//        request.CashBackAmt = transaction.getCashBack().toPlainString();
//        request.ClerkID = transaction.getOperatorId();
//        request.AuthCode = transaction.getAuthorizationNumber();

    }

    protected PosLink createPosLink() {
        paxTerminal = getPaxModel();
        PosLink posLink = new PosLink();
        String path = TcrApplication.get().getApplicationContext().getFilesDir().getAbsolutePath();
        CommSetting settings = new CommSetting(path);
        if (Validator.isIp(paxTerminal.ip)) {
            settings.CommType = TCP_INT;
            settings.DestIP = paxTerminal.ip;
            settings.DestPort = String.valueOf(paxTerminal.port);
        } else {
            settings.CommType = COM_INT;
            settings.SerialPort = paxTerminal.ip;
        }
        settings.TimeOut = String.valueOf(CONNECTION_TIMEOUT);


        posLink.appDataFolder = path;
        settings.SaveCommSettingFile();
        return posLink;

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


}
