package com.kaching123.tcr.service;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackSetAutomaticBatchCloseCommand;
import com.kaching123.tcr.commands.payment.blackstone.payment.BlackUpdateAutomaticHourToCloseBatchCommand;
import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneMIDownloadCommand;
import com.kaching123.tcr.commands.rest.RestCommand;
import com.kaching123.tcr.commands.rest.RestCommand.IntegerResponse;
import com.kaching123.tcr.commands.rest.sync.DBVersionCheckCommand;
import com.kaching123.tcr.commands.rest.sync.GetArrayResponse;
import com.kaching123.tcr.commands.rest.sync.GetCurrentTimestampResponse;
import com.kaching123.tcr.commands.rest.sync.GetPagedArrayResponse;
import com.kaching123.tcr.commands.rest.sync.GetResponse;
import com.kaching123.tcr.commands.rest.sync.Sync2GetRequestBuilder;
import com.kaching123.tcr.commands.rest.sync.SyncApi;
import com.kaching123.tcr.commands.rest.sync.SyncApi2;
import com.kaching123.tcr.commands.rest.sync.SyncUploadRequestBuilder;
import com.kaching123.tcr.commands.support.SendLogCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.BarcodePrefixJdbcConverter;
import com.kaching123.tcr.jdbc.converters.BarcodePrefixJdbcConverter.BarcodePrefixes;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.jdbc.converters.PrepaidTaxJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ShopInfoViewJdbcConverter.ShopInfo;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeePermissionModel;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PlanOptionsResponse;
import com.kaching123.tcr.model.RegisterModel;
import com.kaching123.tcr.model.RegisterModel.RegisterStatus;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.blackstone.payment.request.AutomaticBatchCloseRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.Broker;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.notification.NotificationHelper;
import com.kaching123.tcr.processor.PrepaidUpdateProcessor;
import com.kaching123.tcr.processor.PrepaidUpdateProcessor.IUpdateCallback;
import com.kaching123.tcr.service.response.SyncOldActiveOrdersResponseHandler;
import com.kaching123.tcr.service.response.SyncResponseHandler.HandlerResult;
import com.kaching123.tcr.service.response.SyncSingleResponseHandler;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopProviderExt;
import com.kaching123.tcr.store.ShopProviderExt.Method;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CustomerLoyaltyPointsTable;
import com.kaching123.tcr.store.ShopStore.ItemMatrixTable;
import com.kaching123.tcr.store.ShopStore.LoyaltyIncentiveItemTable;
import com.kaching123.tcr.store.ShopStore.LoyaltyIncentivePlanTable;
import com.kaching123.tcr.store.ShopStore.LoyaltyIncentiveTable;
import com.kaching123.tcr.store.ShopStore.LoyaltyPlanTable;
import com.kaching123.tcr.store.ShopStore.VariantSubItemTable;
import com.kaching123.tcr.store.ShopStore.ActivationCarrierTable;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopStore.CashDrawerMovementTable;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
import com.kaching123.tcr.store.ShopStore.ComposerTable;
import com.kaching123.tcr.store.ShopStore.CreditReceiptTable;
import com.kaching123.tcr.store.ShopStore.CustomerTable;
import com.kaching123.tcr.store.ShopStore.DepartmentTable;
import com.kaching123.tcr.store.ShopStore.EmployeeCommissionsTable;
import com.kaching123.tcr.store.ShopStore.EmployeePermissionTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTimesheetTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;
import com.kaching123.tcr.store.ShopStore.IBemaSyncTable;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.ModifierGroupTable;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.kaching123.tcr.store.ShopStore.OldActiveUnitOrdersQuery;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.kaching123.tcr.store.ShopStore.RegisterTable;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.ShiftTable;
import com.kaching123.tcr.store.ShopStore.TaxGroupTable;
import com.kaching123.tcr.store.ShopStore.UnitLabelTable;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.kaching123.tcr.store.ShopStore.UpdateTimeTable;
import com.kaching123.tcr.store.ShopStore.VariantItemTable;
import com.kaching123.tcr.store.ShopStore.WirelessTable;
import com.kaching123.tcr.store.SyncOpenHelper;
import com.kaching123.tcr.util.JdbcJSONArray;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.kaching123.tcr.util.Util;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.MerchantDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import retrofit.RetrofitError;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

public class SyncCommand implements Runnable {

    private static final int PAGE_ROWS = 1800;

    private static final int FINALIZE_SYNC_RETRIES = 3;

    private static final int MAX_QUERY_PARAMETERS_COUNT = 999;

    public static final String ACTION_SYNC_PROGRESS = "com.kaching123.tcr.service.ACTION_SYNC_PROGRESS";
    public static final String ACTION_SYNC_COMPLETED = "com.kaching123.tcr.service.ACTION_SYNC_COMPLETED";
    public static final String ACTION_SYNC_GAP = "action_sync_gap";
    public static final String EXTRA_TABLE = "table";
    public static final String EXTRA_PAGES = "pages";
    public static final String EXTRA_PROGRESS = "progress";
    public static final String EXTRA_DATA_LABEL = "data_label";
    public static final String EXTRA_SUCCESS = "success";
    public static final String EXTRA_SYNC_LOCKED = "sync_locked";

    private static final String[] TABLES_URIS = new String[]{
            RegisterTable.URI_CONTENT,
            PrinterAliasTable.URI_CONTENT,
            CashDrawerMovementTable.URI_CONTENT,
            CustomerTable.URI_CONTENT,
            EmployeeTable.URI_CONTENT,
            EmployeePermissionTable.URI_CONTENT,
            EmployeeTimesheetTable.URI_CONTENT,
            ShiftTable.URI_CONTENT,
            TaxGroupTable.URI_CONTENT,
            DepartmentTable.URI_CONTENT,
            CategoryTable.URI_CONTENT,
            ItemTable.URI_CONTENT,
            ModifierTable.URI_CONTENT,
            ComposerTable.URI_CONTENT,
            ModifierGroupTable.URI_CONTENT,
            ItemMovementTable.URI_CONTENT,
            UnitTable.URI_CONTENT,
            UnitLabelTable.URI_CONTENT,
            LoyaltyIncentiveTable.URI_CONTENT,
            LoyaltyIncentiveItemTable.URI_CONTENT,
            LoyaltyPlanTable.URI_CONTENT,
            LoyaltyIncentivePlanTable.URI_CONTENT,

            SaleOrderTable.URI_CONTENT,
            SaleItemTable.URI_CONTENT,
            SaleAddonTable.URI_CONTENT,
            PaymentTransactionTable.URI_CONTENT,
            BillPaymentDescriptionTable.URI_CONTENT,
            CreditReceiptTable.URI_CONTENT,
            EmployeeTipsTable.URI_CONTENT,
            EmployeeCommissionsTable.URI_CONTENT,
            VariantItemTable.URI_CONTENT,
            VariantSubItemTable.URI_CONTENT,
            ItemMatrixTable.URI_CONTENT
    };

    private Context service;

    private final boolean isManual;

    private SyncOpenHelper syncOpenHelper;

    private boolean serverHasBeenUpdated;

    public SyncCommand(Context service, boolean isManual) {
        super();
        this.service = service;
        this.isManual = isManual;
    }

    protected TcrApplication getApp() {
        return (TcrApplication) service.getApplicationContext();
    }

    @Override
    public void run() {
        Logger.d("SyncCommand start");
        if (!getApp().isUserLogin()) {
            Logger.d("SyncCommand end: NO USER");
            return;
        }

        NotificationHelper.addSyncNotification(service);
        int count = 0;
        String error = null;
        boolean isOffline = false;
        boolean isSyncLocked = false;

        getApp().lockOnTrainingMode();
        try {
            count = syncNow(getApp().getOperator());
        } catch (SyncException e) {
            error = getErrorString(e.localTable);
        } catch (DBVersionCheckException e) {
            error = getErrorString(null);
        } catch (OfflineException e) {
            error = getErrorString(null);
            isOffline = true;
        } catch (SyncInconsistentException e) {
            error = service.getString(R.string.error_message_sync_inconsistent);
        } catch (SyncLockedException e) {
            error = service.getString(R.string.error_message_sync_locked);
            isSyncLocked = true;
        } catch (SyncInterruptedException e) {
            error = service.getString(R.string.error_message_sync_interrupted);
        } catch (Exception e) {
            //TODO: handle sub commands exceptions
            error = getErrorString(null);
        } finally {
            getApp().unlockOnTrainingMode();
        }
        boolean failure = error != null;
        if (failure) {
            NotificationHelper.showSyncErrorNotification(service, error);
            if (!isOffline)
                SendLogCommand.start(service);
        } else if (count > 0) {
            NotificationHelper.showSyncNewDataNotification(service, count);
        } else {
            NotificationHelper.removeSyncNotification(service);
        }
        fireCompleteEvent(service, !failure, isSyncLocked);
        Logger.d("SyncCommand end");
    }

    private static Handler handler = new Handler();

    public int syncNow(final EmployeeModel employee) throws SyncException, DBVersionCheckException, OfflineException, SyncInconsistentException, SyncLockedException, SyncInterruptedException {
        if (!getApp().isTrainingMode() && !Util.isNetworkAvailable(service)) {
            Logger.e("SyncCommand: NO CONNECTION");
            setOfflineMode(true);
            throw new OfflineException();
        }
        try {
            int result = syncNowInner(employee);
            setOfflineMode(false);
            return result;
        } catch (SyncException e) {
            Logger.e("SyncCommand failed", e);
            setOfflineMode(true);
            throw e;
        } catch (DBVersionCheckException e) {
            Logger.e("SyncCommand failed", e);
            setOfflineMode(true);
            throw e;
        } catch (SyncInconsistentException e) {
            Logger.e("SyncCommand failed", e);
            //we have retrieved the data
            setOfflineMode(false);
            throw e;
        } catch (SyncLockedException e) {
            Logger.e("SyncCommand failed", e);
            setOfflineMode(true);
            throw e;
        } catch (SyncInterruptedException e) {
            Logger.e("SyncCommand failed", e);
            setOfflineMode(true);
            throw e;
        } catch (RuntimeException e) {
            Logger.e("SyncCommand failed", e);
            setOfflineMode(true);
            throw e;
        }
    }

