package com.kaching123.tcr.fragment.reports.sub;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.reports.EmployeeReportsDetailsFragment.IDetailsFragment;
import com.kaching123.tcr.reports.ClockInOutReportQuery;
import com.kaching123.tcr.reports.ClockInOutReportQuery.EmployeeInfo;
import com.kaching123.tcr.reports.ClockInOutReportQuery.TimeInfo;
import com.kaching123.tcr.util.DateUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by gdubina on 29/01/14.
 */
@EFragment(R.layout.reports_employee_attendance_list_fragment)
public class EmployeeClockInOutFragment extends SalesBaseFragment implements IDetailsFragment {

    @FragmentArg
    protected String employeeGuid;

    @Override
    protected ObjectsCursorAdapter<Object> createAdapter() {
        return new TimeAdapter(getActivity());
    }

    @Override
    public Loader<List<Object>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<List<Object>>(getActivity()) {
            @Override
            public List<Object> loadInBackground() {
                Collection<EmployeeInfo> employeeInfos = ClockInOutReportQuery.getItemsClockInOutReport(getActivity(), startTime, endTime, employeeGuid);
                ArrayList<Object> rows = new ArrayList<Object>();
                for (EmployeeInfo i : employeeInfos) {
                    rows.add(new HeaderRow(i.name));
                    rows.addAll(i.times);
                    rows.add(new TotalRow(i.totalMins));
                }
                return rows;
            }
        };
    }

    @Override
    public void updateData(long startTime, long endTime, String employeeGuid) {
        this.employeeGuid = employeeGuid;
        super.updateData(startTime, endTime, 0, -1, null);
    }

    private static class HeaderRow {

        String name;

        private HeaderRow(String name) {
            this.name = name;
        }
    }

    private static class TotalRow {

        BigDecimal total;

        private TotalRow(BigDecimal total) {
            this.total = total;
        }
    }

    private static class TimeAdapter extends ObjectsCursorAdapter<Object> {

        public TimeAdapter(Context context) {
            super(context);
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            Class clazz = getItem(position).getClass();
            if (clazz == HeaderRow.class) {
                return 0;
            } else if (clazz == TotalRow.class) {
                return 2;
            }
            return 1;
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            int type = getItemViewType(position);
            View view;
            if (type == 0) {
                view = View.inflate(getContext(), R.layout.reports_employee_ateendance_item0_view, null);
                UIHolderHeader header = new UIHolderHeader();
                header.name = (TextView) view.findViewById(R.id.name);
                view.setTag(header);
            } else if (type == 2) {
                view = View.inflate(getContext(), R.layout.reports_employee_ateendance_item2_view, null);
                UIHolderTotal total = new UIHolderTotal();
                total.totalHours = (TextView) view.findViewById(R.id.total_hours);
                view.setTag(total);
            } else {
                view = View.inflate(getContext(), R.layout.reports_employee_ateendance_item1_view, null);
                UIHolderTime time = new UIHolderTime();
                time.date = (TextView) view.findViewById(R.id.date);
                time.totalBreaks = (TextView) view.findViewById(R.id.total_break_time);
                time.clockIn = (TextView) view.findViewById(R.id.clock_in);
                time.clockOut = (TextView) view.findViewById(R.id.clock_out);
                time.shift = (TextView) view.findViewById(R.id.shift);
                view.setTag(time);
            }
            return view;
        }

        @Override
        protected View bindView(View convertView, int position, Object item) {
            int type = getItemViewType(position);
            if (type == 0) {
                UIHolderHeader holder = (UIHolderHeader) convertView.getTag();
                HeaderRow header = (HeaderRow) item;
                holder.name.setText(header.name);
            } else if (type == 2) {
                UIHolderTotal holder = (UIHolderTotal) convertView.getTag();
                TotalRow total = (TotalRow) item;
                holder.totalHours.setText(DateUtils.formatMins(total.total));
            } else {
                UIHolderTime holder = (UIHolderTime) convertView.getTag();
                TimeInfo timeInfo = (TimeInfo) item;
                holder.date.setText(DateUtils.dateOnlyFormat(timeInfo.clockIn));

                final boolean sameDay = DateUtils.isSameDay(timeInfo.clockIn, timeInfo.clockOut);

                holder.clockIn.setText(DateUtils.timeOnlyAttendanceFormat(timeInfo.clockIn));
                holder.clockOut.setText(sameDay ? DateUtils.timeOnlyAttendanceFormat(timeInfo.clockOut) : DateUtils.formatFullAttendance(timeInfo.clockOut));
                holder.shift.setText(timeInfo.clockOut == null ? null : DateUtils.formatMins(timeInfo.getDiff()));
                holder.totalBreaks.setText(timeInfo.clockOut == null ? null : DateUtils.formatMins(timeInfo.totalBreak));
            }

            return convertView;
        }

    }

    private static class UIHolderHeader {
        TextView name;
    }

    private static class UIHolderTotal {
        TextView totalHours;
    }

    private static class UIHolderTime {
        TextView date;
        TextView totalBreaks;
        TextView clockIn;
        TextView clockOut;
        TextView shift;
    }

    public static EmployeeClockInOutFragment instance(long startTime, long endTime, long resisterId) {
        return EmployeeClockInOutFragment_.builder().startTime(startTime).endTime(endTime).resisterId(resisterId).build();
    }
}
