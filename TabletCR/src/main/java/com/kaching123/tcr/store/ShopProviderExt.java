package com.kaching123.tcr.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.kaching123.tcr.store.helper.RecalcItemMovementTable;
import com.kaching123.tcr.store.helper.RecalcSaleAddonTable;
import com.kaching123.tcr.store.helper.RecalcSaleItemTable;

/*import com.kaching123.tcr.store.ShopStore.MaxUpdateTableChildTimeQuery;
import com.kaching123.tcr.store.ShopStore.MaxUpdateTableParentTimeQuery;*/

/**
 * Created by gdubina on 10/12/13.
 */
public class ShopProviderExt extends ShopProvider {

    protected final static int MATCH_RAW_TABLE_QUERY = 0x666;

    final static String URI_PATH_RAW_TABLE_QUERY = "URI_RAW_TABLE_QUERY";

    /*public static Uri getRawTableQueryUri(String tableName) {
        return contentUri(URI_PATH_RAW_TABLE_QUERY, tableName);
    }*/

    static {
        matcher.addURI(AUTHORITY, URI_PATH_RAW_TABLE_QUERY + "/*", MATCH_RAW_TABLE_QUERY);
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
        public static final String METHOD_CLEAR_TABLE_IN_SYNC_DB = "method_clear_table_in_sync_db";

        public static final String TRANSACTION_START = "method_transaction_start";
        public static final String TRANSACTION_COMMIT = "method_transaction_commit";
        public static final String TRANSACTION_END = "method_transaction_end";
        public static final String TRANSACTION_YIELD = "method_transaction_yield";
    }

    private static final String ARG_METHOD_RESULT = "arg_method_result";

    public static boolean callMethod(Context context, String method, String arg, Bundle extras) {
        Bundle resultBundle = context.getContentResolver().call(ShopProvider.BASE_URI, method, arg, extras);
        return resultBundle == null ? false : resultBundle.getBoolean(ShopProviderExt.ARG_METHOD_RESULT);
    }

    private RecalcItemMovementTable itemMovementHelper;
    private RecalcSaleItemTable saleItemHelper;
    private RecalcSaleAddonTable saleItemAddonHelper;
    private ProviderQueryHelper providerQueryHelper;

    @Override
    public boolean onCreate() {
        boolean b = super.onCreate();
        itemMovementHelper = new RecalcItemMovementTable(getContext(), dbHelper);
        saleItemHelper = new RecalcSaleItemTable(getContext(), dbHelper);
        saleItemAddonHelper = new RecalcSaleAddonTable(getContext(), dbHelper);
        providerQueryHelper = new ProviderQueryHelper(AUTHORITY, dbHelper);
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
        } else if (Method.METHOD_COPY_TABLE_FROM_SYNC_DB.equals(method) && !TextUtils.isEmpty(arg)) {
            ((ShopOpenHelper) dbHelper).copyTableFromExtraDatabase(arg);
        } else if (Method.METHOD_CLEAR_TABLE_IN_SYNC_DB.equals(method) && !TextUtils.isEmpty(arg)) {
            ((ShopOpenHelper) dbHelper).clearTableInExtraDatabase(arg);
        }
        return resultBundle;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = matcher.match(uri);
        if (match == MATCH_RAW_TABLE_QUERY) {
            final SQLiteQueryBuilder query = new SQLiteQueryBuilder();
            query.setTables(uri.getLastPathSegment());
            return query.query(dbHelper.getReadableDatabase(),
                    projection,
                    selection, selectionArgs,
                    UriBuilder.getGroupBy(uri),
                    null, sortOrder,
                    UriBuilder.getLimit(uri));
        }