    private void setOfflineMode(boolean isOfflineMode) {
        if (!isManual && !getApp().isUserLogin())
            return;
        if (getApp().isTrainingMode())
            return;

        getApp().lockOnOfflineMode();
        try {
            if (!isOfflineMode && !Util.isNetworkAvailable(service)) {
                return;
            }
            if (!isOfflineMode || !getApp().isOfflineMode())
                getApp().setOfflineMode(isOfflineMode ? System.currentTimeMillis() : null);
        } finally {
            getApp().unlockOnOfflineMode();
        }
    }

    private int syncNowInner(final EmployeeModel employee) throws SyncException, DBVersionCheckException, SyncInconsistentException, SyncLockedException, SyncInterruptedException {
        if (getApp().isTrainingMode()) {
//            syncPAXMerchantInfo();
            syncWireless(service);
            return 0;
        }

        boolean wasTipsEnabled = getApp().isTipsEnabled();
        String oldAutoSettlementTime = getApp().getAutoSettlementTime();

        Integer salesHistoryLimit = syncShopInfo(employee);
        if (salesHistoryLimit == null) {
            Logger.w("SyncCommand.syncNowInner(): sales history limit is not set yet");
        }

        checkAutoSettlement(wasTipsEnabled, oldAutoSettlementTime);
//        syncPAXMerchantInfo();

        //go to the blackstone api to refresh cache
        //syncWireless(service);

        int count = 0;
        // download date from our amazon web server - start

        TcrApplication app = TcrApplication.get();
        syncOpenHelper = app.getSyncOpenHelper();

        SyncApi api = getApp().getRestAdapter().create(SyncApi.class);
        SyncApi2 api2 = app.getRestAdapter().create(SyncApi2.class);
        Logger.d("[SYNC HISTORY]SyncCommand: acquiring history lock");
        getApp().lockOnSalesHistory();
        try {
            try {
                Logger.d("[SYNC HISTORY]SyncCommand: history lock acquired");
                checkIsLoadingOldOrders();

                long serverLastTimestamp = getServerCurrentTimestamp(api, employee);
                Long minUpdateTime = salesHistoryLimit == null ? null : serverLastTimestamp - TimeUnit.DAYS.toMillis(salesHistoryLimit);

                Integer oldSalesHistoryLimit = getApp().getSalesHistoryLimit();
                if (oldSalesHistoryLimit != null && salesHistoryLimit != null && salesHistoryLimit > oldSalesHistoryLimit) {
                    Long lastSuccessfulSyncTime = getApp().getLastSuccessfulSyncTime();
                    long oldMinUpdateTime = serverLastTimestamp - TimeUnit.DAYS.toMillis(oldSalesHistoryLimit);
                    if (lastSuccessfulSyncTime != null && lastSuccessfulSyncTime < oldMinUpdateTime)
                        getApp().setLastSuccessfulSyncTime(null);
                }

                getApp().setSalesHistoryLimit(salesHistoryLimit);

                if (oldSalesHistoryLimit == null && salesHistoryLimit != null) {
                    Logger.w("SyncCommand.syncNowInner(): sales history limit first set, notifying");
                    notifySyncGep();
                }

                int retriesCount = FINALIZE_SYNC_RETRIES;
                do {
                    checkIsLoadingOldOrders();

                    if (retriesCount != FINALIZE_SYNC_RETRIES) {
                        serverLastTimestamp = getServerCurrentTimestamp(api, employee);
                        checkIsLoadingOldOrders();
                    }

                    Logger.e("SyncCommand.syncNowInner(): attempt #" + (FINALIZE_SYNC_RETRIES - retriesCount + 1) + "; serverLastTimestamp: " + serverLastTimestamp + "; minUpdateTime: " + minUpdateTime);
                    serverHasBeenUpdated = false;
                    count += syncSingleTable2(service, api2, UnitLabelTable.TABLE_NAME, UnitLabelTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, RegisterTable.TABLE_NAME, RegisterTable.ID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, PrinterAliasTable.TABLE_NAME, PrinterAliasTable.GUID, employee, serverLastTimestamp);

                    count += syncSingleTable2(service, api2, CustomerTable.TABLE_NAME, CustomerTable.GUID, employee, serverLastTimestamp);

                    //employee
                    count += syncSingleTable2(service, api2, EmployeeTable.TABLE_NAME, EmployeeTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, EmployeePermissionTable.TABLE_NAME, EmployeePermissionTable.PERMISSION_ID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, EmployeeTimesheetTable.TABLE_NAME, EmployeeTimesheetTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, ShiftTable.TABLE_NAME, ShiftTable.GUID, employee, serverLastTimestamp);

                    count += syncSingleTable2(service, api2, CashDrawerMovementTable.TABLE_NAME, CashDrawerMovementTable.GUID, employee, serverLastTimestamp);

                    //inventory
                    count += syncSingleTable2(service, api2, TaxGroupTable.TABLE_NAME, TaxGroupTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, DepartmentTable.TABLE_NAME, DepartmentTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, CategoryTable.TABLE_NAME, CategoryTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, ItemTable.TABLE_NAME, ItemTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, ModifierTable.TABLE_NAME, ModifierTable.MODIFIER_GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, ModifierGroupTable.TABLE_NAME, ModifierGroupTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, ComposerTable.TABLE_NAME, ComposerTable.ID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, VariantItemTable.TABLE_NAME, VariantItemTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, VariantSubItemTable.TABLE_NAME, VariantSubItemTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, ItemMatrixTable.TABLE_NAME, ItemMatrixTable.GUID, employee, serverLastTimestamp);

                    //loyalty
                    count += syncSingleTable2(service, api2, LoyaltyIncentiveTable.TABLE_NAME, LoyaltyIncentiveTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, LoyaltyIncentiveItemTable.TABLE_NAME, LoyaltyIncentiveItemTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, LoyaltyPlanTable.TABLE_NAME, LoyaltyPlanTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, LoyaltyIncentivePlanTable.TABLE_NAME, LoyaltyIncentivePlanTable.GUID, employee, serverLastTimestamp);
                    count += syncSingleTable2(service, api2, CustomerLoyaltyPointsTable.TABLE_NAME, CustomerLoyaltyPointsTable.GUID, employee, serverLastTimestamp);

                    //between iterations shouldn't be any gaps
                    boolean firstIteration = retriesCount == FINALIZE_SYNC_RETRIES;
                    Long lastSuccessfulSyncTime = getApp().getLastSuccessfulSyncTime();
                    boolean lastSyncStillActual = salesHistoryLimit == null || (lastSuccessfulSyncTime != null && lastSuccessfulSyncTime >= minUpdateTime);

                    if (firstIteration && !lastSyncStillActual
                            && checkIsSyncGep(syncOpenHelper, minUpdateTime, ItemMovementTable.TABLE_NAME, ItemMovementTable.GUID, null, false)
                            && checkIsOldSyncCache(syncOpenHelper, minUpdateTime, ItemMovementTable.TABLE_NAME, null, false)) {
                        clearSyncCache(syncOpenHelper, new String[]{ItemMovementTable.TABLE_NAME});
                    }
                    checkIsLoadingOldOrders();

                    count += syncSingleTable2(service, api2, ItemMovementTable.TABLE_NAME, ItemMovementTable.GUID, employee, serverLastTimestamp, minUpdateTime, true);

                    //sale

                    boolean isSalesSyncGep = false;
                    if (firstIteration && !lastSyncStillActual) {
                        isSalesSyncGep = checkIsSalesSyncGep(syncOpenHelper, minUpdateTime);
                    }
                    boolean isFirstSync = false;
                    if (isSalesSyncGep) {
                        isFirstSync = isFirstSync(service);

                        if (checkIsSalesOldSyncCache(syncOpenHelper, minUpdateTime))
                            clearSalesSyncCache(syncOpenHelper);

                        getApp().setSalesSyncGapOccurred(true);
                    }
                    checkIsLoadingOldOrders();

                    if (salesHistoryLimit != null) {
                        syncOldActiveOrders(service, api2, employee, minUpdateTime);
                        checkIsLoadingOldOrders();
                    }

                    count += syncTableWithChildren2(service, api2,
                            SaleOrderTable.TABLE_NAME,
                            SaleOrderTable.GUID, SaleOrderTable.PARENT_ID,
                            employee, serverLastTimestamp, minUpdateTime, false);

                    count += syncSingleTable2(service, api2, BillPaymentDescriptionTable.TABLE_NAME, BillPaymentDescriptionTable.GUID, employee, serverLastTimestamp, minUpdateTime, false);

                    count += syncTableWithChildren2(service, api2,
                            SaleItemTable.TABLE_NAME,
                            SaleItemTable.SALE_ITEM_GUID, SaleItemTable.PARENT_GUID,
                            employee, serverLastTimestamp, minUpdateTime, false);

                    count += syncSingleTable2(service, api2, SaleAddonTable.TABLE_NAME, SaleAddonTable.GUID, employee, serverLastTimestamp, minUpdateTime, false);

                    count += syncTableWithChildren2(service, api2,
                            PaymentTransactionTable.TABLE_NAME,
                            PaymentTransactionTable.GUID, PaymentTransactionTable.PARENT_GUID,
                            employee, serverLastTimestamp, minUpdateTime, false);

                    count += syncSingleTable2(service, api2, CreditReceiptTable.TABLE_NAME, CreditReceiptTable.GUID, employee, serverLastTimestamp);

                    count += syncTableWithChildren2(service, api2,
                            EmployeeTipsTable.TABLE_NAME,
                            EmployeeTipsTable.GUID, EmployeeTipsTable.PARENT_GUID,
                            employee, serverLastTimestamp, minUpdateTime, false);

                    count += syncSingleTable2(service, api2, EmployeeCommissionsTable.TABLE_NAME, EmployeeCommissionsTable.GUID, employee, serverLastTimestamp, minUpdateTime, false);


                    //inventory depended from sale
                    count += syncSingleTable2(service, api2, UnitTable.TABLE_NAME, UnitTable.ID, employee, serverLastTimestamp, minUpdateTime, true, isSalesSyncGep, isFirstSync);
                    //end
                } while (serverHasBeenUpdated && --retriesCount > 0);

                if (serverHasBeenUpdated) {
                    Logger.e("SyncCommand failed, couldn't finalize sync, server's data has been modified");
                    throw new SyncInconsistentException();
                }

                //check that sales limit setting hasn't changed
                if (salesHistoryLimit != null) {
                    checkServerSalesHistoryLimit(api, employee, salesHistoryLimit);
                }

                //write data from extra db to main db on success, in transaction, making rows alive
                localSync(minUpdateTime);

                getApp().setLastSuccessfulSyncTime(serverLastTimestamp);

                if (getApp().isSalesSyncGapOccurred()) {
                    notifySyncGep();
                    getApp().setSalesSyncGapOccurred(false);
                }

            } finally {
                syncOpenHelper.close();
            }
        } finally {
            Logger.d("[SYNC HISTORY]SyncCommand: releasing history lock");
            getApp().unlockOnSalesHistory();
            Logger.d("[SYNC HISTORY]SyncCommand: history lock released");
        }
        // download date from our amazon web server - end
        //go to the blackstone api to refresh cache
//        syncWireless(service);
        sendSyncSuccessful(api, employee);

        return count;
    }

