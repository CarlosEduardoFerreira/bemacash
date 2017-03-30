package com.kaching123.tcr.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.getbase.android.db.provider.ProviderAction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.rest.sync.SyncUtil;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.CashDrawerMovementJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CategoryJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CommissionsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ComposerJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CountryJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CreditReceiptJdbcConverter;
import com.kaching123.tcr.jdbc.converters.CustomerJdbcConverter;
import com.kaching123.tcr.jdbc.converters.DepartmentJdbcConverter;
import com.kaching123.tcr.jdbc.converters.EmployeeJdbcConverter;
import com.kaching123.tcr.jdbc.converters.EmployeePermissionJdbcConverter;
import com.kaching123.tcr.jdbc.converters.EmployeeTimesheetJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemMatrixJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemsModifierGroupsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemsModifiersJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ItemsMovementJdbcConverter;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.jdbc.converters.KDSAliasJdbcConverter;
import com.kaching123.tcr.jdbc.converters.LoyaltyPointsMovementJdbcConverter;
import com.kaching123.tcr.jdbc.converters.MunicipalityJdbcConverter;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.jdbc.converters.PrinterAliasJdbcConverter;
import com.kaching123.tcr.jdbc.converters.RegisterJdbcConverter;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemAddonJdbcConverter;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemJdbcConverter;
import com.kaching123.tcr.jdbc.converters.SaleOrdersJdbcConverter;
import com.kaching123.tcr.jdbc.converters.ShiftJdbcConverter;
import com.kaching123.tcr.jdbc.converters.StateJdbcConverter;
import com.kaching123.tcr.jdbc.converters.TaxGroupJdbcConverter;
import com.kaching123.tcr.jdbc.converters.TipsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.UnitLabelJdbcConverter;
import com.kaching123.tcr.jdbc.converters.UnitsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.VariantItemJdbcConverter;
import com.kaching123.tcr.jdbc.converters.VariantSubItemJdbcConverter;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.service.broadcast.BroadcastInfo;
import com.kaching123.tcr.service.broadcast.WifiSocketService;
import com.kaching123.tcr.service.broadcast.messages.NotifyNewCommandMsg;
import com.kaching123.tcr.service.broadcast.messages.RunCallBack;
import com.kaching123.tcr.service.broadcast.messages.RunCommandsMsg;
import com.kaching123.tcr.store.BaseOpenHelper;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.SyncOpenHelper;
import com.kaching123.tcr.store.helper.RecalcItemComposerTable;
import com.kaching123.tcr.store.helper.RecalcItemCostTable;
import com.kaching123.tcr.store.helper.RecalcItemMovementTable;
import com.kaching123.tcr.store.helper.RecalcSaleAddonTable;
import com.kaching123.tcr.store.helper.RecalcSaleItemTable;
import com.kaching123.tcr.util.JdbcJSONObject;


/**
 * Created by Rodrigo Busata on 6/28/2016.
 */
public class LocalSyncHelper {

    public static final String TAG = "LOCAL_SYNC";
    public static final String TAG_HEIGHT = TAG + "_HEIGHT";

    private static final int MAX_RETRY_COMMAND = 10;            // Max commands on Retry Process
    private static final int GET_COMMANDS_BATCH_SIZE = 500;     // Commands Size?
    private static final int RELOAD_INTERVAL = 1000;            // Reload command interval if it needs
    private static final int RELOAD_STOCK_INTERVAL = 10000;     // Reload Stock (items movement)

    private static final int CHECK_COMMANDS_INTERVAL = 1000;    // Request commands from others tablets                         // def=   500
    private static final int RUN_COMMAND_INTERVAL = 200;        // Execute commands on Local Database                           // def=   100
    private static final int FORCE_SYNC_INTERVAL = 20000;       // Interval to force notify others tablets the commands it has  // def= 30000

    public static String LOCAL_SYNC_NEED_COLLECT_DATA = "LOCAL_SYNC_NEED_COLLECT_DATA";
    public static String LOCAL_SYNC_FAILED = "LOCAL_SYNC_FAILED";
    public static String LOCAL_SYNC = "LOCAL_SYNC";
    public static String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static String MESSAGE = "LOCAL_SYNC_MESSAGE";

    private static WifiSocketService wifiSocketService;
    private static LocalSyncHelper sInstance;
    private static RecalcItemMovementTable mItemMovementHelper;
    private static RecalcSaleItemTable mSaleItemHelper;
    private static RecalcSaleAddonTable mSaleItemAddonHelper;
    private static RecalcItemCostTable mCostComposerHelper;
    private static RecalcItemComposerTable mComposerHelper;
    private static boolean willReload;
    private static boolean willReloadStock;

