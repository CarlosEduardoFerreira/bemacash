package com.kaching123.tcr.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.util.ContentValuesUtilBase;

import java.util.Locale;

/**
 * Created by pkabakov on 25.09.2014.
 */
public class SyncOpenHelper extends BaseOpenHelper {

    private static final String DB_NAME_PREFIX = "sync_";

    private static final String SQL_CLEAR_TABLE = "DELETE FROM %s;";

    private static final String MIN_UPDATE_TIME_QUERY = "select " + ShopStore.DEFAULT_UPDATE_TIME + " from %1$s where " + ShopStore.DEFAULT_UPDATE_TIME + " is not null order by " + ShopStore.DEFAULT_UPDATE_TIME + " limit 1";
    private static final String MIN_UPDATE_TIME_PARENT_RELATIONS_QUERY = "select " + ShopStore.DEFAULT_UPDATE_TIME + " from %1$s where " + ShopStore.DEFAULT_UPDATE_TIME + " is not null and %2$s is %3$s order by " + ShopStore.DEFAULT_UPDATE_TIME + " limit 1";

    protected static String getDbName() {
        return DB_NAME_PREFIX + BaseOpenHelper.getDbName();
    }


    public SyncOpenHelper(Context context) {
        super(context, getDbName(), null, getDbVersion());
    }

    @Override
    protected boolean isWriteAheadLoggingEnabled() {
        return false;
    }

    @Override
    protected boolean isForeignKeysEnabled() {
        return false;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        upgradeDrop(db);
    }