    private void checkServerSalesHistoryLimit(SyncApi api, EmployeeModel employee, int salesHistoryLimit) throws SyncException, SyncLockedException {
        Integer serverSalesHistoryLimit = getServerSalesHistoryLimit(api, employee);
        if (serverSalesHistoryLimit == null) {
            Logger.e("SyncCommand: server sales history limit check failed - value is not set on the server, or empty response");
            throw new SyncLockedException();
        }
        if (salesHistoryLimit != serverSalesHistoryLimit) {
            Logger.e("SyncCommand: server sales history limit check failed - value had been changed on the server");
            throw new SyncLockedException();
        }
    }

    private void checkIsLoadingOldOrders() throws SyncInterruptedException {
        Logger.d("[SYNC HISTORY]SyncCommand: checking loading orders flag");
        if (!getApp().isLoadingOldOrders())
            return;

        throw new SyncInterruptedException("download sync interrupted (loading old orders)");
    }

    private void notifySyncGep() {
        Logger.w("[SYNC GAP] notify");
        Intent intent = new Intent(ACTION_SYNC_GAP);
        LocalBroadcastManager.getInstance(service).sendBroadcast(intent);
    }


    private boolean checkIsSalesSyncGep(SyncOpenHelper syncOpenHelper, long minUpdateTime) {


        if (checkIsSyncGep(syncOpenHelper, minUpdateTime, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID, SaleOrderTable.PARENT_ID, false))
            return true;
        if (checkIsSyncGep(syncOpenHelper, minUpdateTime, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID, SaleOrderTable.PARENT_ID, true))
            return true;
        if (checkIsSyncGep(syncOpenHelper, minUpdateTime, BillPaymentDescriptionTable.TABLE_NAME, BillPaymentDescriptionTable.GUID, null, false))
            return true;
        if (checkIsSyncGep(syncOpenHelper, minUpdateTime, SaleItemTable.TABLE_NAME, SaleItemTable.SALE_ITEM_GUID, SaleItemTable.PARENT_GUID, false))
            return true;
        if (checkIsSyncGep(syncOpenHelper, minUpdateTime, SaleItemTable.TABLE_NAME, SaleItemTable.SALE_ITEM_GUID, SaleItemTable.PARENT_GUID, true))
            return true;
        if (checkIsSyncGep(syncOpenHelper, minUpdateTime, SaleAddonTable.TABLE_NAME, SaleAddonTable.GUID, null, false))
            return true;
        if (checkIsSyncGep(syncOpenHelper, minUpdateTime, PaymentTransactionTable.TABLE_NAME, PaymentTransactionTable.GUID, PaymentTransactionTable.PARENT_GUID, false))
            return true;
        if (checkIsSyncGep(syncOpenHelper, minUpdateTime, PaymentTransactionTable.TABLE_NAME, PaymentTransactionTable.GUID, PaymentTransactionTable.PARENT_GUID, true))
            return true;
        if (checkIsSyncGep(syncOpenHelper, minUpdateTime, EmployeeTipsTable.TABLE_NAME, EmployeeTipsTable.GUID, EmployeeTipsTable.PARENT_GUID, false))
            return true;
        if (checkIsSyncGep(syncOpenHelper, minUpdateTime, EmployeeTipsTable.TABLE_NAME, EmployeeTipsTable.GUID, EmployeeTipsTable.PARENT_GUID, true))
            return true;
        if (checkIsSyncGep(syncOpenHelper, minUpdateTime, EmployeeCommissionsTable.TABLE_NAME, EmployeeCommissionsTable.GUID, null, false))
            return true;
        if (checkIsSyncGep(syncOpenHelper, minUpdateTime, UnitTable.TABLE_NAME, UnitTable.ID, null, false))
            return true;
        return false;
    }

    private boolean checkIsSyncGep(SyncOpenHelper syncOpenHelper, long minUpdateTime, String tableName, String guidColumn, String parentIdColumn, boolean isChild) {
        MaxUpdateTime maxUpdateTime = getMaxTimeSingleTable(service, syncOpenHelper, tableName, guidColumn, parentIdColumn, isChild);
        return maxUpdateTime == null || (maxUpdateTime.time < minUpdateTime);
    }

    private boolean checkIsSalesOldSyncCache(SyncOpenHelper syncOpenHelper, long minUpdateTime) {
        //TODO: improve
        if (checkIsOldSyncCache(syncOpenHelper, minUpdateTime, SaleOrderTable.TABLE_NAME, SaleOrderTable.PARENT_ID, false))
            return true;
        if (checkIsOldSyncCache(syncOpenHelper, minUpdateTime, SaleOrderTable.TABLE_NAME, SaleOrderTable.PARENT_ID, true))
            return true;
        if (checkIsOldSyncCache(syncOpenHelper, minUpdateTime, BillPaymentDescriptionTable.TABLE_NAME, null, false))
            return true;
        if (checkIsOldSyncCache(syncOpenHelper, minUpdateTime, SaleItemTable.TABLE_NAME, SaleItemTable.PARENT_GUID, false))
            return true;
        if (checkIsOldSyncCache(syncOpenHelper, minUpdateTime, SaleItemTable.TABLE_NAME, SaleItemTable.PARENT_GUID, true))
            return true;
        if (checkIsOldSyncCache(syncOpenHelper, minUpdateTime, SaleAddonTable.TABLE_NAME, null, false))
            return true;
        if (checkIsOldSyncCache(syncOpenHelper, minUpdateTime, PaymentTransactionTable.TABLE_NAME, PaymentTransactionTable.PARENT_GUID, false))
            return true;
        if (checkIsOldSyncCache(syncOpenHelper, minUpdateTime, PaymentTransactionTable.TABLE_NAME, PaymentTransactionTable.PARENT_GUID, true))
            return true;
        if (checkIsOldSyncCache(syncOpenHelper, minUpdateTime, EmployeeTipsTable.TABLE_NAME, EmployeeTipsTable.PARENT_GUID, false))
            return true;
        if (checkIsOldSyncCache(syncOpenHelper, minUpdateTime, EmployeeTipsTable.TABLE_NAME, EmployeeTipsTable.PARENT_GUID, true))
            return true;
        if (checkIsOldSyncCache(syncOpenHelper, minUpdateTime, EmployeeCommissionsTable.TABLE_NAME, null, false))
            return true;
        if (checkIsOldSyncCache(syncOpenHelper, minUpdateTime, UnitTable.TABLE_NAME, null, false))
            return true;
        return false;
    }

    private boolean checkIsOldSyncCache(SyncOpenHelper syncOpenHelper, long minUpdateTime, String tableName, String parentIdColumn, boolean isChild) {
        Long minCacheUpdateTime = getMinCacheUpdateTime(syncOpenHelper, tableName, parentIdColumn, isChild);
        return minCacheUpdateTime != null && (minCacheUpdateTime < minUpdateTime);
    }

    private void clearSalesSyncCache(SyncOpenHelper syncOpenHelper) {
        //TODO: clear only old data?
        clearSyncCache(syncOpenHelper, new String[]{SaleOrderTable.TABLE_NAME,
                BillPaymentDescriptionTable.TABLE_NAME,
                SaleItemTable.TABLE_NAME,
                SaleAddonTable.TABLE_NAME,
                PaymentTransactionTable.TABLE_NAME,
                EmployeeTipsTable.TABLE_NAME,
                EmployeeCommissionsTable.TABLE_NAME,
                UnitTable.TABLE_NAME});
    }

    private void clearSyncCache(SyncOpenHelper syncOpenHelper, String[] tableNames) {
        Logger.w("[SYNC GAP] clearing sync cache, tables: " + Arrays.toString(tableNames));
        syncOpenHelper.clearTables(tableNames);
    }

    private Long getMinCacheUpdateTime(SyncOpenHelper syncOpenHelper, String tableName, String parentIdColumn, boolean isChild) {
        Cursor c;
        if (TextUtils.isEmpty(parentIdColumn))
            c = syncOpenHelper.getMinUpdateTime(tableName);
        else
            c = syncOpenHelper.getMinUpdateParentTime(tableName, parentIdColumn, isChild);

        Long minCacheUpdateTime = null;
        if (c.moveToFirst())
            minCacheUpdateTime = c.getLong(0);

        c.close();

        return minCacheUpdateTime;
    }

