package com.kaching123.tcr.websvc;

/**
 * @author Ivan v. Rikhmayer
 *
 * This class is intended to server as holders to web realted parameters and attributes.
 */
public abstract class WebAPI {

    public final class BlackStonePrepaidAPI extends WebAPI {

        public static final String REQUEST_URL_ENVIRONMENT_PRODUCTION = "https://services.bstonecorp.com/TransactionBroker/Broker.asmx";

        public static final String NAMESPACE = "http://services.bstonecorp.com/TransactionBroker/Broker";
        public static final String URL = "https://services.bstonecorp.com/TransactionBroker/Broker.asmx";
        public static final String SOAP_ACTION = "http://services.bstonecorp.com/TransactionBroker/Broker/GetSinglePIN";
        public static final String METHOD_NAME = "GetSinglePIN";
        public static final String SOAP_ACTION_GETBILLERCATEGORIES = "http://services.bstonecorp.com/TransactionBroker/Broker/GetBillerCategories";



        public static final String METHOD_NAME_GET_SINGLE_PIN = "GetSinglePIN";
        public static final String METHOD_NAME_GETBILLERCATEGORIES = "GetBillerCategories";
        public static final String METHOD_NAME_VOID_ORDER = "VoidOrder";

        public static final String REQUEST_PARAM_TID = "TID";
        public static final String REQUEST_PARAM_MID = "MID";

        public static final String REQUEST_PARAM_PASSWORD = "Password";
        public static final String REQUEST_PARAM_CASHIER = "Cashier";
        public static final String REQUEST_PARAM_PRODUCTMAINCODE = "ProductMaincode";
        public static final String REQUEST_PARAM_PRODUCTDENOMINATION = "ProductDenomination";
        public static final String REQUEST_PARAM_ORDERID = "OrderID";
        public static final String REQUEST_PARAM_PROFILEID = "ProfileID";
        public static final String REQUEST_PARAM_TRANSACTIONMODE = "TransactionMode";
        public static final String REQUEST_PARAM_SIGNATUREVALUE = "SignatureValue";


        public static final String REQUEST_PARAM_VOIDORDERRESULT = "VoidOrderResult";

        public static final String RESULT_PARAM_ERRORCODE = "ErrorCode";
        public static final String RESULT_PARAM_ERRORMESSAGE = "ErrorMessage";
        public static final String RESULT_PARAM_TRANSACTIONID = "TransactionID";
        public static final String RESULT_PARAM_PINID = "PinID";
        public static final String RESULT_PARAM_PRODUCTMAINCODE = "ProductMainCode";
        public static final String RESULT_PARAM_PRODUCTDENOMINATION = "ProductDenomination";
        public static final String RESULT_PARAM_PINNUMBER = "PinNumber";
        public static final String RESULT_PARAM_CONTROLNUMBER = "ControlNumber";
        public static final String RESULT_PARAM_LANGUAGE = "Language";
        public static final String RESULT_PARAM_PRODUCTSBT = "ProductSBT";
        public static final String RESULT_PARAM_CONN800ENGLISH = "Conn800English";
        public static final String RESULT_PARAM_CUSTOMERSERVICEENGLISH = "CustomerServiceEnglish";
        public static final String RESULT_PARAM_ITEMFK = "ItemFK";
        public static final String RESULT_PARAM_TRANSACTIONMODE = "TransactionMode";
        public static final String RESULT_PARAM_PRODUCTDESCRIPTION = "ProductDescription";
        public static final String RESULT_PARAM_BATCH = "Batch";
        public static final String RESULT_PARAM_EXPIRATIONDATE = "ExpirationDate";
        public static final String RESULT_PARAM_PRODUCTTYPE = "ProductType";
        public static final String RESULT_PARAM_BARCODE = "Barcode";
        public static final String RESULT_PARAM_INSTRUCTIONS = "Instructions";
        public static final String RESULT_PARAM_PRINTERDISCLAIMER = "PrinterDisclaimer";
        public static final String RESULT_PARAM_TOPPEDUPNUMBER = "ToppedUpNumber";
        public static final String RESULT_PARAM_ACCOUNTNUMBER = "AccountNumber";
        public static final String RESULT_PARAM_FOREIGNAMOUNT = "ForeignAmount";
        public static final String RESULT_PARAM_FOREIGNMONEYLEFT = "ForeignMoneyLeft";
        public static final String RESULT_PARAM_REFERENCENUMBER = "ReferenceNumber";
        public static final String RESULT_PARAM_AUTHORIZATIONCODE = "AuthorizationCode";
        public static final String RESULT_PARAM_LEGALINFO = "LegalInfo";
        public static final String RESULT_PARAM_ARRAYOFACCESSPHONE = "LocalAccessPhones";


