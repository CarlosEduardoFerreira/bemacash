package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.EmployeeTipsTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
            _tipsPaymentType(c, c.getColumnIndex(EmployeeTipsTable.PAYMENT_TYPE))
            );
    }

    public TipsModel(String id, String parentId, String employeeId, String shiftId, String orderId, String paymentTransactionId, Date createTime, BigDecimal amount, String comment, PaymentType paymentType) {
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
    }

    @Override
    public String getGuid() {
        return id;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(EmployeeTipsTable.GUID, id);
        v.put(EmployeeTipsTable.PARENT_GUID, parentId);
        v.put(EmployeeTipsTable.EMPLOYEE_ID, employeeId);
        v.put(EmployeeTipsTable.SHIFT_ID, shiftId);
        v.put(EmployeeTipsTable.ORDER_ID, orderId);
        v.put(EmployeeTipsTable.PAYMENT_TRANSACTION_ID, paymentTransactionId);
        v.put(EmployeeTipsTable.CREATE_TIME, createTime.getTime());
        v.put(EmployeeTipsTable.AMOUNT, _decimal(amount));
        v.put(EmployeeTipsTable.COMMENT, comment);
        v.put(EmployeeTipsTable.PAYMENT_TYPE, _enum(paymentType));
        return v;
    }

    public enum PaymentType {
        CASH, CREDIT
    }
}