    private static List<CommandRequest> commandsRequest = new ArrayList<>();
    private static List<String> queueToRequestCommands = new ArrayList<>();
    private static List<String> runningCommandsSerial = new ArrayList<>();

    private static boolean runningCommandsWorker;
    private static boolean runningForceSyncWorker;
    private static boolean runningGetCommandsWorker;

    private static Map<String, Integer> retries = new HashMap<>();

    private static SqlHelper sqlHelper;

    private static boolean ignoreLocalSync;

    public static void disableLocalSync(){
        ignoreLocalSync = true;
    }

    public static void enableLocalSync(){
        ignoreLocalSync = false;
    }


    private static final List<String> ignoreTables = Arrays.asList(
            ShopStore.UnitTable.TABLE_NAME,
            ShopStore.SaleAddonTable.TABLE_NAME,
            ShopStore.EmployeeCommissionsTable.TABLE_NAME
    );

    public static void localSyncError(Context context, String error, String serial) {
        Intent intent = new Intent(LocalSyncHelper.LOCAL_SYNC_FAILED);
        intent.putExtra(LocalSyncHelper.ERROR_MESSAGE, error);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        TcrApplication.get().getLanDevices().remove(new BroadcastInfo(serial));
    }

    public static void setWifiSocketService(WifiSocketService wifiSocketService) {
        LocalSyncHelper.wifiSocketService = wifiSocketService;

        sqlHelper = new SqlHelper(wifiSocketService);
        mItemMovementHelper = new RecalcItemMovementTable(wifiSocketService, sqlHelper);
        mSaleItemHelper = new RecalcSaleItemTable(wifiSocketService, sqlHelper);
        mSaleItemAddonHelper = new RecalcSaleAddonTable(wifiSocketService, sqlHelper);
        mComposerHelper = new RecalcItemComposerTable(wifiSocketService, sqlHelper);
        mCostComposerHelper = new RecalcItemCostTable(wifiSocketService, sqlHelper);
    }

    public static LocalSyncHelper getInstance() {
        if (sInstance == null) {
            sInstance = new LocalSyncHelper();
        }

        return sInstance;
    }

    public static void clearSqlHostTable(ContentResolver cr) {
        Uri uri = ShopProvider.contentUriNoNotify(ShopStore.SqlCommandHostTable.URI_CONTENT);
        int limitRemove = 0;
        Cursor c = cr.query(uri, new String[]{ShopStore.SqlCommandHostTable.GUID}, null, null, null);

        if (c != null) {
            limitRemove = c.getCount() - ShopStore.SqlCommandHostTable.MAX_ROWS;
        }

        if (limitRemove > 0) {
            Logger.d("Removed SQL_COMMAND_CLIENT: " + cr.delete(ShopProvider.contentUriNoNotify(ShopStore.SqlCommandClientTable.URI_CONTENT), ShopStore.SqlCommandClientTable.DELETE_WHERE, new String[]{String.valueOf(limitRemove)}));
            Logger.d("Removed SQL_COMMAND_HOST: " + cr.delete(uri, ShopStore.SqlCommandHostTable.DELETE_WHERE, new String[]{String.valueOf(limitRemove)}));
        }
    }

    public static void runGetCommandsWorker() throws Exception {
        if (runningGetCommandsWorker) {
            Logger.d(TAG + ": runningGetCommandsWorker: Already running!");
            return;
        }

        Logger.d(TAG + ": runningGetCommandsWorker: START");
        runningGetCommandsWorker = true;
        while (runningGetCommandsWorker) {
            Thread.sleep(CHECK_COMMANDS_INTERVAL);
            if (ignoreLocalSync) continue;

            if (queueToRequestCommands.size() > 0 && runningCommandsSerial.size() == 0) {
                OfflineCommandsService.doDownloadLocal(wifiSocketService, queueToRequestCommands.remove(0));
            }
        }
    }

