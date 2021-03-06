package com.kaching123.tcr.store;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.support.SendLogCommand;
import com.kaching123.tcr.fragment.dialog.SyncWaitDialogFragment;
import com.kaching123.tcr.model.RegisterStatus;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.service.SyncCommand.Table;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.kaching123.tcr.store.ShopStore.UpdateTimeTable;
import com.kaching123.tcr.store.migration.IUpdateContainer;
import com.kaching123.tcr.util.ContentValuesUtilBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by pkabakov on 23.04.2014.
 */
public class ShopOpenHelper extends BaseOpenHelper {

    private static final DateFormat FILE_NAME_DATE_FORMAT = new SimpleDateFormat("MM_dd_yyyy");
    private static final int PAGE_ROWS = 1800;

    private static final String EXTRA_DB_ALIAS = "\'extraDb\'";
    private static final String SQL_ATTACH_DB = "ATTACH DATABASE \'%s\' AS " + EXTRA_DB_ALIAS + ";";
    private static final String SQL_DETACH_DB = "DETACH DATABASE " + EXTRA_DB_ALIAS + ";";
    private static final String SQL_CLEAR_TABLE_IN_DB = "DELETE FROM " + EXTRA_DB_ALIAS + ".%s;";

    private TrainingShopOpenHelper trainingShopOpenHelper;
    public static String ACTION_SYNC_PROGRESS = "com.kaching123.tcr.service.ACTION_SYNC_PROGRESS";
    public static String EXTRA_TABLE = "table";
    public static String EXTRA_PAGES = "pages";
    public static String EXTRA_PROGRESS = "progress";
    public static String EXTRA_DATA_LABEL = "data_label";

    public ShopOpenHelper(Context context) {
        super(context);
        trainingShopOpenHelper = new TrainingShopOpenHelper(context);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if (TcrApplication.get().isTrainingMode())
            return trainingShopOpenHelper.getReadableDatabase();
        return super.getReadableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        if (TcrApplication.get().isTrainingMode())
            return trainingShopOpenHelper.getWritableDatabase();
        return super.getWritableDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);