        Cursor c = providerQueryHelper.query(uri, projection, selection, selectionArgs, sortOrder);
        if (c != null)
            return c;

        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] valuesAr) {
        int count = super.bulkInsert(uri, valuesAr);
        if (count > 0) {
            String path = getUriPath(uri);
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
            //itemHelper.recalculateAvailableQty(values.getAsString(ItemMovementTable.ITEM_GUID));
            itemMovementHelper.recalculateMovementAvailableQty(values.getAsString(ItemMovementTable.ITEM_UPDATE_QTY_FLAG));
        }
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        boolean isDraftReset = values.size() == 1 && values.containsKey(ShopStore.DEFAULT_IS_DRAFT);
        if (isDraftReset) {
            //sync process completed need to recalc all instead bulkinsert
            //need to do it before sync flag will be flushed
            String path = getUriPath(uri);
            if (SaleItemTable.URI_CONTENT.equals(path)) {
                Logger.d("RecalculateOrderPrice: bulkInsert SaleItemTable");
                saleItemHelper.bulkRecalcSaleItemTableAfterSync();
            } else if (SaleAddonTable.URI_CONTENT.equals(path)) {
                Logger.d("RecalculateOrderPrice: bulkInsert SaleAddonTable");
                saleItemAddonHelper.bulkRecalcSaleAddonTableAfterSync();
            } else if (ItemMovementTable.URI_CONTENT.equals(path)) {
                Logger.d("recalculateAvailableQty: bulkInsert ItemMovementTable");
                itemMovementHelper.bulkRecalcAvailableItemMovementTableAfterSync(false);
            } else if (ItemTable.URI_CONTENT.equals(path)) {
                Logger.d("recalculateAvailableQty: bulkInsert ItemTable");
                itemMovementHelper.bulkRecalcAvailableItemMovementTableAfterSync(true);
            } else if (SaleOrderTable.URI_CONTENT.equals(path)){
                Logger.d("recalculateAvailableQty: bulkInsert SaleOrderTable");
                saleItemHelper.bulkRecalculateOrderTotalPriceAfterSync();
            }
        }

        int count = super.update(uri, values, selection, selectionArgs);

        if (!isDraftReset && count > 0 && selectionArgs != null && selectionArgs.length == 1) {
            String path = getUriPath(uri);
            if (SaleItemTable.URI_CONTENT.equals(path)) {
                saleItemHelper.recalculateOrderTotalPriceByItem(selectionArgs[0]);
            } else if (SaleOrderTable.URI_CONTENT.equals(path) && !values.containsKey(SaleOrderTable.TML_TOTAL_PRICE)
                    && !values.containsKey(SaleOrderTable.IS_TIPPED) && !isKitchenStatusUpdate(values)) {
                saleItemHelper.recalculateOrderTotalPrice(selectionArgs[0]);
            }/*else if (ItemTable.URI_CONTENT.equals(path) && !values.containsKey(ItemTable.TMP_AVAILABLE_QTY)
                    && (!values.containsKey(SaleItemTable.IS_DELETED) || !(values.getAsInteger(SaleItemTable.IS_DELETED) != 0))) {
                itemHelper.recalculateAvailableQty(selectionArgs[0]);
            }*/
        }
        return count;
    }

    private boolean isKitchenStatusUpdate(ContentValues values) {
        return values.size() == 1 && values.containsKey(SaleOrderTable.KITCHEN_PRINT_STATUS);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String saleOrderGuid = null;
        if (selectionArgs != null && selectionArgs.length == 1) {
            String path = getUriPath(uri);
            if (SaleItemTable.URI_CONTENT.equals(path) || SaleAddonTable.URI_CONTENT.equals(path)) {
                saleOrderGuid = saleItemHelper.getSaleOrderGuidByItem(selectionArgs[0]);
            }
        }

        int count = super.delete(uri, selection, selectionArgs);

        if (count > 0 && saleOrderGuid != null) {
            String path = getUriPath(uri);
            if (SaleItemTable.URI_CONTENT.equals(path)) {
                saleItemHelper.recalculateOrderTotalPrice(saleOrderGuid);
            } else if (SaleAddonTable.URI_CONTENT.equals(path)) {
                saleItemHelper.recalculateOrderTotalPrice(saleOrderGuid);
            }
        }
        return count;
    }

    private String getUriPath(Uri uri) {
        if (uri == null || uri.getPath() == null)
            return null;
        return uri.getPath().substring(1);
    }

}
