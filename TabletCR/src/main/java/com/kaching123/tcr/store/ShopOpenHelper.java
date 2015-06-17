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
import com.kaching123.tcr.fragment.dialog.SyncWaitDialogFragment;
import com.kaching123.tcr.model.RegisterModel.RegisterStatus;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.mayer.sql.update.version.IUpdateContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pkabakov on 23.04.2014.
 */
public class ShopOpenHelper extends BaseOpenHelper {

    private static final DateFormat FILE_NAME_DATE_FORMAT = new SimpleDateFormat("MM_dd_yyyy");
    private static final int PAGE_ROWS = 1800;

    private static final String EXTRA_DB_ALIAS = "\'extraDb\'";
    private static final String SQL_ATTACH_DB = "ATTACH DATABASE \'%s\' AS " + EXTRA_DB_ALIAS + ";";
    private static final String SQL_COPY_TABLE_FROM_DB = "INSERT OR REPLACE INTO %s SELECT * FROM " + EXTRA_DB_ALIAS + ".%s;";
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

    public void copyTableFromExtraDatabase(String tableName, Context context) {
        copyTableFromExtraDatabase(tableName, null, false);
    }

    public void copyUpdateTableFromExtraDatabase(String tableName, String idColumn) {
        copyTableFromExtraDatabase(tableName, idColumn, true);
    }

    public void copyTableFromExtraDatabase(String tableName, String idColumn, boolean insertUpdate) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(EXTRA_DB_ALIAS + '.' + tableName, null, null, null, null, null, null);
        ContentValues values = new ContentValues();
        try {
            while (cursor.moveToNext()) {
                values.clear();
                DatabaseUtils.cursorRowToContentValues(cursor, values);

                if (!insertUpdate)
                    insertValuesFromExtraDatabase(db, tableName, values);
                else
                    insertUpdateValuesFromExtraDatabase(db, tableName, idColumn, values);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    private void insertUpdateValuesFromExtraDatabase(SQLiteDatabase db, String tableName, String idColumn, ContentValues values) {
        try {
            String idValue = values.getAsString(idColumn);
            values.remove(idColumn);
            int updated = db.update(tableName, values, idColumn + " = ?", new String[]{idValue});

            if (updated == 0) {
                values.put(idColumn, idValue);
                db.insertOrThrow(tableName, null, values);
            }
        } catch (SQLiteConstraintException e) {
            Logger.e("ShopOpenHelper.copyTableFromExtraDatabase(): constraint violation, tableName: " + tableName + "; values: " + values);
            throw e;
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
        if (values == null || values.size() == 0)
            return;

        try {
            long rowId = db.insertWithOnConflict(UnitTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if (rowId == -1L)
                throw new SQLiteException();
        } catch (SQLiteConstraintException e) {
            Logger.e("ShopOpenHelper.tryInsertUnit(): constraint violation; values: " + values);
            throw e;
        }
    }

    public void clearTableInExtraDatabase(String tableName) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(String.format(SQL_CLEAR_TABLE_IN_DB, tableName));
    }

}