        @Override
        public String version() {
            return "2.3.3";
        }
    }

    /**
     * Blackstone - related data
     */
    public final class BlackStoneAPI extends WebAPI {

        public static final String REQUEST_PATH_API = "api";
        public static final String REQUEST_PATH_CHECKPROCESSOR = "CheckProcessor";
        public static final String REQUEST_PATH_PROCESSPREAUTH = "ProcessPreauth";
        public static final String REQUEST_PATH_CLOSEPREAUTH = "ClosePreauth";
        public static final String REQUEST_PATH_TRANSACTIONS = "Transactions";
        public static final String REQUEST_PATH_SALE = "Sale";
        public static final String REQUEST_PATH_DOREFUND = "DoRefund";
        public static final String REQUEST_PATH_DOFULLREFUND = "DoFullRefund";
        public static final String REQUEST_PATH_DOVOID = "DoVoid";
        public static final String REQUEST_PATH_DOSETTLEMENT = "DoSettlement";
        public static final String REQUEST_PATH_ADMINISTRATION = "Administration";
        public static final String REQUEST_PATH_SETAUTOMATICBATCHCLOSE = "SetAutomaticBatchClose";
        public static final String REQUEST_PATH_UPDATEAUTOMATICHOURTOCLOSEBATCH = "UpdateAutomaticHourToCloseBatch";

        public static final String REQUEST_PARAM_HOSTPASSWORD = "HostPassword";

        /**
         * Credit card track data. Not required if using the No Swipe method.
         */
        public static final String REQUEST_PARAM_TRACK2 = "track2";

        /**
         * The transaction identifier used for reference purposes only.
         */
        public static final String REQUEST_PARAM_USERTRANSACTIONNUMBER = "UserTransactionNumber";

        /**
         * CREDIT
         Credit transaction
         1
         CREDIT_WITH_TOKEN
         Credit with token transaction
         2
         Pre_Authorization
         Credit preauthorization request
         3
         Pre_Authorization_Completion
         Credit preauthorization completion
         4
         SaleDebit
         Debit Card Sale transaction
         5
         */
        public static final String REQUEST_PARAM_TRANSACTIONTYPE = "transactiontype";

        /**
         * Reversal id.
         */
        public static final String REQUEST_PARAM_REVERSALID = "reversalid";

        /**
         * Credit card verification number (security code). Not required if credit card is swiped.
         */
        public static final String REQUEST_PARAM_CVN = "CVN";

        /**
         * Application Key which uniquely identifies your application as provided by the Blackstone system.
         */
        public static final String REQUEST_PARAM_APPKEY = "AppKey";

        /**
         * Application Type as provided by the Blackstone system.
         */
        public static final String REQUEST_PARAM_APPTYPE = "AppType";

        /**
         * Merchant ID – A number used to identify the merchant.
         */
        public static final String REQUEST_PARAM_MID = "mid";

        /**
         * Cashier ID – A number used to identify the merchant’s subclient.
         */
        public static final String REQUEST_PARAM_CID = "cid";

        /**
         * User credential of the client on the host.
         */
        public static final String REQUEST_PARAM_USERNAME = "UserName";

        /**
         * Password of the client on the host.
         */
        public static final String REQUEST_PARAM_PASSWORD = "Password";

        /**
         * Credit card number
         */
        public static final String REQUEST_PARAM_CARDNUMBER = "CardNumber";

        /**
         * Preauthorization amount
         */
        public static final String REQUEST_PARAM_AMOUNT = "Amount";

        /**
         * Card number. Optional if using swipe method. Otherwise required.
         */
        public static final String REQUEST_PARAM_ACCOUNT = "Account";
        /**
         * Comments
         */
        public static final String REQUEST_PARAM_COMMENTS = "comments";

