package com.kaching123.tcr;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings.Secure;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Base64;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.kaching123.display.SerialPortScale;
import com.kaching123.display.SerialPortScanner;
import com.kaching123.tcr.commands.payment.PaymentGateway;
import com.kaching123.tcr.commands.rest.RestCommand;
import com.kaching123.tcr.commands.rest.RestCommand.PlainTextResponse;
import com.kaching123.tcr.commands.rest.sync.AuthResponse;
import com.kaching123.tcr.commands.rest.sync.DBVersionResponse;
import com.kaching123.tcr.commands.rest.sync.GetArrayResponse;
import com.kaching123.tcr.commands.rest.sync.GetCurrentTimestampResponse;
import com.kaching123.tcr.commands.rest.sync.GetPagedArrayResponse;
import com.kaching123.tcr.commands.rest.sync.GetPrepaidOrderIdResponse;
import com.kaching123.tcr.commands.rest.sync.GetResponse;
import com.kaching123.tcr.commands.rest.sync.v1.UploadResponseV1;
import com.kaching123.tcr.jdbc.converters.BarcodePrefixJdbcConverter.BarcodePrefixes;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo.ViewType;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeePermissionsModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PlanOptionsResponse;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.blackstone.prepaid.Broker;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.pref.ShopPref_;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.store.SyncOpenHelper;
import com.kaching123.tcr.util.JdbcJSONArray;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.kaching123.tcr.util.OrgJsonConverter;
import com.squareup.okhttp.OkHttpClient;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EApplication;
import org.apache.commons.codec.Charsets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

@EApplication
public class TcrApplication extends MultiDexApplication {

    public static final int BARCODE_MIN_LEN = 1;
    public static final int BARCODE_MAX_LEN = 13;
    public static final int EAN_UPC_CODE_MAX_LEN = 13;
    public static final int PRODUCT_CODE_MAX_LEN = 20;

    public static final long MONEYBACK_MAX_TIMESPAN_MSEC = TimeUnit.DAYS.toMillis(30);
    public static final long DEFAULT_OFFLINE_PERIOD = TimeUnit.HOURS.toMillis(192);
    public static final long OFFLINE_PERIOD_NEAR = TimeUnit.HOURS.toMillis(24);

    private static final long FREEMIUM_PLAN_ID = 14;

    private static TcrApplication self;

    private PrepaidUser prepaidUser = new PrepaidUser();
    private User blackstoneUser = new User();

    protected ShopPref_ shopPref;

    private long registerId;
    private String registerSerial;
    private EmployeeModel operator;
    private Set<Permission> operatorPermissions;

    private EmployeeModel prevOperator;
    private Set<Permission> prevOperatorPermissions;

    private ShopInfo shopInfo;
    private String shiftGuid;
    private boolean isShiftOpened;
    private boolean isOperatorClockedIn;
    private String currentOrderGuid;
    private HashSet<String> salesmanGuids = new HashSet<String>();
    private BarcodePrefixes barcodePrefixes;
    private HashMap<Broker, BigDecimal> prepaidTaxes;

    private RestAdapter restAdapter;
    private RestAdapter restAdapterJsonOrg;

    private SerialPortScanner serialPortScanner;
    private SerialPortScale serialPortScale;

    public String emailApiKey = "EvG5Cb8acZC4Dzm6b4a5GRdDBPk362";

    private static final ReentrantLock trainingModeLock = new ReentrantLock();
    private static final ReentrantLock offlineModeLock = new ReentrantLock(true);
    private static final ReentrantLock salesHistoryLock = new ReentrantLock();

    private Boolean isNetworkConnected;

    private SyncOpenHelper syncOpenHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

        self = this;
        Resources res = getResources();

        String apiCred = res.getString(R.string.api_basic_cred);

        restAdapter = createRestAdapter(res, apiCred, new ExtConverter());
        restAdapterJsonOrg = createRestAdapter(res, apiCred, new OrgJsonConverter());

        syncOpenHelper = new SyncOpenHelper(this);

        startService(new Intent(TcrApplication.this, OfflineCommandsService.class));

