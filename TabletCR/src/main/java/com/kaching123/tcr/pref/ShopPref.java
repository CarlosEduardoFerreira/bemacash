package com.kaching123.tcr.pref;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface ShopPref {

    @DefaultBoolean(false)
    boolean ignoreDevices();

    int drawerPinHigh();

    boolean printLogo();

    boolean syncForbidden();

    boolean need2DownloadAfter1stLaunch();

    /**
     * scanner **
     */
    String scannerAddress();

    String scannerName();

    /**
     * sync **
     */
    int syncPeriod();

    /**
     * shop **
     */
    long shopId();

    String shopTaxVat();

    String shopName();

    String shopViewType();

    String shopStatus();

    String prepaidVersionId();

    String shopAddress1();

    String shopAddress2();

    String shopAddress3();

    String shopState();

    String shopPhone();

    String shopEmail();

    String shopSite();

    String shopThanksPhrase();

    String shopPaymentGateway();

    int shopHoldOnOrdersCount();

    boolean printOnholdOrders();

    int printReceiptTwice();

    boolean shopDrawerClosedForSale();

    boolean clockinRequired4Sales();

    String shopFooterMsg1();

    String shopFooterMsg2();

    boolean shopUseCreditReceipt();

    String shopDisplayWelcomeMsg();

    String shopDisplayWelcomeMsgBottom();

    String shopOwnerEmail();

    int shopCreditReceiptExpireTime();

    String shopSupportEmail();

    boolean tipsEnabled();

    boolean tipsOnFlyEnabled();

    String tipsSplitTreshold();

    String tipsWarnThreshold();

    boolean tipsEnabledWasChanged();

    boolean trainingMode();

    boolean commissionControl();

    String defaultStoreCommission();

    int offlinePeriodHours();

    long offlineStartTime();

    long updateCheckTimer();

    long maxItemsCount();

    int salesHistoryLimit();

    long lastSuccessfulSyncTime();

    boolean salesSyncGapOccurred();

    boolean invalidOrdersFound();

    boolean loadingOldOrders();

    String defaultLoyaltyPlanId();

    boolean pricePointsEnabled();

    boolean autoFillPaymentAmountEnabled();

    /**
     * user **
     */
    String currentUserGuid();

    /**
     * screen **
     */
    long prevScreenTimeout();

    /**
     * display **
     */
    String displayName();

    String displayAddress();

    String signaturePrintLimit();

    /**
     * barcode prefix **
     */
    int code10DItem();

    int code6DItem4DPrice();

    int code5DItem5DPrice();

    int code4DItem6DPrice();

    int code3DItem7DPrice();

    int code6DItem4DWeight3Dec();

    int code6DItem4DWeight();

    int code5DItem5DWeight3Dec();

    int code5DItem5DWeight();

    int code5DItem5DWeight0Dec();

    /**
     * prepaid **
     */
    String prepaidUrl();

    int prepaidMid();

    String prepaidPassword();

    String prepaidTransactionMode();

    /**
     * prepaid taxes **
     */
    String wirelessRechargeTax();

    String internationalTopupTax();

    String billPaymentTax();

    String longDistanceTax();

    String sunpassTax();

    String pinlessTax();

    /**
     * blackstone **
     */
    String blackstonePaymentUrl();

    String blackstonePaymentAccount();

    String blackstonePaymentPassword();

    int blackstonePaymentApptype();

    String blackstonePaymentAppkey();

    int blackstonePaymentMid();

    String autoSettlementTime();

    boolean zipMandatory();

    boolean cvnMandatory();

    boolean acceptCreditCards();

    boolean acceptDebitCards();

    boolean acceptEbtCards();

    boolean acceptSerializableItems();

    boolean isAutoSettlementForceEnabled();

    boolean isAutoSettlementTimeForceUpdated();

    /**
     * blackstone pax **
     */
    String paxSerial();

    boolean isBlackstonePax();

    String paxUrl();

    int paxPort();

    boolean paxTipsEnabled();

    boolean isV1CommandsSent();

    String lastUncompletedSaleOrderGuid();

    // Default Unit Label shortcut and description
    String unitLabelDefaultShortcut();

    String unitLabelDefaultDescription();


    boolean printerTwoCopiesReceipt();

    boolean NeedBillpaymentUpdated();

    boolean SunpassActivated();

    boolean BillPaymentActivated();

    String lastUserName();

    String lastUserPassword();

    boolean printDropOrPayout();

    boolean printDetailReceipt();

    int paxTimeOut();

    String usbMSRName();

    String scaleName();

    String scaleAddress();

    String updateUrl();

    String updateFilePath();

    String updateRequire();

    long updateTime();

    long lastUpdateTime();

    boolean updateApprove();

    boolean enableEreportDepartSale();

    boolean enableEreportItemSale();

    boolean DirecTvPRActivated();

    boolean IVULotoActivated();

    String ivulotoMID();

    String terminalID();

    String terminalPassword();

    boolean removeCheckAndOfflineCredit();

    long planId();

    long countryId();

    boolean customerPopupScreenEnabled();

    String customerPopupScreenMessage();

    /**
     * freemium settings
     */

    boolean modifiersAvailable();

    boolean customerEditingAvailable();

    int employeeLimit();

    boolean exportInventory();

    boolean employeeCustomPermitissions();

    boolean scalesConnection();

    boolean isInventoryLimited();

    long inventoryLimit();

    boolean stockTracking();

    boolean isZReportFiltersAllowed();

    boolean isPurchaseOrdersAviable();

    boolean isJustZReportAllowed();

    boolean isXReportAllowed();

    boolean isSalesReportAllowed();

    boolean isSalesByItemReportAllowed();

    boolean isPrepaidItemsSalesReportAllowed();

    boolean isToptenSoldRevenuesReportAllowed();

    boolean isToptenPrepaidRevenuesReportAllowed();

    boolean isToptenSoldItemsReportAllowed();

    boolean isSalesByDepartmentReportAllowed();

    boolean isSalesByTenderTypeReportAllowed();

    boolean isSalesByCustomersReportAllowed();

    boolean isSalesReturnsReportAllowed();

    boolean isReturnedItemsReportAllowed();

    boolean isRestockReportReportAllowed();

    boolean isInventoryValueReportAllowed();

    boolean isItemQuantityLogReportAllowed();

    boolean isInventoryStatusReportAllowed();

    boolean isEmployeeAttendanceReportAllowed();

    boolean isEmployeePayrollReportAllowed();

    boolean isEmployeeShiftReportAllowed();

    boolean isEmployeeCommissionsReportAllowed();

    boolean isPurchaseOrdersReportAllowed();

    boolean isItemsPurchaseHistoryReportAllowed();

    boolean isReStockReportAllowed();

    boolean isShiftGratuityReportAllowed();

    boolean isSummaryGratuityReportAllowed();

    boolean isDropsAndPayoutsReportAllowed();

    boolean isSalesSummaryReportReportAllowed();

    boolean blackStonePRepaidSolution();

}
