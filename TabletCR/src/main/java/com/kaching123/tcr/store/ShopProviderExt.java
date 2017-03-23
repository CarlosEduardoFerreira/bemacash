package com.kaching123.tcr.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.LoyaltyPointsMovementTable;
import com.kaching123.tcr.store.ShopStore.OldActiveUnitOrdersQuery;
import com.kaching123.tcr.store.ShopStore.OldMovementGroupsQuery;
import com.kaching123.tcr.store.ShopStore.OldSaleOrdersQuery;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.helper.RecalcItemComposerTable;
import com.kaching123.tcr.store.helper.RecalcItemCostTable;
import com.kaching123.tcr.store.helper.RecalcItemMovementTable;
import com.kaching123.tcr.store.helper.RecalcLoyaltyPointsHelper;
import com.kaching123.tcr.store.helper.RecalcSaleAddonTable;
import com.kaching123.tcr.store.helper.RecalcSaleItemTable;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*import com.kaching123.tcr.store.ShopStore.MaxUpdateTableChildTimeQuery;
import com.kaching123.tcr.store.ShopStore.MaxUpdateTableParentTimeQuery;*/

/**
 * Created by gdubina on 10/12/13.
 */
public class ShopProviderExt extends ShopProvider {

    protected final static int MATCH_RAW_TABLE_QUERY = 0x666;
    protected final static int MATCH_OLD_SALE_ORDERS_QUERY = 0x667;
    protected final static int MATCH_OLD_MOVEMENT_GROUPS_QUERY = 0x668;
    protected final static int MATCH_OLD_ACTIVE_UNIT_ORDERS_QUERY = 0x669;

