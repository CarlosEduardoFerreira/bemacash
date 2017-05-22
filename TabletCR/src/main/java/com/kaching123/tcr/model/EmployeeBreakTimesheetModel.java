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
 * Created by mboychenko on 5/22/2017.
 */

public class EmployeeBreakTimesheetModel implements IValueModel {

    public final String guid;
    public Date startBreak;
    public Date breakEnd;
    public final String employeeGuid;
    public final String clockInGuid;

    private List<String> mIgnoreFields;

    public EmployeeBreakTimesheetModel(String guid, Date startBreak, Date stopBreak, String employeeGuid, String clockInGuid, List<String> ignoreFields) {
        this.guid = guid;
        this.startBreak = startBreak;
        this.breakEnd = stopBreak;
        this.employeeGuid = employeeGuid;
        this.clockInGuid = clockInGuid;

        this.mIgnoreFields = ignoreFields;
    }

    public EmployeeBreakTimesheetModel(Cursor c) {
        this(
                c.getString(c.getColumnIndex(ShopStore.EmployeeBreaksTimesheetTable.GUID)),
                c.getInt(c.getColumnIndex(ShopStore.EmployeeBreaksTimesheetTable.BREAK_START)) == 0 ? null :
                        new Date(c.getInt(c.getColumnIndex(ShopStore.EmployeeBreaksTimesheetTable.BREAK_START))),
                c.getInt(c.getColumnIndex(ShopStore.EmployeeBreaksTimesheetTable.BREAK_END)) == 0 ? null :
                        new Date(c.getInt(c.getColumnIndex(ShopStore.EmployeeBreaksTimesheetTable.BREAK_END))),
                c.getString(c.getColumnIndex(ShopStore.EmployeeBreaksTimesheetTable.EMPLOYEE_GUID)),
                c.getString(c.getColumnIndex(ShopStore.EmployeeBreaksTimesheetTable.CLOCK_IN_GUID)),
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

        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.EmployeeBreaksTimesheetTable.GUID)) values.put(ShopStore.EmployeeBreaksTimesheetTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.EmployeeBreaksTimesheetTable.BREAK_START)) _putDate(values, ShopStore.EmployeeBreaksTimesheetTable.BREAK_START, startBreak);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.EmployeeBreaksTimesheetTable.BREAK_END)) _putDate(values, ShopStore.EmployeeBreaksTimesheetTable.BREAK_END, breakEnd);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.EmployeeBreaksTimesheetTable.EMPLOYEE_GUID)) values.put(ShopStore.EmployeeBreaksTimesheetTable.EMPLOYEE_GUID, employeeGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.EmployeeBreaksTimesheetTable.CLOCK_IN_GUID)) values.put(ShopStore.EmployeeBreaksTimesheetTable.CLOCK_IN_GUID, clockInGuid);
        return values;
    }

    public ContentValues toUpdateValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        _nullableDate(v, ShopStore.EmployeeBreaksTimesheetTable.BREAK_END, breakEnd);
        return v;
    }

    @Override
    public String getIdColumn() {
        return ShopStore.EmployeeBreaksTimesheetTable.GUID;
    }

}