        /**
         * Card Verification
         */
        public static final String REQUEST_PARAM_CV = "cv";

        /**
         * Credit card expiration titledDate (MMYY)
         */
        public static final String REQUEST_PARAM_EXPDATE = "expDate";

        /**
         * The transaction identifier provided by the Blackstone system at the time of the original transaction.
         */
        public static final String REQUEST_PARAM_SERVICETRANSACTIONNUMBER = "ServiceTransactionNumber";

        /**
         * Card track data. Optional if using the No Swipe method.
         */
        public static final String REQUEST_PARAM_TRACKDATA = "TrackData";

        /**
         * Cardholder title as it appears on the credit card
         */
        public static final String REQUEST_PARAM_NAMEONCARD = "NameonCard";

        /**
         * Cardholder street address
         */
        public static final String REQUEST_PARAM_STREET = "Street";

        /**
         * Cardholder zip code
         */
        public static final String REQUEST_PARAM_ZIPCODE = "zipCode";

        /**
         * Must be ‘True’ to be able to close the preauthorization request.
         */
        public static final String REQUEST_PARAM_ISCLOSABLE = "IsClosable";

        /**
         * Street verification message
         */
        public static final String RESULT_PARAM_AVS = "avs";

        /**
         * CV verification message
         */
        public static final String RESULT_PARAM_CV = "cv";

        /**
         * Supporting message when the transaction fails. Provides contextual information based on the code received. (Inherited from Response.)
         */
        public static final String RESULT_PARAM_MSG = "Msg";

        /**
         * Processor response message
         */
        public static final String RESULT_PARAM_MSOFT_CODE = "msoft_code";

        /**
         * Reference to the merchant plan being used and the related rates.
         */
        public static final String RESULT_PARAM_PAYMENTPLANINFO = "PaymentPlanInfo";

        /**
         * Bank response message
         */
        public static final String RESULT_PARAM_PHARD_CODE = "phard_code";

        /**
         * The Response Code is a numeric value that represents the results of the request.
         */
        public static final String RESULT_PARAM_RESPONSECODE = "ResponseCode";

        /**
         * Transaction identifier
         */
        public static final String RESULT_PARAM_SERVICEREFERENCENUMBER = "ServiceReferenceNumber";

        /**
         * Transaction processing informational message
         */
        public static final String RESULT_PARAM_VERBIAGE = "Verbiage";

        /**
         * Bank’s authorization number. Null if transaction was not successful.
         */
        public static final String RESULT_PARAM_AUTHORIZATIONNUMBER = "AuthorizationNumber";

        public static final String LAST_FOUR = "LastFour";
        /**
         * Credit Card Type
         */
        public static final String RESULT_PARAM_CARDTYPE = "CardType";

        public static final String RESULT_PARAM_CODES = "Codes";

        /**
         *
         */
        public static final String RESULT_PARAM_PLANID = "PlanId";

        /**
         *
         */
        public static final String RESULT_PARAM_SWIPEDISCOUNT = "SwipeDiscount";

        /**
         *
         */
        public static final String RESULT_PARAM_SWIPETRANSACTIONFEE = "SwipeTransactionFee";

        /**
         *
         */
        public static final String RESULT_PARAM_NONSWIPEDISCOUNT = "NonSwipeDiscount";

        /**
         *
         */
        public static final String RESULT_PARAM_NONSWIPETRANSACTIONFEE = "NonSwipeTransactionFee";

        /**
         *
         */
        public static final String RESULT_PARAM_MONTHLYFEE = "MonthlyFee";

        /**
         *
         */
        public static final String RESULT_PARAM_DESCRIPTION = "Description";

        /**
         *
         */
        public static final String RESULT_PARAM_PLANNAME = "PlanName";

        /**
         *
         */
        public static final String RESULT_PARAM_DISPLAYNAME = "DisplayName";

        /**
         * Additional Tip Amount
         */
        public static final String REQUEST_PARAM_ADDITIONALTIPAMOUNT = "AdditionalTipAmount";

        public static final String REQUEST_PARAM_ADDITIONALTIP = "AdditionalTip";

        /**
         *
         */
        public static final String REQUEST_PARAM_TIME = "Time";

        @Override
        public String version() {
            return "2.2";
        }
    }

    /**
     * returns the version of the API declared
     */
    public abstract String version();
}