    private boolean isFirstSync(Context context) {
        if (!isTableEmpty(context, RegisterTable.URI_CONTENT, RegisterTable.ID))
            return false;
        if (!isTableEmpty(context, PrinterAliasTable.URI_CONTENT, PrinterAliasTable.GUID))
            return false;
        if (!isTableEmpty(context, CustomerTable.URI_CONTENT, CustomerTable.GUID))
            return false;
        if (!isTableEmpty(context, EmployeeTable.URI_CONTENT, EmployeeTable.ID))
            return false;
        if (!isTableEmpty(context, EmployeePermissionTable.URI_CONTENT, EmployeePermissionTable.USER_GUID))
            return false;
        if (!isTableEmpty(context, EmployeeTimesheetTable.URI_CONTENT, EmployeeTimesheetTable.GUID))
            return false;
        if (!isTableEmpty(context, ShiftTable.URI_CONTENT, ShiftTable.GUID))
            return false;
        if (!isTableEmpty(context, CashDrawerMovementTable.URI_CONTENT, CashDrawerMovementTable.GUID))
            return false;
        if (!isTableEmpty(context, TaxGroupTable.URI_CONTENT, TaxGroupTable.ID))
            return false;
        if (!isTableEmpty(context, DepartmentTable.URI_CONTENT, DepartmentTable.ID))
            return false;
        if (!isTableEmpty(context, CategoryTable.URI_CONTENT, CategoryTable.ID))
            return false;
        if (!isTableEmpty(context, ItemTable.URI_CONTENT, ItemTable.GUID))
            return false;
        if (!isTableEmpty(context, ModifierTable.URI_CONTENT, ModifierTable.MODIFIER_GUID))
            return false;
        if (!isTableEmpty(context, ModifierGroupTable.URI_CONTENT, ModifierGroupTable.GUID))
            return false;
        if (!isTableEmpty(context, ItemMovementTable.URI_CONTENT, ItemMovementTable.ITEM_GUID))
            return false;
        if (!isTableEmpty(context, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID))
            return false;
        if (!isTableEmpty(context, BillPaymentDescriptionTable.TABLE_NAME, BillPaymentDescriptionTable.GUID))
            return false;
        if (!isTableEmpty(context, SaleItemTable.TABLE_NAME, SaleItemTable.SALE_ITEM_GUID))
            return false;
        if (!isTableEmpty(context, SaleAddonTable.TABLE_NAME, SaleAddonTable.GUID))
            return false;
        if (!isTableEmpty(context, PaymentTransactionTable.TABLE_NAME, PaymentTransactionTable.GUID))
            return false;
        if (!isTableEmpty(context, CreditReceiptTable.TABLE_NAME, CreditReceiptTable.GUID))
            return false;
        if (!isTableEmpty(context, EmployeeTipsTable.TABLE_NAME, EmployeeTipsTable.GUID))
            return false;
        if (!isTableEmpty(context, EmployeeCommissionsTable.TABLE_NAME, EmployeeCommissionsTable.GUID))
            return false;
        if (!isTableEmpty(context, UnitTable.TABLE_NAME, UnitTable.ID))
            return false;
        if (!isTableEmpty(context, VariantItemTable.TABLE_NAME, VariantItemTable.ID))
            return false;
        if (!isTableEmpty(context, VariantSubItemTable.TABLE_NAME, VariantSubItemTable.ID))
            return false;
        if (!isTableEmpty(context, ItemMatrixTable.TABLE_NAME, ItemMatrixTable.ID))
            return false;
        if (!isTableEmpty(context, ComposerTable.TABLE_NAME, ComposerTable.ID))
            return false;
        if (!isTableEmpty(context, LoyaltyIncentiveTable.TABLE_NAME, LoyaltyIncentiveTable.GUID))
            return false;
        if (!isTableEmpty(context, LoyaltyIncentiveItemTable.TABLE_NAME, LoyaltyIncentiveItemTable.GUID))
            return false;
        if (!isTableEmpty(context, LoyaltyPlanTable.TABLE_NAME, LoyaltyPlanTable.GUID))
            return false;
        if (!isTableEmpty(context, LoyaltyIncentivePlanTable.TABLE_NAME, LoyaltyIncentivePlanTable.GUID))
            return false;

        return true;
    }

    private boolean isTableEmpty(Context context, String uriPath, String primaryKeyColumn) {
        Cursor c = ProviderAction.query(ShopProviderExt.contentUri(uriPath))
                .projection("count(" + primaryKeyColumn + ")")
                .perform(context);
        int count = 0;
        if (c.moveToFirst())
            count = c.getInt(0);
        c.close();
        return count == 0;
    }

    private void sendSyncSuccessful(SyncApi api, EmployeeModel employee) {
        try {
            RestCommand.Response resp = api.setRegisterLastUpdate(getApp().emailApiKey, SyncUploadRequestBuilder.getReqCredentials(employee, getApp()));
            if (resp == null || !resp.isSuccess()) {
                Logger.e("SyncCommand.sendSyncSuccessful(): failed, response: " + resp);
            }
        } catch (Exception e) {
            Logger.e("SyncCommand.sendSyncSuccessful(): failed", e);
        }
    }

    private long getServerCurrentTimestamp(SyncApi api, EmployeeModel employee) throws SyncException, SyncLockedException {
        Long currentServerTimestamp;
        GetCurrentTimestampResponse response;
        try {
            response = api.getCurrentTimestamp(getApp().emailApiKey, SyncUploadRequestBuilder.getReqCredentials(employee, getApp()));
        } catch (Exception e) {
            Logger.e("SyncCommand.getServerCurrentTimestamp(): failed", e);
            throw new SyncException();
        }
        Logger.d("SyncCommand.getServerCurrentTimestamp(): response: " + (response == null ? null : response.entity));
        if (response != null && response.isSyncLockedError()) {
            throw new SyncLockedException();
        }
        currentServerTimestamp = response == null ? null : response.getCurrentTimestamp();
        if (currentServerTimestamp == null) {
            Logger.e("SyncCommand.getServerCurrentTimestamp(): failed, empty response");
            throw new SyncException();
        }
        return currentServerTimestamp;
    }

    private Integer getServerSalesHistoryLimit(SyncApi api, EmployeeModel employee) throws SyncException, SyncLockedException {
        IntegerResponse response;
        try {
            response = api.getMaxHistoryLimit(getApp().emailApiKey, SyncUploadRequestBuilder.getReqCredentials(employee, getApp()));
        } catch (Exception e) {
            Logger.e("SyncCommand.getServerSalesHistoryLimit(): failed", e);
            throw new SyncException();
        }
        Logger.d("SyncCommand.getServerSalesHistoryLimit(): response: " + response);

        if (response != null && response.isSyncLockedError()) {
            Logger.e("SyncCommand.getServerSalesHistoryLimit(): failed, response: " + response);
            throw new SyncLockedException();
        }

        if (response == null || !response.isSuccess()) {
            Logger.e("SyncCommand.getServerSalesHistoryLimit(): failed, response: " + response);
            throw new SyncException();
        }

        if (response.entity == null) {
            Logger.w("SyncCommand.getServerSalesHistoryLimit(): empty response");
        }
        return response.entity;
    }

    private int localSync(Long minUpdateTime) throws SyncException, SyncInterruptedException {
        checkIsLoadingOldOrders();

        int count = 0;
        long registerId = getApp().getRegisterId();

        fireEvent(service, null, service.getString(R.string.sync_recalc_data), 0, 0);

        //TODO: improve
        syncOpenHelper.getWritableDatabase();
        ShopProviderExt.callMethod(service, Method.METHOD_ATTACH_SYNC_DB, null, null);
        try {
            ShopProviderExt.callMethod(service, Method.TRANSACTION_START, null, null);
            Logger.d("[SYNC] SyncCommand: transaction start");
            try {
                checkIsLoadingOldOrders();

                count += syncLocalSingleTable(service, ShopStore.UnitLabelTable.TABLE_NAME, UnitLabelTable.GUID);
                count += syncLocalSingleTable(service, RegisterTable.TABLE_NAME, RegisterTable.ID);
                count += syncLocalSingleTable(service, PrinterAliasTable.TABLE_NAME, PrinterAliasTable.GUID);
                count += syncLocalSingleTable(service, LoyaltyIncentiveTable.TABLE_NAME, LoyaltyIncentiveTable.GUID);
                count += syncLocalSingleTable(service, LoyaltyPlanTable.TABLE_NAME, LoyaltyPlanTable.GUID);
                count += syncLocalSingleTable(service, LoyaltyIncentivePlanTable.TABLE_NAME, LoyaltyIncentivePlanTable.GUID);
                count += syncLocalSingleTable(service, CustomerTable.TABLE_NAME, CustomerTable.GUID);

                //employee
                count += syncLocalSingleTable(service, EmployeeTable.TABLE_NAME, EmployeeTable.GUID);
                //TODO: check
                count += syncLocalSingleTable(service, EmployeePermissionTable.TABLE_NAME, EmployeePermissionTable.PERMISSION_ID);
                count += syncLocalSingleTable(service, EmployeeTimesheetTable.TABLE_NAME, EmployeeTimesheetTable.GUID);
                count += syncLocalSingleTable(service, ShiftTable.TABLE_NAME, ShiftTable.GUID);

                count += syncLocalSingleTable(service, CashDrawerMovementTable.TABLE_NAME, CashDrawerMovementTable.GUID);

                //inventory
                count += syncLocalSingleTable(service, TaxGroupTable.TABLE_NAME, TaxGroupTable.GUID);
                count += syncLocalSingleTable(service, DepartmentTable.TABLE_NAME, DepartmentTable.GUID);
                count += syncLocalSingleTable(service, CategoryTable.TABLE_NAME, CategoryTable.GUID);
                count += syncLocalSingleTable(service, ItemTable.TABLE_NAME, ItemTable.GUID);
                count += syncLocalSingleTable(service, ModifierTable.TABLE_NAME, ModifierTable.MODIFIER_GUID);
                count += syncLocalSingleTable(service, ModifierGroupTable.TABLE_NAME, ModifierGroupTable.GUID);
                count += syncLocalSingleTable(service, VariantItemTable.TABLE_NAME, VariantItemTable.GUID);
                count += syncLocalSingleTable(service, VariantSubItemTable.TABLE_NAME, VariantSubItemTable.GUID);
                count += syncLocalSingleTable(service, ItemMatrixTable.TABLE_NAME, ItemMatrixTable.GUID);
                count += syncLocalSingleTable(service, ComposerTable.TABLE_NAME, ComposerTable.ID);
                count += syncLocalSingleTable(service, ItemMovementTable.TABLE_NAME, ItemMovementTable.GUID);
                count += syncLocalSingleTable(service, LoyaltyIncentiveItemTable.TABLE_NAME, LoyaltyIncentiveItemTable.GUID);
                count += syncLocalSingleTable(service, CustomerLoyaltyPointsTable.TABLE_NAME, CustomerLoyaltyPointsTable.GUID);

                //sale


                count += syncLocalSingleTable(service, SaleOrderTable.TABLE_NAME, SaleOrderTable.GUID, SaleOrderTable.PARENT_ID, true);

                count += syncLocalSingleTable(service, BillPaymentDescriptionTable.TABLE_NAME, BillPaymentDescriptionTable.GUID, true);

                count += syncLocalSingleTable(service, SaleItemTable.TABLE_NAME, SaleItemTable.SALE_ITEM_GUID, SaleItemTable.PARENT_GUID, true);

                count += syncLocalSingleTable(service, SaleAddonTable.TABLE_NAME, SaleAddonTable.GUID, true);

                count += syncLocalSingleTable(service, PaymentTransactionTable.TABLE_NAME, PaymentTransactionTable.GUID, PaymentTransactionTable.PARENT_GUID, true);


                count += syncLocalSingleTable(service, CreditReceiptTable.TABLE_NAME, CreditReceiptTable.GUID);

                count += syncLocalSingleTable(service, EmployeeTipsTable.TABLE_NAME, EmployeeTipsTable.GUID, EmployeeTipsTable.PARENT_GUID, true);

                count += syncLocalSingleTable(service, EmployeeCommissionsTable.TABLE_NAME, EmployeeCommissionsTable.GUID, true);


                //inventory depended from sale
                count += syncLocalSingleTable(service, UnitTable.TABLE_NAME, UnitTable.ID);

                if (getApp().isSalesSyncGapOccurred() || getApp().isInvalidOrdersFound()) {
                    ArrayList<String> invalidOldActiveUnitOrders = getInvalidOldActiveUnitOrders(minUpdateTime);
                    boolean hasInvalidOrders = invalidOldActiveUnitOrders != null && !invalidOldActiveUnitOrders.isEmpty();

                    if (getApp().isSalesSyncGapOccurred() && hasInvalidOrders) {
                        getApp().setInvalidOrdersFound(true);
                        Logger.w("[INVALID ORDERS] invalid old active unit orders found - flag set ");
                    }

                    if (hasInvalidOrders)
                        fixInvalidOldActiveUnitOrders(invalidOldActiveUnitOrders);

                    checkIsLoadingOldOrders();
                }
                //end

                executeHook(RegisterTable.URI_CONTENT, RegisterTable.ID, registerId <= 0 ? null : String.valueOf(registerId), registerHookListener);
                checkIsLoadingOldOrders();
                executeHook(EmployeePermissionTable.URI_CONTENT, EmployeePermissionTable.USER_GUID, getApp().getOperatorGuid(), employeePermissionsHookListener);
                checkIsLoadingOldOrders();

                //mark DB as synced
                markRecordsAsLive(service);

                ShopProviderExt.callMethod(service, Method.TRANSACTION_COMMIT, null, null);
            } finally {
                ShopProviderExt.callMethod(service, Method.TRANSACTION_END, null, null);
                Logger.d("[SYNC] SyncCommand: transaction end");
            }
        } finally {
            ShopProviderExt.callMethod(service, Method.METHOD_DETACH_SYNC_DB, null, null);
        }

        registerHookListener.onFinish();
        employeePermissionsHookListener.onFinish();

        return count;
    }

