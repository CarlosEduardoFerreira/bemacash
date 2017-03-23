package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ShiftTable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;
import static com.kaching123.tcr.util.CursorUtil._selectionArgs;
import static com.kaching123.tcr.util.DateUtils.getStartOfDay;

/**
 * Created by gdubina on 03/12/13.
 */
public class ShiftModel implements IValueModel {

    protected static final Uri URI_SHIFT = ShopProvider.getContentUri(ShiftTable.URI_CONTENT);
    protected static final Uri URI_SALE_ORDER = ShopProvider.getContentUri(ShopStore.SaleOrderTable.URI_CONTENT);

    public String guid;
    public Date startTime;
    public Date endTime;

    public String openManagerId;
    public String closeManagerId;

    public long registerId;
    public BigDecimal openAmount;
    public BigDecimal closeAmount;

    private List<String> mIgnoreFields;

    public ShiftModel(Cursor cursor) {
        this(cursor.getString(cursor.getColumnIndex(ShiftTable.GUID)),
                _nullableDate(cursor, cursor.getColumnIndex(ShiftTable.START_TIME)),
                _nullableDate(cursor, cursor.getColumnIndex(ShiftTable.END_TIME)),
                cursor.getString(cursor.getColumnIndex(ShiftTable.OPEN_MANAGER_ID)),
                cursor.getString(cursor.getColumnIndex(ShiftTable.CLOSE_MANAGER_ID)),
                cursor.getLong(cursor.getColumnIndex(ShiftTable.REGISTER_ID)),
                _decimal(cursor, cursor.getColumnIndex(ShiftTable.OPEN_AMOUNT), BigDecimal.ZERO),
                _decimal(cursor, cursor.getColumnIndex(ShiftTable.CLOSE_AMOUNT), BigDecimal.ZERO),
                null);
    }

    public static ShiftModel getInstance(Cursor cursor) {
        return new ShiftModel(cursor.getString(cursor.getColumnIndex(ShiftTable.GUID)),
                new Date(cursor.getLong(cursor.getColumnIndex(ShiftTable.START_TIME))),
                new Date(cursor.getLong(cursor.getColumnIndex(ShiftTable.END_TIME))),
                cursor.getString(cursor.getColumnIndex(ShiftTable.OPEN_MANAGER_ID)),
                cursor.getString(cursor.getColumnIndex(ShiftTable.CLOSE_MANAGER_ID)),
                cursor.getLong(cursor.getColumnIndex(ShiftTable.REGISTER_ID)),
                _decimal(cursor, cursor.getColumnIndex(ShiftTable.OPEN_AMOUNT), BigDecimal.ZERO),
                _decimal(cursor, cursor.getColumnIndex(ShiftTable.CLOSE_AMOUNT), BigDecimal.ZERO),
                null);
    }

    public static ShiftModel getById(Context context, String guid) {
        final Cursor c = ProviderAction.query(URI_SHIFT)
                .where(ShiftTable.GUID + " = ?", guid)
                .perform(context);
        ShiftModel model = null;
        if (c.moveToNext()) {
            model = ShiftModel.getInstance(c);
            c.close();
        }
        return model;
    }

    public static List<String> getDailyGuidList(Context context) {
        final Cursor c = ProviderAction.query(URI_SHIFT)
                .where(ShiftTable.START_TIME + " > ? OR " + ShiftTable.END_TIME + " > ? OR "
                                + ShiftTable.END_TIME + " IS NULL", getStartOfDay().getTime(),
                        getStartOfDay().getTime())
                .perform(context);
        final List<String> guidList = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                final String currentGuid = c.getString(c.getColumnIndex(ShiftTable.GUID));
                guidList.add(currentGuid);
            } while (c.moveToNext());
            c.close();
        }
        return guidList;
    }

    public static List<String> getDailyGuidList(Context context, long registerID, long fromDate, long toDate) {
        Uri uri;
        String[] selectionArgs;

        if (registerID == 0) {
            uri = ShopProvider.contentUri(ShopStore.SaleOrderDailyRawQuery.URI_CONTENT);
            selectionArgs = _selectionArgs(fromDate, toDate);
        } else {
            uri = ShopProvider.contentUri(ShopStore.SaleOrderDailyRegisterRawQuery.URI_CONTENT);
            selectionArgs = _selectionArgs(fromDate, toDate, registerID);
        }

        Cursor c = context.getContentResolver().query(
                uri,
                null,
                null,
                selectionArgs,
                null
        );

        final List<String> guidList = new ArrayList<>();

        if (c != null && c.moveToFirst()) {
            do {
                final String currentGuid = c.getString(c.getColumnIndex(ShopStore.SaleOrderTable.GUID));
                guidList.add(currentGuid);
            } while (c.moveToNext());
            c.close();
        }
        return guidList;
    }

    public static String getLastDailyGuid(Context context, long registerId) {
        String lastShiftGuid = null;
        Cursor c = null;
        Query query = ProviderAction.query(URI_SHIFT)
                .projection(ShiftTable.GUID);
        if (registerId == 0)
            c = query.orderBy(ShiftTable.START_TIME + " DESC")
                    .perform(context);
        else
            c = query.where(ShiftTable.REGISTER_ID + " = ?", registerId)
                    .orderBy(ShiftTable.START_TIME + " DESC")
                    .perform(context);
        if (c.moveToFirst()) {
            lastShiftGuid = c.getString(c.getColumnIndex(ShiftTable.GUID));
        }
        c.close();
        return lastShiftGuid;
    }

    public ShiftModel(String guid, Date startTime, Date endTime, String openManagerId,
                      String closeManagerId, long registerId, BigDecimal openAmount,
                      BigDecimal closeAmount, List<String> ignoreFields) {
        this.guid = guid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.openManagerId = openManagerId;
        this.closeManagerId = closeManagerId;
        this.registerId = registerId;
        this.openAmount = openAmount;
        this.closeAmount = closeAmount;

        this.mIgnoreFields = ignoreFields;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(ShiftTable.GUID)) v.put(ShiftTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShiftTable.START_TIME)) _nullableDate(v, ShiftTable.START_TIME, startTime);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShiftTable.END_TIME)) _nullableDate(v, ShiftTable.END_TIME, endTime);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShiftTable.OPEN_MANAGER_ID)) v.put(ShiftTable.OPEN_MANAGER_ID, openManagerId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShiftTable.CLOSE_MANAGER_ID)) v.put(ShiftTable.CLOSE_MANAGER_ID, closeManagerId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShiftTable.REGISTER_ID)) v.put(ShiftTable.REGISTER_ID, registerId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShiftTable.OPEN_AMOUNT)) v.put(ShiftTable.OPEN_AMOUNT, _decimal(openAmount));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShiftTable.CLOSE_AMOUNT)) v.put(ShiftTable.CLOSE_AMOUNT, _decimal(closeAmount));
        return v;
    }

    @Override
    public String getIdColumn() {
        return ShiftTable.GUID;
    }

    public ContentValues toUpdateValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(ShiftTable.END_TIME)) _nullableDate(v, ShiftTable.END_TIME, endTime);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShiftTable.CLOSE_MANAGER_ID)) v.put(ShiftTable.CLOSE_MANAGER_ID, closeManagerId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShiftTable.CLOSE_AMOUNT)) v.put(ShiftTable.CLOSE_AMOUNT, _decimal(closeAmount));
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShiftModel that = (ShiftModel) o;

        return guid.equals(that.guid);

    }

    @Override
    public int hashCode() {
        return guid.hashCode();
    }
}
