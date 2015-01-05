package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore;

import java.util.Date;

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

    public EmployeeTimesheetModel(String guid, Date clockIn, Date clockOut, String employeeGuid) {
        this.guid = guid;
        this.clockIn = clockIn;
        this.clockOut = clockOut;
        this.employeeGuid = employeeGuid;
    }


    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.EmployeeTimesheetTable.GUID, guid);
        _putDate(values, ShopStore.EmployeeTimesheetTable.CLOCK_IN, clockIn);
        _putDate(values, ShopStore.EmployeeTimesheetTable.CLOCK_OUT, clockOut);
        values.put(ShopStore.EmployeeTimesheetTable.EMPLOYEE_GUID, employeeGuid);
        return values;
    }

    public ContentValues toUpdateValues() {
        ContentValues v = new ContentValues(1);
        _nullableDate(v, ShopStore.EmployeeTimesheetTable.CLOCK_OUT, clockOut);
        return v;
    }
}
