package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo.ViewType;
import com.kaching123.tcr.util.JdbcJSONObject;

import org.json.JSONException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by hamsterksu on 28.11.13.
 */
public class ShopInfoViewJdbcConverter {

    public static final String VIEW_NAME = "SHOP_INFO_VIEW";

    public static final String ID = "ID";
    public static final String NAME = "NAME";
    public static final String VIEW_TYPE = "VIEW_TYPE";
    public static final String TAX_VAT = "TAX_VAT";

    public static final String ADDRESS1 = "ADDRESS1";
    public static final String ADDRESS2 = "ADDRESS2";
    public static final String ADDRESS3 = "ADDRESS3";
    public static final String STATE = "STATE";

    public static final String PHONE = "PHONE";
    public static final String EMAIL = "EMAIL";
    public static final String SITE = "SITE";

    public static final String THANKS_PHRASE = "THANKS_PHRASE";
    public static final String PAYMENT_GATEWAY = "PAYMENT_GATEWAY";

    public static final String HOLD_ON_ORDERS_COUNT = "HOLD_ON_ORDERS_COUNT";
    public static final String PRINT_ONHOLD_ORDERS = "PRINT_RECEIPT_FOR_ONHOLD_ORDERS_ENABLED";
    public static final String DRAWER_CLOSED_FOR_SALE = "DRAWER_CLOSED_FOR_SALE";
    public static final String SALES_FOR_CLOCK_OUT_ENABLED = "SALES_FOR_CLOCK_OUT_ENABLED";

    public static final String FOOTER_MESSAGE1 = "FOOTER_MESSAGE1";
    public static final String FOOTER_MESSAGE2 = "FOOTER_MESSAGE2";
    public static final String USE_CREDIT_RECEIPT = "USE_CREDIT_RECEIPT";

    public static final String DISPLAY_WELCOME_MESSAGE = "DISPLAY_WELCOME_MESSAGE";
    public static final String DISPLAY_WELCOME_MESSAGE_BOTTOM = "DISPLAY_WELCOME_MESSAGE_BOTTOM";
    public static final String SIGNATURE_PRINT_LIMIT = "SIGNATURE_PRINT_LIMIT";
    public static final String OWNER_EMAIL = "OWNER_EMAIL";
    public static final String CREDIT_RECEIPT_EXPIRE_TIME = "CREDIT_RECEIPT_EXPIRE_TIME";

    public static final String PREPAID_URL = "PREPAID_URL";
    public static final String PREPAID_MID = "PREPAID_MID";
    public static final String PREPAID_PASSWORD = "PREPAID_PASSWORD";
    public static final String PREPAID_TRANSACTION_MODE = "PREPAID_TRANSACTION_MODE";
    public static final String TIPS_ENABLED = "TIPS_ENABLED";
    public static final String TIPS_SPLIT_TRESHOLD = "TIPS_SPLIT_TRESHOLD";
    public static final String TIPS_ON_FLY_ENABLED = "TIPS_ON_FLY_ENABLED";
    public static final String TIPS_WARN_THRESHOLD = "TIPS_WARN_THRESHOLD";

    public static final String ZIP_CODE_MANDATORY = "ZIP_CODE_MANDATORY";
    public static final String CVN_MANDATORY = "CVN_MANDATORY";
    public static final String ACCEPT_CREDIT_CARDS = "ACCEPT_CREDIT_CARDS";
    public static final String ACCEPT_DEBIT_CARDS = "ACCEPT_DEBIT_CARDS";
    public static final String ACCEPT_EBT_CARDS = "ACCEPT_EBT_CARDS";
    public static final String ACCEPT_SERIALIZABLE = "SERIALIZABLE_ITEMS";

    public static final String BLACKSTONE_PAYMENT_URL = "BLACKSTONE_PAYMENT_URL";
    public static final String BLACKSTONE_PAYMENT_ACCOUNT = "BLACKSTONE_PAYMENT_ACCOUNT";
    public static final String BLACKSTONE_PAYMENT_PASSWORD = "BLACKSTONE_PAYMENT_PASSWORD";
    public static final String BLACKSTONE_PAYMENT_APPTYPE = "BLACKSTONE_PAYMENT_APPTYPE";
    public static final String BLACKSTONE_PAYMENT_APPKEY = "BLACKSTONE_PAYMENT_APPKEY";
    public static final String BLACKSTONE_PAYMENT_MID = "BLACKSTONE_PAYMENT_MID";
    public static final String SUPPORT_EMAIL = "SUPPORT_EMAIL";

    public static final String AUTO_SETTLEMENT_TIME = "AUTO_SETTLEMENT_TIME";