        lazyInstantiateShopPref();
        initPref();
    }

    public synchronized SyncOpenHelper getSyncOpenHelper() {
        return syncOpenHelper;
    }

    private RestAdapter createRestAdapter(Resources res, String apiCred, Converter converter) {
        RestAdapter.Builder adapterBuilder = new RestAdapter.Builder().setEndpoint(res.getString(R.string.api_server_url));
        if (!TextUtils.isEmpty(apiCred)) {
            adapterBuilder.setRequestInterceptor(new BaseAuthorizationInterceptor(apiCred));
        }

        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(1, TimeUnit.MINUTES);

        return adapterBuilder.setConverter(converter)
                .setClient(new OkClient(client))
                .setLogLevel(BuildConfig.DEBUG ? LogLevel.FULL : LogLevel.NONE)
                .setLog(new AndroidLog("TCR"))
                .build();
    }

    @Background
    public void initPref() {
        Logger.d("PrintReceiptTwice = " + shopPref.printReceiptTwice().get());
        synchronized (this) {
            shopInfo = new ShopInfo(
                    shopPref.shopId().get(),
                    shopPref.shopName().get(),
                    _enum(ViewType.class, shopPref.shopViewType().get(), ViewType.RETAIL),
                    _decimal(shopPref.shopTaxVat().getOr(null)),
                    shopPref.shopAddress1().getOr(null),
                    shopPref.shopAddress2().getOr(null),
                    shopPref.shopAddress3().getOr(null),
                    shopPref.shopState().getOr(null),
                    shopPref.shopPhone().getOr(null),
                    shopPref.shopEmail().getOr(null),
                    shopPref.shopSite().getOr(null),
                    shopPref.shopThanksPhrase().getOr(null),
                    _enum(PaymentGateway.class, shopPref.shopPaymentGateway().getOr(PaymentGateway.CASH.name()), PaymentGateway.CASH),
                    shopPref.shopHoldOnOrdersCount().get(),
                    shopPref.printOnholdOrders().get(),
                    shopPref.shopDrawerClosedForSale().get(),
                    shopPref.clockinRequired4Sales().get(),
                    shopPref.shopFooterMsg1().getOr(null),
                    shopPref.shopFooterMsg2().getOr(null),
                    shopPref.shopUseCreditReceipt().getOr(false),
                    shopPref.shopDisplayWelcomeMsg().getOr(null),
                    shopPref.shopDisplayWelcomeMsgBottom().getOr(null),
                    _decimal(shopPref.signaturePrintLimit().getOr(null)),
                    shopPref.shopOwnerEmail().getOr(null),
                    shopPref.shopCreditReceiptExpireTime().getOr(0),
                    shopPref.prepaidUrl().getOr(null),
                    shopPref.prepaidMid().getOr(0),
                    shopPref.prepaidPassword().getOr(null),
                    shopPref.prepaidTransactionMode().getOr(null),
                    shopPref.tipsEnabled().get(),
                    _decimal(shopPref.tipsSplitTreshold().getOr(null)),
                    shopPref.tipsEnabled().get(),
                    _decimal(shopPref.tipsWarnThreshold().getOr(null)),
                    shopPref.zipMandatory().getOr(false),
                    shopPref.cvnMandatory().getOr(true),

                    shopPref.acceptCreditCards().getOr(true),
                    shopPref.acceptDebitCards().getOr(true),
                    shopPref.acceptEbtCards().getOr(true),
                    shopPref.acceptSerializableItems().getOr(true),

                    shopPref.blackstonePaymentUrl().getOr(null),
                    shopPref.blackstonePaymentAccount().getOr(null),
                    shopPref.blackstonePaymentPassword().getOr(null),
                    shopPref.blackstonePaymentApptype().getOr(0),
                    shopPref.blackstonePaymentAppkey().getOr(null),
                    shopPref.blackstonePaymentMid().getOr(0),

                    shopPref.shopSupportEmail().getOr(null),

                    shopPref.autoSettlementTime().getOr(null),
                    shopPref.commissionControl().getOr(false),
                    _decimal(shopPref.defaultStoreCommission().getOr(null)),
                    shopPref.offlinePeriodHours().getOr(0),

                    shopPref.unitLabelDefaultShortcut().getOr(null),
                    shopPref.unitLabelDefaultDescription().getOr(null),

                    shopPref.printerTwoCopiesReceipt().getOr(false),
                    shopPref.printReceiptTwice().getOr(1),
                    shopPref.printDetailReceipt().getOr(false),
                    shopPref.maxItemsCount().getOr(0),
                    shopPref.printDropOrPayout().getOr(true),
                    shopPref.updateCheckTimer().getOr(0),
                    shopPref.enableEreportDepartSale().getOr(false),
                    shopPref.enableEreportItemSale().getOr(false),
                    shopPref.ivulotoMID().get(),
                    shopPref.terminalID().get(),
                    shopPref.terminalPassword().get(),
                    shopPref.removeCheckAndOfflineCredit().get(),
                    shopPref.planId().get());
        }
        barcodePrefixes = new BarcodePrefixes(
                shopPref.code10DItem().get(),
                shopPref.code6DItem4DPrice().get(),
                shopPref.code5DItem5DPrice().get(),
                shopPref.code4DItem6DPrice().get(),
                shopPref.code3DItem7DPrice().get(),
                shopPref.code6DItem4DWeight3Dec().get(),
                shopPref.code6DItem4DWeight().get(),
                shopPref.code5DItem5DWeight3Dec().get(),
                shopPref.code5DItem5DWeight().get(),
                shopPref.code5DItem5DWeight0Dec().get()
        );

        prepaidTaxes = new HashMap<Broker, BigDecimal>();
        prepaidTaxes.put(Broker.WIRELESS_RECHARGE, _decimal(shopPref.wirelessRechargeTax().getOr("0")));
        prepaidTaxes.put(Broker.INTERNATIONAL_TOPUP, _decimal(shopPref.internationalTopupTax().getOr("0")));
        prepaidTaxes.put(Broker.BILL_PAYMENT, _decimal(shopPref.billPaymentTax().getOr("0")));
        prepaidTaxes.put(Broker.LONG_DISTANCE, _decimal(shopPref.longDistanceTax().getOr("0")));
        prepaidTaxes.put(Broker.SUNPASS, _decimal(shopPref.sunpassTax().getOr("0")));
        prepaidTaxes.put(Broker.PINLESS, _decimal(shopPref.pinlessTax().getOr("0")));

        registerSerial = !Build.UNKNOWN.equals(Build.SERIAL) ? Build.SERIAL : Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        registerSerial += cut4Symbols(Secure.getString(getContentResolver(), Secure.ANDROID_ID));
        registerSerial = formatByBlocksString(registerSerial);

        setUsers();

    }

    private synchronized void lazyInstantiateShopPref() {
        if (shopPref == null)
            shopPref = new ShopPref_(TcrApplication.this);
    }

    public void clearDbRelatedPreferences() {
        getShopPref().prepaidVersionId().put(null);
        setPaxTipsEnabled(false);
        setLastSuccessfulSyncTime(null);
        setSalesSyncGapOccurred(false);
        setInvalidOrdersFound(false);
    }

    private String cut4Symbols(String s) {
        if (s == null)
            return "";
        if (s.length() < 4) {
            return s;
        }
        return s.substring(0, 3);
    }

    private static final int BLOCK_SIZE = 6;

    private String formatByBlocksString(String str) {
        int blocks = str.length() / BLOCK_SIZE;
        StringBuilder builder = new StringBuilder(str);
        for (int i = 0; i < blocks - 1; i++) {
            builder.insert((i + 1) * BLOCK_SIZE + i, '-');
        }
        return builder.toString();
    }

    public Boolean isNetworkConnected() {
        return isNetworkConnected;
    }

    public void setNetworkConnected(boolean isNetworkConnected) {
        this.isNetworkConnected = isNetworkConnected;
    }

    private void setUsers() {
        setPrepaidUser();
        setBlackstoneUser();
    }

    private synchronized void setBlackstoneUser() {
        blackstoneUser = new User(
                shopInfo.blackstonePaymentAccount,
                shopInfo.blackstonePaymentPassword,
                shopInfo.blackstonePaymentMid,
                blackstoneUser.getCid(),
                shopInfo.blackstonePaymentAppkey,
                shopInfo.blackstonePaymentApptype);
    }

    private synchronized void setPrepaidUser() {
        prepaidUser = new PrepaidUser(shopInfo.prepaidPassword, shopInfo.prepaidMid, prepaidUser.getTid());
    }


    public void setCurrentOrderGuid(String currentOrderGuid) {
        this.currentOrderGuid = currentOrderGuid;
    }

    public String getCurrentOrderGuid() {
        return currentOrderGuid;
    }

    public void setSalesmanGuids(HashSet<String> salesmanGuids) {
        this.salesmanGuids = salesmanGuids;
    }

    public HashSet<String> getSalesmanGuids() {
        return salesmanGuids;
    }

    public synchronized boolean isUserLogin() {
        return operator != null;
    }

    public synchronized ViewType getStartView() {
        return shopInfo == null || shopInfo.viewType == null ? ViewType.RETAIL : shopInfo.viewType;
    }

    public ShopPref_ getShopPref() {
        lazyInstantiateShopPref();
        return shopPref;
    }

    public synchronized User getBlackStoneUser() {
        return blackstoneUser;
    }

    public synchronized PrepaidUser getPrepaidUser() {
        return prepaidUser;
    }

    public synchronized long getShopId() {
        if (operator == null || operator.shopId == 0L)
            Logger.e("[CREDS] TcrApplication.getShopId(): NO SHOP ID! operator: " + operator, new RuntimeException());
        return operator == null ? 0 : operator.shopId;
    }

    public synchronized String getOperatorGuid() {
        if (operator == null || TextUtils.isEmpty(operator.guid))
            Logger.e("[CREDS] TcrApplication.getOperatorGuid(): NO OPERATOR ID! operator: " + operator, new RuntimeException());
        return operator == null ? null : operator.guid;
    }

    public synchronized String getOperatorLogin() {
        if (operator == null || TextUtils.isEmpty(operator.login))
            Logger.e("[CREDS] TcrApplication.getOperatorLogin(): NO OPERATOR LOGIN! operator: " + operator, new RuntimeException());
        return operator == null ? null : operator.login;
    }

    public synchronized String getOperatorFullName() {
        return operator == null ? null : operator.fullName();
    }

    public synchronized void setOperatorWithPermissions(EmployeePermissionsModel operator, boolean clearPrevOperator) {
        if (!clearPrevOperator && this.operator != null) {
            prevOperator = this.operator;
            prevOperatorPermissions = this.operatorPermissions;
        } else if (clearPrevOperator) {
            prevOperator = null;
            prevOperatorPermissions = null;
        }
        this.operator = operator == null ? null : operator.employee;
        this.operatorPermissions = operator == null ? null : operator.permissions;
        shopPref.currentUserGuid().put(operator == null ? null : this.operator.guid);
    }

    public synchronized void restorePrevOperator() {
        this.operator = prevOperator;
        this.operatorPermissions = prevOperatorPermissions;
        prevOperator = null;
        prevOperatorPermissions = null;
    }

    public synchronized boolean hasPrevOperator() {
        return prevOperator != null;
    }

    public synchronized int getSuspendedMaxCount() {
        return shopInfo.holdOnOrderCount;
    }

    public synchronized BigDecimal getTaxVat() {
        return shopInfo == null || shopInfo.taxVat == null ? BigDecimal.ZERO : shopInfo.taxVat;
    }

    public String getShiftGuid() {
        if (TextUtils.isEmpty(shiftGuid))
            Logger.e("[CREDS] TcrApplication.getShiftGuid(): NO SHIFT ID! shiftGuid: " + shiftGuid, new RuntimeException());
        return shiftGuid;
    }

    public void setShiftGuid(String shiftGuid) {
        this.shiftGuid = shiftGuid;
    }

    public void setShiftOpened(boolean isShiftOpened) {
        this.isShiftOpened = isShiftOpened;
    }

    public boolean isShiftOpened() {
        return isShiftOpened;
    }

    public void setOperatorClockedIn(boolean isOperatorClockedIn) {
        this.isOperatorClockedIn = isOperatorClockedIn;
    }

    public boolean isOperatorClockedIn() {
        return isOperatorClockedIn;
    }

    public void setRegisterId(long registerId, int prepaidTid, int blackstonePaymentCid) {
        this.registerId = registerId;
        setUsers(prepaidTid, blackstonePaymentCid);
    }

    public synchronized void setUsers(int prepaidTid, int blackstonePaymentCid) {
        prepaidUser = new PrepaidUser(prepaidUser.getPassword(), prepaidUser.getMid(), prepaidTid);
        blackstoneUser = new User(blackstoneUser.getUsername(), blackstoneUser.getPassword(), blackstoneUser.getMid(), blackstonePaymentCid, blackstoneUser.getAppkey(), blackstoneUser.getApptype());
    }

    public long getRegisterId() {
        if (registerId == 0L)
            Logger.e("[CREDS] TcrApplication.getRegisterId(): NO REGISTER ID! registerId: " + registerId, new RuntimeException());
        return registerId;
    }

    public String getRegisterSerial() {
        if (TextUtils.isEmpty(registerSerial))
            Logger.e("[CREDS] TcrApplication.getRegisterSerial(): NO REGISTER SERIAL! registerSerial: " + registerSerial, new RuntimeException());
        return registerSerial;
    }

    public void lockOnSalesHistory() {
        salesHistoryLock.lock();
    }

    public void unlockOnSalesHistory() {
        salesHistoryLock.unlock();
    }

    public void setSalesHistoryLimit(Integer salesHistoryLimit) {
        shopPref.salesHistoryLimit().put(salesHistoryLimit == null ? 0 : salesHistoryLimit);
    }

    public Integer getSalesHistoryLimit() {
        int salesHistoryLimit = shopPref.salesHistoryLimit().getOr(0);
        if (salesHistoryLimit == 0) {
            Logger.w("TcrApplication: sales history limit is not set yet");
        }
        return salesHistoryLimit == 0 ? null : salesHistoryLimit;
    }

    public Long getMinSalesHistoryLimitDate() {
        Integer salesHistoryLimit = getSalesHistoryLimit();
        if (salesHistoryLimit == null)
            return null;
        return System.currentTimeMillis() - TimeUnit.DAYS.toMillis(salesHistoryLimit);
    }

    public Date getMinSalesHistoryLimitDateDayRounded(Calendar localTimeCalendar) {
        Long minDateTime = getMinSalesHistoryLimitDate();
        if (minDateTime == null)
            return null;

        localTimeCalendar.setTimeInMillis(minDateTime);
        localTimeCalendar.set(Calendar.HOUR_OF_DAY, 00);
        localTimeCalendar.set(Calendar.MINUTE, 00);
        localTimeCalendar.set(Calendar.SECOND, 00);
        localTimeCalendar.set(Calendar.MILLISECOND, 0);
        localTimeCalendar.add(Calendar.DATE, 1);
        return localTimeCalendar.getTime();
    }

    public void saveShopInfo(ShopInfo info) {
        saveShopInfoInner(info);
        shopPref.edit()
                .shopTaxVat().put(_decimal(info.taxVat))
                .shopViewType().put(info.viewType == null ? ViewType.RETAIL.name() : info.viewType.name())
                .shopName().put(info.name)
                .shopAddress1().put(info.address1)
                .shopAddress2().put(info.address2)
                .shopAddress3().put(info.address3)
                .shopState().put(info.state)
                .shopPhone().put(info.phone)
                .shopEmail().put(info.email)
                .shopSite().put(info.site)
                .shopThanksPhrase().put(info.thanksPhrase)
                .shopPaymentGateway().put(info.paymentGateway == null ? null : info.paymentGateway.name())
                .shopHoldOnOrdersCount().put(info.holdOnOrderCount)
                .printOnholdOrders().put(info.printOnholdOrders)
                .shopDrawerClosedForSale().put(info.drawerClosedForSale)
                .clockinRequired4Sales().put(info.clockinRequired4Sales)
                .shopDisplayWelcomeMsg().put(info.displayWelcomeMsg)
                .shopDisplayWelcomeMsgBottom().put(info.displayWelcomeMsgBottom)
                .shopUseCreditReceipt().put(info.useCreditReceipt)
                .signaturePrintLimit().put(_decimal(info.signaturePrintLimit))
                .shopOwnerEmail().put(info.ownerEmail)
                .shopCreditReceiptExpireTime().put(info.creditReceiptExpireTime)
                .prepaidUrl().put(info.prepaidUrl)
                .prepaidMid().put(info.prepaidMid)
                .prepaidPassword().put(info.prepaidPassword)
                .prepaidTransactionMode().put(info.prepaidTransactionMode)
                .tipsEnabled().put(info.tipsEnabled)
                .tipsSplitTreshold().put(_decimal(info.tipsSplitTreshold))
                .tipsOnFlyEnabled().put(info.tipsOnFlyEnabled)
                .tipsWarnThreshold().put(_decimal(info.tipsWarnThreshold))
                .zipMandatory().put(info.zipMandatory)
                .cvnMandatory().put(info.cvnMandatory)
                .acceptCreditCards().put(info.acceptCreditCards)
                .acceptDebitCards().put(info.acceptDebitCards)
                .acceptEbtCards().put(info.acceptEbtCards)
                .acceptSerializableItems().put(info.acceptSerializable)
                .blackstonePaymentUrl().put(info.blackstonePaymentUrl)
                .blackstonePaymentAccount().put(info.blackstonePaymentAccount)
                .blackstonePaymentPassword().put(info.blackstonePaymentPassword)
                .blackstonePaymentApptype().put(info.blackstonePaymentApptype)
                .blackstonePaymentAppkey().put(info.blackstonePaymentAppkey)
                .blackstonePaymentMid().put(info.blackstonePaymentMid)
                .shopSupportEmail().put(info.supportEmail)
                .autoSettlementTime().put(info.autoSettlementTime)
                .commissionControl().put(info.commissionControl)
                .defaultStoreCommission().put(_decimal(info.defaultStoreCommission))
                .offlinePeriodHours().put(info.offlinePeriodHours)
                .unitLabelDefaultShortcut().put(info.defUnitLabelShortcut)
                .unitLabelDefaultDescription().put(info.defUnitLabelDescription)
                .printerTwoCopiesReceipt().put(info.printerTwoCopiesReceipt)
                .printDetailReceipt().put(info.printDetailReceipt)
                .printReceiptTwice().put(info.printReceiptTwice)
                .maxItemsCount().put(info.maxItemsCount)
                .printDropOrPayout().put(info.printDropOrPayout)
                .updateCheckTimer().put(info.updateCheckTimer)
                .enableEreportDepartSale().put(info.enableEreportDepartSale)
                .enableEreportItemSale().put(info.enableEreportItemSale)
                .ivulotoMID().put(info.ivulotoMid)
                .terminalID().put(info.terminalID)
                .terminalPassword().put(info.terminalPassword)
                .removeCheckAndOfflineCredit().put(info.removeCheckAndOfflineCredit)
                .planId().put(info.planId)
                .apply();

        setUsers();
    }

    public synchronized void saveShopInfoInner(ShopInfo info) {
        shopInfo = info;
    }

    public void saveBarcodePrefixes(BarcodePrefixes barcodePrefixes) {
        this.barcodePrefixes = barcodePrefixes;
        shopPref.edit()
                .code10DItem().put(barcodePrefixes.code10DItem)
                .code6DItem4DPrice().put(barcodePrefixes.code6DItem4DPrice)
                .code5DItem5DPrice().put(barcodePrefixes.code5DItem5DPrice)
                .code4DItem6DPrice().put(barcodePrefixes.code4DItem6DPrice)
                .code3DItem7DPrice().put(barcodePrefixes.code3DItem7DPrice)
                .code6DItem4DWeight3Dec().put(barcodePrefixes.code6DItem4DWeight3Dec)
                .code6DItem4DWeight().put(barcodePrefixes.code6DItem4DWeight)
                .code5DItem5DWeight3Dec().put(barcodePrefixes.code5DItem5DWeight3Dec)
                .code5DItem5DWeight().put(barcodePrefixes.code5DItem5DWeight)
                .code5DItem5DWeight0Dec().put(barcodePrefixes.code5DItem5DWeight0Dec)
                .apply();
    }

    public void savePrepaidTaxes(HashMap<Broker, BigDecimal> taxes) {
        this.prepaidTaxes = taxes;
        shopPref.edit()
                .wirelessRechargeTax().put(_decimal(taxes.get(Broker.WIRELESS_RECHARGE)))
                .internationalTopupTax().put(_decimal(taxes.get(Broker.INTERNATIONAL_TOPUP)))
                .billPaymentTax().put(_decimal(taxes.get(Broker.BILL_PAYMENT)))
                .longDistanceTax().put(_decimal(taxes.get(Broker.LONG_DISTANCE)))
                .sunpassTax().put(_decimal(taxes.get(Broker.SUNPASS)))
                .pinlessTax().put(_decimal(taxes.get(Broker.PINLESS)))
                .apply();
    }

    public synchronized EmployeeModel getOperator() {
        return operator;
    }

    public synchronized Set<Permission> getOperatorPermissions() {
        return operatorPermissions;
    }

    public synchronized void setOperatorPermissions(Set<Permission> permissions) {
        operatorPermissions = permissions;
    }

    public synchronized void updateOperatorPermissions(Set<Permission> enabledPermissions, Set<Permission> disabledPermissions) {
        Set<Permission> newPermissions = enabledPermissions == null ? new HashSet<Permission>() : enabledPermissions;
        for (Permission permission : operatorPermissions) {
            if (disabledPermissions == null || !disabledPermissions.contains(permission))
                newPermissions.add(permission);
        }
        operatorPermissions = newPermissions;
    }

    public boolean isFreemium() {
        final boolean isFreemium = shopPref.planId().get() == FREEMIUM_PLAN_ID;
        Logger.d("[Freemium] %s", isFreemium);
        return isFreemium;
    }

    public boolean hasPermission(Permission permissions) {
        if (permissions == null)
            return true;
        Set<Permission> operatorPermissions = getOperatorPermissions();
        return operatorPermissions == null ? false : operatorPermissions.contains(permissions);
    }

    public synchronized boolean isTipsEnabled() {
        return shopInfo.tipsEnabled;
    }

    public synchronized boolean isCommissionsEnabled() {
        return shopInfo.commissionControl;
    }

    public synchronized BigDecimal getDefaultStoreCommission() {
        return shopInfo.defaultStoreCommission;
    }

    public synchronized boolean getPrinterTwoCopiesReceipt() {
        return shopInfo.printerTwoCopiesReceipt;
    }

    public void setSunpassActivated(boolean activated) {
        shopPref.SunpassActivated().put(activated);
    }

    public boolean getSunpassActivated() {
        return shopPref.SunpassActivated().get();
    }

    public void setDirecTvPRActivated(boolean activated) {
        shopPref.DirecTvPRActivated().put(activated);
    }

    public boolean getDirecTvPRActivated() {
        return shopPref.DirecTvPRActivated().get();
    }

    public void setIVULotoActivated(boolean activated) {
        shopPref.IVULotoActivated().put(activated);
    }

    public boolean getIVULotoActivated() {
        return shopPref.IVULotoActivated().get();
    }

    public void setRemoveCheckAndOfflineCredit(boolean removeCheckAndOfflineCredit) {
        shopPref.removeCheckAndOfflineCredit().put(removeCheckAndOfflineCredit);
    }

    public boolean getRemoveCheckAndOfflineCredit() {
        return shopPref.removeCheckAndOfflineCredit().get();
    }

    public void setIvulotoMID(String ivulotoMID) {
        shopPref.ivulotoMID().put(ivulotoMID);
    }

    public String getIvulotoMID() {
        return shopPref.ivulotoMID().get();
    }

    public void setTerminalID(String terminalID) {
        shopPref.terminalID().put(terminalID);
    }

    public String getterminalID() {
        return shopPref.terminalID().get();
    }

    public void setTerminalPassword(String terminalPassword) {
        shopPref.terminalPassword().put(terminalPassword);
    }

    public String getTerminalPassword() {
        return shopPref.terminalPassword().get();
    }

    public void setBillPaymentActivated(boolean activated) {
        shopPref.BillPaymentActivated().put(activated);
    }

    public boolean getBillPaymentActivated() {
        return shopPref.BillPaymentActivated().get();
    }

    public boolean isPaxConfigured() {
        return !TextUtils.isEmpty(shopPref.paxUrl().get());
    }

    public boolean isPaxTipsEnabled() {
        return shopPref.paxTipsEnabled().get();
    }

    public boolean isBlackstonePax() {

        return shopPref.isBlackstonePax().get();

    }

    public void setPaxSerial(String serial) {
        shopPref.paxSerial().put(serial);
    }

    public String getPaxSerial() {
        return shopPref.paxSerial().get();
    }

    public void setUpdateApprove(boolean approve) {
        shopPref.updateApprove().put(approve);
    }

    public boolean getUpdateApprove() {
        return shopPref.updateApprove().get();
    }

    public void setUpdateURL(String url) {
        shopPref.updateUrl().put(url);
    }

    public String getUpdateURL() {
        return shopPref.updateUrl().get();
    }

    public void setUpdateRequire(String require) {
        shopPref.updateRequire().put(require);
    }

    public String getUpdateRequire() {
        return shopPref.updateRequire().get();
    }

    public void setUpdateTime(long time) {
        shopPref.updateTime().put(time);
    }

    public long getUpdateTime() {
        return shopPref.updateTime().getOr(0);
    }

    public void setLastUpdateTime(long time) {
        shopPref.lastUpdateTime().put(time);
    }

    public long getLastUpdateTime() {
        return shopPref.lastUpdateTime().getOr(0);
    }

    public void setPaxTipsEnabled(boolean isPaxTipsEnabled) {
        shopPref.paxTipsEnabled().put(isPaxTipsEnabled);
    }

    public void lockOnTrainingMode() {
        trainingModeLock.lock();
    }

    public void unlockOnTrainingMode() {
        trainingModeLock.unlock();
    }

    public boolean isTrainingMode() {
        return shopPref.trainingMode().get();
    }

    public void setTrainingMode(boolean isTrainingMode) {
        shopPref.trainingMode().put(isTrainingMode);
    }

    public void lockOnOfflineMode() {
        offlineModeLock.lock();
    }

    public void unlockOnOfflineMode() {
        offlineModeLock.unlock();
    }

    public void setOfflineMode(Long offlineStartTime) {
        Logger.d("[OFFLINE MODE] set to: " + offlineStartTime);
        shopPref.offlineStartTime().put(offlineStartTime == null ? 0L : offlineStartTime);
    }

    public boolean isOfflineMode() {
        return shopPref.offlineStartTime().getOr(0L) > 0L;
    }

    public boolean isOfflineModeExpired() {
        Long offlineStartTime = getOfflineStartTime();
        if (offlineStartTime == null) {
            return false;
        }
        long offlinePeriod = getOfflinePeriod();
        long currentTime = System.currentTimeMillis();

        boolean result = (currentTime < offlineStartTime) || (currentTime - offlineStartTime) > offlinePeriod;
        Logger.d("[OFFLINE MODE] expired: " + result);
        return result;
    }

    public boolean isOfflineModeNearExpiration() {
        Long offlineStartTime = getOfflineStartTime();
        if (offlineStartTime == null) {
            return false;
        }
        long offlinePeriod = getOfflinePeriod();
        long currentTime = System.currentTimeMillis();

        boolean result = (currentTime < offlineStartTime) || (currentTime - offlineStartTime) >= (offlinePeriod - OFFLINE_PERIOD_NEAR);
        Logger.d("[OFFLINE MODE] near expiration: " + result);
        return result;
    }

    private Long getOfflineStartTime() {
        long offlineStartTime = shopPref.offlineStartTime().getOr(0L);
        if (offlineStartTime <= 0L) {
            Logger.d("[OFFLINE MODE] not set");
            return null;
        }
        return offlineStartTime;
    }

    public void setLastSuccessfulSyncTime(Long lastSuccsessfulSyncTime) {
        shopPref.lastSuccessfulSyncTime().put(lastSuccsessfulSyncTime == null ? 0L : lastSuccsessfulSyncTime);
    }

    public Long getLastSuccessfulSyncTime() {
        long lastSuccsessfulSyncTime = shopPref.lastSuccessfulSyncTime().getOr(0L);
        return lastSuccsessfulSyncTime == 0L ? null : lastSuccsessfulSyncTime;
    }

    public boolean isSalesSyncGapOccurred() {
        return shopPref.salesSyncGapOccurred().getOr(false);
    }

    public void setSalesSyncGapOccurred(boolean value) {
        shopPref.salesSyncGapOccurred().put(value);
    }

    public boolean isInvalidOrdersFound() {
        return shopPref.invalidOrdersFound().getOr(false);
    }

    public void setInvalidOrdersFound(boolean value) {
        shopPref.invalidOrdersFound().put(value);
    }

    public boolean isLoadingOldOrders() {
        return shopPref.loadingOldOrders().getOr(false);
    }

    public void setLoadingOldOrders(boolean value) {
        shopPref.loadingOldOrders().put(value);
    }

    private long getOfflinePeriod() {
        int offlinePeriodHours = shopPref.offlinePeriodHours().getOr(0);
        long offlinePeriod;
        if (offlinePeriodHours == 0) {
            Logger.e("[OFFLINE MODE] offline period not set - fallback to default");
            offlinePeriod = DEFAULT_OFFLINE_PERIOD;
        } else {
            offlinePeriod = TimeUnit.HOURS.toMillis(offlinePeriodHours);
        }
        return offlinePeriod;
    }

    public void setNeedBillPaymentUpdated(boolean needed) {
        shopPref.NeedBillpaymentUpdated().put(needed);
    }

    public boolean getNeedBillPaymentUpdated() {
        return shopPref.NeedBillpaymentUpdated().getOr(true);
    }

    public void setPaxTimeOut(int timeout) {
        shopPref.paxTimeOut().put(timeout);
    }

    public int getPaxTimeOut() {
        return shopPref.paxTimeOut().getOr(0);
    }

    public void setUpdateFilePath(String path) {
        shopPref.updateFilePath().put(path);
    }

    public String getUpdateFilePath() {
        return shopPref.updateFilePath().get();
    }

    public boolean getPrintDropOrPayout() {
        return shopPref.printDropOrPayout().getOr(true);
    }

    public void setLastUserName(String name) {
        shopPref.lastUserName().put(name);
    }

    public String getLastUserName() {
        return shopPref.lastUserName().getOr(null);
    }

    public void setLastUserPassword(String password) {
        shopPref.lastUserPassword().put(password);
    }

    public String getLastUserPassword() {
        return shopPref.lastUserPassword().getOr(null);
    }

    public synchronized ShopInfo getShopInfo() {
        return shopInfo;
    }

    public BarcodePrefixes getBarcodePrefixes() {
        return barcodePrefixes;
    }

    public BigDecimal getPrepaidTax(Broker prepaidType) {
        return prepaidTaxes.get(prepaidType);
    }

    public boolean isTipsEnabledWasChanged() {
        return shopPref.tipsEnabledWasChanged().get();
    }


    public void setTipsEnabledWasChanged(boolean tipsEnabledWasChanged) {
        shopPref.tipsEnabledWasChanged().put(tipsEnabledWasChanged);
    }


    public static TcrApplication get() {
        return self;
    }

    public int getDrawerClosedValue() {
        return shopPref.drawerPinHigh().getOr(1);
    }

    public boolean printLogo() {
        return shopPref.printLogo().getOr(false);
    }

    public RestAdapter getRestAdapter() {
        return restAdapter;
    }

    public RestAdapter getRestAdapterJsonOrg() {
        return restAdapterJsonOrg;
    }

    public synchronized boolean isPrepaidUserValid() {
        return prepaidUser.isValid();
    }

    public synchronized boolean isPaymentUserValid() {
        return blackstoneUser.isValid();
    }

    public synchronized String getAutoSettlementTime() {
        return shopInfo.autoSettlementTime;
    }

    public class BaseAuthorizationInterceptor implements RequestInterceptor {

        private String userAndPassword;

        public BaseAuthorizationInterceptor(String userAndPassword) {
            this.userAndPassword = userAndPassword;
        }

        @Override
        public void intercept(RequestFacade requestFacade) {
            final String authorizationValue = "Basic " + Base64.encodeToString(userAndPassword.getBytes(), 0);
            requestFacade.addHeader("Authorization", authorizationValue);
        }
    }

    private class ExceptionHandler implements Thread.UncaughtExceptionHandler {

        private final Thread.UncaughtExceptionHandler defaultExceptionHandler;

        public ExceptionHandler() {
            defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        }

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Logger.e("ExceptionHandler.uncaughtException():", ex);
            defaultExceptionHandler.uncaughtException(thread, ex);
        }
    }

    private static class ExtConverter extends GsonConverter {

        public ExtConverter() {
            super(new Gson(), "UTF-8");
        }

        @Override
        public Object fromBody(TypedInput typedInput, Type type) throws ConversionException {
            if (type == PlainTextResponse.class || type == RestCommand.Response.class) {
                return super.fromBody(typedInput, type);
            }
            String str;
            try {
                str = CharStreams.toString(new InputStreamReader(typedInput.in(), Charsets.UTF_8));
            } catch (IOException e) {
                return null;
            }

            try {
                JdbcJSONObject response = new JdbcJSONObject(str);
                if (type == AuthResponse.class) {
                    return new AuthResponse(response.getString("status"), response.getString("message"), response.getJSONObject("data"));
                } else if (type == DBVersionResponse.class) {
                    return new DBVersionResponse(response.getString("status"), response.getString("message"), response.getString("data"));
                } else if (type == GetResponse.class) {
                    return new GetResponse(response.getString("status"), response.getString("message"),
                            response.isNull("entity")
                                    ? (!response.isNull("data") ? response.getJSONObject("data") : null)
                                    : response.getJSONObject("entity")
                    );
                } else if (type == GetArrayResponse.class) {
                    return new GetArrayResponse(
                            response.getString("status"),
                            response.getString("message"),
                            !response.isNull("data") ? response.getJSONArray("data") : null
                    );
                } else if (type == GetPagedArrayResponse.class) {
                    JdbcJSONArray ar = null;
                    int rows = 0;
                    if (!response.isNull("data")) {
                        JdbcJSONObject data = response.getJSONObject("data");
                        rows = data.getInt("rows");
                        if (!data.isNull("data")) {
                            ar = data.getJSONArray("data");
                        }
                    }
                    return new GetPagedArrayResponse(
                            response.getString("status"),
                            response.getString("message"),
                            ar,
                            rows
                    );
                } else if (type == UploadResponseV1.class) {
                    return new UploadResponseV1(response.getString("status"), response.getString("message"), null/*response.getJSONObject("data")*/);
                } else if (type == GetPrepaidOrderIdResponse.class) {
                    return new GetPrepaidOrderIdResponse(response.getString("status"), response.getString("message"), response.getString("data"));
                } else if (type == GetCurrentTimestampResponse.class) {
                    return new GetCurrentTimestampResponse(response.getString("status"), response.getString("message"), response.getString("data"));
                } else if (type == RestCommand.IntegerResponse.class) {
                    return new RestCommand.IntegerResponse(response.getString("status"), response.getString("message"), response.isNull("data") ? null : response.getInt("data"));
                } else if (type == PlanOptionsResponse.class) {
                    return new PlanOptionsResponse(response.getString("status"), response.getString("message"), response.isNull("entity")
                            ? (!response.isNull("data") ? response.getJSONObject("data") : null)
                            : response.getJSONObject("entity"));
                } else {
                    throw new ConversionException("Can't parse response. Unknown type: " + type);
                }

            } catch (Exception e) {
                throw new ConversionException("Can't parse response: " + str, e);
            }
        }
    }

    public InputStream getScannerIS() {
        serialPortScanner = new SerialPortScanner();

        return serialPortScanner.getInputStreamReader();
    }

    public InputStream getScaleIS() {
        serialPortScale = new SerialPortScale();

        return serialPortScale.getInputStreamReader();
    }

    public void closeSerialScanner() {
        if (serialPortScanner != null) {
            try {
                serialPortScanner.close();
                serialPortScanner = null;
            } catch (IOException e) {
                e.printStackTrace();
                Logger.d("Tcrapplication closeSerialScanner faile: " + e.toString());
                serialPortScanner = null;
            }
        }
    }
}