    private ArrayList<String> getInvalidOldActiveUnitOrders(long minUpdateTime) {
        Cursor c = ProviderAction.query(ShopProvider.contentUri(OldActiveUnitOrdersQuery.CONTENT_PATH))
                .where("", minUpdateTime)
                .perform(service);

        if (c.getCount() == 0) {
            c.close();
            return null;
        }

        ArrayList<String> invalidOrderGuids = new ArrayList<String>(c.getCount());
        while (c.moveToNext()) {
            String orderGuid = c.getString(0);
            invalidOrderGuids.add(orderGuid);
        }
        c.close();

        Logger.w("[INVALID ORDERS] invalid old active unit orders found: " + invalidOrderGuids);

        return invalidOrderGuids;
    }

    private void fixInvalidOldActiveUnitOrders(ArrayList<String> invalidOldActiveUnitOrders) {
        if (invalidOldActiveUnitOrders == null || invalidOldActiveUnitOrders.isEmpty())
            return;

        ArrayList<String> orderGuids = new ArrayList<String>();
        int pos = 0;
        int count = 0;
        StringBuilder inBuilder = new StringBuilder();
        for (String orderGuid : invalidOldActiveUnitOrders) {
            orderGuids.add(orderGuid);

            if (orderGuids.size() == MAX_QUERY_PARAMETERS_COUNT || pos++ == (invalidOldActiveUnitOrders.size() - 1)) {
                inBuilder.setLength(0);
                inBuilder.append(" in (");
                for (int i = 0; i < orderGuids.size(); i++) {
                    if (i > 0)
                        inBuilder.append(',');
                    inBuilder.append('?');
                }
                inBuilder.append(')');

                //TODO: add check - old active order?
                count += ProviderAction.update(ShopProvider.contentUri(UnitTable.URI_CONTENT))
                        .value(UnitTable.SALE_ORDER_ID, null)
                        .where(UnitTable.SALE_ORDER_ID + inBuilder.toString(), orderGuids.toArray(new String[orderGuids.size()]))
                        .perform(service);

                orderGuids.clear();
            }
        }
        Logger.w("[INVALID ORDERS] invalid old active unit orders fixed, units unlinked: " + count);
    }

    private void executeHook(String uriPath, String guidColumn, String hookGuid, HookListener hookListener) {
        if (TextUtils.isEmpty(hookGuid) || hookListener == null)
            return;

        Cursor cursor = null;
        try {
            cursor = service.getContentResolver().query(ShopProvider.contentUri(uriPath), null,
                    guidColumn + " = ? AND " + ShopStore.DEFAULT_IS_DRAFT + " = ?", new String[]{hookGuid, "1"},
                    null);
            if (cursor.getCount() == 0) {
                return;
            }

            hookListener.onHook(cursor);
        } finally {
            if (cursor != null)
                cursor.close();
            if (!isManual)
                ShopProviderExt.callMethod(service, Method.TRANSACTION_YIELD, null, null);
        }
    }

    private void markRecordsAsLive(Context context) throws SyncInterruptedException {
        for (String uri : TABLES_URIS) {
            ProviderAction.update(ShopProvider.contentUri(uri))
                    .where(IBemaSyncTable.UPDATE_IS_DRAFT + " = ?", "1")
                    .value(IBemaSyncTable.UPDATE_IS_DRAFT, 0)
                    .perform(context);
            if (!isManual)
                ShopProviderExt.callMethod(service, Method.TRANSACTION_YIELD, null, null);
            checkIsLoadingOldOrders();
        }
    }

    private int syncTableWithChildren2(Context context, SyncApi2 api, String localTable, String guidColumn, String parentIdColumn, EmployeeModel employeeModel, long serverLastUpdateTime, Long minUpdateTime, boolean fillHistoryGep) throws SyncException, SyncLockedException, SyncInterruptedException {
        int count = syncSingleTable2(context, api, localTable, guidColumn, employeeModel, true, parentIdColumn, false, serverLastUpdateTime, minUpdateTime, true, fillHistoryGep, false, false);
        count += syncSingleTable2(context, api, localTable, guidColumn, employeeModel, true, parentIdColumn, true, serverLastUpdateTime, minUpdateTime, true, fillHistoryGep, false, false);
        return count;
    }

    private int syncSingleTable2(Context context, SyncApi2 api, String localTable, String guidColumn, EmployeeModel employeeModel, long serverLastUpdateTime) throws SyncException, SyncLockedException, SyncInterruptedException {
        return syncSingleTable2(context, api, localTable, guidColumn, employeeModel, false, null, false, serverLastUpdateTime, 0L, false, false, false, false);
    }

    private int syncSingleTable2(Context context, SyncApi2 api, String localTable, String guidColumn, EmployeeModel employeeModel, long serverLastUpdateTime, Long minUpdateTime, boolean fillHistoryGep) throws SyncException, SyncLockedException, SyncInterruptedException {
        return syncSingleTable2(context, api, localTable, guidColumn, employeeModel, false, null, false, serverLastUpdateTime, minUpdateTime, true, fillHistoryGep, false, false);
    }

    private int syncSingleTable2(Context context, SyncApi2 api, String localTable, String guidColumn, EmployeeModel employeeModel, long serverLastUpdateTime, Long minUpdateTime, boolean fillHistoryGep, boolean isSyncGap, boolean isFirstSync) throws SyncException, SyncLockedException, SyncInterruptedException {
        return syncSingleTable2(context, api, localTable, guidColumn, employeeModel, false, null, false, serverLastUpdateTime, minUpdateTime, true, fillHistoryGep, isSyncGap, isFirstSync);
    }

    private int syncSingleTable2(Context context,
                                 SyncApi2 api,
                                 String localTable,
                                 String guidColumn,
                                 EmployeeModel employeeModel,
                                 boolean supportParentChildRelations,
                                 String parentIdColumn,
                                 boolean isChild,
                                 long serverLastUpdateTime,
                                 Long minUpdateTime,
                                 boolean limitHistory,
                                 boolean fillHistoryGep,
                                 boolean isSyncGap,
                                 boolean isFirstSync) throws SyncException, SyncLockedException, SyncInterruptedException {

        fireEvent(context, localTable);
        TcrApplication app = TcrApplication.get();

        SyncSingleResponseHandler handler = new SyncSingleResponseHandler(
                syncOpenHelper,
                JdbcFactory.getConverter(localTable),
                localTable,
                serverLastUpdateTime
        );

        int step = 0;
        boolean hasNext = true;
        int size = 0;
        int pages = -1;
        while (hasNext) {
            step++;

            MaxUpdateTime updateTime = getMaxTimeSingleTable(context, syncOpenHelper, localTable, guidColumn, parentIdColumn, isChild);

            if (limitHistory && minUpdateTime != null) {
                if (!fillHistoryGep && (updateTime == null || updateTime.time < minUpdateTime)) {
                    updateTime = new MaxUpdateTime(minUpdateTime, null);
                }
            }

            try {
                JdbcConverter converter = JdbcFactory.getConverter(localTable);
                GetPagedArrayResponse resp = getResponse(api,
                        employeeModel, localTable, supportParentChildRelations, isChild, app, minUpdateTime, updateTime, converter,
                        fillHistoryGep, isSyncGap, isFirstSync);
                Logger.d("Resp = %s", resp);
                if (resp != null && resp.isSyncLockedError()) {
                    throw new SyncLockedException();
                }
                if (resp == null || !resp.isSuccess()) {
                    throw new SyncException(localTable);
                }
                if (pages == -1) {
                    pages = (resp.rows + PAGE_ROWS - 1) / PAGE_ROWS;
                }
                HandlerResult handlerResult = handler.handleResponse(resp);
                size += handlerResult.dataCount;
                if (handlerResult.dataCount > 0) {
                    //int pages = resp.rows / PAGE_ROWS + (resp.rows % PAGE_ROWS > 0 ? 1 : 0);
                    fireEvent(context, localTable, pages, step);
                }
                if (handlerResult.serverHasBeenUpdated) {
                    Logger.w("SyncCommand.syncSingleTable2(): server data has been updated; table: " + localTable);
                    serverHasBeenUpdated = true;
                }
                hasNext = handlerResult.dataCount > 0;
            } catch (SyncLockedException e) {
                throw e;
            } catch (SyncException e) {
                throw e;
            } catch (Exception e) {
                Logger.e("sync table " + localTable + " exception", e);
                throw new SyncException(localTable);
            }

            checkIsLoadingOldOrders();
        }

        return size;
    }

