package com.kaching123.tcr.fragment.employee;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.store.user.AddClockInCommand;
import com.kaching123.tcr.commands.store.user.AddClockInCommand.BaseAddClockInCommandCallback;
import com.kaching123.tcr.commands.store.user.UpdateClockInOutCommand;
import com.kaching123.tcr.commands.store.user.UpdateClockInOutCommand.BaseUpdateClockInCommandCallback;
import com.kaching123.tcr.component.picker.DateTimePickerFragment;
import com.kaching123.tcr.component.picker.DateTimePickerFragment.OnDateTimeSetListener;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.employee.EmployeeAttendanceFragment.TimeInfo;
import com.kaching123.tcr.fragment.reports.DateRangeFragment;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.EmployeeTimesheetTable;
import com.kaching123.tcr.util.DateUtils;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.util.DateUtils.formatMilisec;

/**
 * Created by gdubina on 05/02/14.
 */
@EFragment(R.layout.employee_attendance_fragment)
@OptionsMenu(R.menu.employee_attendance_actions)
public class EmployeeAttendanceFragment extends DateRangeFragment implements LoaderCallbacks<List<TimeInfo>> {

    private static final int MAX_PERIODS_COUNT = 12 * 31;
    private static final Uri CLOCK_IN_OUT_URI = ShopProvider.getContentUri(EmployeeTimesheetTable.URI_CONTENT);

    @ViewById(android.R.id.list)
    protected ListView list;

    @FragmentArg
    protected String employeeGuid;

    private TimeAdapter adapter;

