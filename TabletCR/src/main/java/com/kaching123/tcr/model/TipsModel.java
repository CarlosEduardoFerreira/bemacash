package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;
import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;
import static com.kaching123.tcr.model.ContentValuesUtil._tipsPaymentType;

/**
 * Created by pkabakov on 16.05.2014.
 */
public class TipsModel implements IValueModel, Serializable {

    public String id;
    public String parentId;
    public String employeeId;
    public String shiftId;
    public String orderId;
    public String paymentTransactionId;
    public Date createTime;
    public BigDecimal amount;
    public String comment;
    public PaymentType paymentType;

    private List<String> mIgnoreFields;

    public TipsModel(Cursor c) {
        this(
            c.getString(c.getColumnIndex(EmployeeTipsTable.GUID)),
            c.getString(c.getColumnIndex(EmployeeTipsTable.PARENT_GUID)),
            c.getString(c.getColumnIndex(EmployeeTipsTable.EMPLOYEE_ID)),
            c.getString(c.getColumnIndex(EmployeeTipsTable.SHIFT_ID)),
            c.getString(c.getColumnIndex(EmployeeTipsTable.ORDER_ID)),
            c.getString(c.getColumnIndex(EmployeeTipsTable.PAYMENT_TRANSACTION_ID)),
            _nullableDate(c, c.getColumnIndex(EmployeeTipsTable.CREATE_TIME)),
            _decimal(c, c.getColumnIndex(EmployeeTipsTable.AMOUNT), BigDecimal.ZERO),
            c.getString(c.getColumnIndex(EmployeeTipsTable.COMMENT)),
            _tipsPaymentType(c, c.getColumnIndex(EmployeeTipsTable.PAYMENT_TYPE)),
            null
            );
    }

    public TipsModel(String id, String parentId, String employeeId, String shiftId, String orderId,
                     String paymentTransactionId, Date createTime, BigDecimal amount,
                     String comment, PaymentType paymentType, List<String> ignoreFields) {
        this.id = id;
        this.parentId = parentId;
        this.employeeId = employeeId;
        this.shiftId = shiftId;
        this.orderId = orderId;
        this.paymentTransactionId = paymentTransactionId;
        this.createTime = createTime;
        this.amount = amount;
        this.comment = comment;
        this.paymentType = paymentType;

        this.mIgnoreFields = ignoreFields;
    }

    @Override
    public String getGuid() {
        return id;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTipsTable.GUID)) v.put(EmployeeTipsTable.GUID, id);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTipsTable.PARENT_GUID)) v.put(EmployeeTipsTable.PARENT_GUID, parentId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTipsTable.EMPLOYEE_ID)) v.put(EmployeeTipsTable.EMPLOYEE_ID, employeeId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTipsTable.SHIFT_ID)) v.put(EmployeeTipsTable.SHIFT_ID, shiftId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTipsTable.ORDER_ID)) v.put(EmployeeTipsTable.ORDER_ID, orderId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTipsTable.PAYMENT_TRANSACTION_ID)) v.put(EmployeeTipsTable.PAYMENT_TRANSACTION_ID, paymentTransactionId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTipsTable.CREATE_TIME)) v.put(EmployeeTipsTable.CREATE_TIME, createTime.getTime());
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTipsTable.AMOUNT)) v.put(EmployeeTipsTable.AMOUNT, _decimal(amount));
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTipsTable.COMMENT)) v.put(EmployeeTipsTable.COMMENT, comment);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTipsTable.PAYMENT_TYPE)) v.put(EmployeeTipsTable.PAYMENT_TYPE, _enum(paymentType));
        return v;
    }

    @Override
    public String getIdColumn() {
        return EmployeeTipsTable.GUID;
    }

    public enum PaymentType {
        CASH, CREDIT
    }
}