    public static void runCommandsWorker() throws Exception {
        if (runningCommandsWorker) {
            Logger.d(TAG + ": runCommandsWorker: Already running!");
            return;
        }

        Logger.d(TAG + ": runCommandsWorker: START");
        runningCommandsWorker = true;
        while (runningCommandsWorker) {
            Thread.sleep(RUN_COMMAND_INTERVAL);
            if (ignoreLocalSync) continue;

            if (commandsRequest.size() > 0) {
                CommandRequest commandRunner = commandsRequest.remove(0);
                if (commandRunner == null) {
                    Logger.e(TAG_HEIGHT + ": CommandRequest is NULL - " + commandsRequest.toString());
                    continue;
                }

                if (!runningCommandsSerial.contains(commandRunner.request.serial)) runningCommandsSerial.add(commandRunner.request.serial);

                List<String> commandWithSuccess = new ArrayList<>();
                commandRunner.request.commandsSend = sortCommandsMap(commandRunner.request.commandsSend);

                SQLiteDatabase db = sqlHelper.getWritableDatabase();
                db.beginTransaction();
                try {
                    for (String guid : commandRunner.request.commandsSend.keySet()) {
                        RunCommandsMsg.SqlCommand command = commandRunner.request.commandsSend.get(guid);
                        if (runCommand(db, command)) {
                            commandWithSuccess.add(guid);
                        } else {
                            checkRetryCommands(commandRunner, commandWithSuccess);
                        }
                    }

                } catch (Exception e) {
                    Logger.e(TAG_HEIGHT, e);
                    checkRetryCommands(commandRunner, commandWithSuccess);

                } finally {
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }

                if (!willReloadStock) {
                    willReloadStock = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(RELOAD_STOCK_INTERVAL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            willReloadStock = false;

                            mItemMovementHelper.bulkRecalcAvailableItemMovementTableAfterSync(false);
                            mSaleItemHelper.bulkRecalcSaleItemTableAfterSync();
                            mSaleItemAddonHelper.bulkRecalcSaleAddonTableAfterSync();
                            mCostComposerHelper.recalcAfterSync();
                            mComposerHelper.recalcAfterSync();
                        }
                    }).start();
                }

                if (!willReload) {
                    willReload = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(RELOAD_INTERVAL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            willReload = false;

                            LocalBroadcastManager.getInstance(wifiSocketService).sendBroadcast(new Intent(SyncCommand.ACTION_SYNC_GAP));
                            LocalBroadcastManager.getInstance(wifiSocketService).sendBroadcast(new Intent(LOCAL_SYNC_NEED_COLLECT_DATA));
                        }
                    }).start();
                }

                runningCommandsSerial.remove(commandRunner.request.serial);
                WifiSocketService.getInstance().sendMsg(commandRunner.socket, new RunCallBack(commandRunner.request.uuid(), TcrApplication.get().getRegisterSerial(), commandWithSuccess).toJson());
            }
        }
    }

    private static void checkRetryCommands(CommandRequest commandRequest, List<String> commandWithSuccess){
        for (String command : commandWithSuccess){
            commandRequest.request.commandsSend.remove(command);
        }

        Logger.e(TAG_HEIGHT + ": command didn't work - " + commandRequest.request.commandsSend.get(commandRequest.request.commandsSend.keySet().iterator().next()).command);

        if (!retries.containsKey(commandRequest.request.serial) || retries.get(commandRequest.request.serial) <= MAX_RETRY_COMMAND) {
            commandsRequest.add(commandRequest);
            retries.put(commandRequest.request.serial, !retries.containsKey(commandRequest.request.serial) ? 1 : retries.get(commandRequest.request.serial) + 1);
        }
    }


    public static boolean runCommand(SQLiteDatabase db, RunCommandsMsg.SqlCommand sqlCommand) {
        SqlCommandObj sqlCommandObj = new Gson().fromJson(sqlCommand.command, SqlCommandObj.class);

        Logger.d(TAG + ": Running command: " + sqlCommand.command);
        for (SqlCommandObj.SqlCommandObjOperation operation : sqlCommandObj.operations) {
            if (operation == null || operation.action == null) {
                Logger.e(TAG_HEIGHT, new Throwable("SQl command incorrect: " + sqlCommand));
                return false;
            }

            try {
                Log.d("BemaCarl","LocalSyncHelper.runCommand.operation.action: " + operation.action);
                Log.d("BemaCarl","LocalSyncHelper.runCommand.operation.table: " + operation.table);
                Log.d("BemaCarl","LocalSyncHelper.runCommand.operation.args: " + operation.args);
                if ("INSERT".equals(operation.action.toUpperCase()) || "REPLACE".equals(operation.action.toUpperCase())) {
                    Log.d("BemaCarl","LocalSyncHelper.runCommand----------------------- INSERT or REPLACE Start  -----------------------");
                    Object[] model = getContentValuesAndGuidColumn(operation, true);

                    if (model == null || model.length == 0) {
                        Log.e("BemaCarl","LocalSyncHelper.runCommand.model:    Table not found");
                        Logger.e(TAG_HEIGHT, new Throwable("Table not found: " + operation.table + " - " + "put this on LocalSyncHelper.getModel() method right now!"));
                        return false;
                    }
                    ContentValues contentValues = (ContentValues) model[0];
                    if (contentValues.size() == 0){
                        Log.e("BemaCarl","LocalSyncHelper.runCommand: contentValues.size() == 0");
                        return true;
                    }

                    Log.d("BemaCarl","LocalSyncHelper.runCommand.contentValues:    " + contentValues);
                    long idInsertedRow = 0;
                    if(ShopStore.EmployeePermissionTable.TABLE_NAME.equals(operation.table)){
                        idInsertedRow = db.insertWithOnConflict(operation.table, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                    }else{
                        idInsertedRow = db.insertWithOnConflict(operation.table, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                    }
                    Log.d("BemaCarl","LocalSyncHelper.runCommand.idInsertedRow:     " + idInsertedRow);
                    Log.d("BemaCarl","LocalSyncHelper.runCommand----------------------- INSERT or REPLACE End   -----------------------");

                } else if ("UPDATE".equals(operation.action.toUpperCase())) {
                    Log.d("BemaCarl","LocalSyncHelper.runCommand----------------------- UPDATE Start  -----------------------");
                    LinkedTreeMap<String, Object> where = (LinkedTreeMap<String, Object>) operation.args.get("where");

                    Object value = where.get(where.keySet().iterator().next());

                    if (value == null) return false;
                    String id = value.toString();
                    Log.d("BemaCarl","LocalSyncHelper.runCommand.id: " + id);

                    Object value2 = where.get(JdbcBuilder.FIELD_UPDATE_TIME_LOCAL);
                    Date updateTimeLocal = value2 != null ? SyncUtil.formatMillisec(value2.toString()) : null;

                    Object[] model = getContentValuesAndGuidColumn(operation, false);
                    if (model == null || model.length == 0) {
                        Logger.e(TAG_HEIGHT, new Throwable("Table not found: " + sqlCommand));
                        return false;
                    }
                    ContentValues contentValues = (ContentValues) model[0];
                    if (contentValues.size() == 0) return true;

                    contentValues.remove((String) model[1]);
                    contentValues.remove("create_time");

                    model[1] = resolveWhereClause(operation, where);

                    String whereString = String.format("%s = '%s'", model[1].toString(), id);

                    if (updateTimeLocal != null && JdbcFactory.getConverter(operation.table).supportUpdateTimeLocalFlag()
                            && !(contentValues.getAsBoolean(ShopStore.DEFAULT_IS_DELETED) != null && contentValues.getAsBoolean(ShopStore.DEFAULT_IS_DELETED))) {
                        whereString += String.format(" AND (%s < %s OR %s IS NULL)",
                                ShopStore.DEFAULT_UPDATE_TIME_LOCAL, updateTimeLocal.getTime(), ShopStore.DEFAULT_UPDATE_TIME_LOCAL);
                    }
                    Log.d("BemaCarl","LocalSyncHelper.runCommand----------------------- db.update Start  -----------------------");
                    Log.d("BemaCarl","LocalSyncHelper.runCommand.operation.table:  " + operation.table);
                    Log.d("BemaCarl","LocalSyncHelper.runCommand.contentValues:    " + contentValues);
                    Log.d("BemaCarl","LocalSyncHelper.runCommand.whereString:      " + whereString);
                    int affectedRows = db.update(operation.table, contentValues, whereString, null);
                    Log.d("BemaCarl","LocalSyncHelper.runCommand.affectedRows:     " + affectedRows);
                    Log.d("BemaCarl","LocalSyncHelper.runCommand----------------------- db.update End   -----------------------");
                    if (affectedRows == 0 && !ignoreTables.contains(operation.table)) {
                        if (!areThereRow(db, operation.table, model[1].toString(), id)){
                            if(!JdbcFactory.getConverter(operation.table).isChild()){
                                return false;
                            }
                        }
                    }

                    if(operation.table.equals(ShopStore.SaleOrderTable.TABLE_NAME) && affectedRows != 0) {
                        LocalBroadcastManager.getInstance(wifiSocketService).sendBroadcast(new Intent(SyncCommand.ACTION_SYNC_GAP));
                    }
                    Log.d("BemaCarl","LocalSyncHelper.runCommand----------------------- UPDATE End   -----------------------");
                } else {
                    Logger.e(TAG_HEIGHT, new Throwable("Action not found: " + operation.action));
                    return false;
                }

            } catch (Exception e) {
                Logger.e(TAG, e);
                return false;
            }
        }
        return true;
    }


    private static boolean areThereRow(SQLiteDatabase db, String table, String idColumn, String idValue) {
                Cursor c = db.query(table, new String[]{idColumn},
                        String.format("%s = '%s'", idColumn, idValue), null, null, null, null, null);
            return c.getCount() > 0;
    }


    public static Object[] getContentValuesAndGuidColumn(SqlCommandObj.SqlCommandObjOperation operation, boolean insertOrReplace) throws JSONException {
        Log.d("BemaCarl","LocalSyncHelper.getContentValuesAndGuidColumn------------- Start getContentValuesAndGuidColumn -------------");
        if (operation == null) return null;
        JdbcJSONObject json;
        Gson gson = new GsonBuilder().serializeNulls().create();
        if (insertOrReplace) {
            json = new JdbcJSONObject(gson.toJson(operation.args));
        } else {
            json = new JdbcJSONObject(gson.toJson(operation.args.get("update")));
        }
        Log.d("BemaCarl","LocalSyncHelper.getContentValuesAndGuidColumn.json: " + json);
        IValueModel valueModel = getModel(operation, json);
        if (valueModel == null) {
            Log.e("BemaCarl","LocalSyncHelper.getContentValuesAndGuidColumn: valueModel == null");
            return null;
        }
        Log.d("BemaCarl","LocalSyncHelper.getContentValuesAndGuidColumn: valueModel.getClass()" + valueModel.getClass());
        ContentValues contentValues = valueModel.toValues();
        if (json.has(JdbcBuilder.FIELD_IS_DELETED)) {
            contentValues.put(ShopStore.DEFAULT_IS_DELETED, json.getInt(JdbcBuilder.FIELD_IS_DELETED));
        }
        if (json.has(JdbcBuilder.FIELD_UPDATE_TIME)) {
            contentValues.put(ShopStore.DEFAULT_UPDATE_TIME, json.getTimestamp(JdbcBuilder.FIELD_UPDATE_TIME).getTime());
        }
        if (json.has(JdbcBuilder.FIELD_UPDATE_TIME_LOCAL)) {
            Date datetime = json.getTimestamp(JdbcBuilder.FIELD_UPDATE_TIME_LOCAL);
            if (datetime != null) {
                contentValues.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, datetime.getTime());
            }
        }

        contentValues.put(ShopStore.ISupportDraftTable.UPDATE_IS_DRAFT, 1);

        Object[] returned = new Object[2];
        returned[0] = contentValues;
        returned[1] = valueModel.getIdColumn();
        Log.d("BemaCarl","LocalSyncHelper.getContentValuesAndGuidColumn------------- Start getContentValuesAndGuidColumn -------------");
        return returned;
    }

    private static Object resolveWhereClause(SqlCommandObj.SqlCommandObjOperation operation, LinkedTreeMap<String, Object> where){
        Log.d("BemaCarl","LocalSyncHelper.resolveWhereClause----------------------- Start resolveWhereClause -----------------------");
        String whereClause = null;
        JSONObject json = new JSONObject(where);
        JdbcJSONObject jdbcJson = null;
        try {
            jdbcJson = new JdbcJSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(jdbcJson != null) {
            Log.d("BemaCarl", "LocalSyncHelper.resolveWhereClause.json.toString(): " + json.toString());
            IValueModel model = getModel(operation, jdbcJson);
            String webGuidColumn = JdbcFactory.getConverter(model).getGuidColumn();
            Log.d("BemaCarl","LocalSyncHelper.resolveWhereClause.webGuidColumn: " + webGuidColumn);
            whereClause = model.getIdColumn();
            if(!where.containsKey(webGuidColumn)) {
                ContentValues contentValues = model.toValues();
                Log.d("BemaCarl", "LocalSyncHelper.resolveWhereClause.contentValues: " + contentValues);
                contentValues.remove(JdbcBuilder.FIELD_UPDATE_TIME_LOCAL.toLowerCase());
                Set setWhere = contentValues.valueSet();

                Log.d("BemaCarl", "LocalSyncHelper.resolveWhereClause.where: " + where);
                //Set setWhere = where.entrySet();
                Iterator itWhere = setWhere.iterator();
                if (itWhere.hasNext()) {
                    Map.Entry clause = (Map.Entry) itWhere.next();
                    whereClause = clause.getKey().toString();
                    Log.d("BemaCarl", "LocalSyncHelper.resolveWhereClause.clause: key=" + clause.getKey() + "|value=" + clause.getValue());
                }
            }
        }else{
            Log.d("BemaCarl", "LocalSyncHelper.resolveWhereClause: jdbcJson is null");
        }
        Log.d("BemaCarl","LocalSyncHelper.resolveWhereClause----------------------- End resolveWhereClause  -----------------------");
        return whereClause;
    }


    private static IValueModel getModel(SqlCommandObj.SqlCommandObjOperation operation, JdbcJSONObject json) {
        IValueModel model = null;

        try {
            String table = operation.table;
            Log.d("BemaCarl","LocalSyncHelper.getModel----------------------- start -----------------------");
            Log.d("BemaCarl","LocalSyncHelper.getModel.operation.table: " + operation.table);
            Log.d("BemaCarl","LocalSyncHelper.getModel.operation.action: " + operation.action);
            Log.d("BemaCarl","LocalSyncHelper.getModel.operation.args: " + operation.args);
            Log.d("BemaCarl","LocalSyncHelper.getModel.json: " + json);
            if (JdbcConverter.compareTable(table, ShopStore.CashDrawerMovementTable.TABLE_NAME, CashDrawerMovementJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.CashDrawerMovementTable.TABLE_NAME;
                model = new CashDrawerMovementJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.CategoryTable.TABLE_NAME, CategoryJdbcConverter.CATEGORY_TABLE_NAME)) {
                operation.table = ShopStore.CategoryTable.TABLE_NAME;
                model = new CategoryJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.ComposerTable.TABLE_NAME, ComposerJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.ComposerTable.TABLE_NAME;
                model = new ComposerJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.CountryTable.TABLE_NAME, CountryJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.CountryTable.TABLE_NAME;
                model = new CountryJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.CreditReceiptTable.TABLE_NAME, CreditReceiptJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.CreditReceiptTable.TABLE_NAME;
                model = new CreditReceiptJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.CustomerTable.TABLE_NAME, CustomerJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.CustomerTable.TABLE_NAME;
                model = new CustomerJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.DepartmentTable.TABLE_NAME, DepartmentJdbcConverter.DEPARTMENT_TABLE_NAME)) {
                operation.table = ShopStore.DepartmentTable.TABLE_NAME;
                model = new DepartmentJdbcConverter().toValues(json);
                Log.d("BemaCarl7","LocalSyncHelper.getModel.model.getIdColumn(): " + model.getIdColumn());
                Log.d("BemaCarl7","LocalSyncHelper.getModel.json: " + json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.EmployeeCommissionsTable.TABLE_NAME, CommissionsJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.EmployeeCommissionsTable.TABLE_NAME;
                model = new CommissionsJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.EmployeePermissionTable.TABLE_NAME, EmployeePermissionJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.EmployeePermissionTable.TABLE_NAME;
                model = new EmployeePermissionJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.EmployeeTable.TABLE_NAME, EmployeeJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.EmployeeTable.TABLE_NAME;
                model = new EmployeeJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.EmployeeTimesheetTable.TABLE_NAME, EmployeeTimesheetJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.EmployeeTimesheetTable.TABLE_NAME;
                model = new EmployeeTimesheetJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.EmployeeTipsTable.TABLE_NAME, TipsJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.EmployeeTipsTable.TABLE_NAME;
                model = new TipsJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.ItemMatrixTable.TABLE_NAME, ItemMatrixJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.ItemMatrixTable.TABLE_NAME;
                model = new ItemMatrixJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.ItemMovementTable.TABLE_NAME, ItemsMovementJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.ItemMovementTable.TABLE_NAME;
                model = new ItemsMovementJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.ItemTable.TABLE_NAME, ItemsJdbcConverter.ITEM_TABLE_NAME)) {
                operation.table = ShopStore.ItemTable.TABLE_NAME;
                model = new ItemsJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.ModifierGroupTable.TABLE_NAME, ItemsModifierGroupsJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.ModifierGroupTable.TABLE_NAME;
                model = new ItemsModifierGroupsJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.ModifierTable.TABLE_NAME, ItemsModifiersJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.ModifierTable.TABLE_NAME;
                model = new ItemsModifiersJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.MunicipalityTable.TABLE_NAME, MunicipalityJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.MunicipalityTable.TABLE_NAME;
                model = new MunicipalityJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.KDSAliasTable.TABLE_NAME, KDSAliasJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.KDSAliasTable.TABLE_NAME;
                model = new KDSAliasJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.LoyaltyPointsMovementTable.TABLE_NAME, LoyaltyPointsMovementJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.LoyaltyPointsMovementTable.TABLE_NAME;
                model = new LoyaltyPointsMovementJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.PaymentTransactionTable.TABLE_NAME, PaymentTransactionJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.PaymentTransactionTable.TABLE_NAME;
                model = new PaymentTransactionJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.PrinterAliasTable.TABLE_NAME, PrinterAliasJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.PrinterAliasTable.TABLE_NAME;
                model = new PrinterAliasJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.RegisterTable.TABLE_NAME, RegisterJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.RegisterTable.TABLE_NAME;
                model = new RegisterJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.SaleAddonTable.TABLE_NAME, SaleOrderItemAddonJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.SaleAddonTable.TABLE_NAME;
                model = new SaleOrderItemAddonJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.SaleItemTable.TABLE_NAME, SaleOrderItemJdbcConverter.SALE_ORDER_ITEMS_TABLE_NAME)) {
                operation.table = ShopStore.SaleItemTable.TABLE_NAME;
                model = new SaleOrderItemJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.SaleOrderTable.TABLE_NAME, SaleOrdersJdbcConverter.SALE_ORDER_TABLE_NAME)) {
                operation.table = ShopStore.SaleOrderTable.TABLE_NAME;
                model = new SaleOrdersJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.ShiftTable.TABLE_NAME, ShiftJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.ShiftTable.TABLE_NAME;
                model = new ShiftJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.StateTable.TABLE_NAME, StateJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.StateTable.TABLE_NAME;
                model = new StateJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.TaxGroupTable.TABLE_NAME, TaxGroupJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.TaxGroupTable.TABLE_NAME;
                model = new TaxGroupJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.UnitLabelTable.TABLE_NAME, UnitLabelJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.UnitLabelTable.TABLE_NAME;
                model = new UnitLabelJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.UnitTable.TABLE_NAME, UnitsJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.UnitTable.TABLE_NAME;
                model = new UnitsJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.TaxGroupTable.TABLE_NAME, TaxGroupJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.TaxGroupTable.TABLE_NAME;
                model = new TaxGroupJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.VariantItemTable.TABLE_NAME, VariantItemJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.VariantItemTable.TABLE_NAME;
                model = new VariantItemJdbcConverter().toValues(json);
            }
            if (JdbcConverter.compareTable(table, ShopStore.VariantSubItemTable.TABLE_NAME, VariantSubItemJdbcConverter.TABLE_NAME)) {
                operation.table = ShopStore.VariantSubItemTable.TABLE_NAME;
                model = new VariantSubItemJdbcConverter().toValues(json);
            }
        } catch (JSONException e) {
            Logger.e(TAG_HEIGHT, e);
        }
        if( model != null )
            Log.d("BemaCarl","LocalSyncHelper.getModel.model.getIdColumn(): " + model.getIdColumn());
        else
            Log.d("BemaCarl","LocalSyncHelper.getModel.model: model is null!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Log.d("BemaCarl","LocalSyncHelper.getModel------------------------ finish ---------------------");

        return model;
    }



    private static Map<String, RunCommandsMsg.SqlCommand> sortCommandsMap(Map<String, RunCommandsMsg.SqlCommand> commandsSend) {
        List<String> mapKeys = new ArrayList<>(commandsSend.keySet());
        List<RunCommandsMsg.SqlCommand> mapValues = new ArrayList<>(commandsSend.values());
        Collections.sort(mapValues, new Comparator<RunCommandsMsg.SqlCommand>() {
            @Override
            public int compare(RunCommandsMsg.SqlCommand lhs, RunCommandsMsg.SqlCommand rhs) {
                return lhs.order - rhs.order;
            }
        });
        Collections.sort(mapKeys);

        LinkedHashMap<String, RunCommandsMsg.SqlCommand> sortedMap = new LinkedHashMap<>();

        for (RunCommandsMsg.SqlCommand val : mapValues) {
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                RunCommandsMsg.SqlCommand comp1 = commandsSend.get(key);

                if (comp1.equals(val)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    public static void checkAndStartWorkers(){
        if (!runningCommandsWorker) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        runCommandsWorker();

                    } catch (Exception e) {
                        runningCommandsWorker = false;
                        Logger.e("runCommandsWorker", e);
                    }
                }
            }).start();
        }

        if (!runningGetCommandsWorker) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        runGetCommandsWorker();

                    } catch (Exception e) {
                        runningGetCommandsWorker = false;
                        Logger.e("runningGetCommandsWorker", e);
                    }
                }
            }).start();
        }

        if (!runningForceSyncWorker) {
            runningForceSyncWorker = true;
            Logger.d(TAG + ": runningForceSyncWorker: START");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            Thread.sleep(FORCE_SYNC_INTERVAL);
                            if (ignoreLocalSync) continue;

                            notifyChanges();

                        } catch (Exception e) {
                            runningForceSyncWorker = false;
                            Logger.e("runningForceSyncWorker", e);
                        }
                    }
                }
            }).start();
        }
    }


    public void workRequestRunCommand(RunCommandsMsg request, Socket socket) throws IOException {
        Logger.d(TAG + ": received " + request.commandsSend.size() + " commands");
        if (request.commandsSend.size() == 0){
            wifiSocketService.sendMsg(socket, new RunCallBack(request.uuid()).toJson());
            return;
        }

        commandsRequest.add(new CommandRequest(socket, request));
    }

    public void workRequestCommands(String serial, Socket socket) {
        final Map<String, RunCommandsMsg.SqlCommand> commands = new HashMap<>();
        try{
            Cursor c = ProviderAction.query(ShopProvider.contentUri(ShopStore.SqlCommandHostQuery.URI_CONTENT))
                    .where("", serial, GET_COMMANDS_BATCH_SIZE)
                    .perform(wifiSocketService);

            while (c != null && c.moveToNext()) {
                commands.put(c.getString(0), new RunCommandsMsg.SqlCommand(c.getInt(2), c.getString(1)));
            }
            RunCommandsMsg requestSend = new RunCommandsMsg(TcrApplication.get().getRegisterSerial(), commands);
            wifiSocketService.sendMsg(socket, requestSend.toJson());

        } catch (Exception e) {
            Logger.e(TAG_HEIGHT, e);
        }
    }

    public static void notifyChanges() {
        Logger.d(LocalSyncHelper.TAG_HEIGHT + ": Notifying all devices about new changes");

        if (!TcrApplication.get().getShopPref().enabledLocalSync().get()) return;

        NotifyNewCommandMsg request = new NotifyNewCommandMsg(TcrApplication.get().getRegisterSerial());
        try {
            if (wifiSocketService != null) {
                wifiSocketService.makeMsgAndSend(request.toJson(), true, null);
            }

        } catch (Exception e) {
            Logger.e(TAG_HEIGHT, e);
        }
    }

    public boolean workCallBack(RunCallBack request, ContentResolver cr, Socket socket) {
        if (request != null && request.commandWithSuccess != null) {
            for (String guid : request.commandWithSuccess) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ShopStore.SqlCommandClientTable.CLIENT_SERIAL, request.serial);
                contentValues.put(ShopStore.SqlCommandClientTable.COMMAND_GUID, guid);
                cr.insert(ShopProvider.contentUriNoNotify(ShopStore.SqlCommandClientTable.URI_CONTENT), contentValues);
            }

            if (request.commandWithSuccess.size() > 0) {
                Logger.d(TAG + ": Sending more commands on port " + socket.getPort());

                workRequestCommands(request.serial, socket);
                return false;
            }
        }
        return true;
    }

    public static void addInQueue(String serial){
        if (!queueToRequestCommands.contains(serial) && !runningCommandsSerial.contains(serial)) {
            Logger.d(TAG_HEIGHT + ": Adding queue " + serial);
            queueToRequestCommands.add(serial);

        } else {
            Logger.d(TAG_HEIGHT + ": IGNORE serial on queue " + serial);
        }
    }

    private static class SqlHelper extends BaseOpenHelper {
        public SqlHelper(Context context) {
            super(context);
        }
    }

    public static class SqlCommandObj {
        public String method;
        public List<SqlCommandObjOperation> operations;

        public static class SqlCommandObjOperation {
            public String action;
            public String table;
            public Map<String, Object> args;

            public SqlCommandObjOperation(String table) {
                this.table = table;
            }
        }
    }

    private class CommandRequest {

        Socket socket;
        RunCommandsMsg request;

        public CommandRequest(Socket socket, RunCommandsMsg request) {
            this.socket = socket;
            this.request = request;
        }
    }


}