    private long newClockin;
    private long newClockout;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        list.setAdapter(adapter = new TimeAdapter(getActivity()));
        loadData();
    }

    @Override
    protected void loadData() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    protected int getMaxPeriod() {
        return MAX_PERIODS_COUNT;
    }

    @Override
    public Loader<List<TimeInfo>> onCreateLoader(int i, Bundle bundle) {
        return CursorLoaderBuilder
                .forUri(CLOCK_IN_OUT_URI)
                .projection(
                        EmployeeTimesheetTable.GUID,
                        EmployeeTimesheetTable.CLOCK_IN,
                        EmployeeTimesheetTable.CLOCK_OUT)
                .where(EmployeeTimesheetTable.EMPLOYEE_GUID + " =?", employeeGuid)
                .where(EmployeeTimesheetTable.CLOCK_IN + " >= ? and " + EmployeeTimesheetTable.CLOCK_IN + " <= ?", fromDate.getTime(), toDate.getTime())
                .orderBy(EmployeeTimesheetTable.CLOCK_IN)
                .transformRow(new Function<Cursor, TimeInfo>() {
                    @Override
                    public TimeInfo apply(Cursor c) {
                        return new TimeInfo(c.getString(0), c.getLong(1), c.getLong(2));
                    }
                })
                .build(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<TimeInfo>> loader, List<TimeInfo> c) {
        adapter.changeCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<List<TimeInfo>> loader) {
        adapter.changeCursor(null);
    }

    @OptionsItem
    protected void actionAddSelected() {
        setClockIn();
    }

    private void setClockIn() {
        EmployeeAttendanceDateTimePickerFragment.show(
                getActivity(),
                new Date(),
                true,
                new EmployeeAttendanceDateTimePickerFragment.OnDateTimeSetListener() {
                    @Override
                    public boolean onDateTimeSet(long datetime) {
                        newClockin = datetime;
                        setClockOut();
                        return true;
                    }
                }
        );
    }

    private void setClockOut() {
        EmployeeAttendanceDateTimePickerFragment.show(
                getActivity(),
                new Date(),
                false,
                new EmployeeAttendanceDateTimePickerFragment.OnDateTimeSetListener() {
                    @Override
                    public boolean onDateTimeSet(long datetime) {
                        if (datetime != 0 && datetime < newClockin){
                            showClockoutLessClockinAlertDialog();
                            return false;
                        }
                        newClockout = datetime;
                        AddClockInCommand.start(getActivity(), employeeGuid, newClockin, newClockout, addClockInCommandCallback);
                        return true;
                    }
                }
        );
    }

    public static EmployeeAttendanceFragment instance(String employeeGuid) {
        return EmployeeAttendanceFragment_.builder().employeeGuid(employeeGuid).build();
    }

    private class TimeAdapter extends ObjectsCursorAdapter<TimeInfo> {

        public TimeAdapter(Context context) {
            super(context);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View v = View.inflate(getContext(), R.layout.employee_attendance_item_view, null);
            ViewHolder holder = new ViewHolder(
                    (TextView) v.findViewById(R.id.time_in),
                    (TextView) v.findViewById(R.id.time_out),
                    (TextView) v.findViewById(R.id.time_total)
            );
            v.setTag(holder);
            holder.in.setOnClickListener(inEdit);
            holder.out.setOnClickListener(outEdit);

            return v;
        }

        @Override
        protected View bindView(View v, int position, TimeInfo i) {
            ViewHolder holder = (ViewHolder) v.getTag();

            holder.in.setTag(i);
            holder.out.setTag(i);

            holder.in.setText(DateUtils.dateAndTimeShortAttendanceFormat(new Date(i.in)));
            long total = 0;
            if (i.out == 0) {
                holder.out.setText(null);
            } else {
                total = i.out - i.in;
                holder.out.setText(DateUtils.dateAndTimeShortAttendanceFormat(new Date(i.out)));
            }

            holder.total.setText(formatMilisec(total));
            return v;
        }
    }

    private OnClickListener inEdit = new OnClickListener() {
        @Override
        public void onClick(View v) {
            TimeInfo info = (TimeInfo) v.getTag();
            updateTime(info.timeGuid, true, info.in, info.out);
        }
    };

    private OnClickListener outEdit = new OnClickListener() {
        @Override
        public void onClick(View v) {
            TimeInfo info = (TimeInfo) v.getTag();
            updateTime(info.timeGuid, false, info.in, info.out);
        }
    };

    private void updateTime(final String timeGuid, final boolean in, final long inTime, final long outTime) {
        Date date;
        long time = in ? inTime : outTime;
        if (time == 0) {
            date = new Date();
        } else {
            date = new Date(time);
        }
        DateTimePickerFragment.show(getActivity(), date, new OnDateTimeSetListener() {
            @Override
            public boolean onDateTimeSet(Date datetime) {
                if(in && outTime > 0 && datetime.getTime() > outTime){
                    showClockoutLessClockinAlertDialog();
                    return false;
                }
                if(!in && datetime.getTime() < inTime){
                    showClockoutLessClockinAlertDialog();
                    return false;
                }
                UpdateClockInOutCommand.start(getActivity(), employeeGuid, timeGuid, in, datetime.getTime(), updateClockInCommandCallback);
                return true;
            }
        });
    }

    private void showClockoutLessClockinAlertDialog(){
        AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.employee_attendance_error_clock_in));
    }

    private static class ViewHolder {
        TextView in;
        TextView out;
        TextView total;

        private ViewHolder(TextView in, TextView out, TextView total) {
            this.in = in;
            this.out = out;
            this.total = total;
        }
    }

    public static class TimeInfo {
        String timeGuid;
        long in;
        long out;

        private TimeInfo(String timeGuid, long in, long out) {
            this.timeGuid = timeGuid;
            this.in = in;
            this.out = out;
        }
    }

    private BaseAddClockInCommandCallback addClockInCommandCallback = new BaseAddClockInCommandCallback() {
        @Override
        protected void onOverlaps() {
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.employee_add_attendance_error_overlaps));
        }
    };

    private BaseUpdateClockInCommandCallback updateClockInCommandCallback = new BaseUpdateClockInCommandCallback() {
        @Override
        protected void onOverlaps() {
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.employee_edit_attendance_error_overlaps));
        }
    };


}