    private void syncOldActiveOrders(Context context, SyncApi2 api, EmployeeModel employeeModel,
                                     long minUpdateTime) throws SyncException, SyncLockedException {
        //TODO: fire specific event?
        fireEvent(context, SaleOrderTable.TABLE_NAME);
        TcrApplication app = TcrApplication.get();

        try {
            GetArrayResponse resp = makeOldActiveOrdersRequest(api, app.emailApiKey,
                    SyncUploadRequestBuilder.getReqCredentials(employeeModel, app),
                    Sync2GetRequestBuilder.getOldActiveOrdersRequest(minUpdateTime));
            Logger.d("Resp = %s", resp);
            if (resp != null && resp.isSyncLockedError()) {
                throw new SyncLockedException();
            }
            if (resp == null || !resp.isSuccess()) {
                throw new SyncException(SaleOrderTable.TABLE_NAME);
            }
            boolean hasData = new SyncOldActiveOrdersResponseHandler(syncOpenHelper).handleResponse(resp);
            if (hasData) {
                fireEvent(context, SaleOrderTable.TABLE_NAME, 1, 1);
            }
        } catch (SyncLockedException e) {
            throw e;
        } catch (SyncException e) {
            throw e;
        } catch (Exception e) {
            Logger.e("sync old active orders exception", e);
            throw new SyncException(SaleOrderTable.TABLE_NAME);
        }
    }

    private GetPagedArrayResponse getResponse(SyncApi2 api, EmployeeModel employeeModel, String localTable, boolean supportParentChildRelations, boolean isChild, TcrApplication app,
                                              Long minUpdateTime, MaxUpdateTime updateTime, JdbcConverter converter, boolean fillHistoryGep, boolean isSyncGap, boolean isFirstSync) throws JSONException, SyncException {
        JSONObject credentials = SyncUploadRequestBuilder.getReqCredentials(employeeModel, app);

        if (fillHistoryGep && minUpdateTime != null
                && (updateTime == null || updateTime.time < minUpdateTime)) {
            if (UnitTable.TABLE_NAME.equals(localTable)) {
                if (!isSyncGap || isFirstSync)
                    return makeUnitsRequest(api, app.emailApiKey,
                            credentials,
                            Sync2GetRequestBuilder.getHistoryLimitRequest(
                                    minUpdateTime,
                                    updateTime,
                                    converter.getGuidColumn(),
                                    PAGE_ROWS));
            } else if (ItemMovementTable.TABLE_NAME.equals(localTable)) {
                return makeMovementsRequest(api, app.emailApiKey,
                        credentials,
                        Sync2GetRequestBuilder.getHistoryLimitRequest(
                                minUpdateTime,
                                updateTime,
                                converter.getGuidColumn(),
                                PAGE_ROWS));
            } else {
                throw new IllegalArgumentException("fill history limit gap feature is not supported for table: " + localTable);
            }
        }

        return makeRequest(api, app.emailApiKey,
                credentials,
                Sync2GetRequestBuilder.getRequestFull(
                        converter.getTableName(),
                        updateTime,
                        converter.getGuidColumn(),
                        supportParentChildRelations ? converter.getParentGuidColumn() : null,
                        isChild,
                        PAGE_ROWS));
    }

    private int syncLocalSingleTable(Context context, String localTable, String idColumn) throws SyncException, SyncInterruptedException {
        return syncLocalSingleTable(context, localTable, idColumn, null, false);
    }

    private int syncLocalSingleTable(Context context, String localTable, String idColumn, boolean insertUpdate) throws SyncException, SyncInterruptedException {
        return syncLocalSingleTable(context, localTable, idColumn, null, insertUpdate);
    }

    private int syncLocalSingleTable(Context context, String localTable, String idColumn, String parentIdColumn, boolean insertUpdate) throws SyncException, SyncInterruptedException {
        if (!insertUpdate)
            ShopProviderExt.copyTableFromSyncDb(context, localTable, idColumn, parentIdColumn);
        else
            ShopProviderExt.copyUpdateTableFromSyncDb(context, localTable, idColumn, parentIdColumn);
        if (!isManual)
            ShopProviderExt.callMethod(service, Method.TRANSACTION_YIELD, null, null);
        checkIsLoadingOldOrders();

        //TODO: use drop instead?
        ShopProviderExt.callMethod(context, Method.METHOD_CLEAR_TABLE_IN_SYNC_DB, localTable, null);
        if (!isManual)
            ShopProviderExt.callMethod(service, Method.TRANSACTION_YIELD, null, null);
        checkIsLoadingOldOrders();

        return 0;
    }

    private GetPagedArrayResponse makeRequest(SyncApi2 api, String apiKey, JSONObject credentials, JSONObject entity) throws JSONException, SyncException {
        int retry = 0;
        while (retry++ < 5) {
            try {
                return api.download(apiKey, credentials, entity);
            } catch (RetrofitError e) {
                Logger.e("attempt: " + retry, e);
            }
        }
        throw new SyncException();
    }

    private GetPagedArrayResponse makeUnitsRequest(SyncApi2 api, String apiKey, JSONObject credentials, JSONObject entity) throws JSONException, SyncException {
        int retry = 0;
        while (retry++ < 5) {
            try {
                return api.downloadUnitsLimited(apiKey, credentials, entity);
            } catch (RetrofitError e) {
                Logger.e("attempt: " + retry, e);
            }
        }
        throw new SyncException();
    }

    private GetPagedArrayResponse makeMovementsRequest(SyncApi2 api, String apiKey, JSONObject credentials, JSONObject entity) throws JSONException, SyncException {
        int retry = 0;
        while (retry++ < 5) {
            try {
                return api.downloadMovementGroups(apiKey, credentials, entity);
            } catch (RetrofitError e) {
                Logger.e("attempt: " + retry, e);
            }
        }
        throw new SyncException();
    }

    private GetArrayResponse makeOldActiveOrdersRequest(SyncApi2 api, String apiKey, JSONObject credentials, JSONObject entity) throws JSONException, SyncException {
        int retry = 0;
        while (retry++ < 5) {
            try {
                return api.downloadOldActiveOrders(apiKey, credentials, entity);
            } catch (RetrofitError e) {
                Logger.e("attempt: " + retry, e);
            }
        }
        throw new SyncException();
    }

    private GetResponse makeShopInfoRequest(SyncApi2 api, String apiKey, JSONObject credentials) throws JSONException, SyncException {
        int retry = 0;
        while (retry++ < 5) {
            try {
                return api.downloadShopInfo(apiKey, credentials);
            } catch (RetrofitError e) {
                Logger.e("attempt: " + retry, e);
            }
        }
        throw new SyncException();
    }

    private boolean dbVersionCheck(EmployeeModel employee) throws SyncException {
        DBVersionCheckCommand.DBVersionCheckError dbVersionError = new DBVersionCheckCommand().sync(getApp(), employee.login, employee.password);
        if (dbVersionError == null)
            return true;

        switch (dbVersionError) {
            case INVALID_VERSION:
                Logger.e("SyncCommand.dbVersionCheck(): old db version");
                return false;
            default:
                Logger.e("SyncCommand.dbVersionCheck(): could not get db version!");
                throw new SyncException();
        }
    }

    private void syncWireless(Context context) {
        try {
            fireEvent(context, WirelessTable.TABLE_NAME, 1, 0);
            PrepaidUser prepaidUser = getApp().getPrepaidUser();
            if (!getApp().isUserLogin()) {
                prepaidUser = updatePrepaidUser(context, prepaidUser);
            }
            PrepaidUpdateProcessor.create()
                    .setVersionNumber(getApp().getShopPref().prepaidVersionId().get())
                    .setTransactionId(0)
                    .setUser(prepaidUser)
                    .setCallback(new IUpdateCallback() {
                        @Override
                        public void onError(String reason) {
                        }

                        @Override
                        public void onComplete(boolean upToDate) {
                        }
                    })
                    .build()
                    .checkSync(context, false);
        } catch (Exception e) {
            Logger.e("Wireless update failed", e);
        }
    }

    private PrepaidUser updatePrepaidUser(Context context, PrepaidUser prepaidUser) {
        Cursor cursor = ProviderAction.query(ShopProvider.getContentUri(RegisterTable.URI_CONTENT))
                .projection(RegisterTable.PREPAID_TID)
                .where(RegisterTable.REGISTER_SERIAL + " = ?", getApp().getRegisterSerial())
                .where(RegisterTable.STATUS + " <> ?", RegisterStatus.BLOCKED.ordinal())
                .perform(context);
        if (cursor.moveToFirst()) {
            int tid = cursor.getInt(0);
            prepaidUser = new PrepaidUser(prepaidUser.getPassword(), prepaidUser.getMid(), tid);
        }
        cursor.close();
        return prepaidUser;
    }

    private void fireEvent(Context context, String table) {
        fireEvent(context, table, null, 0, 0);
    }

    private void fireEvent(Context context, String table, int pages, int progress) {
        fireEvent(context, table, null, pages, progress);
    }

