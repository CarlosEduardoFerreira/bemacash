package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CreditReceiptTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by gdubina on 24/02/14.
 */
public class CreditReceiptModel implements IValueModel, Serializable {

    public String guid;
    public String cashierGuid;
    public long registerId;
    public String shiftId;
    public Date createTime;
    public BigDecimal amount;
    public long printNumber;
    public int expireTime;

    private List<String> mIgnoreFields;

    public CreditReceiptModel(String guid,
                              String cashierGuid,
                              long registerId,
                              String shiftId,
                              Date createTime,
                              BigDecimal amount,
                              long printNumber,
                              int expireTime,
                              List<String> ignoreFields) {
        this.guid = guid;
        this.cashierGuid = cashierGuid;
        this.registerId = registerId;
        this.shiftId = shiftId;
        this.createTime = createTime;
        this.amount = amount;
        this.printNumber = printNumber;
        this.expireTime = expireTime;

        this.mIgnoreFields = ignoreFields;
    }

    public CreditReceiptModel(Cursor c) {
        this(
                c.getString(c.getColumnIndex(ShopStore.CreditReceiptTable.GUID)),
                c.getString(c.getColumnIndex(CreditReceiptTable.CASHIER_GUID)),
                c.getLong(c.getColumnIndex(CreditReceiptTable.REGISTER_ID)),
                c.getString(c.getColumnIndex(CreditReceiptTable.SHIFT_ID)),
                new Date(c.getLong(c.getColumnIndex(CreditReceiptTable.CREATE_TIME))),
                new BigDecimal(c.getDouble(c.getColumnIndex(CreditReceiptTable.AMOUNT))),
                c.getLong(c.getColumnIndex(CreditReceiptTable.PRINT_NUMBER)),
                c.getInt(c.getColumnIndex(CreditReceiptTable.EXPIRE_TIME)),
                null
        );
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        if (mIgnoreFields == null || !mIgnoreFields.contains(CreditReceiptTable.GUID)) v.put(CreditReceiptTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CreditReceiptTable.CASHIER_GUID)) v.put(CreditReceiptTable.CASHIER_GUID, cashierGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CreditReceiptTable.REGISTER_ID)) v.put(CreditReceiptTable.REGISTER_ID, registerId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CreditReceiptTable.SHIFT_ID)) v.put(CreditReceiptTable.SHIFT_ID, shiftId);

        if (mIgnoreFields == null || !mIgnoreFields.contains(CreditReceiptTable.CREATE_TIME)) v.put(CreditReceiptTable.CREATE_TIME, createTime.getTime());
        if (mIgnoreFields == null || !mIgnoreFields.contains(CreditReceiptTable.AMOUNT)) v.put(CreditReceiptTable.AMOUNT, _decimal(amount));
        if (mIgnoreFields == null || !mIgnoreFields.contains(CreditReceiptTable.PRINT_NUMBER)) v.put(CreditReceiptTable.PRINT_NUMBER, printNumber);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CreditReceiptTable.EXPIRE_TIME)) v.put(CreditReceiptTable.EXPIRE_TIME, expireTime);


        return v;
    }

    @Override
    public String getIdColumn() {
        return CreditReceiptTable.GUID;
    }
}