    public static final String COMMISSION_CONTROL = "COMMISSION_CONTROL";
    public static final String DEFAULT_STORE_COMMISSION = "DEFAULT_STORE_COMMISSION";

    public static final String OFFLINE_PERIOD = "OFFLINE_PERIOD";
    public static final String PRINTER_TWO_COPIES_RECEIPT = "PRINTER_TWO_COPIES_RECEIPT";

    public static final String MAX_ITEMS_COUNT = "MAX_ITEMS_COUNT";

    public static final String SHOP_STATUS = "STATUS";


    public static ShopInfo read(ResultSet rs) throws SQLException {
        return new ShopInfo(
                rs.getLong(ID),
                rs.getString(NAME),
                _enum(ViewType.class, rs.getString(VIEW_TYPE), ViewType.RETAIL),
                rs.getBigDecimal(TAX_VAT),
                rs.getString(ADDRESS1),
                rs.getString(ADDRESS2),
                rs.getString(ADDRESS3),
                rs.getString(STATE),
                rs.getString(PHONE),
                rs.getString(EMAIL),
                rs.getString(SITE),
                rs.getString(THANKS_PHRASE),
                _enum(PaymentGateway.class, rs.getString(PAYMENT_GATEWAY), PaymentGateway.CASH),
                rs.getInt(HOLD_ON_ORDERS_COUNT),
                rs.getBoolean(PRINT_ONHOLD_ORDERS),
                rs.getBoolean(DRAWER_CLOSED_FOR_SALE),
                !rs.getBoolean(SALES_FOR_CLOCK_OUT_ENABLED),
                rs.getString(FOOTER_MESSAGE1),
                rs.getString(FOOTER_MESSAGE2),
                rs.getBoolean(USE_CREDIT_RECEIPT),
                rs.getString(DISPLAY_WELCOME_MESSAGE),
                rs.getString(DISPLAY_WELCOME_MESSAGE_BOTTOM),
                rs.getBigDecimal(SIGNATURE_PRINT_LIMIT),
                rs.getString(OWNER_EMAIL),
                rs.getInt(CREDIT_RECEIPT_EXPIRE_TIME),
                rs.getString(PREPAID_URL),
                rs.getInt(PREPAID_MID),
                rs.getString(PREPAID_PASSWORD),
                rs.getString(PREPAID_TRANSACTION_MODE),
                rs.getBoolean(TIPS_ENABLED),
                rs.getBigDecimal(TIPS_SPLIT_TRESHOLD),
                rs.getBoolean(TIPS_ON_FLY_ENABLED),
                rs.getBigDecimal(TIPS_WARN_THRESHOLD),
                rs.getBoolean(ZIP_CODE_MANDATORY),
                rs.getBoolean(CVN_MANDATORY),
                rs.getBoolean(ACCEPT_CREDIT_CARDS),
                rs.getBoolean(ACCEPT_DEBIT_CARDS),
                rs.getBoolean(ACCEPT_EBT_CARDS),
                rs.getBoolean(ACCEPT_SERIALIZABLE),
                rs.getString(BLACKSTONE_PAYMENT_URL),
                rs.getString(BLACKSTONE_PAYMENT_ACCOUNT),
                rs.getString(BLACKSTONE_PAYMENT_PASSWORD),
                rs.getInt(BLACKSTONE_PAYMENT_APPTYPE),
                rs.getString(BLACKSTONE_PAYMENT_APPKEY),
                rs.getInt(BLACKSTONE_PAYMENT_MID),
                rs.getString(SUPPORT_EMAIL),
                rs.getString(AUTO_SETTLEMENT_TIME),
                rs.getBoolean(COMMISSION_CONTROL),
                rs.getBigDecimal(DEFAULT_STORE_COMMISSION),
                rs.getInt(OFFLINE_PERIOD),
                rs.getBoolean(PRINTER_TWO_COPIES_RECEIPT),
                rs.getLong(MAX_ITEMS_COUNT)
                );
    }

