package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.CreditReceiptTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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

    public CreditReceiptModel(String guid, String cashierGuid, long registerId, String shiftId, Date createTime, BigDecimal amount, long printNumber, int expireTime) {
        this.guid = guid;
        this.cashierGuid = cashierGuid;
        this.registerId = registerId;
        this.shiftId = shiftId;
        this.createTime = createTime;
        this.amount = amount;
        this.printNumber = printNumber;
        this.expireTime = expireTime;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(CreditReceiptTable.GUID, guid);
        v.put(CreditReceiptTable.CASHIER_GUID, cashierGuid);
        v.put(CreditReceiptTable.REGISTER_ID, registerId);
        v.put(CreditReceiptTable.SHIFT_ID, shiftId);

        v.put(CreditReceiptTable.CREATE_TIME, createTime.getTime());
        v.put(CreditReceiptTable.AMOUNT, _decimal(amount));
        v.put(CreditReceiptTable.PRINT_NUMBER, printNumber);
        v.put(CreditReceiptTable.EXPIRE_TIME, expireTime);

        return v;
    }
}