    @Override
    protected void clearDbRelatedPreferences() {
        //do nothing
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ShopSchemaEx.onCreate(db, true, true);
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
                    if (tableName.equals(ShopStore.SaleOrderTable.TABLE_NAME) && values.containsKey(ShopStore.SaleOrderTable.STATUS)) {
                        try (
                                Cursor c = db.query(ShopStore.SaleOrderTable.TABLE_NAME, new String[]{ShopStore.SaleOrderTable.STATUS},
                                        ShopStore.SaleOrderTable.GUID + " = ?", new String[]{idValue}, null, null, null)) {
                            if (c != null && c.moveToFirst()){
                                int status = c.getInt(0);
                                if (status != 0){
                                    values.put(ShopStore.SaleOrderTable.STATUS, status);
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

            if (ShopStore.UnitTable.TABLE_NAME.equals(tableName)) {
                values.put(idColumn, idValue);
                if (tryFixUnit(db, values)) {
                    tryInsertUpdateUnit(db, values, hasData);
                    return;
                }
            }
            throw e;
        }
    }


    private boolean tryFixUnit(SQLiteDatabase db, ContentValues values) {
        Unit.Status status = Unit.Status.values()[values.getAsInteger(ShopStore.UnitTable.STATUS)];
        String saleOrderId = values.getAsString(ShopStore.UnitTable.SALE_ORDER_ID);
        String childOrderId = values.getAsString(ShopStore.UnitTable.CHILD_ORDER_ID);

        //unit doesn't belong to any order
        if (TextUtils.isEmpty(saleOrderId) && TextUtils.isEmpty(childOrderId)) {
            Logger.e("ShopOpenHelper.copyTableFromExtraDatabase(): failed to fix unit, not referring to any order; values: " + values);
            return false;
        }

        //unit is sold
        if (status == Unit.Status.SOLD) {
            //check which order is missing
            Cursor c = db.query(ShopStore.SaleOrderTable.TABLE_NAME, new String[]{"1"},
                    ShopStore.SaleOrderTable.GUID + " = ?", new String[]{saleOrderId}, null, null, null);
            boolean hasSaleOrder = c.getCount() > 0;
            c.close();

            if (hasSaleOrder) {
                if (TextUtils.isEmpty(childOrderId)) {
                    Logger.e("ShopOpenHelper.copyTableFromExtraDatabase(): failed to fix unit, sold but not referring to any missing order; values: " + values);
                    return false;
                }
                values.putNull(ShopStore.UnitTable.CHILD_ORDER_ID);
                return true;
            }

            //insert sold unit without order ids - will de removed later
            values.putNull(ShopStore.UnitTable.SALE_ORDER_ID);
            values.putNull(ShopStore.UnitTable.CHILD_ORDER_ID);
            return true;
        }
        //unit is active
        if (TextUtils.isEmpty(childOrderId)) {
            Logger.e("ShopOpenHelper.copyTableFromExtraDatabase(): failed to fix unit, it belongs to active order; values: " + values);
            return false;
        }
        //unit is refund
        if (TextUtils.isEmpty(saleOrderId)) {
            values.putNull(ShopStore.UnitTable.CHILD_ORDER_ID);
            return true;
        }
        //unit is active or refunded
        //TODO: active orders should be loaded separately from the server(before orders)
        //check if sale order is missing
        Cursor c = db.query(ShopStore.SaleOrderTable.TABLE_NAME, new String[]{"1"},
                ShopStore.SaleOrderTable.GUID + " = ?", new String[]{saleOrderId}, null, null, null);
        boolean hasSaleOrder = c.getCount() > 0;

        if (hasSaleOrder) {
            values.putNull(ShopStore.UnitTable.CHILD_ORDER_ID);
            return true;
        }
        //should be refund unit
        //active orders should be loaded separately from the server(before orders)
        values.putNull(ShopStore.UnitTable.SALE_ORDER_ID);
        values.putNull(ShopStore.UnitTable.CHILD_ORDER_ID);
        return true;
    }


    private void tryInsertUpdateUnit(SQLiteDatabase db, ContentValues values, Boolean hasData) {
        try {
            String idValue = null;
            int updated = 0;
            if (hasData == null || hasData) {
                idValue = values.getAsString(ShopStore.UnitTable.ID);
                values.remove(ShopStore.UnitTable.ID);
                updated = db.update(ShopStore.UnitTable.TABLE_NAME, values, ShopStore.UnitTable.ID + " = ?", new String[]{idValue});
            }
            if (hasData != null && hasData && updated == 0) {
                throw new SQLiteException();
            }

            if ((hasData == null && updated == 0) || (hasData != null && !hasData)) {
                if (idValue != null)
                    values.put(ShopStore.UnitTable.ID, idValue);
                db.insertOrThrow(ShopStore.UnitTable.TABLE_NAME, null, values);
            }
        } catch (SQLiteConstraintException e) {
            Logger.e("ShopOpenHelper.tryInsertUpdateUnit(): constraint violation; values: " + values);
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



    private String getWhereUpdateTimeLocal(ContentValues values){
        if (TextUtils.isEmpty(values.getAsString(ShopStore.DEFAULT_UPDATE_TIME_LOCAL))) return "";
        return String.format(" AND (%s <= %s OR %s IS NULL) ", ShopStore.DEFAULT_UPDATE_TIME_LOCAL, values.get(ShopStore.DEFAULT_UPDATE_TIME_LOCAL), ShopStore.DEFAULT_UPDATE_TIME_LOCAL);
    }

    public synchronized boolean insert(String tableName, ContentValues[] valuesArray) {
        return insert(tableName, valuesArray, true);
    }

    public synchronized boolean insert(String tableName, ContentValues[] valuesArray, boolean inTransaction) {
        if (TextUtils.isEmpty(tableName) || valuesArray == null || valuesArray.length == 0)
            return false;

        SQLiteDatabase db = getWritableDatabase();
        if (inTransaction)
            db.beginTransaction();
        try {
            for (ContentValues values : valuesArray) {
                long id = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                if (id == -1L) {
                    Logger.e("SyncOpenHelper.insert(): can't insert values: " + values + "; tableName: " + tableName);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Logger.e("SyncOpenHelper.insert(): tableName: " + tableName + " error", e);
        } finally {
            if (inTransaction) {
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        }
        return false;
    }

    public synchronized void clearTables(String[] tableNames) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (String tableName: tableNames) {
                db.delete(tableName, null, null);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public synchronized Cursor getMaxUpdateTime(String[] selectionArgs) {
        return ProviderQueryHelper.getMaxUpdateTime(this, selectionArgs);
    }

    public synchronized Cursor getMaxUpdateParentTime(String[] selectionArgs) {
        return ProviderQueryHelper.getMaxUpdateParentTime(this, selectionArgs);
    }

    public synchronized Cursor getMinUpdateTime(String tableName) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(String.format(Locale.US, MIN_UPDATE_TIME_QUERY, new String[]{tableName}), null);
    }

    public synchronized Cursor getMinUpdateParentTime(String tableName, String parentIdColumn, boolean isChild) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(String.format(Locale.US, MIN_UPDATE_TIME_PARENT_RELATIONS_QUERY, new String[]{tableName, parentIdColumn, isChild ? " not null " : " null "}), null);
    }


    public void saveMaxUpdateTime(SQLiteDatabase db, String tableName, boolean isParent, long updateTime, String guid) {
        SyncCommand.Table table = SyncCommand.Table.getTable(tableName, isParent);

        ContentValues values = new ContentValues();
        values.put(ShopStore.UpdateTimeTable.UPDATE_TIME, updateTime);
        values.put(ShopStore.UpdateTimeTable.GUID, guid);

        String tableId = String.valueOf(ContentValuesUtilBase._enum(table));
        int updated = db.update(ShopStore.UpdateTimeTable.TABLE_NAME, values,
                ShopStore.UpdateTimeTable.TABLE_ID + " = ?"
                        + " AND (" + ShopStore.UpdateTimeTable.UPDATE_TIME + " < ?"
                        + " OR (" + ShopStore.UpdateTimeTable.UPDATE_TIME + " = ? AND " + ShopStore.UpdateTimeTable.GUID + " < ?))",
                new String[]{tableId, String.valueOf(updateTime), String.valueOf(updateTime), guid});

        if (updated != 0) {
            return;
        }

        Cursor c = db.rawQuery("SELECT " + ShopStore.UpdateTimeTable.UPDATE_TIME + " FROM " + ShopStore.UpdateTimeTable.TABLE_NAME
                        + " WHERE " + ShopStore.UpdateTimeTable.UPDATE_TIME + " > ? AND " + ShopStore.UpdateTimeTable.TABLE_ID + " = ?",
                new String[]{String.valueOf(updateTime), tableId});
        if (c.moveToFirst()) {
            return;
        }
        values.put(ShopStore.UpdateTimeTable.TABLE_ID, ContentValuesUtilBase._enum(table));
        long rowId = db.insertWithOnConflict(ShopStore.UpdateTimeTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (rowId == -1L)
            throw new SQLiteException();
    }
}