    public static ShopInfo read(JdbcJSONObject rs) throws JSONException {
        return new ShopInfo(
                rs.getLong(ID),
                rs.getString(NAME),
                _enum(ViewType.class, rs.getString(VIEW_TYPE), ViewType.RETAIL),
                rs.getBigDecimal(TAX_VAT),
                rs.getString(ADDRESS1),
                rs.getString(ADDRESS2),
                rs.getString(ADDRESS3),
                rs.getString(STATE),
                rs.getString(PHONE),
                rs.getString(EMAIL),
                rs.getString(SITE),
                rs.getString(THANKS_PHRASE),
                _enum(PaymentGateway.class, rs.getString(PAYMENT_GATEWAY), PaymentGateway.CASH),
                rs.getInt(HOLD_ON_ORDERS_COUNT),
                rs.getBoolean(PRINT_ONHOLD_ORDERS),
                rs.getBoolean(DRAWER_CLOSED_FOR_SALE),
                !rs.getBoolean(SALES_FOR_CLOCK_OUT_ENABLED),
                rs.getString(FOOTER_MESSAGE1),
                rs.getString(FOOTER_MESSAGE2),
                rs.getBoolean(USE_CREDIT_RECEIPT),
                rs.getString(DISPLAY_WELCOME_MESSAGE),
                rs.getString(DISPLAY_WELCOME_MESSAGE_BOTTOM),
                rs.getBigDecimal(SIGNATURE_PRINT_LIMIT),
                rs.getString(OWNER_EMAIL),
                rs.getInt(CREDIT_RECEIPT_EXPIRE_TIME),
                rs.getString(PREPAID_URL),
                rs.getInt(PREPAID_MID),
                rs.getString(PREPAID_PASSWORD),
                rs.getString(PREPAID_TRANSACTION_MODE),
                rs.getBoolean(TIPS_ENABLED),
                rs.getBigDecimal(TIPS_SPLIT_TRESHOLD),
                rs.getBoolean(TIPS_ON_FLY_ENABLED),
                rs.getBigDecimal(TIPS_WARN_THRESHOLD),
                rs.getBoolean(ZIP_CODE_MANDATORY),
                rs.getBoolean(CVN_MANDATORY),
                rs.getBoolean(ACCEPT_CREDIT_CARDS),
                rs.getBoolean(ACCEPT_DEBIT_CARDS),
                rs.getBoolean(ACCEPT_EBT_CARDS),
                rs.getBoolean(ACCEPT_SERIALIZABLE),
                rs.getString(BLACKSTONE_PAYMENT_URL),
                rs.getString(BLACKSTONE_PAYMENT_ACCOUNT),
                rs.getString(BLACKSTONE_PAYMENT_PASSWORD),
                rs.getInt(BLACKSTONE_PAYMENT_APPTYPE),
                rs.getString(BLACKSTONE_PAYMENT_APPKEY),
                rs.getInt(BLACKSTONE_PAYMENT_MID),
                rs.getString(SUPPORT_EMAIL),
                rs.getString(AUTO_SETTLEMENT_TIME),
                rs.getBoolean(COMMISSION_CONTROL),
                rs.getBigDecimal(DEFAULT_STORE_COMMISSION),
                rs.getInt(OFFLINE_PERIOD),
                rs.getBoolean(PRINTER_TWO_COPIES_RECEIPT),
                //TODO delete
                rs.optLong(MAX_ITEMS_COUNT, Long.MAX_VALUE));
    }

    public static final class ShopInfo {

        public static enum ViewType {RETAIL, QUICK_SERVICE, WIRELESS}

        public final long id;
        public final String name;
        public final ViewType viewType;
        public final BigDecimal taxVat;

        public final String address1;
        public final String address2;
        public final String address3;
        public final String state;

        public final String phone;
        public final String email;
        public final String site;
        public final String thanksPhrase;
        public final PaymentGateway paymentGateway;
        public final int holdOnOrderCount;
        public final boolean printOnholdOrders;
        public final boolean drawerClosedForSale;
        public final boolean clockinRequired4Sales;

        public final String footerMsg1;
        public final String footerMsg2;

        public final boolean useCreditReceipt;

        public final String displayWelcomeMsg;
        public final String displayWelcomeMsgBottom;
        public final BigDecimal signaturePrintLimit;
        public final String ownerEmail;
        public final int creditReceiptExpireTime;

        public final String prepaidUrl;
        public final int prepaidMid;
        public final String prepaidPassword;
        public final String prepaidTransactionMode;
        public final boolean tipsEnabled;
        public final boolean tipsOnFlyEnabled;
        public final BigDecimal tipsSplitTreshold;
        public final BigDecimal tipsWarnThreshold;


        public boolean zipMandatory;
        public boolean cvnMandatory;
        public boolean acceptCreditCards;
        public boolean acceptDebitCards;
        public boolean acceptEbtCards;
        public boolean acceptSerializable;

        public final String blackstonePaymentUrl;
        public final String blackstonePaymentAccount;
        public final String blackstonePaymentPassword;
        public final int blackstonePaymentApptype;
        public final String blackstonePaymentAppkey;
        public final int blackstonePaymentMid;
        public final String supportEmail;
        public final String autoSettlementTime;

