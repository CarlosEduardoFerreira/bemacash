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
import com.kaching123.tcr.commands.payment.pax.PaxMIDownloadCommand;
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
import com.kaching123.tcr.fragment.dialog.SyncWaitDialogFragment;
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
import com.kaching123.tcr.model.RegisterModel;
import com.kaching123.tcr.model.RegisterModel.RegisterStatus;
import com.kaching123.tcr.model.payment.blackstone.payment.User;
import com.kaching123.tcr.model.payment.blackstone.payment.request.AutomaticBatchCloseRequest;
import com.kaching123.tcr.model.payment.blackstone.prepaid.Broker;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PrepaidUser;
import com.kaching123.tcr.notification.NotificationHelper;
import com.kaching123.tcr.processor.PrepaidUpdateProcessor;
import com.kaching123.tcr.processor.PrepaidUpdateProcessor.IUpdateCallback;
import com.kaching123.tcr.service.response.SyncResponseHandler.HandlerResult;
import com.kaching123.tcr.service.response.SyncSingleResponseHandler;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopProviderExt;
import com.kaching123.tcr.store.ShopProviderExt.Method;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ActivationCarrierTable;
import com.kaching123.tcr.store.ShopStore.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopStore.CashDrawerMovementTable;
import com.kaching123.tcr.store.ShopStore.CategoryTable;
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
import com.kaching123.tcr.store.ShopStore.MaxUpdateTableTimeParentRelationsQuery;
import com.kaching123.tcr.store.ShopStore.MaxUpdateTableTimeQuery;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;
import com.kaching123.tcr.store.ShopStore.RegisterTable;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.ShiftTable;
import com.kaching123.tcr.store.ShopStore.TaxGroupTable;
import com.kaching123.tcr.store.ShopStore.UnitTable;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import retrofit.RetrofitError;

public class SyncCommand implements Runnable {

    private static final int PAGE_ROWS = 1800;

    private static final int FINALIZE_SYNC_RETRIES = 3;

    public static String ACTION_SYNC_PROGRESS = "com.kaching123.tcr.service.ACTION_SYNC_PROGRESS";
    public static String ACTION_SYNC_COMPLETED = "com.kaching123.tcr.service.ACTION_SYNC_COMPLETED";
    public static String EXTRA_TABLE = "table";
    public static String EXTRA_PAGES = "pages";
    public static String EXTRA_PROGRESS = "progress";
    public static String EXTRA_DATA_LABEL = "data_label";
    public static String EXTRA_SUCCESS = "success";

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
            ItemMovementTable.URI_CONTENT,
            UnitTable.URI_CONTENT,

            SaleOrderTable.URI_CONTENT,
            SaleItemTable.URI_CONTENT,
            SaleAddonTable.URI_CONTENT,
            PaymentTransactionTable.URI_CONTENT,
            BillPaymentDescriptionTable.URI_CONTENT,
            CreditReceiptTable.URI_CONTENT,
            EmployeeTipsTable.URI_CONTENT,
            EmployeeCommissionsTable.URI_CONTENT
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

        getApp().lockOnTrainingMode();
        try {
            count = syncNow(getApp().getOperator(), getApp().getShopId());
        } catch (SyncException e) {
            error = getErrorString(e.localTable);
        } catch (DBVersionCheckException e) {
            error = getErrorString(null);
        } catch (OfflineException e) {
            error = getErrorString(null);
            isOffline = true;
        } catch (SyncInconsistentException e) {
            error = service.getString(R.string.error_message_sync_inconsistent);
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
        fireCompleteEvent(service, !failure);
        Logger.d("SyncCommand end");
    }

    private static Handler handler = new Handler();

