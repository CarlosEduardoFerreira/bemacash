package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.collect.ImmutableSortedSet;
import com.kaching123.tcr.reports.ClockInOutReportQuery.EmployeeInfo;
import com.kaching123.tcr.reports.ClockInOutReportQuery.TimeInfo;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.EmployeeComissionView2.ComissionTable;
import com.kaching123.tcr.store.ShopSchema2.EmployeeTimesheetView2.EmployeeTable;
import com.kaching123.tcr.store.ShopSchema2.EmployeeTimesheetView2.TimeTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeeComissionView;
import com.kaching123.tcr.store.ShopStore.EmployeeTimesheetView;
import com.kaching123.tcr.util.CalculationUtil;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by vkompaniets on 04.02.14.
 */
public class PayrollReportQuery {

    private static final Uri URI_TIME = ShopProvider.getContentUri(EmployeeTimesheetView.URI_CONTENT);
    private static final Uri URI_COMMISSION = ShopProvider.getContentUri(EmployeeComissionView.URI_CONTENT);
    private static final Uri URI_EMPLOYEE_TIMESHEET_WITH_BREAKS = ShopProvider.getContentUri(ShopStore.EmployeeBreaksTimesheetTable.URI_CONTENT);

    public static Collection<EmployeePayrollInfo> getItems(Context context, long startTime, long endTime, String employeeGuid) {

        Query commissionQuery = ProviderAction.query(URI_COMMISSION)
                .projection(ComissionTable.EMPLOYEE_ID, ComissionTable.AMOUNT)
                .where(ComissionTable.CREATE_TIME + " >= ? and " + ComissionTable.CREATE_TIME + " <= ?", startTime, endTime);

        if(!TextUtils.isEmpty(employeeGuid)){
            commissionQuery.where(ComissionTable.EMPLOYEE_ID + " = ?", employeeGuid);
        }

        Cursor commissionCursor = commissionQuery.perform(context);

        HashMap<String, BigDecimal> commissions = new HashMap<String, BigDecimal>();
        while (commissionCursor.moveToNext()){
            String guid = commissionCursor.getString(0);
            BigDecimal commission = commissions.get(guid);
            if (commission == null){
                commission = _decimal(commissionCursor, 1, BigDecimal.ZERO);
                commissions.put(guid, commission);
                continue;
            }
            commission = commission.add(_decimal(commissionCursor, 1, BigDecimal.ZERO));
            commissions.put(guid, commission);
        }
        commissionCursor.close();

        Query timeQuery = ProviderAction.query(URI_TIME)
                .where(TimeTable.CLOCK_IN + " >= ? and " + TimeTable.CLOCK_IN + " <= ?", startTime, endTime);

        if(!TextUtils.isEmpty(employeeGuid)){
            timeQuery.where(EmployeeTable.GUID + " = ?", employeeGuid);
        }
        Cursor timeCursor = timeQuery.perform(context);

        HashMap<String, EmployeePayrollInfo> result = new HashMap<String, EmployeePayrollInfo>();
        while (timeCursor.moveToNext()) {
            String guid = timeCursor.getString(timeCursor.getColumnIndex(EmployeeTable.GUID));

            long clockIn = timeCursor.getLong(timeCursor.getColumnIndex(TimeTable.CLOCK_IN));
            long clockOut = timeCursor.getLong(timeCursor.getColumnIndex(TimeTable.CLOCK_OUT));
            String clockGuid = timeCursor.getString(timeCursor.getColumnIndex(TimeTable.GUID));

            Cursor c1 = ProviderAction.query(URI_EMPLOYEE_TIMESHEET_WITH_BREAKS)
                    .where(ShopStore.EmployeeBreaksTimesheetTable.CLOCK_IN_GUID + " = ?", clockGuid)
                    .where(ShopStore.EmployeeBreaksTimesheetTable.BREAK_END + " IS NOT NULL")
                    .perform(context);

            BigDecimal totalBreakForClockIn = BigDecimal.ZERO;

            while (c1.moveToNext()) {
                Date brStart = new Date(c1.getInt(c1.getColumnIndex(ShopStore.EmployeeBreaksTimesheetTable.BREAK_START)));
                Date brEnd = new Date(c1.getInt(c1.getColumnIndex(ShopStore.EmployeeBreaksTimesheetTable.BREAK_END)));
                long min = (brEnd.getTime() - brStart.getTime())/1000/60;
                totalBreakForClockIn = totalBreakForClockIn.add(BigDecimal.valueOf(min));
            }
            c1.close();

            EmployeePayrollInfo info = result.get(guid);
            if(info == null){
                BigDecimal commission = commissions.get(guid);
                info = new EmployeePayrollInfo(
                        concatFullname(timeCursor.getString(timeCursor.getColumnIndex(EmployeeTable.FIRST_NAME)),
                        timeCursor.getString(timeCursor.getColumnIndex(EmployeeTable.LAST_NAME))),
                        _decimal(timeCursor, timeCursor.getColumnIndex(EmployeeTable.HOURLY_RATE), BigDecimal.ZERO),
                        commission,
                        totalBreakForClockIn);
                result.put(guid, info);
            } else {
                info.totalBreaks = info.totalBreaks.add(totalBreakForClockIn);
            }
            TimeInfo timeInfo = new TimeInfo(clockGuid, new Date(clockIn), clockOut == 0 ? null : new Date(clockOut));
            info.times.add(timeInfo);
            info.totalMins = info.totalMins.add(timeInfo.getDiff());
        }
        timeCursor.close();
        calcTotalDue(result);

        return convert(result);

    }

    private static Collection<EmployeePayrollInfo> convert(HashMap<String, EmployeePayrollInfo> result){
        return ImmutableSortedSet.orderedBy(new Comparator<EmployeePayrollInfo>() {
            @Override
            public int compare(EmployeePayrollInfo l, EmployeePayrollInfo r) {
                return l.name == null ? -1 : r.name == null ? 1 : l.name.compareTo(r.name);
            }
        }).addAll(result.values().iterator()).build();
    }

    private static void calcTotalDue(HashMap<String, EmployeePayrollInfo> map){
        for (EmployeePayrollInfo info : map.values()){
            info.totalDue = CalculationUtil.getTotalDue(info.hRate, info.totalMins);
        }
    }

    public static class EmployeePayrollInfo extends EmployeeInfo{

        public BigDecimal hRate = BigDecimal.ZERO;
        public BigDecimal totalDue = BigDecimal.ZERO;
        public BigDecimal totalBreaks = BigDecimal.ZERO;
        public BigDecimal commission = BigDecimal.ZERO;

        public EmployeePayrollInfo(String name, BigDecimal hRate, BigDecimal commission, BigDecimal totalBreaks) {
            super(name);
            this.hRate = hRate;
            this.totalBreaks = totalBreaks;
            if (commission != null){
                this.commission = commission;
            }

        }
    }
}
