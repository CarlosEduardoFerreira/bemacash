package com.kaching123.tcr.reports;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.collect.ImmutableSortedSet;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.TipsModel.PaymentType;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.TipsReportView2.EmployeeTable;
import com.kaching123.tcr.store.ShopSchema2.TipsReportView2.ShiftTable;
import com.kaching123.tcr.store.ShopSchema2.TipsReportView2.TipsTable;
import com.kaching123.tcr.store.ShopStore.TipsReportView;
import com.kaching123.tcr.util.DateUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import static com.kaching123.tcr.fragment.UiHelper.concatFullname;
import static com.kaching123.tcr.util.DateUtils.dateAndTimeAttendanceFormat;
import static com.kaching123.tcr.util.DateUtils.dateAndTimeShortAttendanceFormat;

/**
 * Created by vkompaniets on 18.06.2014.
 */

public class EmployeeTipsReportQuery {

    private static final Uri URI_TIPS_REPORT = ShopProvider.getContentUri(TipsReportView.URI_CONTENT);

    public static Collection<ShiftTipsInfo> getItems (Context context, long startTime, long endTime, String selectedEmployeeGuid) {

        Query query = ProviderAction.query(URI_TIPS_REPORT)
                .where(ShiftTable.START_TIME + " > ? and " + ShiftTable.START_TIME + " < ?", startTime, endTime);

        if (selectedEmployeeGuid != null){
            query.where(TipsTable.EMPLOYEE_ID + " = ?", selectedEmployeeGuid);
        }

        Cursor c = query.perform(context);
        HashMap<String, ShiftTipsInfo> shiftTips = new HashMap<String, ShiftTipsInfo>();
        while (c.moveToNext()){
            final String shiftGuid = c.getString(c.getColumnIndex(TipsTable.SHIFT_ID));
            ShiftTipsInfo shiftTipsInfo = shiftTips.get(shiftGuid);
            if (shiftTipsInfo == null){
                final long start = c.getLong(c.getColumnIndex(ShiftTable.START_TIME));
                final long end = c.getLong(c.getColumnIndex(ShiftTable.END_TIME));
                shiftTipsInfo = new ShiftTipsInfo(start, end);
                shiftTips.put(shiftGuid, shiftTipsInfo);
            }

            final String employeeGuid = c.getString(c.getColumnIndex(TipsTable.EMPLOYEE_ID));
            EmployeeTipsInfo employeeTipsInfo = shiftTipsInfo.tempEmployeeTipsInfos.get(employeeGuid);
            if (employeeTipsInfo == null){
                final String fullName = employeeGuid == null ? "House" : concatFullname(c.getString(c.getColumnIndex(EmployeeTable.FIRST_NAME)), c.getString(c.getColumnIndex(EmployeeTable.LAST_NAME)));
                employeeTipsInfo = new EmployeeTipsInfo(fullName);
                shiftTipsInfo.tempEmployeeTipsInfos.put(employeeGuid, employeeTipsInfo);
            }

            final BigDecimal tipsAmount = ContentValuesUtil._decimal(c, c.getColumnIndex(TipsTable.AMOUNT), BigDecimal.ZERO);
            final PaymentType paymentType = ContentValuesUtil._tipsPaymentType(c, c.getColumnIndex(TipsTable.PAYMENT_TYPE));
            if (paymentType == PaymentType.CASH){
                employeeTipsInfo.cashTips = employeeTipsInfo.cashTips.add(tipsAmount);
            }else {
                employeeTipsInfo.creditTips= employeeTipsInfo.creditTips.add(tipsAmount);
            }
        }
        c.close();

        return convert(shiftTips);
    }

    public static String getShift2PeriodString (long start, long end, boolean shortFormat){
        return new ShiftTipsInfo(start, end).toPeriodString(shortFormat);
    }

    private static Collection<ShiftTipsInfo> convert(HashMap<String, ShiftTipsInfo> shiftTips) {
        for (Entry<String, ShiftTipsInfo> entry : shiftTips.entrySet()){
            ShiftTipsInfo shiftTipsInfo = entry.getValue();
            shiftTipsInfo.employeeTipsInfos = ImmutableSortedSet.orderedBy(new Comparator<EmployeeTipsInfo>() {
                @Override
                public int compare(EmployeeTipsInfo l, EmployeeTipsInfo r) {
                    return l.fullName.compareTo(r.fullName);
                }
            }).addAll(shiftTipsInfo.tempEmployeeTipsInfos.values().iterator()).build();
        }

        return ImmutableSortedSet.orderedBy(new Comparator<ShiftTipsInfo>() {
            @Override
            public int compare(ShiftTipsInfo l, ShiftTipsInfo r) {
                long result = r.start - l.start;
                return result < 0 ? -1 : result == 0 ? 0 : 1;
            }
        }).addAll(shiftTips.values().iterator()).build();
    }

    public static class EmployeeTipsInfo {
        public String fullName;
        public BigDecimal cashTips = BigDecimal.ZERO;
        public BigDecimal creditTips = BigDecimal.ZERO;

        public EmployeeTipsInfo(String fullName) {
            this.fullName = fullName;
        }

        public boolean zeroTips(){
            return cashTips.compareTo(BigDecimal.ZERO) == 0 && creditTips.compareTo(BigDecimal.ZERO) == 0;
        }
    }

    public static class ShiftTipsInfo {
        public long start;
        public long end;
        public Collection<EmployeeTipsInfo> employeeTipsInfos;

        private HashMap<String, EmployeeTipsInfo> tempEmployeeTipsInfos = new HashMap<String, EmployeeTipsInfo>();

        public ShiftTipsInfo(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public String toPeriodString(boolean shortt) {
            Date start = this.start != 0 ? new Date(this.start) : null;
            Date end = this.end != 0 ? new Date(this.end) : null;
            final boolean sameDay = DateUtils.isSameDay(start, end);
            if (end == null) {
                return shortt ? dateAndTimeShortAttendanceFormat(start) : dateAndTimeAttendanceFormat(start);
            }else {
                return String.format("%s - %s", shortt ? dateAndTimeShortAttendanceFormat(start) : dateAndTimeAttendanceFormat(start), sameDay ? DateUtils.timeOnlyAttendanceFormat(end) : shortt ? dateAndTimeShortAttendanceFormat(end) : dateAndTimeAttendanceFormat(end));
            }
        }

        public boolean zeroTips(){
            if (employeeTipsInfos == null || employeeTipsInfos.isEmpty())
                return true;

            for (EmployeeTipsInfo i : employeeTipsInfos){
                if (!i.zeroTips())
                    return false;
            }

            return true;
        }
    }


}