    public int syncNow(final EmployeeModel employee, final long shopId) throws SyncException, DBVersionCheckException, OfflineException, SyncInconsistentException {
        if (!getApp().isTrainingMode() && !Util.isNetworkAvailable(service)) {
            Logger.e("SyncCommand: NO CONNECTION");
            setOfflineMode(true);
            throw new OfflineException();
        }
        try {
            int result = syncNowInner(employee, shopId);
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

    private int syncNowInner(final EmployeeModel employee, final long shopId) throws SyncException, DBVersionCheckException, SyncInconsistentException {
        if (getApp().isTrainingMode()) {
            syncPAXMerchantInfo();
            syncWireless(service);
            return 0;
        }

        int count = 0;
        // download date from our amazon web server - start

        TcrApplication app = TcrApplication.get();
        syncOpenHelper = app.getSyncOpenHelper();

        SyncApi api = getApp().getRestAdapter().create(SyncApi.class);
        SyncApi2 api2 = app.getRestAdapter().create(SyncApi2.class);
        try {
            int retriesCount = FINALIZE_SYNC_RETRIES;
            do {
                long serverLastTimestamp = getServerCurrentTimestamp(api, employee);
                Logger.e("SyncCommand.syncNowInner(): attempt #" + (FINALIZE_SYNC_RETRIES - retriesCount + 1) + "; serverLastTimestamp: " + serverLastTimestamp);
                serverHasBeenUpdated = false;
                count += syncSingleTable2(service, api2, RegisterTable.URI_CONTENT, RegisterTable.TABLE_NAME, RegisterTable.ID, employee, serverLastTimestamp);
                count += syncSingleTable2(service, api2, PrinterAliasTable.URI_CONTENT, PrinterAliasTable.TABLE_NAME, PrinterAliasTable.GUID, employee, serverLastTimestamp);
                count += syncSingleTable2(service, api2, CustomerTable.URI_CONTENT, CustomerTable.TABLE_NAME, CustomerTable.GUID, employee, serverLastTimestamp);

                //employee
                count += syncSingleTable2(service, api2, EmployeeTable.URI_CONTENT, EmployeeTable.TABLE_NAME, EmployeeTable.GUID, employee, serverLastTimestamp);
                count += syncSingleTable2(service, api2, EmployeePermissionTable.URI_CONTENT, EmployeePermissionTable.TABLE_NAME, EmployeePermissionTable.PERMISSION_ID, employee, serverLastTimestamp);
                count += syncSingleTable2(service, api2, EmployeeTimesheetTable.URI_CONTENT, EmployeeTimesheetTable.TABLE_NAME, EmployeeTimesheetTable.GUID, employee, serverLastTimestamp);
                count += syncSingleTable2(service, api2, ShiftTable.URI_CONTENT, ShiftTable.TABLE_NAME, ShiftTable.GUID, employee, serverLastTimestamp);

                count += syncSingleTable2(service, api2, CashDrawerMovementTable.URI_CONTENT, CashDrawerMovementTable.TABLE_NAME, CashDrawerMovementTable.GUID, employee, serverLastTimestamp);

                //inventory
                count += syncSingleTable2(service, api2, TaxGroupTable.URI_CONTENT, TaxGroupTable.TABLE_NAME, TaxGroupTable.GUID, employee, serverLastTimestamp);
                count += syncSingleTable2(service, api2, DepartmentTable.URI_CONTENT, DepartmentTable.TABLE_NAME, DepartmentTable.GUID, employee, serverLastTimestamp);
                count += syncSingleTable2(service, api2, CategoryTable.URI_CONTENT, CategoryTable.TABLE_NAME, CategoryTable.GUID, employee, serverLastTimestamp);
                count += syncSingleTable2(service, api2, ItemTable.URI_CONTENT, ItemTable.TABLE_NAME, ItemTable.GUID, employee, serverLastTimestamp);
                count += syncSingleTable2(service, api2, ModifierTable.URI_CONTENT, ModifierTable.TABLE_NAME, ModifierTable.MODIFIER_GUID, employee, serverLastTimestamp);
                count += syncSingleTable2(service, api2, ItemMovementTable.URI_CONTENT, ItemMovementTable.TABLE_NAME, ItemMovementTable.GUID, employee, serverLastTimestamp);

                //sale
                count += syncSingleTable2(service, api2, BillPaymentDescriptionTable.URI_CONTENT, BillPaymentDescriptionTable.TABLE_NAME, BillPaymentDescriptionTable.GUID, employee, serverLastTimestamp);

                count += syncTableWithChildren2(service, api2,
                        SaleOrderTable.URI_CONTENT, SaleOrderTable.TABLE_NAME,
                        SaleOrderTable.GUID, SaleOrderTable.PARENT_ID,
                        employee, serverLastTimestamp);

                count += syncTableWithChildren2(service, api2,
                        SaleItemTable.URI_CONTENT, SaleItemTable.TABLE_NAME,
                        SaleItemTable.SALE_ITEM_GUID, SaleItemTable.PARENT_GUID,
                        employee, serverLastTimestamp);

                count += syncSingleTable2(service, api2, SaleAddonTable.URI_CONTENT, SaleAddonTable.TABLE_NAME, SaleAddonTable.GUID, employee, serverLastTimestamp);

                count += syncTableWithChildren2(service, api2,
                        PaymentTransactionTable.URI_CONTENT, PaymentTransactionTable.TABLE_NAME,
                        PaymentTransactionTable.GUID, PaymentTransactionTable.PARENT_GUID,
                        employee, serverLastTimestamp);


                count += syncSingleTable2(service, api2, CreditReceiptTable.URI_CONTENT, CreditReceiptTable.TABLE_NAME, CreditReceiptTable.GUID, employee, serverLastTimestamp);
                count += syncTableWithChildren2(service, api2,
                        EmployeeTipsTable.URI_CONTENT, EmployeeTipsTable.TABLE_NAME,
                        EmployeeTipsTable.GUID, EmployeeTipsTable.PARENT_GUID,
                        employee, serverLastTimestamp);

                count += syncSingleTable2(service, api2, EmployeeCommissionsTable.URI_CONTENT, EmployeeCommissionsTable.TABLE_NAME, EmployeeCommissionsTable.GUID, employee, serverLastTimestamp);

                //inventory depended from sale
                count += syncSingleTable2(service, api2, UnitTable.URI_CONTENT, UnitTable.TABLE_NAME, UnitTable.ID, employee, serverLastTimestamp);
                //end
            } while (serverHasBeenUpdated && --retriesCount > 0);

            if (serverHasBeenUpdated) {
                Logger.e("SyncCommand failed, couldn't finalize sync, server's data has been modified");
                throw new SyncInconsistentException();
            }

            //write data from extra db to main db on success, in transaction, making rows alive
            localSync();
        } finally {
            syncOpenHelper.close();
        }

        boolean wasTipsEnabled = getApp().isTipsEnabled();
        String oldAutoSettlementTime = getApp().getAutoSettlementTime();

        syncShopInfo(employee);

        checkAutoSettlement(wasTipsEnabled, oldAutoSettlementTime);
        syncPAXMerchantInfo();

        // download date from our amazon web server - end

        //go to the blackstone api to refresh cache
        syncWireless(service);

        sendSyncSuccessful(api, employee);

        return count;
    }

    private void sendSyncSuccessful(SyncApi api, EmployeeModel employee) {
        try {
            api.setRegisterLastUpdate(getApp().emailApiKey, SyncUploadRequestBuilder.getReqCredentials(employee, getApp()));
        } catch (Exception e) {
            Logger.e("SyncCommand.sendSyncSuccessful(): failed", e);
        }
    }

    private long getServerCurrentTimestamp(SyncApi api, EmployeeModel employee) throws SyncException {
        Long currentServerTimestamp;
        GetCurrentTimestampResponse response;
        try {
            response = api.getCurrentTimestamp(getApp().emailApiKey, SyncUploadRequestBuilder.getReqCredentials(employee, getApp()));
        } catch (Exception e) {
            Logger.e("SyncCommand.getServerCurrentTimestamp(): failed", e);
            throw new SyncException();
        }
        Logger.d("SyncCommand.getServerCurrentTimestamp(): response: " + (response == null ? null : response.entity));
        currentServerTimestamp = response == null ? null : response.getCurrentTimestamp();
        if (currentServerTimestamp == null) {
            Logger.e("SyncCommand.getServerCurrentTimestamp(): failed, empty response");
            throw new SyncException();
        }
        return currentServerTimestamp;
    }

    private int localSync() throws SyncException {
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
                count += syncLocalSingleTable(service, RegisterTable.TABLE_NAME);
                count += syncLocalSingleTable(service, PrinterAliasTable.TABLE_NAME);
                count += syncLocalSingleTable(service, CustomerTable.TABLE_NAME);

                //employee
                count += syncLocalSingleTable(service, EmployeeTable.TABLE_NAME);
                count += syncLocalSingleTable(service, EmployeePermissionTable.TABLE_NAME);
                count += syncLocalSingleTable(service, EmployeeTimesheetTable.TABLE_NAME);
                count += syncLocalSingleTable(service, ShiftTable.TABLE_NAME);

                count += syncLocalSingleTable(service, CashDrawerMovementTable.TABLE_NAME);

                //inventory
                count += syncLocalSingleTable(service, TaxGroupTable.TABLE_NAME);
                count += syncLocalSingleTable(service, DepartmentTable.TABLE_NAME);
                count += syncLocalSingleTable(service, CategoryTable.TABLE_NAME);
                count += syncLocalSingleTable(service, ItemTable.TABLE_NAME);
                count += syncLocalSingleTable(service, ModifierTable.TABLE_NAME);
                count += syncLocalSingleTable(service, ItemMovementTable.TABLE_NAME);

                //sale
                count += syncLocalSingleTable(service, BillPaymentDescriptionTable.TABLE_NAME);

                count += syncLocalSingleTable(service, SaleOrderTable.TABLE_NAME);

                count += syncLocalSingleTable(service, SaleItemTable.TABLE_NAME);

                count += syncLocalSingleTable(service, SaleAddonTable.TABLE_NAME);

                count += syncLocalSingleTable(service, PaymentTransactionTable.TABLE_NAME);


                count += syncLocalSingleTable(service, CreditReceiptTable.TABLE_NAME);
                count += syncLocalSingleTable(service, EmployeeTipsTable.TABLE_NAME);

                count += syncLocalSingleTable(service, EmployeeCommissionsTable.TABLE_NAME);

                //inventory depended from sale
                count += syncLocalSingleTable(service, UnitTable.TABLE_NAME);
                //end

                executeHook(RegisterTable.URI_CONTENT, RegisterTable.ID, registerId <= 0 ? null : String.valueOf(registerId), registerHookListener);
                executeHook(EmployeePermissionTable.URI_CONTENT, EmployeePermissionTable.USER_GUID, getApp().getOperatorGuid(), employeePermissionsHookListener);

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

    private void markRecordsAsLive(Context context) {
        for (String uri : TABLES_URIS) {
            ProviderAction.update(ShopProvider.contentUri(uri))
                    .where(IBemaSyncTable.UPDATE_IS_DRAFT + " = ?", "1")
                    .value(IBemaSyncTable.UPDATE_IS_DRAFT, 0)
                    .perform(context);
            if (!isManual)
                ShopProviderExt.callMethod(service, Method.TRANSACTION_YIELD, null, null);
        }
    }

    private int syncTableWithChildren2(Context context, SyncApi2 api, String localUriPath, String localTable, String guidColumn, String parentIdColumn, EmployeeModel employeeModel, long serverLastUpdateTime) throws SyncException {
        int count = syncSingleTable2(context, api, localUriPath, localTable, guidColumn, employeeModel, true, parentIdColumn, false, serverLastUpdateTime);
        count += syncSingleTable2(context, api, localUriPath, localTable, guidColumn, employeeModel, true, parentIdColumn, true, serverLastUpdateTime);
        return count;
    }

    private int syncSingleTable2(Context context, SyncApi2 api, String localUriPath, String localTable, String guidColumn, EmployeeModel employeeModel, long serverLastUpdateTime) throws SyncException {
        return syncSingleTable2(context, api, localUriPath, localTable, guidColumn, employeeModel, false, null, false, serverLastUpdateTime);
    }

    private int syncSingleTable2(Context context, SyncApi2 api, String localUriPath, String localTable, String guidColumn, EmployeeModel employeeModel,
                                 boolean supportParentChildRelations,
                                 String parentIdColumn,
                                 boolean isChild,
                                 long serverLastUpdateTime) throws SyncException {

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
            try {
                JdbcConverter converter = JdbcFactory.getConverter(localTable);
                GetPagedArrayResponse resp = makeRequest(api, app.emailApiKey,
                        SyncUploadRequestBuilder.getReqCredentials(employeeModel, app),
                        Sync2GetRequestBuilder.getRequestFull(
                                converter.getTableName(),
                                updateTime,
                                converter.getGuidColumn(),
                                supportParentChildRelations ? converter.getParentGuidColumn() : null,
                                isChild,
                                PAGE_ROWS));
                Logger.d("Resp = %s", resp);
                if (resp == null || !resp.isSuccess()) {
                    throw new SyncException(localTable);
                }
                if (pages == -1) {
                    pages = (resp.rows + PAGE_ROWS - 1) / PAGE_ROWS;
                }
                HandlerResult handlerResult = handler.handleResponse(resp);
                if (handlerResult.hasData) {
                    //int pages = resp.rows / PAGE_ROWS + (resp.rows % PAGE_ROWS > 0 ? 1 : 0);
                    fireEvent(context, localTable, pages, step);
                }
                if (handlerResult.serverHasBeenUpdated) {
                    Logger.w("SyncCommand.syncSingleTable2(): server data has been updated; table: " + localTable);
                    serverHasBeenUpdated = true;
                }
                hasNext = handlerResult.hasData;
            } catch (Exception e) {
                Logger.e("sync table " + localTable + " exception", e);
                throw new SyncException(localTable);
            }
        }

        return size;
    }

    private int syncLocalSingleTable(Context context, String localTable) throws SyncException {
        //TODO: fire spec event?
        fireEvent(context, SyncWaitDialogFragment.SYNC_LOCAL + localTable);

        ShopProviderExt.callMethod(context, Method.METHOD_COPY_TABLE_FROM_SYNC_DB, localTable, null);
        if (!isManual)
            ShopProviderExt.callMethod(service, Method.TRANSACTION_YIELD, null, null);

        //TODO: use drop instead?
        ShopProviderExt.callMethod(context, Method.METHOD_CLEAR_TABLE_IN_SYNC_DB, localTable, null);
        if (!isManual)
            ShopProviderExt.callMethod(service, Method.TRANSACTION_YIELD, null, null);

        /*if (count != 0) {
            //TODO: fire spec event?
            //fireEvent(context, localTable, pages, step);
        }*/

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

    private void fireCompleteEvent(Context context, boolean success) {
        if (!isManual)
            return;

        Intent intent = new Intent(ACTION_SYNC_COMPLETED);
        intent.putExtra(EXTRA_SUCCESS, success);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void syncShopInfo(EmployeeModel employeeModel) throws SyncException {
        TcrApplication app = TcrApplication.get();
        SyncApi2 api = app.getRestAdapter().create(SyncApi2.class);

        fireEvent(service, null, service.getString(R.string.sync_shop_info), 0, 0);
        try {
            GetResponse resp = makeShopInfoRequest(api, app.emailApiKey, SyncUploadRequestBuilder.getReqCredentials(employeeModel, app));
            if (resp == null || !resp.isSuccess()) {
                Logger.e("can't parse shop", new RuntimeException());
                throw new SyncException();
            }
            JdbcJSONObject entity = resp.getEntity();
            syncBarcodePrefix(entity.getJSONArray("BARCODE_PREFIXES"));
            syncPrepaidTaxes(entity.getJSONArray("PREPAID_ITEM_TAXES"));
            syncActivationCarriers(entity.getJSONArray("ACTIVATION_CARRIERS"));
            syncShop(entity.getJSONObject("SHOP"));
        } catch (Exception e) {
            Logger.e("Can't sync shop info", e);
            throw new SyncException();
        }
    }

    private void syncShop(JdbcJSONObject shop) throws SyncException {
        if (shop == null) {
            Logger.e("can't parse shop", new RuntimeException());
            throw new SyncException();
        }
        ShopInfo info;
        try {
            info = ShopInfoViewJdbcConverter.read(shop);
        } catch (JSONException e) {
            Logger.e("can't parse shop", e);
            throw new SyncException();
        }
        boolean oldTipsEnabled = getApp().isTipsEnabled();
        getApp().saveShopInfo(info);
        checkTipsEnabled(oldTipsEnabled, info.tipsEnabled);
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

        MerchantDetails merchantDetails = new PaxMIDownloadCommand().sync(service, PaxModel.get());
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

        private MaxUpdateTime(long time, String guid) {
            this.time = time;
            this.guid = guid;
        }
    }

    private static MaxUpdateTime getMaxTimeSingleTable(Context context, SyncOpenHelper syncOpenHelper, String tableName, String id, String parentIdColumn, boolean isChild) {
        if (parentIdColumn == null) {
            return getMaxTime(context, syncOpenHelper, MaxUpdateTableTimeQuery.URI_CONTENT, new String[]{tableName, id}, true);
        }
        return getMaxTime(context, syncOpenHelper, MaxUpdateTableTimeParentRelationsQuery.URI_CONTENT, new String[]{tableName, id, parentIdColumn, isChild ? "not null" : "null"}, true);
    }

    private static MaxUpdateTime getMaxTime(Context context, SyncOpenHelper syncOpenHelper, String contentUri, String[] args, boolean needGuid) {
        MaxUpdateTime mainMaxUpdateTime = getMaxTime(context, syncOpenHelper, contentUri, args, needGuid, false);
        MaxUpdateTime syncMaxUpdateTime = getMaxTime(context, syncOpenHelper, contentUri, args, needGuid, true);
        MaxUpdateTime maxUpdateTime = mainMaxUpdateTime;
        if (mainMaxUpdateTime == null)
            maxUpdateTime = syncMaxUpdateTime;
        else if (syncMaxUpdateTime != null && syncMaxUpdateTime.time > mainMaxUpdateTime.time)
            maxUpdateTime = syncMaxUpdateTime;
        return maxUpdateTime;
    }

    private static MaxUpdateTime getMaxTime(Context context, SyncOpenHelper syncOpenHelper, String contentUri, String[] args, boolean needGuid, boolean fromSyncDatabase) {
        Cursor c;
        if (!fromSyncDatabase) {
            c = context.getContentResolver().query(
                    ShopProvider.contentUri(contentUri),
                    null,
                    null,
                    args,
                    null);
        } else {
            c = contentUri.equals(MaxUpdateTableTimeQuery.URI_CONTENT) ? syncOpenHelper.getMaxUpdateTime(args) : syncOpenHelper.getMaxUpdateParentTime(args);
        }

        MaxUpdateTime time = null;
        if (c.moveToFirst()) {
            time = new MaxUpdateTime(c.getLong(0), needGuid ? c.getString(1) : null);
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
}
