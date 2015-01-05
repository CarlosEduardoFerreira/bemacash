package com.kaching123.tcr.model;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.net.Uri;

import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SqlCommandTable;

/**
 * Created by gdubina on 20/11/13.
 */
public final class SqlCommandHelper {

    private SqlCommandHelper() {
    }

    private static final Uri URI_SQL_COMMAND = ShopProvider.getContentUri(SqlCommandTable.URI_CONTENT);

    public static ContentProviderOperation addSqlCommand(SingleSqlCommand sql) {
        BatchSqlCommand batch = new BatchSqlCommand(sql);
        return addSqlCommands(batch);
    }

    public static ContentProviderOperation addSqlCommands(BatchSqlCommand batchSql) {
        ContentValues values = getContentValues(batchSql);
        return ContentProviderOperation.newInsert(URI_SQL_COMMAND).withValues(values).build();
    }

    private static ContentValues getContentValues(BatchSqlCommand batchSql) {
        ContentValues values = new ContentValues();
        values.put(SqlCommandTable.CREATE_TIME, System.currentTimeMillis());
        values.put(SqlCommandTable.SQL_COMMAND, batchSql.toJson());
        return values;
    }

/*    public static boolean groupSqlCommands(Context context, String currentTransactionId) {

        Cursor c = context.getContentResolver().query(URI_SQL_COMMAND,
                new String[]{SqlCommandTable.ID, SqlCommandTable.SQL_COMMAND},
                SqlCommandTable.TRANSACTION_ID + " = ? and " + SqlCommandTable.IS_READY + " = ?",
                new String[]{currentTransactionId, "0"}, null);

        try {
            BatchSqlCommand batch = null;
            long mainId = 0;
            if (c.moveToFirst()) {
                mainId = c.getLong(0);
                String json = c.getString(1);
                batch = BatchSqlCommand.fromJson(json);
            } else {
                return false;
            }

            ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
            while (c.moveToNext()) {
                long id = c.getLong(0);
                String json = c.getString(1);
                batch.add(BatchSqlCommand.fromJson(json));
                operations.add(ContentProviderOperation.newDelete(ShopProvider.getContentUri(SqlCommandTable.URI_CONTENT, id)).build());
            }
            operations.add(ContentProviderOperation.newUpdate(ShopProvider.getContentUri(SqlCommandTable.URI_CONTENT, mainId)).withValues(getContentValues(batch, null)).build());
            context.getContentResolver().applyBatch(ShopProvider.AUTHORITY, operations);
            ShopProvider.notifyUri(context.getContentResolver(), URI_SQL_COMMAND);
            return true;
        } catch (JSONException e) {
            Logger.e("can't group commands", e);
            return false;
        } catch (RemoteException e) {
            Logger.e("can't update commands", e);
            return false;
        } catch (OperationApplicationException e) {
            Logger.e("can't update commands", e);
            return false;
        } finally {
            c.close();
        }
    }*/
}