    final static String URI_PATH_RAW_TABLE_QUERY = "URI_RAW_TABLE_QUERY";

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    protected void scheduleUpdate() {
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                composerHelper.recalcAfterSync();
                costComposerHelper.recalcAfterSync();
                contentResolver.notifyChange(contentUri("items_ext_view"), null);
            }
        }, 500L, TimeUnit.MILLISECONDS);
    }


    /*public static Uri getRawTableQueryUri(String tableName) {
        return contentUri(URI_PATH_RAW_TABLE_QUERY, tableName);
    }*/

    static {
        matcher.addURI(AUTHORITY, URI_PATH_RAW_TABLE_QUERY + "/*", MATCH_RAW_TABLE_QUERY);
        matcher.addURI(AUTHORITY, OldSaleOrdersQuery.CONTENT_PATH, MATCH_OLD_SALE_ORDERS_QUERY);
        matcher.addURI(AUTHORITY, OldMovementGroupsQuery.CONTENT_PATH, MATCH_OLD_MOVEMENT_GROUPS_QUERY);
        matcher.addURI(AUTHORITY, OldActiveUnitOrdersQuery.CONTENT_PATH, MATCH_OLD_ACTIVE_UNIT_ORDERS_QUERY);
    }

    public static class Method {
        public static final String METHOD_EXPORT_DATABASE = "method_export_database";
        public static final String METHOD_COPY_TRAINING_DATABASE = "method_copy_training_database";
        public static final String METHOD_CREATE_TRAINING_DATABASE = "method_create_training_database";
        public static final String METHOD_CLEAR_TRAINING_DATABASE = "method_clear_training_database";
        public static final String METHOD_CLEAR_DATABASE_KEEP_SYNC = "method_clear_database_keep_sync";
        public static final String METHOD_ATTACH_SYNC_DB = "method_attach_sync_db";
        public static final String METHOD_DETACH_SYNC_DB = "method_detach_sync_db";
        public static final String METHOD_COPY_TABLE_FROM_SYNC_DB = "method_copy_table_from_sync_db";
        public static final String METHOD_COPY_UPDATE_TABLE_FROM_SYNC_DB = "method_copy_update_table_from_sync_db";
        public static final String METHOD_CLEAR_TABLE_IN_SYNC_DB = "method_clear_table_in_sync_db";
        public static final String METHOD_INSERT_UPDATE = "method_insert_update";

        public static final String TRANSACTION_START = "method_transaction_start";
        public static final String TRANSACTION_COMMIT = "method_transaction_commit";
        public static final String TRANSACTION_END = "method_transaction_end";
        public static final String TRANSACTION_YIELD = "method_transaction_yield";

        public static final String METHOD_VACUUM = "method_vacuum";
    }

    private static final String ARG_METHOD_RESULT = "arg_method_result";

    private static final String KEY_TABLE_NAME = "KEY_TABLE_NAME";
    private static final String KEY_ID_COLUMN = "KEY_ID_COLUMN";
    private static final String KEY_PARENT_ID_COLUMN = "KEY_PARENT_ID_COLUMN";
    private static final String KEY_VALUES_ARRAY = "KEY_VALUES_ARRAY";

    public static boolean callMethod(Context context, String method, String arg, Bundle extras) {
        Bundle resultBundle = context.getContentResolver().call(ShopProvider.BASE_URI, method, arg, extras);
        return resultBundle == null ? false : resultBundle.getBoolean(ShopProviderExt.ARG_METHOD_RESULT);
    }

    public static void copyTableFromSyncDb(Context context, String tableName, String idColumn, String parentIdColumn) {
        Bundle extras = new Bundle();
        extras.putString(KEY_TABLE_NAME, tableName);
        extras.putString(KEY_ID_COLUMN, idColumn);
        extras.putString(KEY_PARENT_ID_COLUMN, parentIdColumn);
        callMethod(context, Method.METHOD_COPY_TABLE_FROM_SYNC_DB, null, extras);
    }

    public static void copyUpdateTableFromSyncDb(Context context, String tableName, String idColumn, String parentIdColumn) {
        Bundle extras = new Bundle();
        extras.putString(KEY_TABLE_NAME, tableName);
        extras.putString(KEY_ID_COLUMN, idColumn);
        extras.putString(KEY_PARENT_ID_COLUMN, parentIdColumn);
        callMethod(context, Method.METHOD_COPY_UPDATE_TABLE_FROM_SYNC_DB, null, extras);
    }

    public static void insertUpdateValues(Context context, String tableName, String idColumn, ContentValues[] valuesArray) {
        Bundle extras = new Bundle();
        extras.putString(KEY_TABLE_NAME, tableName);
        extras.putString(KEY_ID_COLUMN, idColumn);
        extras.putParcelableArray(KEY_VALUES_ARRAY, valuesArray);
        callMethod(context, Method.METHOD_INSERT_UPDATE, null, extras);
    }

    private RecalcItemMovementTable itemMovementHelper;
    private RecalcSaleItemTable saleItemHelper;
    private RecalcSaleAddonTable saleItemAddonHelper;
    private ProviderQueryHelper providerQueryHelper;
    private RecalcItemComposerTable composerHelper;
    private RecalcItemCostTable costComposerHelper;
    private RecalcLoyaltyPointsHelper loyaltyPointsHelper;

    @Override
    public boolean onCreate() {
        boolean b = super.onCreate();
        itemMovementHelper = new RecalcItemMovementTable(getContext(), dbHelper);
        saleItemHelper = new RecalcSaleItemTable(getContext(), dbHelper);
        saleItemAddonHelper = new RecalcSaleAddonTable(getContext(), dbHelper);
        providerQueryHelper = new ProviderQueryHelper(AUTHORITY, dbHelper);
        composerHelper = new RecalcItemComposerTable(getContext(), dbHelper);
        costComposerHelper = new RecalcItemCostTable(getContext(), dbHelper);
        loyaltyPointsHelper = new RecalcLoyaltyPointsHelper(getContext(), dbHelper);

        ShopStore.init();
        return b;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        Bundle resultBundle = null;

        if (Method.TRANSACTION_START.equalsIgnoreCase(method)) {
            Logger.d("beginTransaction");
            dbHelper.getWritableDatabase().beginTransaction();
            return null;
        }
        if (Method.TRANSACTION_COMMIT.equalsIgnoreCase(method)) {
            Logger.d("setTransactionSuccessful");
            dbHelper.getWritableDatabase().setTransactionSuccessful();
            return null;
        }
        if (Method.TRANSACTION_END.equalsIgnoreCase(method)) {
            Logger.d("endTransaction");
            dbHelper.getWritableDatabase().endTransaction();
            return null;
        }
        if (Method.TRANSACTION_YIELD.equalsIgnoreCase(method)) {
            Logger.d("yieldTransaction");
            boolean result = dbHelper.getWritableDatabase().yieldIfContendedSafely();
            resultBundle = new Bundle();
            resultBundle.putBoolean(ARG_METHOD_RESULT, result);
        }

        if (Method.METHOD_EXPORT_DATABASE.equals(method) && !TextUtils.isEmpty(arg)) {
            boolean result = ((ShopOpenHelper) dbHelper).exportDatabase(getContext(), arg);
            resultBundle = new Bundle();
            resultBundle.putBoolean(ARG_METHOD_RESULT, result);
        } else if (Method.METHOD_COPY_TRAINING_DATABASE.equals(method)) {
            boolean result = ((ShopOpenHelper) dbHelper).copyToTrainingDatabase(getContext());
            resultBundle = new Bundle();
            resultBundle.putBoolean(ARG_METHOD_RESULT, result);
        } else if (Method.METHOD_CREATE_TRAINING_DATABASE.equals(method)) {
            boolean result = ((ShopOpenHelper) dbHelper).createTrainingDatabase();
            resultBundle = new Bundle();
            resultBundle.putBoolean(ARG_METHOD_RESULT, result);
        } else if (Method.METHOD_CLEAR_TRAINING_DATABASE.equals(method)) {
            boolean result = ((ShopOpenHelper) dbHelper).clearTrainingDatabase();
            resultBundle = new Bundle();
            resultBundle.putBoolean(ARG_METHOD_RESULT, result);
        } else if (Method.METHOD_CLEAR_DATABASE_KEEP_SYNC.equals(method)) {
            boolean result = ((ShopOpenHelper) dbHelper).clearDatabaseKeepSync();
            resultBundle = new Bundle();
            resultBundle.putBoolean(ARG_METHOD_RESULT, result);
        } else if (Method.METHOD_ATTACH_SYNC_DB.equals(method)) {
            ((ShopOpenHelper) dbHelper).attachSyncAsExtraDatabase();
        } else if (Method.METHOD_DETACH_SYNC_DB.equals(method)) {
            ((ShopOpenHelper) dbHelper).detachExtraDatabase();
        } else if (Method.METHOD_COPY_TABLE_FROM_SYNC_DB.equals(method) && extras != null && !extras.isEmpty()) {
            ((ShopOpenHelper) dbHelper).copyTableFromExtraDatabase(getContext(),extras.getString(KEY_TABLE_NAME), extras.getString(KEY_ID_COLUMN), extras.getString(KEY_PARENT_ID_COLUMN));
        } else if (Method.METHOD_COPY_UPDATE_TABLE_FROM_SYNC_DB.equals(method) && extras != null && !extras.isEmpty()) {
            ((ShopOpenHelper) dbHelper).copyUpdateTableFromExtraDatabase(getContext(),extras.getString(KEY_TABLE_NAME), extras.getString(KEY_ID_COLUMN), extras.getString(KEY_PARENT_ID_COLUMN));
        } else if (Method.METHOD_CLEAR_TABLE_IN_SYNC_DB.equals(method) && !TextUtils.isEmpty(arg)) {
            ((ShopOpenHelper) dbHelper).clearTableInExtraDatabase(arg);
        } else if (Method.METHOD_VACUUM.equalsIgnoreCase(method)) {
            boolean result = ((ShopOpenHelper) dbHelper).vacuum();
            resultBundle = new Bundle();
            resultBundle.putBoolean(ARG_METHOD_RESULT, result);
        } else if (Method.METHOD_INSERT_UPDATE.equalsIgnoreCase(method) && extras != null && !extras.isEmpty()) {
            ((ShopOpenHelper) dbHelper).insertUpdateValues(extras.getString(KEY_TABLE_NAME), extras.getString(KEY_ID_COLUMN), (ContentValues[]) extras.getParcelableArray(KEY_VALUES_ARRAY));
        }
        return resultBundle;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        int match = matcher.match(uri);
        if (match == MATCH_RAW_TABLE_QUERY || (selection != null && selection.contains("(is_deleted = 0 OR is_deleted = 1)"))) {
            final SQLiteQueryBuilder query = new SQLiteQueryBuilder();
            query.setTables(uri.getLastPathSegment());
            return query.query(dbHelper.getReadableDatabase(),
                    projection,
                    selection, selectionArgs,
                    UriBuilder.getGroupBy(uri),
                    null, sortOrder,
                    UriBuilder.getLimit(uri));
        }
        if (match == MATCH_OLD_SALE_ORDERS_QUERY) {
            return dbHelper.getReadableDatabase().rawQuery(String.format(Locale.US, OldSaleOrdersQuery.QUERY, selectionArgs), null);
        }
        if (match == MATCH_OLD_MOVEMENT_GROUPS_QUERY) {
            return dbHelper.getReadableDatabase().rawQuery(String.format(Locale.US, OldMovementGroupsQuery.QUERY, selectionArgs), null);
        }
        if (match == MATCH_OLD_ACTIVE_UNIT_ORDERS_QUERY) {
            return dbHelper.getReadableDatabase().rawQuery(String.format(Locale.US, OldActiveUnitOrdersQuery.QUERY, selectionArgs), null);
        }

        Cursor c = providerQueryHelper.query(uri, projection, selection, selectionArgs, sortOrder);
        if (c != null)
            return c;

        if(selection != null) selection = selection.equals("()") ? "" : selection;
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] valuesAr) {
        Log.d("BemaCarl1","ShopProviderExt.bulkInsert");
        int count = super.bulkInsert(uri, valuesAr);
        if (count > 0) {
            String path = getUriPath(uri);
            Log.d("BemaCarl1","ShopProviderExt.bulkInsert.path: " + path);
            if (SaleItemTable.URI_CONTENT.equals(path)) {
                Logger.d("RecalculateOrderPrice: bulkInsert SaleItemTable");
                saleItemHelper.bulkRecalcSaleItemTable(valuesAr);
            } else if (SaleAddonTable.URI_CONTENT.equals(path)) {
                Logger.d("RecalculateOrderPrice: bulkInsert SaleAddonTable");
                saleItemAddonHelper.bulkRecalcSaleAddonTable(valuesAr);
            } else if (ItemMovementTable.URI_CONTENT.equals(path)) {
                Logger.d("recalculateAvailableQty: bulkInsert ItemMovementTable");
                itemMovementHelper.bulkRecalcAvailableItemMovementTable(valuesAr);
            } /*else if (ItemTable.URI_CONTENT.equals(path)) {
                //TODO: depends on download sync logic!
                Logger.d("recalculateAvailableQty: bulkInsert ItemTable");
                itemHelper.bulkRecalcAvailableItemTable(valuesAr);
            }*/
        }
        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result = super.insert(uri, values);
        String path = getUriPath(uri);
        if (SaleItemTable.URI_CONTENT.equals(path)) {
            saleItemHelper.recalculateOrderTotalPrice(values.getAsString(SaleItemTable.ORDER_GUID));
            saleItemHelper.recalculateSaleItemsAvailableQty(values.getAsString(SaleItemTable.PARENT_GUID));
        } else if (SaleAddonTable.URI_CONTENT.equals(path)) {
            saleItemHelper.recalculateOrderTotalPriceByItem(values.getAsString(SaleAddonTable.ITEM_GUID));
        } else if (ItemMovementTable.URI_CONTENT.equals(path)) {
            itemMovementHelper.recalculateMovementAvailableQty(values.getAsString(ItemMovementTable.ITEM_UPDATE_QTY_FLAG));
            scheduleUpdate();
        } else if (LoyaltyPointsMovementTable.URI_CONTENT.equals(path)){
            loyaltyPointsHelper.recalculateCustomerLoyaltyPoints(values.getAsString(LoyaltyPointsMovementTable.CUSTOMER_ID));
        }
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String path = getUriPath(uri);
        if (ShopStore.ComposerTable.URI_CONTENT.equals(path)){
            scheduleUpdate();
        }
        return super.delete(uri, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        boolean isDraftReset = values.size() == 1 && values.containsKey(ShopStore.DEFAULT_IS_DRAFT);
        String path = getUriPath(uri);
        if (isDraftReset) {
            //sync process completed need to recalc all instead bulkinsert
            //need to do it before sync flag will be flushed
            if (SaleItemTable.URI_CONTENT.equals(path)) {
                Logger.d("RecalculateOrderPrice: bulkInsert SaleItemTable");
                saleItemHelper.bulkRecalcSaleItemTableAfterSync();
            } else if (SaleAddonTable.URI_CONTENT.equals(path)) {
                Logger.d("RecalculateOrderPrice: bulkInsert SaleAddonTable");
                saleItemAddonHelper.bulkRecalcSaleAddonTableAfterSync();
            } else if (ItemMovementTable.URI_CONTENT.equals(path)) {
                Logger.d("recalculateAvailableQty: bulkInsert ItemMovementTable");
                itemMovementHelper.bulkRecalcAvailableItemMovementTableAfterSync(false);
                scheduleUpdate();
            } else if (ItemTable.URI_CONTENT.equals(path)) {
                Logger.d("recalculateAvailableQty: bulkInsert ItemTable");
                itemMovementHelper.bulkRecalcAvailableItemMovementTableAfterSync(true);
                scheduleUpdate();
            } else if (SaleOrderTable.URI_CONTENT.equals(path)) {
                Logger.d("recalculateAvailableQty: bulkInsert SaleOrderTable");
                saleItemHelper.bulkRecalculateOrderTotalPriceAfterSync();
            }else if (ShopStore.ComposerTable.URI_CONTENT.equals(path)){
                scheduleUpdate();
            }else if (LoyaltyPointsMovementTable.URI_CONTENT.equals(path)){
                loyaltyPointsHelper.bulkRecalcCustomerLoyaltyPointsAfterSync();
            }
        }


        int count = super.update(uri, values, selection, selectionArgs);

        if (!isDraftReset && count > 0 && selectionArgs != null && selectionArgs.length == 1) {
            if (SaleItemTable.URI_CONTENT.equals(path)) {
                saleItemHelper.recalculateOrderTotalPriceByItem(selectionArgs[0]);
            } else if (SaleOrderTable.URI_CONTENT.equals(path) && !values.containsKey(SaleOrderTable.TML_TOTAL_PRICE)
                    && !values.containsKey(SaleOrderTable.IS_TIPPED) && !isKitchenStatusUpdate(values)) {
                saleItemHelper.recalculateOrderTotalPrice(selectionArgs[0]);
            }else if (ItemTable.URI_CONTENT.equals(path)) {
                scheduleUpdate();
            }
        }

        if (!isDraftReset && LoyaltyPointsMovementTable.URI_CONTENT.equals(path)){
            loyaltyPointsHelper.recalculateCustomerLoyaltyPoints2(selectionArgs[0]);
        }

        return count;
    }

    private boolean isKitchenStatusUpdate(ContentValues values) {
        return values.size() == 1 && values.containsKey(SaleOrderTable.KITCHEN_PRINT_STATUS);
    }

    private String getUriPath(Uri uri) {
        if (uri == null || uri.getPath() == null)
            return null;
        return uri.getPath().substring(1);
    }

}