        public final boolean commissionControl;
        public final BigDecimal defaultStoreCommission;

        public final int offlinePeriodHours;

        public final boolean printerTwoCopiesReceipt;

        public final long maxItemsCount;

        public ShopInfo(long id, String name, ViewType viewType, BigDecimal taxVat,
                        String address1,
                        String address2,
                        String address3,
                        String state, String phone,
                        String email,
                        String site,
                        String thanksPhrase,
                        PaymentGateway paymentGateway,
                        int holdOnOrderCount,
                        boolean printOnholdOrders,
                        boolean drawerClosedForSale,
                        boolean clockinRequired4Sales,
                        String footerMsg1,
                        String footerMsg2,
                        boolean useCreditReceipt,
                        String displayWelcomeMsg,
                        String displayWelcomeMsgBottom,
                        BigDecimal signaturePrintLimit,
                        String ownerEmail,
                        int creditReceiptExpireTime,
                        String prepaidUrl,
                        int prepaidMid,
                        String prepaidPassword,
                        String prepaidTransactionMode,
                        boolean tipsEnabled,
                        BigDecimal tipsSplitTreshold,
                        boolean tipsOnFlyEnabled,
                        BigDecimal tipsWarnThreshold,
                        boolean zipMandatory,
                        boolean cvnMandatory,
                        boolean acceptCreditCards,
                        boolean acceptDebitCards,
                        boolean acceptEbtCards,
                        boolean acceptSerializable,
                        String blackstonePaymentUrl,
                        String blackstonePaymentAccount,
                        String blackstonePaymentPassword,
                        int blackstonePaymentApptype,
                        String blackstonePaymentAppkey,
                        int blackstonePaymentMid,
                        String supportEmail,
                        String autoSettlementTime,
                        boolean commissionControl,
                        BigDecimal defaultStoreCommission,
                        int offlinePeriodHours,
                        boolean printerTwoCopiesReceipt,
                        long maxItemsCount) {
            this.id = id;
            this.name = name;
            this.viewType = viewType;
            this.taxVat = taxVat;
            this.acceptSerializable = acceptSerializable;

            this.address1 = address1;
            this.address2 = address2;
            this.address3 = address3;
            this.state = state;
            this.phone = phone;
            this.email = email;
            this.site = site;
            this.thanksPhrase = thanksPhrase;
            this.paymentGateway = paymentGateway;
            this.holdOnOrderCount = holdOnOrderCount;
            this.printOnholdOrders = printOnholdOrders;
            this.drawerClosedForSale = drawerClosedForSale;
            this.clockinRequired4Sales = clockinRequired4Sales;
            this.footerMsg1 = footerMsg1;
            this.footerMsg2 = footerMsg2;
            this.useCreditReceipt = useCreditReceipt;
            this.displayWelcomeMsg = displayWelcomeMsg;
            this.displayWelcomeMsgBottom = displayWelcomeMsgBottom;
            this.signaturePrintLimit = signaturePrintLimit;
            this.ownerEmail = ownerEmail;
            this.creditReceiptExpireTime = creditReceiptExpireTime;

            this.prepaidUrl = prepaidUrl;
            this.prepaidMid = prepaidMid;
            this.prepaidPassword = prepaidPassword;
            this.prepaidTransactionMode = prepaidTransactionMode;
            this.tipsEnabled = tipsEnabled;
            this.tipsSplitTreshold = tipsSplitTreshold;
            this.tipsOnFlyEnabled = tipsOnFlyEnabled;
            this.tipsWarnThreshold = tipsWarnThreshold;

            this.zipMandatory = zipMandatory;
            this.cvnMandatory = cvnMandatory;
            this.acceptCreditCards = acceptCreditCards;
            this.acceptDebitCards = acceptDebitCards;
            this.acceptEbtCards = acceptEbtCards;

            this.blackstonePaymentUrl = blackstonePaymentUrl;
            this.blackstonePaymentAccount = blackstonePaymentAccount;
            this.blackstonePaymentPassword = blackstonePaymentPassword;
            this.blackstonePaymentApptype = blackstonePaymentApptype;
            this.blackstonePaymentAppkey = blackstonePaymentAppkey;
            this.blackstonePaymentMid = blackstonePaymentMid;
            this.supportEmail = supportEmail;
            this.autoSettlementTime = autoSettlementTime;

            this.commissionControl = commissionControl;
            this.defaultStoreCommission = defaultStoreCommission;

            this.offlinePeriodHours = offlinePeriodHours;

            this.printerTwoCopiesReceipt = printerTwoCopiesReceipt;

            this.maxItemsCount = maxItemsCount;
        }

    }
}
