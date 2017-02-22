package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;

import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;
import static com.kaching123.tcr.model.ContentValuesUtil._putDate;

/**
 * Created by pkabakov on 24/12/13.
 */
public class EmployeeTimesheetModel implements IValueModel{

    public final String guid;
    public Date clockIn;
    public Date clockOut;
    public final String employeeGuid;

    private List<String> mIgnoreFields;

    public EmployeeTimesheetModel(String guid, Date clockIn, Date clockOut, String employeeGuid, List<String> ignoreFields) {
        this.guid = guid;
        this.clockIn = clockIn;
        this.clockOut = clockOut;
        this.employeeGuid = employeeGuid;

        this.mIgnoreFields = ignoreFields;
    }

    public EmployeeTimesheetModel(Cursor c) {
        this(
                c.getString(c.getColumnIndex(ShopStore.EmployeeTimesheetTable.GUID)),
                new Date(c.getInt(c.getColumnIndex(ShopStore.EmployeeTimesheetTable.CLOCK_IN))),
                new Date(c.getInt(c.getColumnIndex(ShopStore.EmployeeTimesheetTable.CLOCK_OUT))),
                c.getString(c.getColumnIndex(ShopStore.EmployeeTimesheetTable.EMPLOYEE_GUID)),
                null
        );
    }


    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.EmployeeTimesheetTable.GUID)) values.put(ShopStore.EmployeeTimesheetTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.EmployeeTimesheetTable.CLOCK_IN)) _putDate(values, ShopStore.EmployeeTimesheetTable.CLOCK_IN, clockIn);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.EmployeeTimesheetTable.CLOCK_OUT)) _putDate(values, ShopStore.EmployeeTimesheetTable.CLOCK_OUT, clockOut);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.EmployeeTimesheetTable.EMPLOYEE_GUID)) values.put(ShopStore.EmployeeTimesheetTable.EMPLOYEE_GUID, employeeGuid);
        return values;
    }

    public ContentValues toUpdateValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        _nullableDate(v, ShopStore.EmployeeTimesheetTable.CLOCK_OUT, clockOut);
        return v;
    }

    @Override
    public String getIdColumn() {
        return ShopStore.EmployeeTimesheetTable.GUID;
    }

}