        if (oldVersion < IUpdateContainer.VERSION5) {
            Logger.d("ShopOpenHelper.onUpgrade(): need2DownloadAfter1stLaunch() set to true");
            ((TcrApplication) mContext.getApplicationContext()).getShopPref().need2DownloadAfter1stLaunch().put(true);
        }
    }

    public boolean exportDatabase(Context context, String folderName) {
        boolean isTrainingMode = TcrApplication.get().isTrainingMode();
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            String dbName = isTrainingMode ? TrainingShopOpenHelper.getDbName() : getDbName();
            File currentDbFile = context.getDatabasePath(dbName);
            File backupDbFile = new File(folderName, getFileName(dbName));

            copyDatabaseFile(currentDbFile, backupDbFile);

            return true;
        } catch (Exception e) {
            Logger.e("ShopOpenHelper.exportDatabase(): failed", e);
        } finally {
            db.endTransaction();
        }
        return false;
    }

    public boolean copyToTrainingDatabase(Context context) {
        SQLiteDatabase db = super.getWritableDatabase();
        trainingShopOpenHelper.close();
        db.beginTransaction();
        try {
            if (!db.isDatabaseIntegrityOk()) {
                Logger.e("ShopOpenHelper.copyToTrainingDatabase(): failed integrity check!");
                return false;
            }

            File currentDbFile = context.getDatabasePath(getDbName());
            File trainingDbFile = context.getDatabasePath(TrainingShopOpenHelper.getDbName());

            copyDatabaseFile(currentDbFile, trainingDbFile);
            return true;
        } catch (Exception e) {
            Logger.e("ShopOpenHelper.copyToTrainingDatabase(): failed", e);
        } finally {
            db.endTransaction();
        }
        return false;
    }

    public boolean createTrainingDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();
        SQLiteDatabase trainingDb = trainingShopOpenHelper.getWritableDatabase();
        db.beginTransaction();
        trainingDb.beginTransaction();
        try {
            ShopSchemaEx.onDrop(trainingDb);
            ShopSchemaEx.onCreate(trainingDb);

            String registerSerial = TcrApplication.get().getRegisterSerial();
            String operatorGuid = TcrApplication.get().getOperatorGuid();
            copyTableAcrossDatabases(db, trainingDb, ShopStore.RegisterTable.TABLE_NAME,
                    ShopStore.RegisterTable.REGISTER_SERIAL + " = ? AND " + ShopStore.RegisterTable.STATUS + " <> ?",
                    new String[]{registerSerial, String.valueOf(RegisterStatus.BLOCKED.ordinal())},
                    true);
            copyTableAcrossDatabases(db, trainingDb, ShopStore.EmployeeTable.TABLE_NAME, ShopStore.EmployeeTable.GUID, operatorGuid, true);
            copyTableAcrossDatabases(db, trainingDb, ShopStore.EmployeePermissionTable.TABLE_NAME, ShopStore.EmployeePermissionTable.USER_GUID, operatorGuid);

            trainingDb.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Logger.e("ShopOpenHelper.createTrainingDatabase(): failed", e);
        } finally {
            db.endTransaction();
            trainingDb.endTransaction();
        }
        return false;
    }

    private void copyTableAcrossDatabases(SQLiteDatabase sourceDb, SQLiteDatabase destinationDb, String tableName, String guidField, String guid) throws SQLException {
        copyTableAcrossDatabases(sourceDb, destinationDb, tableName, guidField, guid, false);
    }

    private void copyTableAcrossDatabases(SQLiteDatabase sourceDb, SQLiteDatabase destinationDb, String tableName, String guidField, String guid, boolean failOnEmptyData) throws SQLException {
        String selection = null;
        String[] selectionArgs = null;
        if (!TextUtils.isEmpty(guidField)) {
            selection = guidField + " = ?";
            selectionArgs = new String[]{guid};
        }
        copyTableAcrossDatabases(sourceDb, destinationDb, tableName, selection, selectionArgs, failOnEmptyData);
    }

    private void copyTableAcrossDatabases(SQLiteDatabase sourceDb, SQLiteDatabase destinationDb, String tableName, String selection, String[] selectionArgs, boolean failOnEmptyData) throws SQLException {
        Cursor cursor = sourceDb.query(tableName, null, selection, selectionArgs, null, null, null);
        if (cursor == null || (failOnEmptyData && cursor.getCount() == 0))
            throw new SQLException();

        ContentValues values = new ContentValues();
        try {
            while (cursor.moveToNext()) {
                values.clear();
                DatabaseUtils.cursorRowToContentValues(cursor, values);
                long rowId = destinationDb.insert(tableName, null, values);
                if (rowId == -1L)
                    throw new SQLException();
            }
        } finally {
            cursor.close();
        }
    }

    public boolean vacuum() {
        SQLiteDatabase db = super.getWritableDatabase();
        try {
            db.execSQL("VACUUM");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean clearTrainingDatabase() {
        return trainingShopOpenHelper.clearDatabase();
    }

    public boolean clearDatabaseKeepSync() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ShopSchemaEx.onDrop(db, true);
            ShopSchemaEx.onCreate(db, true);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Logger.e("ShopOpenHelper.clearDatabaseKeepSync(): failed", e);
        } finally {
            db.endTransaction();
        }
        return false;
    }

    private static void copyDatabaseFile(File sourceDbFile, File destinationDbFile) throws IOException {
        FileChannel src = null;
        FileChannel dst = null;
        try {
            src = new FileInputStream(sourceDbFile).getChannel();
            dst = new FileOutputStream(destinationDbFile).getChannel();
            dst.transferFrom(src, 0, src.size());
        } finally {
            if (src != null)
                try {
                    src.close();
                } catch (IOException e) {
                    Logger.e("ShopOpenHelper.copyDatabaseFile(): close with error", e);
                }
            if (dst != null)
                try {
                    dst.close();
                } catch (IOException e) {
                    Logger.e("ShopOpenHelper.copyDatabaseFile(): close with error", e);
                }
        }
    }

    private static String getFileName(String dbName) {
        String timestamp = FILE_NAME_DATE_FORMAT.format(new Date());
        return dbName.replace(".", "_" + timestamp + ".");
    }

    public void attachSyncAsExtraDatabase() {
        String dbFilePath = mContext.getDatabasePath(SyncOpenHelper.getDbName()).getAbsolutePath();
        attachExtraDatabase(dbFilePath);
    }

    private void attachExtraDatabase(String dbFilePath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(String.format(SQL_ATTACH_DB, dbFilePath));
    }

    public void detachExtraDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(String.format(SQL_DETACH_DB));
    }

    public void copyTableFromExtraDatabase(Context context,String tableName, String idColumn, String parentIdColumn) {
        copyTableFromExtraDatabase(context, tableName, idColumn, parentIdColumn, false);
    }

    public void copyUpdateTableFromExtraDatabase(Context context,String tableName, String idColumn, String parentIdColumn) {
        copyTableFromExtraDatabase(context, tableName, idColumn, parentIdColumn, true);
    }

    private void copyTableFromExtraDatabase(Context context, String tableName, String idColumn, String parentIdColumn, boolean insertUpdate) {
        SQLiteDatabase db = getWritableDatabase();

        boolean hasChildren = !TextUtils.isEmpty(parentIdColumn);

        String orderBy;
        if (!hasChildren) {
            orderBy = ShopStore.DEFAULT_UPDATE_TIME + ", " + idColumn;
        } else {
            orderBy = parentIdColumn + " is not null, " + ShopStore.DEFAULT_UPDATE_TIME + ", " + idColumn;
        }

        Cursor cursor = db.query(EXTRA_DB_ALIAS + '.' + tableName,
                null,
                null, null,
                null, null,
                orderBy);
        if (cursor.getCount() == 0) {
            cursor.close();
            return;
        }

        ContentValues values = new ContentValues();

        Boolean lastIsParent = null;
        Long lastUpdateTime = null;
        String lastGuid = null;

        int pages = (PAGE_ROWS + cursor.getCount()) / PAGE_ROWS;
        int steps = 0;

        try {
            while (cursor.moveToNext()) {
                steps++;
                int currentPage = steps / PAGE_ROWS;
                values.clear();
                DatabaseUtils.cursorRowToContentValues(cursor, values);
                fireEvent(context, tableName, null, pages, currentPage);
                if (hasChildren) {
                    boolean isParent = TextUtils.isEmpty(values.getAsString(parentIdColumn));
                    if (lastIsParent != null && lastIsParent && !isParent) {
                        saveMaxUpdateTime(db, tableName, true, lastUpdateTime, lastGuid);
                    }

                    lastIsParent = isParent;
                }

                lastUpdateTime = values.getAsLong(ShopStore.DEFAULT_UPDATE_TIME);
                lastGuid = values.getAsString(idColumn);

                if (!insertUpdate)
                    insertValuesFromExtraDatabase(db, tableName, values);
                else
                    insertUpdateValuesFromExtraDatabase(db, tableName, idColumn, values);
            }

            if (!hasChildren || lastIsParent) {
                saveMaxUpdateTime(db, tableName, true, lastUpdateTime, lastGuid);
            } else {
                saveMaxUpdateTime(db, tableName, false, lastUpdateTime, lastGuid);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    public void saveMaxUpdateTime(SQLiteDatabase db, String tableName, boolean isParent, long updateTime, String guid) {
        Table table = Table.getTable(tableName, isParent);

        ContentValues values = new ContentValues();
        values.put(UpdateTimeTable.UPDATE_TIME, updateTime);
        values.put(UpdateTimeTable.GUID, guid);

        String tableId = String.valueOf(ContentValuesUtilBase._enum(table));
        int updated = db.update(UpdateTimeTable.TABLE_NAME, values,
                UpdateTimeTable.TABLE_ID + " = ?"
                        + " AND (" + UpdateTimeTable.UPDATE_TIME + " < ?"
                        + " OR (" + UpdateTimeTable.UPDATE_TIME + " = ? AND " + UpdateTimeTable.GUID + " < ?))",
                new String[]{tableId, String.valueOf(updateTime), String.valueOf(updateTime), guid});

        if (updated != 0) {
            return;
        }

        Cursor c = db.rawQuery("SELECT " + UpdateTimeTable.UPDATE_TIME + " FROM " + UpdateTimeTable.TABLE_NAME
                        + " WHERE " + UpdateTimeTable.UPDATE_TIME + " > ? AND " + UpdateTimeTable.TABLE_ID + " = ?",
                new String[]{String.valueOf(updateTime), tableId});
        if (c.moveToFirst()) {
            return;
        }
        values.put(UpdateTimeTable.TABLE_ID, ContentValuesUtilBase._enum(table));
        long rowId = db.insertWithOnConflict(UpdateTimeTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (rowId == -1L)
            throw new SQLiteException();
    }

    public void insertUpdateValues(String tableName, String idColumn, ContentValues[] valuesArray) {
        SQLiteDatabase db = getWritableDatabase();

        for (ContentValues values: valuesArray) {
            insertUpdateValuesFromExtraDatabase(db, tableName, idColumn, values);
        }

    }

    public synchronized Cursor getMaxUpdateTime(String[] selectionArgs) {
        return ProviderQueryHelper.getMaxUpdateTime(this, selectionArgs);
    }

    public synchronized Cursor getMaxUpdateParentTime(String[] selectionArgs) {
        return ProviderQueryHelper.getMaxUpdateParentTime(this, selectionArgs);
    }

    public synchronized void beginTransaction() {
        getWritableDatabase().beginTransaction();
    }

    public synchronized void setTransactionSuccessful() {
        getWritableDatabase().setTransactionSuccessful();
    }

    public synchronized void endTransaction() {
        getWritableDatabase().endTransaction();
    }

    private String getWhereUpdateTimeLocal(ContentValues values){
        if (TextUtils.isEmpty(values.getAsString(ShopStore.DEFAULT_UPDATE_TIME_LOCAL))) return "";
        return String.format(" AND (%s <= %s OR %s IS NULL) ", ShopStore.DEFAULT_UPDATE_TIME_LOCAL, values.get(ShopStore.DEFAULT_UPDATE_TIME_LOCAL), ShopStore.DEFAULT_UPDATE_TIME_LOCAL);
    }

    public synchronized void insertUpdateValues(SQLiteDatabase db, String tableName, String idColumn, ContentValues values, boolean supportUpdateTimeLocal) {
        boolean hasData = true;
        String idValue = values.getAsString(idColumn);
        try {
            values.remove(idColumn);
            int updated;

            if (supportUpdateTimeLocal && !(values.getAsBoolean(ShopStore.DEFAULT_IS_DELETED) != null && values.getAsBoolean(ShopStore.DEFAULT_IS_DELETED))) {
                if (tableName.equals(ShopStore.EmployeePermissionTable.TABLE_NAME)){
                    updated = db.update(tableName, values, idColumn + " = ? AND " + ShopStore.EmployeePermissionTable.USER_GUID + " = ?" + getWhereUpdateTimeLocal(values),
                            new String[]{idValue, values.get(ShopStore.EmployeePermissionTable.USER_GUID).toString()});

                } else {
                    if (tableName.equals(SaleOrderTable.TABLE_NAME) && values.containsKey(SaleOrderTable.STATUS)) {
                        try (
                                Cursor c = db.query(SaleOrderTable.TABLE_NAME, new String[]{SaleOrderTable.STATUS},
                                        SaleOrderTable.GUID + " = ?", new String[]{idValue}, null, null, null)) {
                            if (c != null && c.moveToFirst()){
                                int status = c.getInt(0);
                                if (status != 0){
                                    values.put(SaleOrderTable.STATUS, status);
                                }
                            }
                        }
                    }
                    updated = db.update(tableName, values, idColumn + " = ? " + getWhereUpdateTimeLocal(values), new String[]{idValue});
                }

            } else {
                updated = db.update(tableName, values, idColumn + " = ? ", new String[]{idValue});
            }

            if (updated == 0 && !areThereItem(db, tableName, idColumn, idValue, values)) {
                hasData = false;
                values.put(idColumn, idValue);
                db.insertOrThrow(tableName, null, values);

            } else {
                Logger.d("insertUpdateValues: IGNORING VALUES, updated rows " + updated + " - " + values);
            }

        } catch (SQLiteConstraintException e) {
            Logger.e("ShopOpenHelper.insertUpdateValuesFromExtraDatabase(): constraint violation, tableName: " + tableName + "; values: " + values);

            if (UnitTable.TABLE_NAME.equals(tableName)) {
                values.put(idColumn, idValue);
                if (tryFixUnit(db, values)) {
                    tryInsertUpdateUnit(db, values, hasData);
                    return;
                }
            }
            throw e;
        }
    }

    private boolean areThereItem(SQLiteDatabase db, String tableName, String idColumn, String idValue, ContentValues values) {
        if (tableName.equals(ShopStore.EmployeePermissionTable.TABLE_NAME)){
            try (
                    Cursor c = db.query(tableName, new String[]{idColumn}, idColumn + " = ? AND " + ShopStore.EmployeePermissionTable.USER_GUID
                            + " = ?", new String[]{idValue, values.get(ShopStore.EmployeePermissionTable.USER_GUID).toString()}, null, null, null);
            ) {
                return c.getCount() > 0;
            }
        } else {
            try (
                    Cursor c = db.query(tableName, new String[]{idColumn}, idColumn + " = ?", new String[]{idValue}, null, null, null);
            ) {
                return c.getCount() > 0;
            }
        }
    }

    private void insertUpdateValuesFromExtraDatabase(SQLiteDatabase db, String tableName, String idColumn, ContentValues values) {
        boolean hasData = true;
        String idValue = values.getAsString(idColumn);
        try {
            values.remove(idColumn);
            int updated = db.update(tableName, values, idColumn + " = ?", new String[]{idValue});

            if (updated == 0) {
                hasData = false;
                values.put(idColumn, idValue);
                db.insertOrThrow(tableName, null, values);
            }
        } catch (SQLiteConstraintException e) {
            Logger.e("ShopOpenHelper.insertUpdateValuesFromExtraDatabase(): constraint violation, tableName: " + tableName + "; values: " + values);
            SendLogCommand.start(mContext);
            if (UnitTable.TABLE_NAME.equals(tableName)) {
                values.put(idColumn, idValue);
                if (tryFixUnit(db, values)) {
                    tryInsertUpdateUnit(db, values, hasData);
                    return;
                }
            }
//            throw e;
        }
    }

    private void fireEvent(Context context, String table, String dataLabel, int pages, int progress) {
        Intent intent = new Intent(ACTION_SYNC_PROGRESS);
        intent.putExtra(EXTRA_TABLE, SyncWaitDialogFragment.SYNC_LOCAL + table);
        intent.putExtra(EXTRA_DATA_LABEL, dataLabel);
        intent.putExtra(EXTRA_PAGES, pages);
        intent.putExtra(EXTRA_PROGRESS, progress);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void insertValuesFromExtraDatabase(SQLiteDatabase db, String tableName, ContentValues values) {
        try {
            long rowId = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if (rowId == -1L)
                throw new SQLiteException();
        } catch (SQLiteConstraintException e) {
            Logger.e("ShopOpenHelper.copyTableFromExtraDatabase(): constraint violation, tableName: " + tableName + "; values: " + values);
            if (UnitTable.TABLE_NAME.equals(tableName)) {
                if (tryFixUnit(db, values)) {
                    tryInsertUnit(db, values);
                    return;
                }
            }
            throw e;
        }
    }

    private boolean tryFixUnit(SQLiteDatabase db, ContentValues values) {
        Unit.Status status = Unit.Status.values()[values.getAsInteger(UnitTable.STATUS)];
        String saleOrderId = values.getAsString(UnitTable.SALE_ORDER_ID);
        String childOrderId = values.getAsString(UnitTable.CHILD_ORDER_ID);

        //unit doesn't belong to any order
        if (TextUtils.isEmpty(saleOrderId) && TextUtils.isEmpty(childOrderId)) {
            Logger.e("ShopOpenHelper.copyTableFromExtraDatabase(): failed to fix unit, not referring to any order; values: " + values);
            return false;
        }

        //unit is sold
        if (status == Unit.Status.SOLD) {
            //check which order is missing
            Cursor c = db.query(SaleOrderTable.TABLE_NAME, new String[]{"1"},
                    SaleOrderTable.GUID + " = ?", new String[]{saleOrderId}, null, null, null);
            boolean hasSaleOrder = c.getCount() > 0;
            c.close();

            if (hasSaleOrder) {
                if (TextUtils.isEmpty(childOrderId)) {
                    Logger.e("ShopOpenHelper.copyTableFromExtraDatabase(): failed to fix unit, sold but not referring to any missing order; values: " + values);
                    return false;
                }
                values.putNull(UnitTable.CHILD_ORDER_ID);
                return true;
            }

            //insert sold unit without order ids - will de removed later
            values.putNull(UnitTable.SALE_ORDER_ID);
            values.putNull(UnitTable.CHILD_ORDER_ID);
            return true;
        }
        //unit is active
        if (TextUtils.isEmpty(childOrderId)) {
            Logger.e("ShopOpenHelper.copyTableFromExtraDatabase(): failed to fix unit, it belongs to active order; values: " + values);
            return false;
        }
        //unit is refund
        if (TextUtils.isEmpty(saleOrderId)) {
            values.putNull(UnitTable.CHILD_ORDER_ID);
            return true;
        }
        //unit is active or refunded
        //TODO: active orders should be loaded separately from the server(before orders)
        //check if sale order is missing
        Cursor c = db.query(SaleOrderTable.TABLE_NAME, new String[]{"1"},
                SaleOrderTable.GUID + " = ?", new String[]{saleOrderId}, null, null, null);
        boolean hasSaleOrder = c.getCount() > 0;

        if (hasSaleOrder) {
            values.putNull(UnitTable.CHILD_ORDER_ID);
            return true;
        }
        //should be refund unit
        //active orders should be loaded separately from the server(before orders)
        values.putNull(UnitTable.SALE_ORDER_ID);
        values.putNull(UnitTable.CHILD_ORDER_ID);
        return true;
    }

    private void tryInsertUnit(SQLiteDatabase db, ContentValues values) {
        try {
            long rowId = db.insertWithOnConflict(UnitTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if (rowId == -1L)
                throw new SQLiteException();
        } catch (SQLiteConstraintException e) {
            Logger.e("ShopOpenHelper.tryInsertUnit(): constraint violation; values: " + values);
            throw e;
        }
    }

    private void tryInsertUpdateUnit(SQLiteDatabase db, ContentValues values, Boolean hasData) {
        try {
            String idValue = null;
            int updated = 0;
            if (hasData == null || hasData) {
                idValue = values.getAsString(UnitTable.ID);
                values.remove(UnitTable.ID);
                updated = db.update(UnitTable.TABLE_NAME, values, UnitTable.ID + " = ?", new String[]{idValue});
            }
            if (hasData != null && hasData && updated == 0) {
                throw new SQLiteException();
            }

            if ((hasData == null && updated == 0) || (hasData != null && !hasData)) {
                if (idValue != null)
                    values.put(UnitTable.ID, idValue);
                db.insertOrThrow(UnitTable.TABLE_NAME, null, values);
            }
        } catch (SQLiteConstraintException e) {
            Logger.e("ShopOpenHelper.tryInsertUpdateUnit(): constraint violation; values: " + values);
            throw e;
        }
    }

    public void clearTableInExtraDatabase(String tableName) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(String.format(SQL_CLEAR_TABLE_IN_DB, tableName));
    }

    public synchronized ContentValues insert(String tableName, ContentValues[] valuesArray, String idColumn, boolean supportUpdateTimeLocal) {

        ContentValues currentValues = new ContentValues();

        if (TextUtils.isEmpty(tableName) || valuesArray == null || valuesArray.length == 0)
            return currentValues;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            for (ContentValues values : valuesArray) {
                currentValues = values;
                insertUpdateValues(db, tableName, idColumn, values, supportUpdateTimeLocal);
            }

            db.setTransactionSuccessful();
            return null;

        } catch (Exception e) {
            Logger.e("SyncOpenHelper.insert(): tableName: " + tableName + " error", e);

        } finally {
            db.endTransaction();
        }
        return currentValues;
    }

}
