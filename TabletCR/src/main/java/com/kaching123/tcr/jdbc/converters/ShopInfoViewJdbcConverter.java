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

    private static final String ID = "ID";
    private static final String NAME = "NAME";
    private static final String VIEW_TYPE = "VIEW_TYPE";
    private static final String TAX_VAT = "TAX_VAT";

    private static final String ADDRESS1 = "ADDRESS1";
    private static final String ADDRESS2 = "ADDRESS2";
    private static final String ADDRESS3 = "ADDRESS3";
    private static final String STATE = "STATE";

    private static final String PHONE = "PHONE";
    private static final String EMAIL = "EMAIL";
    private static final String SITE = "SITE";

    private static final String THANKS_PHRASE = "THANKS_PHRASE";
    private static final String PAYMENT_GATEWAY = "PAYMENT_GATEWAY";

    private static final String HOLD_ON_ORDERS_COUNT = "HOLD_ON_ORDERS_COUNT";
    private static final String PRINT_ONHOLD_ORDERS = "PRINT_RECEIPT_FOR_ONHOLD_ORDERS_ENABLED";
    private static final String DRAWER_CLOSED_FOR_SALE = "DRAWER_CLOSED_FOR_SALE";
    private static final String SALES_FOR_CLOCK_OUT_ENABLED = "SALES_FOR_CLOCK_OUT_ENABLED";

    private static final String FOOTER_MESSAGE1 = "FOOTER_MESSAGE1";
    private static final String FOOTER_MESSAGE2 = "FOOTER_MESSAGE2";
    private static final String USE_CREDIT_RECEIPT = "USE_CREDIT_RECEIPT";

    private static final String DISPLAY_WELCOME_MESSAGE = "DISPLAY_WELCOME_MESSAGE";
    private static final String DISPLAY_WELCOME_MESSAGE_BOTTOM = "DISPLAY_WELCOME_MESSAGE_BOTTOM";
    private static final String SIGNATURE_PRINT_LIMIT = "SIGNATURE_PRINT_LIMIT";
    private static final String OWNER_EMAIL = "OWNER_EMAIL";
    private static final String CREDIT_RECEIPT_EXPIRE_TIME = "CREDIT_RECEIPT_EXPIRE_TIME";

    private static final String PREPAID_URL = "PREPAID_URL";
    private static final String PREPAID_MID = "PREPAID_MID";
    private static final String PREPAID_PASSWORD = "PREPAID_PASSWORD";
    private static final String PREPAID_TRANSACTION_MODE = "PREPAID_TRANSACTION_MODE";
    private static final String TIPS_ENABLED = "TIPS_ENABLED";
    private static final String TIPS_SPLIT_TRESHOLD = "TIPS_SPLIT_TRESHOLD";
    private static final String TIPS_ON_FLY_ENABLED = "TIPS_ON_FLY_ENABLED";
    private static final String TIPS_WARN_THRESHOLD = "TIPS_WARN_THRESHOLD";

    private static final String ZIP_CODE_MANDATORY = "ZIP_CODE_MANDATORY";
    private static final String CVN_MANDATORY = "CVN_MANDATORY";
    private static final String ACCEPT_CREDIT_CARDS = "ACCEPT_CREDIT_CARDS";
    private static final String ACCEPT_DEBIT_CARDS = "ACCEPT_DEBIT_CARDS";
    private static final String ACCEPT_EBT_CARDS = "ACCEPT_EBT_CARDS";
    private static final String ACCEPT_SERIALIZABLE = "SERIALIZABLE_ITEMS";

    private static final String BLACKSTONE_PAYMENT_URL = "BLACKSTONE_PAYMENT_URL";
    private static final String BLACKSTONE_PAYMENT_ACCOUNT = "BLACKSTONE_PAYMENT_ACCOUNT";
    private static final String BLACKSTONE_PAYMENT_PASSWORD = "BLACKSTONE_PAYMENT_PASSWORD";
    private static final String BLACKSTONE_PAYMENT_APPTYPE = "BLACKSTONE_PAYMENT_APPTYPE";
    private static final String BLACKSTONE_PAYMENT_APPKEY = "BLACKSTONE_PAYMENT_APPKEY";
    private static final String BLACKSTONE_PAYMENT_MID = "BLACKSTONE_PAYMENT_MID";
    private static final String SUPPORT_EMAIL = "SUPPORT_EMAIL";

    private static final String AUTO_SETTLEMENT_TIME = "AUTO_SETTLEMENT_TIME";

    private static final String COMMISSION_CONTROL = "COMMISSION_CONTROL";
    private static final String DEFAULT_STORE_COMMISSION = "DEFAULT_STORE_COMMISSION";

    private static final String OFFLINE_PERIOD = "OFFLINE_PERIOD";

    private static final String DEF_UNIT_LBL_SH = "DEFAULT_UNIT_LBL_SHORTCUT";
    private static final String DEF_UNIT_LBL_DESR = "DEFAULT_UNIT_LBL_DESCRIPTION";


    private static final String PRINTER_TWO_COPIES_RECEIPT = "PRINTER_TWO_COPIES_RECEIPT";
    private static final String PRINTER_RECEIPT_TWICE = "PRINTER_RECEIPT_TWICE";

    private static final String PRINTER_DETAIL_RECEIPT = "PRINTER_DETAIL_RECEIPT";

    private static final String ITEMS_COUNT = "ITEMS_COUNT";

    private static final String SHOP_STATUS = "STATUS";

    private static final String UPDATE_CHECK_TIMER = "UPDATE_CHECK_TIMER";


    private static final String MAX_HISTORY_RANGE = "MAX_HISTORY_RANGE";

    private static final String PRINT_DROP_OR_PAYOUT = "PRINT_DROP_OR_PAYOUT";

    private static final String ENABLE_XREPORT_DEPART_SALE = "ENABLE_XREPORT_DEPART_SALE";
    private static final String ENABLE_XREPORT_ITEM_SALE = "ENABLE_XREPORT_ITEM_SALE";

    private static final String IVULOTO_MID = "IVULOTO_MID";

    private static final String TERMINAL_ID = "TERMINAL_ID";

    private static final String TERMINAL_PASSWORD = "TERMINAL_PASSWORD";

    private static final String REMOVE_CHECK_AND_OFFLINECREDIT = "REMOVE_CHECK_AND_OFFLINECREDIT";

    private static final String PLAN_ID = "PLAN_ID";

    private static final String COUNTRY_ID = "COUNTRY_ID";

    private static final String CUSTOMER_POPUP_SCREEN_ENABLED = "CUSTOMER_POPUP_SCREEN";
    private static final String CUSTOMER_POPUP_SCREEN_MESSAGE = "CUSTOMER_POPUP_SCREEN_MESSAGE";

    private static final String BLACKSTONE_PREPAID_SOLUTION  = "BLACKSTONE_PREPAID_SOLUTION";
    
    private static final String DEFAULT_LOYALTY_PLAN_ID = "DEFAULT_LOYALTY_PLAN";
    private static final String LOYALTY_POINTS_FOR_DOLLAR_AMOUNT = "PRICE_POINTS_ENABLED";
    private static final String AUTOFILL_PAYMENT_AMOUNT_ENABLED = "AUTOFILL_PAYMENT_AMOUNT";

    public static final String GIFT_CARD  = "GIFT_CARD";
    public static final String PRINT_RECEIPT_DEFAULT  = "PRINT_RECEIPT_DEFAULT";
    public static final String EMAIL_RECEIPT_DEFAULT  = "EMAIL_RECEIPT_DEFAULT";
    private static final String CREDIT_PAYMENT_BUTTON = "CREDIT_PAYMENT_BUTTON";
    private static final String DEBIT_CARD_PAYMENT_BUTTON = "DEBIT_CARD_PAYMENT_BUTTON";
    private static final String EBT_FOOD_STAMP_PAYMENT_BUTTON = "EBT_FOOD_STAMP_PAYMENT_BUTTON";
    private static final String EBT_CASH_PAYMENT_BUTTON = "EBT_CASH_PAYMENT_BUTTON";
    private static final String OFFLINE_CREDIT_PAYMENT_BUTTON = "OFFLINE_CREDIT_PAYMENT_BUTTON";
    private static final String CHECK_PAYMENT_BUTTON = "CHECK_PAYMENT_BUTTON";
    private static final String AUTOGENERATE_PRODUCT_CODE = "AUTOGENERATE_PRODUCT_CODE";

    private static final String DIGITAL_SIGNATURE = "DIGITAL_SIGNATURE";
    private static final String SIGNATURE_RECEIPT = "SIGNATURE_RECEIPT";

    private static final String DEFINED_ON_HOLD = "DEFINED_ON_HOLD";


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
                rs.getString(DEF_UNIT_LBL_SH),
                rs.getString(DEF_UNIT_LBL_DESR),
                rs.getBoolean(PRINTER_TWO_COPIES_RECEIPT),
                rs.getInt(PRINTER_RECEIPT_TWICE),
                rs.getBoolean(PRINTER_DETAIL_RECEIPT),
                rs.getLong(ITEMS_COUNT),
                rs.getBoolean(PRINT_DROP_OR_PAYOUT),
                rs.getLong(UPDATE_CHECK_TIMER),
                rs.getBoolean(ENABLE_XREPORT_DEPART_SALE),
                rs.getBoolean(ENABLE_XREPORT_ITEM_SALE),
                rs.getString(IVULOTO_MID),
                rs.getString(TERMINAL_ID),
                rs.getString(TERMINAL_PASSWORD),
                rs.getBoolean(REMOVE_CHECK_AND_OFFLINECREDIT),
                rs.getLong(PLAN_ID),
                rs.getLong(COUNTRY_ID),
                rs.getBoolean(CUSTOMER_POPUP_SCREEN_ENABLED),
                rs.getString(CUSTOMER_POPUP_SCREEN_MESSAGE),
                rs.getBoolean(BLACKSTONE_PREPAID_SOLUTION),
                rs.getString(DEFAULT_LOYALTY_PLAN_ID),
                rs.getBoolean(LOYALTY_POINTS_FOR_DOLLAR_AMOUNT),
                rs.getBoolean(AUTOFILL_PAYMENT_AMOUNT_ENABLED),
                rs.getBoolean(GIFT_CARD),
                rs.getBoolean(CREDIT_PAYMENT_BUTTON),
                rs.getBoolean(DEBIT_CARD_PAYMENT_BUTTON),
                rs.getBoolean(EBT_FOOD_STAMP_PAYMENT_BUTTON),
                rs.getBoolean(EBT_CASH_PAYMENT_BUTTON),
                rs.getBoolean(OFFLINE_CREDIT_PAYMENT_BUTTON),
                rs.getBoolean(CHECK_PAYMENT_BUTTON),
                rs.getBoolean(PRINT_RECEIPT_DEFAULT),
                rs.getBoolean(EMAIL_RECEIPT_DEFAULT),
                rs.getBoolean(AUTOGENERATE_PRODUCT_CODE),
                rs.getBoolean(DIGITAL_SIGNATURE),
                rs.getString(SIGNATURE_RECEIPT),
                rs.getBoolean(DEFINED_ON_HOLD)
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
                rs.getString(DEF_UNIT_LBL_SH),
                rs.getString(DEF_UNIT_LBL_DESR),
                rs.getBoolean(PRINTER_TWO_COPIES_RECEIPT),
                rs.getInt(PRINTER_RECEIPT_TWICE),
                rs.getBoolean(PRINTER_DETAIL_RECEIPT),
                rs.optLong(ITEMS_COUNT, Long.MAX_VALUE),
                rs.getBoolean(PRINT_DROP_OR_PAYOUT),
                rs.getLong(UPDATE_CHECK_TIMER),
                rs.getBoolean(ENABLE_XREPORT_DEPART_SALE),
                rs.getBoolean(ENABLE_XREPORT_ITEM_SALE),
                rs.getString(IVULOTO_MID),
                rs.getString(TERMINAL_ID),
                rs.getString(TERMINAL_PASSWORD),
                rs.getBoolean(REMOVE_CHECK_AND_OFFLINECREDIT),
                rs.getLong(PLAN_ID),
                rs.getLong(COUNTRY_ID),
                rs.getBoolean(CUSTOMER_POPUP_SCREEN_ENABLED),
                rs.getString(CUSTOMER_POPUP_SCREEN_MESSAGE),
                rs.getBoolean(BLACKSTONE_PREPAID_SOLUTION ),
                rs.getString(DEFAULT_LOYALTY_PLAN_ID),
                rs.getBoolean(LOYALTY_POINTS_FOR_DOLLAR_AMOUNT),
                rs.getBoolean(AUTOFILL_PAYMENT_AMOUNT_ENABLED),
                rs.getBoolean(GIFT_CARD),
                rs.getBoolean(CREDIT_PAYMENT_BUTTON),
                rs.getBoolean(DEBIT_CARD_PAYMENT_BUTTON),
                rs.getBoolean(EBT_FOOD_STAMP_PAYMENT_BUTTON),
                rs.getBoolean(EBT_CASH_PAYMENT_BUTTON),
                rs.getBoolean(OFFLINE_CREDIT_PAYMENT_BUTTON),
                rs.getBoolean(CHECK_PAYMENT_BUTTON),
                rs.getBoolean(PRINT_RECEIPT_DEFAULT),
                rs.getBoolean(EMAIL_RECEIPT_DEFAULT),
                rs.getBoolean(AUTOGENERATE_PRODUCT_CODE),
                rs.getBoolean(DIGITAL_SIGNATURE),
                rs.getString(SIGNATURE_RECEIPT),
                rs.getBoolean(DEFINED_ON_HOLD)
        );
    }

    public static Integer getSalesHistoryLimit(JdbcJSONObject rs) throws JSONException {
        int salesHistoryLimit = rs.getInt(MAX_HISTORY_RANGE);
        if (salesHistoryLimit == 0)
            return null;
        return salesHistoryLimit;
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
        public final String defUnitLabelShortcut;
        public final String defUnitLabelDescription;
        public final int offlinePeriodHours;
        public final boolean printerTwoCopiesReceipt;
        public final int printReceiptTwice;
        public final boolean printDetailReceipt;
        public final long inventoryLimit;
        public final long updateCheckTimer;
        public final boolean printDropOrPayout;
        public final boolean enableEreportDepartSale;
        public final boolean enableEreportItemSale;
        public final String ivulotoMid;
        public final String terminalID;
        public final String terminalPassword;
        public final boolean removeCheckAndOfflineCredit;
        public final long planId;
        public final long countryId;
        public final boolean customerPopupScreenEnabled;
        public final String customerPopupScreenMessage;
        public final boolean blackStonePRepaidSolution;
        public final String defaultLoyaltyPlanId;
        public final boolean loyaltyPointsForDollarAmount;
        public final boolean autoFillPaymentAmount;

        public final boolean giftCardSolutionEnabled;
        public final boolean creditPaymentButtonEnabled;
        public final boolean debitCardPaymentButtonEnabled;
        public final boolean ebtFoodStampPaymentEnabled;
        public final boolean ebtCashPaymentButtonEnabled;
        public final boolean offlineCreditPaymentButtonEnabled;
        public final boolean checkPaymentButtonEnabled;
        public final boolean printReceiptDefault;
        public final boolean emailReceiptDefault;
        public final boolean autogenerateProductCode;

        public final boolean digitalSignature;
        public final String signatureReceipt;

        public final boolean definedOnHold;



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
                        String defUnitLabelShortcut,
                        String defUnitLabelDescription,
                        boolean printerTwoCopiesReceipt,
                        int printReceiptTwice,
                        boolean printDetailReceipt,
                        long inventoryLimit,
                        boolean printDropOrPayout,
                        long updateCheckTimer,
                        boolean enableEreportDepartSale,
                        boolean enableEreportItemSale,
                        String ivulotoMid,
                        String terminalID,
                        String terminalPassword,
                        boolean removeCheckAndOfflineCredit,
                        long planId,
                        long countryId,
                        boolean customerPopupScreenEnabled,
                        String customerPopupScreenMessage,
                        boolean blackStonePRepaidSolution,
                        String defaultLoyaltyPlanId,
                        boolean loyaltyPointsForDollarAmount,
                        boolean autoFillPaymentAmount,
                        boolean giftCardSolutionEnabled,
                        boolean creditPaymentButtonEnabled,
                        boolean debitCardPaymentButtonEnabled,
                        boolean ebtFoodStampPaymentEnabled,
                        boolean ebtCashPaymentButtonEnabled,
                        boolean offlineCreditPaymentButtonEnabled,
                        boolean checkPaymentButtonEnabled,
                        boolean printReceiptDefault,
                        boolean emailReceiptDefault,
                        boolean autogenerateProductCode,
                        boolean digitalSignature,
                        String signatureReceipt,
                        boolean definedOnHold) {
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

            this.defUnitLabelShortcut = defUnitLabelShortcut;
            this.defUnitLabelDescription = defUnitLabelDescription;


            this.printerTwoCopiesReceipt = printerTwoCopiesReceipt;

            this.printReceiptTwice = printReceiptTwice;

            this.printDetailReceipt = printDetailReceipt;

            this.inventoryLimit = inventoryLimit;

            this.printDropOrPayout = printDropOrPayout;
            this.updateCheckTimer = updateCheckTimer;

            this.enableEreportDepartSale = enableEreportDepartSale;
            this.enableEreportItemSale = enableEreportItemSale;

            this.ivulotoMid = ivulotoMid;

            this.terminalID = terminalID;

            this.terminalPassword = terminalPassword;

            this.removeCheckAndOfflineCredit = removeCheckAndOfflineCredit;

            this.planId = planId;
            this.countryId = countryId;

            this.customerPopupScreenEnabled = customerPopupScreenEnabled;
            this.customerPopupScreenMessage = customerPopupScreenMessage;
            this.blackStonePRepaidSolution = blackStonePRepaidSolution;
            this.defaultLoyaltyPlanId = defaultLoyaltyPlanId;
            this.loyaltyPointsForDollarAmount = loyaltyPointsForDollarAmount;
            this.autoFillPaymentAmount = autoFillPaymentAmount;
            this.giftCardSolutionEnabled = giftCardSolutionEnabled;
            this.creditPaymentButtonEnabled = creditPaymentButtonEnabled;
            this.debitCardPaymentButtonEnabled = debitCardPaymentButtonEnabled;
            this.ebtFoodStampPaymentEnabled = ebtFoodStampPaymentEnabled;
            this.ebtCashPaymentButtonEnabled = ebtCashPaymentButtonEnabled;
            this.offlineCreditPaymentButtonEnabled = offlineCreditPaymentButtonEnabled;
            this.checkPaymentButtonEnabled = checkPaymentButtonEnabled;
            this.printReceiptDefault = printReceiptDefault;
            this.emailReceiptDefault = emailReceiptDefault;
            this.autogenerateProductCode = autogenerateProductCode;

            this.digitalSignature = digitalSignature;
            this.signatureReceipt = signatureReceipt;

            this.definedOnHold = definedOnHold;
        }

    }
}
