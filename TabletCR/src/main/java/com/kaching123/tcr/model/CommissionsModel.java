package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.EmployeeCommissionsTable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;

/**
 * Created by pkabakov on 09.07.2014.
 */
public class CommissionsModel implements IValueModel {

    public final String id;
    public final String employeeId;
    public final String shiftId;
    public final String orderId;
    public final Date createTime;
    public final BigDecimal amount;

    private List<String> mIgnoreFields;

    public CommissionsModel(Cursor c) {
        this(
                c.getString(c.getColumnIndex(EmployeeCommissionsTable.GUID)),
                c.getString(c.getColumnIndex(EmployeeCommissionsTable.EMPLOYEE_ID)),
                c.getString(c.getColumnIndex(EmployeeCommissionsTable.SHIFT_ID)),
                c.getString(c.getColumnIndex(EmployeeCommissionsTable.ORDER_ID)),
                _nullableDate(c, c.getColumnIndex(EmployeeCommissionsTable.CREATE_TIME)),
                _decimal(c, c.getColumnIndex(EmployeeCommissionsTable.AMOUNT), BigDecimal.ZERO),
                null
        );
    }

    public CommissionsModel(String id, String employeeId, String shiftId, String orderId, Date createTime, BigDecimal amount, List<String> ignoreFields) {
        this.id = id;
        this.employeeId = employeeId;
        this.shiftId = shiftId;
        this.orderId = orderId;
        this.createTime = createTime;
        this.amount = amount;

        this.mIgnoreFields = ignoreFields;
    }

    @Override
    public String getGuid() {
        return id;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeCommissionsTable.GUID)) v.put(EmployeeCommissionsTable.GUID, id);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeCommissionsTable.EMPLOYEE_ID)) v.put(EmployeeCommissionsTable.EMPLOYEE_ID, employeeId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeCommissionsTable.SHIFT_ID)) v.put(EmployeeCommissionsTable.SHIFT_ID, shiftId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeCommissionsTable.ORDER_ID)) v.put(EmployeeCommissionsTable.ORDER_ID, orderId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeCommissionsTable.CREATE_TIME)) v.put(EmployeeCommissionsTable.CREATE_TIME, createTime.getTime());
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeCommissionsTable.AMOUNT)) v.put(EmployeeCommissionsTable.AMOUNT, _decimal(amount));
        return v;
    }

    @Override
    public String getIdColumn() {
        return EmployeeCommissionsTable.GUID;
    }
}