    private void fireEvent(Context context, String table, String dataLabel, int pages, int progress) {
        if (!isManual)
            return;

        Intent intent = new Intent(ACTION_SYNC_PROGRESS);
        intent.putExtra(EXTRA_TABLE, table);
        intent.putExtra(EXTRA_DATA_LABEL, dataLabel);
        intent.putExtra(EXTRA_PAGES, pages);
        intent.putExtra(EXTRA_PROGRESS, progress);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void fireCompleteEvent(Context context, boolean success, boolean isSyncLocked) {
        if (!isManual)
            return;

        Intent intent = new Intent(ACTION_SYNC_COMPLETED);
        intent.putExtra(EXTRA_SUCCESS, success);
        intent.putExtra(EXTRA_SYNC_LOCKED, isSyncLocked);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private Integer syncShopInfo(EmployeeModel employeeModel) throws SyncException, SyncLockedException {
        TcrApplication app = TcrApplication.get();
        SyncApi2 api = app.getRestAdapter().create(SyncApi2.class);

        fireEvent(service, null, service.getString(R.string.sync_shop_info), 0, 0);

        Integer salesHistoryLimit;
        try {
            GetResponse resp = makeShopInfoRequest(api, app.emailApiKey, SyncUploadRequestBuilder.getReqCredentials(employeeModel, app));
            if (resp != null && resp.isSyncLockedError()) {
                throw new SyncLockedException();
            }
            if (resp == null || !resp.isSuccess()) {
                Logger.e("can't parse shop", new RuntimeException());
                throw new SyncException();
            }
            JdbcJSONObject entity = resp.getEntity();
            syncBarcodePrefix(entity.getJSONArray("BARCODE_PREFIXES"));
            syncPrepaidTaxes(entity.getJSONArray("PREPAID_ITEM_TAXES"));
            syncActivationCarriers(entity.getJSONArray("ACTIVATION_CARRIERS"));
            salesHistoryLimit = syncShop(entity.getJSONObject("SHOP"));

            if (app.isFreemium()) {
                Logger.d("[Freemium] Getting settings for current plan");
                PlanOptionsResponse freemiumOptions = api.getFreemiumOptions(app.emailApiKey,
                        SyncUploadRequestBuilder.getReqCredentials(employeeModel, app));
                freemiumOptions.get().save();
            }
        } catch (SyncException e) {
            throw e;
        } catch (SyncLockedException e) {
            throw e;
        } catch (Exception e) {
            Logger.e("Can't sync shop info", e);
            throw new SyncException();
        }
        return salesHistoryLimit;
    }

    private Integer syncShop(JdbcJSONObject shop) throws SyncException {
        if (shop == null) {
            Logger.e("can't parse shop", new RuntimeException());
            throw new SyncException();
        }
        ShopInfo info;
        Integer salesHistoryLimit;
        try {
            info = ShopInfoViewJdbcConverter.read(shop);
            salesHistoryLimit = ShopInfoViewJdbcConverter.getSalesHistoryLimit(shop);
        } catch (JSONException e) {
            Logger.e("can't parse shop", e);
            throw new SyncException();
        }
        boolean oldTipsEnabled = getApp().isTipsEnabled();
        getApp().saveShopInfo(info);
        checkTipsEnabled(oldTipsEnabled, info.tipsEnabled);
        return salesHistoryLimit;
    }

    private void checkTipsEnabled(boolean oldTipsEnabled, final boolean newTipsEnabled) {
        if (oldTipsEnabled == newTipsEnabled)
            return;

        if (isManual) {
            getApp().getShopPref().tipsEnabledWasChanged().put(true);
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(service, newTipsEnabled ? R.string.warning_message_tips_enabled : R.string.warning_message_tips_disabled, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void syncBarcodePrefix(JdbcJSONArray jsonArray) throws SyncException {
        if (jsonArray == null) {
            Logger.e("can't sync barcodeprefix", new RuntimeException());
            throw new SyncException();
        }
        BarcodePrefixes barcodePrefixes = null;
        try {
            barcodePrefixes = BarcodePrefixJdbcConverter.read(jsonArray);
        } catch (JSONException e) {
            Logger.e("can't sync barcodeprefix", e);
            throw new SyncException();
        }
        ((TcrApplication) service.getApplicationContext()).saveBarcodePrefixes(barcodePrefixes);
    }

    private void syncPrepaidTaxes(JdbcJSONArray jsonArray) throws SyncException {
        if (jsonArray == null) {
            Logger.e("can't sync prepaid taxes", new RuntimeException());
            throw new SyncException();
        }
        HashMap<Broker, BigDecimal> taxes = null;
        try {
            taxes = PrepaidTaxJdbcConverter.read(jsonArray);
        } catch (JSONException e) {
            Logger.e("can't sync prepaid taxes", e);
            throw new SyncException();
        }
        ((TcrApplication) service.getApplicationContext()).savePrepaidTaxes(taxes);
    }

    private void syncActivationCarriers(JdbcJSONArray jsonArray) throws SyncException {
        if (jsonArray == null) {
            Logger.e("can't sync activation carriers", new RuntimeException());
            throw new SyncException();
        }

        String localTable = ActivationCarrierTable.TABLE_NAME;
        SyncSingleResponseHandler handler = new SyncSingleResponseHandler(null,
                JdbcFactory.getConverter(localTable),
                null, 0L
        );
        ArrayList<ContentValues> values;
        try {
            values = handler.parseResponse(new GetArrayResponse(null, null, jsonArray));
        } catch (JSONException e) {
            Logger.e("parse ActivationCarriers error", e);
            throw new SyncException(localTable);
        }

        if (values == null) {
            Logger.e("can't sync activation carriers: empty response");
            throw new SyncException(localTable);
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>(values.size() + 1);
        ops.add(ContentProviderOperation.newDelete(ShopProvider.contentUri(ActivationCarrierTable.URI_CONTENT)).build());
        for (ContentValues value : values) {
            ops.add(ContentProviderOperation.newInsert(ShopProvider.contentUri(ActivationCarrierTable.URI_CONTENT))
                    .withValues(value)
                    .build());
        }
        try {
            service.getContentResolver().applyBatch(ShopProvider.AUTHORITY, ops);
        } catch (Exception e) {
            Logger.e("insert ActivationCarriers error", e);
            throw new SyncException(localTable);
        }
    }

    private void checkAutoSettlement(boolean wasTipsEnabled, String oldAUtoSettlementTime) {
        TcrApplication app = getApp();
        String autoSettlementTime = app.getAutoSettlementTime();
        boolean dataChanged = (!wasTipsEnabled && app.isTipsEnabled())
                || ((oldAUtoSettlementTime == null && autoSettlementTime != null) || (oldAUtoSettlementTime != null && !oldAUtoSettlementTime.equals(app.getAutoSettlementTime())));
        boolean isAutoSettlementForceEnabled = app.getShopPref().isAutoSettlementForceEnabled().get();
        boolean isAutoSettlementTimeForceUpdated = app.getShopPref().isAutoSettlementTimeForceUpdated().get();
        if (!app.isTipsEnabled()) {
            Logger.d("SyncCommand.checkAutoSettlement(): tips are disabled - flags dropped");
            app.getShopPref().isAutoSettlementForceEnabled().put(false);
            app.getShopPref().isAutoSettlementTimeForceUpdated().put(false);
            return;
        }
        if (!dataChanged && isAutoSettlementForceEnabled && isAutoSettlementTimeForceUpdated) {
            Logger.d("SyncCommand.checkAutoSettlement(): no need to force enable");
            return;
        }
        if (dataChanged) {
            Logger.d("SyncCommand.checkAutoSettlement(): need to force enable - flags dropped");
            app.getShopPref().isAutoSettlementForceEnabled().put(false);
            app.getShopPref().isAutoSettlementTimeForceUpdated().put(false);
            isAutoSettlementForceEnabled = false;
            isAutoSettlementTimeForceUpdated = false;
        }

        fireEvent(service, null, service.getString(R.string.sync_auto_settlement), 1, 0);

        User blackstoneUser = app.getBlackStoneUser();
        if (!getApp().isUserLogin())
            blackstoneUser = updateBlackstoneUser(service, blackstoneUser);
        if (!blackstoneUser.isValid()) {
            Logger.e("SyncCommand.checkAutoSettlement(): blackstone user is invalid!");
            return;
        }
        if (TextUtils.isEmpty(autoSettlementTime)) {
            Logger.e("SyncCommand.checkAutoSettlement(): auto settlement time is empty!");
            return;
        }

        boolean success = true;
        if (!isAutoSettlementForceEnabled) {
            success = new BlackSetAutomaticBatchCloseCommand().sync(service, new AutomaticBatchCloseRequest().setUser(blackstoneUser).setTime(autoSettlementTime));
            if (success) {
                app.getShopPref().isAutoSettlementForceEnabled().put(true);
            } else {
                Logger.e("SyncCommand.checkAutoSettlement(): failed to force enable auto settlement!");
            }
        }
        if (success && !isAutoSettlementTimeForceUpdated) {
            success = new BlackUpdateAutomaticHourToCloseBatchCommand().sync(service, new AutomaticBatchCloseRequest().setUser(blackstoneUser).setTime(autoSettlementTime));
            if (success) {
                app.getShopPref().isAutoSettlementTimeForceUpdated().put(true);
            } else {
                Logger.e("SyncCommand.checkAutoSettlement(): failed to force update time of auto settlement!");
            }
        }

        if (success) {
            fireEvent(service, null, service.getString(R.string.sync_auto_settlement), 1, 1);
        }
    }

    private void syncPAXMerchantInfo() {
        boolean shouldSync = isManual;
        if (!shouldSync) {
            Logger.d("SyncCommand.syncPAXMerchantInfo(): will not sync: is manual:  " + isManual + ", is login: " + !getApp().isUserLogin());
            return;
        }

        fireEvent(service, null, service.getString(R.string.sync_pax_merchant_info), 1, 0);

        boolean paxConfigured = getApp().isPaxConfigured();
        if (!paxConfigured) {
            Logger.e("SyncCommand.syncPAXMerchantInfo(): failed: PAX not configured!");
            return;
        }

        MerchantDetails merchantDetails = new PaxBlackstoneMIDownloadCommand().sync(service, PaxModel.get());
        if (merchantDetails == null) {
            Logger.e("SyncCommand.syncPAXMerchantInfo(): failed: command failed!");
            return;
        }

        fireEvent(service, null, service.getString(R.string.sync_pax_merchant_info), 1, 1);
    }

    private User updateBlackstoneUser(Context context, User blackstoneUser) {
        Cursor cursor = ProviderAction.query(ShopProvider.getContentUri(RegisterTable.URI_CONTENT))
                .projection(RegisterTable.BLACKSTONE_PAYMENT_CID)
                .where(RegisterTable.REGISTER_SERIAL + " = ?", getApp().getRegisterSerial())
                .where(RegisterTable.STATUS + " <> ?", RegisterStatus.BLOCKED.ordinal())
                .perform(context);
        if (cursor.moveToFirst()) {
            int cid = cursor.getInt(0);
            blackstoneUser = new User(blackstoneUser.getUsername(), blackstoneUser.getPassword(), blackstoneUser.getMid(), cid, blackstoneUser.getAppkey(), blackstoneUser.getApptype());
        }
        cursor.close();
        return blackstoneUser;
    }

    private String getErrorString(String localTable) {
        if (ItemTable.TABLE_NAME.equals(localTable)) {
            return service.getString(R.string.sync_err_items);
        } else if (SaleOrderTable.TABLE_NAME.equals(localTable)) {
            return service.getString(R.string.sync_err_sale_orders);
        } else if (SaleItemTable.TABLE_NAME.equals(localTable)) {
            return service.getString(R.string.sync_err_sale_order_items);
        }
        return service.getString(R.string.sync_err_unknown);
    }

    public static class MaxUpdateTime {
        public final long time;
        public final String guid;

        public MaxUpdateTime(long time, String guid) {
            this.time = time;
            this.guid = guid;
        }
    }

    private static MaxUpdateTime getMaxTimeSingleTable(Context context, SyncOpenHelper syncOpenHelper, String tableName, String id, String parentIdColumn, boolean isChild) {
        boolean hasChildren = parentIdColumn != null;
        String[] args;
        if (!hasChildren) {
            args = new String[]{tableName, id};
        } else {
            args = new String[]{tableName, id, parentIdColumn, isChild ? "not null" : "null"};
        }
        Table table = Table.getTable(tableName, !isChild);


        MaxUpdateTime mainMaxUpdateTime = getMaxTimeInner(context, table);
        MaxUpdateTime syncMaxUpdateTime = getMaxTimeSyncInner(syncOpenHelper, hasChildren, args);
        MaxUpdateTime maxUpdateTime = mainMaxUpdateTime;
        if (mainMaxUpdateTime == null)
            maxUpdateTime = syncMaxUpdateTime;
        else if (syncMaxUpdateTime != null && syncMaxUpdateTime.time > mainMaxUpdateTime.time)
            maxUpdateTime = syncMaxUpdateTime;
        return maxUpdateTime;
    }

    private static MaxUpdateTime getMaxTimeSyncInner(SyncOpenHelper syncOpenHelper, boolean hasChildren, String[] args) {
        Cursor c = !hasChildren ? syncOpenHelper.getMaxUpdateTime(args) : syncOpenHelper.getMaxUpdateParentTime(args);

        MaxUpdateTime time = null;
        if (c.moveToFirst()) {
            time = new MaxUpdateTime(c.getLong(0), c.getString(1));
        }
        c.close();
        return time;
    }

    private static MaxUpdateTime getMaxTimeInner(Context context, Table table) {
        Cursor c = context.getContentResolver().query(
                ShopProvider.contentUri(UpdateTimeTable.URI_CONTENT),
                new String[]{UpdateTimeTable.UPDATE_TIME, UpdateTimeTable.GUID},
                UpdateTimeTable.TABLE_ID + " = ?", new String[]{String.valueOf(_enum(table))},
                null);
        MaxUpdateTime time = null;
        if (c.moveToFirst()) {
            time = new MaxUpdateTime(c.getLong(0), c.getString(1));
        }
        c.close();
        return time;
    }

    private HookListener<RegisterModel> registerHookListener = new HookListener<RegisterModel>() {

        private RegisterModel model;

        @Override
        public void onHook(RegisterModel model) {
            this.model = model;
        }

        @Override
        protected RegisterModel getModel(Cursor c) {
            return new RegisterModel(c);
        }

        @Override
        public void onFinish() {
            if (model == null)
                return;

            getApp().setUsers(model.prepaidTid, model.blackstonePaymentCid);
        }
    };

    private HookListener<EmployeePermissionModel> employeePermissionsHookListener = new HookListener<EmployeePermissionModel>() {

        private Set<Permission> enabledPermissions;
        private Set<Permission> disabledPermissions;

        @Override
        public void onHook(EmployeePermissionModel model) {
            Permission permission = Permission.valueOfOrNull(model.permissionId);
            if (permission == null)
                return;

            if (model.enabled) {
                if (enabledPermissions == null)
                    enabledPermissions = new HashSet<Permission>();
                enabledPermissions.add(permission);
            } else {
                if (disabledPermissions == null)
                    disabledPermissions = new HashSet<Permission>();
                disabledPermissions.add(permission);
            }
        }

        @Override
        protected EmployeePermissionModel getModel(Cursor c) {
            return new EmployeePermissionModel(c);
        }

        @Override
        public void onFinish() {
            if (enabledPermissions == null && disabledPermissions == null)
                return;

            getApp().updateOperatorPermissions(enabledPermissions, disabledPermissions);
        }
    };

    public static class SyncException extends Exception {

        public String localTable;

        private SyncException() {

        }

        private SyncException(String localTable) {
            this.localTable = localTable;
        }
    }

    public static class DBVersionCheckException extends Exception {

    }

    public static class OfflineException extends Exception {

    }

    public static class SyncInconsistentException extends Exception {

    }

    public static class SyncLockedException extends Exception {

    }

    public static class SyncInterruptedException extends Exception {

        public SyncInterruptedException(String message) {
            super(message);
        }

    }

    public static abstract class HookListener<T extends IValueModel> {

        void onHook(Cursor c) {
            while (c.moveToNext()) {
                onHook(getModel(c));
            }
        }

        protected abstract void onHook(T model);

        protected abstract T getModel(Cursor c);

        abstract void onFinish();
    }

    public enum Table {
        //NOTE: don't change order - stored as int in db
        REGISTER(RegisterTable.TABLE_NAME, true),
        PRINTER_ALIAS(PrinterAliasTable.TABLE_NAME, true),
        CASH_DRAWER_MOVEMENT(CashDrawerMovementTable.TABLE_NAME, true),
        CUSTOMER(CustomerTable.TABLE_NAME, true),
        EMPLOYEE(EmployeeTable.TABLE_NAME, true),
        EMPLOYEE_PERMISSION(EmployeePermissionTable.TABLE_NAME, true),
        EMPLOYEE_TIMESHEET(EmployeeTimesheetTable.TABLE_NAME, true),
        SHIFT(ShiftTable.TABLE_NAME, true),
        TAX_GROUP(TaxGroupTable.TABLE_NAME, true),
        DEPARTMENT(DepartmentTable.TABLE_NAME, true),
        CATEGORY(CategoryTable.TABLE_NAME, true),
        ITEM(ItemTable.TABLE_NAME, true),

        MODIFIER_GROUP(ShopStore.ModifierGroupTable.TABLE_NAME, true),
        MODIFIER(ModifierTable.TABLE_NAME, true),
        VARIANT(VariantItemTable.TABLE_NAME, true),
        SUB_VARIANT(VariantSubItemTable.TABLE_NAME, true),
        MATRIX(ItemMatrixTable.TABLE_NAME, true),
        ITEM_MOVEMENT(ItemMovementTable.TABLE_NAME, true),
        UNIT(UnitTable.TABLE_NAME, true),
        SALE_ORDER(SaleOrderTable.TABLE_NAME, true),
        REFUND_ORDER(SaleOrderTable.TABLE_NAME, false),
        SALE_ITEM(SaleItemTable.TABLE_NAME, true),
        REFUND_ITEM(SaleItemTable.TABLE_NAME, false),
        SALE_ADDON(SaleAddonTable.TABLE_NAME, true),
        PAYMENT_TRANSACTION(PaymentTransactionTable.TABLE_NAME, true),
        REFUND_PAYMENT_TRANSACTION(PaymentTransactionTable.TABLE_NAME, false),
        BILL_PAYMENT_DESCRIPTION(BillPaymentDescriptionTable.TABLE_NAME, true),
        CREDIT_RECEIPT(CreditReceiptTable.TABLE_NAME, true),
        EMPLOYEE_TIPS(EmployeeTipsTable.TABLE_NAME, true),
        REFUND_EMPLOYEE_TIPS(EmployeeTipsTable.TABLE_NAME, false),
        EMPLOYEE_COMMISSIONS(EmployeeCommissionsTable.TABLE_NAME, true),
        COMPOSER(ShopStore.ComposerTable.TABLE_NAME, true),
        UNIT_LABEL(ShopStore.UnitLabelTable.TABLE_NAME, true),
        LOYALTY_INCENTIVE(LoyaltyIncentiveTable.TABLE_NAME, true),
        LOYALTY_INCENTIVE_ITEM(LoyaltyIncentiveItemTable.TABLE_NAME, true),
        LOYALTY_PLAN(LoyaltyPlanTable.TABLE_NAME, true),
        LOYALTY_INCENTIVE_PLAN(LoyaltyIncentivePlanTable.TABLE_NAME, true),
        CUSTOMER_LOYALTY_POINTS(CustomerLoyaltyPointsTable.TABLE_NAME, true);

        public final String tableName;
        public final boolean isParent;

        Table(String tableName, boolean isParent) {
            this.tableName = tableName;
            this.isParent = isParent;
        }

        public static Table getTable(String tableName, boolean isParent) {
            for (Table table : Table.values()) {
                if (table.tableName.equals(tableName) && table.isParent == isParent)
                    return table;
            }
            throw new IllegalArgumentException("unknown sync table: tableName: " + tableName + ", isParent: " + isParent);
        }
    }

}
