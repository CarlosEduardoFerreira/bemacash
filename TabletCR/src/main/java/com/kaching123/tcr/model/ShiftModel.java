package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ShiftTable;

import java.math.BigDecimal;
import java.util.Date;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;

/**
 * Created by gdubina on 03/12/13.
 */
public class ShiftModel implements IValueModel {

    public String guid;
    public Date startTime;
    public Date endTime;

    public String openManagerId;
    public String closeManagerId;

    public long registerId;
    public BigDecimal openAmount;
    public BigDecimal closeAmount;

    public ShiftModel(Cursor cursor) {
        this(cursor.getString(cursor.getColumnIndex(ShiftTable.GUID)),
                _nullableDate(cursor, cursor.getColumnIndex(ShiftTable.START_TIME)),
                _nullableDate(cursor, cursor.getColumnIndex(ShiftTable.END_TIME)),
                cursor.getString(cursor.getColumnIndex(ShiftTable.OPEN_MANAGER_ID)),
                cursor.getString(cursor.getColumnIndex(ShiftTable.CLOSE_MANAGER_ID)),
                cursor.getLong(cursor.getColumnIndex(ShiftTable.REGISTER_ID)),
                _decimal(cursor, cursor.getColumnIndex(ShiftTable.OPEN_AMOUNT)),
                _decimal(cursor, cursor.getColumnIndex(ShiftTable.CLOSE_AMOUNT)));
    }

    public static ShiftModel getInstance(Cursor cursor) {
        return new ShiftModel(cursor.getString(cursor.getColumnIndex(ShiftTable.GUID)),
                new Date(cursor.getLong(cursor.getColumnIndex(ShiftTable.START_TIME))),
                new Date(cursor.getLong(cursor.getColumnIndex(ShiftTable.END_TIME))),
                cursor.getString(cursor.getColumnIndex(ShiftTable.OPEN_MANAGER_ID)),
                cursor.getString(cursor.getColumnIndex(ShiftTable.CLOSE_MANAGER_ID)),
                cursor.getLong(cursor.getColumnIndex(ShiftTable.REGISTER_ID)),
                _decimal(cursor, cursor.getColumnIndex(ShiftTable.OPEN_AMOUNT)),
                _decimal(cursor, cursor.getColumnIndex(ShiftTable.CLOSE_AMOUNT)));
    }

    public static ShiftModel getById(Context context, String guid) {
        final Cursor c = ProviderAction.query(ShopProvider.getContentUri(ShiftTable.URI_CONTENT))
                .where(ShiftTable.GUID + " = ?", guid)
                .perform(context);
        ShiftModel model = null;
        if (c.moveToNext()) {
            model = ShiftModel.getInstance(c);
            c.close();
        }
        return model;
    }

    public ShiftModel(String guid, Date startTime, Date endTime, String openManagerId,
                      String closeManagerId, long registerId, BigDecimal openAmount, BigDecimal closeAmount) {
        this.guid = guid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.openManagerId = openManagerId;
        this.closeManagerId = closeManagerId;
        this.registerId = registerId;
        this.openAmount = openAmount;
        this.closeAmount = closeAmount;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues(7);
        v.put(ShiftTable.GUID, guid);
        _nullableDate(v, ShiftTable.START_TIME, startTime);
        _nullableDate(v, ShiftTable.END_TIME, endTime);

        v.put(ShiftTable.OPEN_MANAGER_ID, openManagerId);
        v.put(ShiftTable.CLOSE_MANAGER_ID, closeManagerId);

        v.put(ShiftTable.REGISTER_ID, registerId);
        v.put(ShiftTable.OPEN_AMOUNT, _decimal(openAmount));
        v.put(ShiftTable.CLOSE_AMOUNT, _decimal(closeAmount));
        return v;
    }

    public ContentValues toUpdateValues() {
        ContentValues v = new ContentValues(3);
        _nullableDate(v, ShiftTable.END_TIME, endTime);
        v.put(ShiftTable.CLOSE_MANAGER_ID, closeManagerId);
        v.put(ShiftTable.CLOSE_AMOUNT, _decimal(closeAmount));
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShiftModel that = (ShiftModel) o;

        if (!guid.equals(that.guid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return guid.hashCode();
    }
}
