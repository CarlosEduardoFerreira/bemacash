package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.collect.ImmutableSortedSet;
import com.kaching123.tcr.model.EmployeeBreakTimesheetModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.EmployeeTimesheetView2.EmployeeTable;
import com.kaching123.tcr.store.ShopSchema2.EmployeeTimesheetView2.TimeTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeeTimesheetTable;
import com.kaching123.tcr.store.ShopStore.EmployeeTimesheetView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;

/**
 * Created by gdubina on 29/01/14.
 */
public class ClockInOutReportQuery {

    public static enum Error {OVERLAPS};

    private static final Uri URI_TIME = ShopProvider.getContentUri(EmployeeTimesheetView.URI_CONTENT);
    private static final Uri URI_EMPLOYEE_TIMESHEET_WITH_BREAKS = ShopProvider.getContentUri(ShopStore.EmployeeBreaksTimesheetTable.URI_CONTENT);
    public static final String EXTRA_OVERLAPS = "EXTRA_OVERLAPS";

    public static Collection<EmployeeInfo> getItemsClockInOutReport(Context context, long startTime, long endTime, String employeeGuid) {

        Query query = ProviderAction.query(URI_TIME)
                .where(TimeTable.CLOCK_IN + " >= ? and " + TimeTable.CLOCK_IN + " <= ?", startTime, endTime);

        if(!TextUtils.isEmpty(employeeGuid)){
            query.where(EmployeeTable.GUID + " = ?", employeeGuid);
        }
        Cursor c = query.perform(context);

        HashMap<String, EmployeeInfo> result = new HashMap<>();
        while (c.moveToNext()) {
            String guid = c.getString(c.getColumnIndex(EmployeeTable.GUID));

            long clockIn = c.getLong(c.getColumnIndex(TimeTable.CLOCK_IN));
            long clockOut = c.getLong(c.getColumnIndex(TimeTable.CLOCK_OUT));
            String clockGuid = c.getString(c.getColumnIndex(TimeTable.GUID));

            EmployeeInfo info = result.get(guid);
            if(info == null){
                info = new EmployeeInfo(concatFullname(c.getString(c.getColumnIndex(EmployeeTable.FIRST_NAME)), c.getString(c.getColumnIndex(EmployeeTable.LAST_NAME))));
                result.put(guid, info);
            }

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

            if (totalBreakForClockIn.compareTo(BigDecimal.ZERO) == 0) {
                totalBreakForClockIn = null;
            }

            TimeInfo timeInfo = new TimeInfo(clockGuid, new Date(clockIn), clockOut == 0 ? null : new Date(clockOut), totalBreakForClockIn);
            info.times.add(timeInfo);
            info.totalMins = info.totalMins.add(timeInfo.getDiff());
        }

        c.close();
        return convert(result);
    }

    private static Collection<EmployeeInfo> convert(HashMap<String, EmployeeInfo> result){
        for (Entry<String, EmployeeInfo> e : result.entrySet()){
            Collection<TimeInfo> timeInfos = e.getValue().times;
            e.getValue().times = ImmutableSortedSet.orderedBy(new Comparator<TimeInfo>() {
                @Override
                public int compare(TimeInfo t1, TimeInfo t2) {
                    return t1.clockIn == null ? -1 : t2.clockIn == null ? 1 : t1.clockIn.compareTo(t2.clockIn);
                }
            }).addAll(timeInfos.iterator()).build();
        }

        return ImmutableSortedSet.orderedBy(new Comparator<EmployeeInfo>() {
            @Override
            public int compare(EmployeeInfo l, EmployeeInfo r) {
                return l.name == null ? -1 : r.name == null ? 1 : l.name.compareTo(r.name);
            }
        }).addAll(result.values().iterator()).build();
    }

    public static class EmployeeInfo {
        public String name;

        public BigDecimal totalMins = BigDecimal.ZERO;
        public Collection<TimeInfo> times = new ArrayList<TimeInfo>();

        public EmployeeInfo(String name) {
            this.name = name;
        }
    }

    public static boolean isOverlapped(Context context, TimeInfo newTime, String employeeGuid){
        Cursor c = ProviderAction.query(ShopProvider.getContentUri(EmployeeTimesheetTable.URI_CONTENT))
                .projection(EmployeeTimesheetTable.GUID, EmployeeTimesheetTable.CLOCK_IN, EmployeeTimesheetTable.CLOCK_OUT)
                .where(EmployeeTimesheetTable.EMPLOYEE_GUID + " = ?", employeeGuid)
                .perform(context);

        boolean overlaps = false;
        while (c.moveToNext()){
            TimeInfo time = new TimeInfo(c.getString(0), new Date(c.getLong(1)), c.getLong(2) == 0 ? null : new Date(c.getLong(2)));
            if (time.overlaps(newTime)){
                overlaps = true;
                break;
            }
        }
        c.close();

        return overlaps;
    }

    public static class TimeInfo {

        public final String guid;
        public final Date clockIn;
        public final Date clockOut;
        public BigDecimal totalBreak;

        public TimeInfo(String guid, Date clockIn, Date clockOut) {
            this.guid = guid;
            this.clockIn = clockIn;
            this.clockOut = clockOut;
        }

        public TimeInfo(String guid, Date clockIn, Date clockOut, BigDecimal totalBreak) {
            this.guid = guid;
            this.clockIn = clockIn;
            this.clockOut = clockOut;
            this.totalBreak = totalBreak;
        }

        public BigDecimal getDiff() {
            if(clockIn == null || clockOut == null)
                return BigDecimal.ZERO;
            long min = (clockOut.getTime() - clockIn.getTime())/1000/60;
            return BigDecimal.valueOf(min);
        }

        public boolean overlaps(TimeInfo time){
            // time.out > this.in && time.in < this.out;
            return (time.clockOut == null || time.clockOut.after(this.clockIn)) &&
                   (this.clockOut == null || time.clockIn.before(this.clockOut)) &&
                    !time.guid.equals(this.guid);
        }
    }
}
