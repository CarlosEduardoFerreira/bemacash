package com.kaching123.tcr.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.RegisterModel.RegisterStatus;
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

    private static final String EXTRA_DB_ALIAS = "\'extraDb\'";
    private static final String SQL_ATTACH_DB = "ATTACH DATABASE \'%s\' AS " + EXTRA_DB_ALIAS + ";";
    private static final String SQL_COPY_TABLE_FROM_DB = "INSERT OR REPLACE INTO %s SELECT * FROM " + EXTRA_DB_ALIAS + ".%s;";
    private static final String SQL_DETACH_DB = "DETACH DATABASE " + EXTRA_DB_ALIAS + ";";
    private static final String SQL_CLEAR_TABLE_IN_DB = "DELETE FROM " + EXTRA_DB_ALIAS + ".%s;";

    private TrainingShopOpenHelper trainingShopOpenHelper;

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

        if (oldVersion < IUpdateContainer.VERSION5){
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
                    ShopStore.RegisterTable.REGISTER_SERIAL + " = ? AND "+ ShopStore.RegisterTable.STATUS + " <> ?",
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
            selectionArgs = new String[] {guid};
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

    public void copyTableFromExtraDatabase(String tableName) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(EXTRA_DB_ALIAS + '.' + tableName, null, null, null, null, null, null);
        ContentValues values = new ContentValues();
        try {
            while (cursor.moveToNext()) {
                values.clear();
                DatabaseUtils.cursorRowToContentValues(cursor, values);
                long rowId = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (rowId == -1L)
                    throw new SQLiteException();
            }
        } catch (SQLiteConstraintException e) {
            Logger.e("ShopOpenHelper.copyTableFromExtraDatabase(): constraint violation, tableName: " + tableName + "; values: " + values);
            throw e;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    public void clearTableInExtraDatabase(String tableName) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(String.format(SQL_CLEAR_TABLE_IN_DB, tableName));
    }

}
