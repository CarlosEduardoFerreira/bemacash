package com.kaching123.tcr.fragment.reports;

import android.view.View;
import android.widget.EditText;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.component.picker.DateTimePickerFragment;
import com.kaching123.tcr.component.picker.DateTimePickerFragment.OnDateTimeSetListener;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by gdubina on 23.01.14.
 */
@EFragment
public abstract class DateRangeFragment extends SuperBaseFragment {

    protected static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);
    protected static final long HOUR_IN_MILLIS = TimeUnit.HOURS.toMillis(1);
    protected static final SimpleDateFormat periodDateFormat = new SimpleDateFormat("h:mm a  dd MMM");

    @ViewById
    protected EditText fromEdit;

    @ViewById
    protected EditText toEdit;

    protected Calendar dateTimePickerCalendar = Calendar.getInstance();
    protected Date fromDate;
    protected Date toDate;
    protected boolean inDays;

    @AfterViews
    protected void initViews() {
        initPeriodDates();
        setPeriodDates();

        fromEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker(fromDate);
            }
        });
        toEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker(toDate);
            }
        });

    }

    private void initPeriodDates() {
        fromDate = initFromDate(dateTimePickerCalendar);
        toDate = initToDate(dateTimePickerCalendar);
        inDays = isPeriodInDays(fromDate, toDate);
    }

    protected Date initFromDate(Calendar dateTimePickerCalendar) {
        dateTimePickerCalendar.setTimeInMillis(new Date().getTime());
        dateTimePickerCalendar.set(Calendar.HOUR_OF_DAY, 00);
        dateTimePickerCalendar.set(Calendar.MINUTE, 00);
        dateTimePickerCalendar.set(Calendar.SECOND, 00);
        dateTimePickerCalendar.set(Calendar.MILLISECOND, 0);
        return new Date(dateTimePickerCalendar.getTimeInMillis());
    }

    protected Date initToDate(Calendar dateTimePickerCalendar) {
        dateTimePickerCalendar.setTimeInMillis(new Date().getTime());
        dateTimePickerCalendar.set(Calendar.HOUR_OF_DAY, 23);
        dateTimePickerCalendar.set(Calendar.MINUTE, 59);
        dateTimePickerCalendar.set(Calendar.SECOND, 59);
        dateTimePickerCalendar.set(Calendar.MILLISECOND, 0);
        return new Date(dateTimePickerCalendar.getTimeInMillis());
    }

    protected void showDateTimePicker(final Date date) {
        dateTimePickerCalendar.setTimeInMillis(date.getTime());
        DateTimePickerFragment.show(getActivity(), date, new OnDateTimeSetListener() {
            @Override
            public boolean onDateTimeSet(Date dateTime) {
                dateTimePickerCalendar.setTime(dateTime);
                final boolean isFromDate = date == fromDate;
                dateTimePickerCalendar.set(Calendar.SECOND, isFromDate ? 0 : 59);
                dateTimePickerCalendar.set(Calendar.MILLISECOND, 0);
                return setPeriodDate(date, dateTimePickerCalendar.getTime());
            }
        });

        /*DatePickerFragment.show(getActivity(), dateTimePickerCalendar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
                TimePickerFragment.show(getActivity(), dateTimePickerCalendar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        dateTimePickerCalendar.set(Calendar.YEAR, year);
                        dateTimePickerCalendar.set(Calendar.MONTH, monthOfYear);
                        dateTimePickerCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        dateTimePickerCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        dateTimePickerCalendar.set(Calendar.MINUTE, minute);
                        final boolean isFromDate = date == fromDate;
                        dateTimePickerCalendar.set(Calendar.SECOND, isFromDate ? 0 : 59);
                        dateTimePickerCalendar.set(Calendar.MILLISECOND, 0);
                        setPeriodDate(date, new Date(dateTimePickerCalendar.getTimeInMillis()));
                    }
                });
            }
        });*/
    }

    protected boolean setPeriodDate(Date date, Date newDate) {
        if(date.getTime() == newDate.getTime())
            return true;

        if (!validatePeriodDates(date, newDate))
            return false;

        date.setTime(newDate.getTime());
        inDays = isPeriodInDays(fromDate, toDate);
        setPeriodDates();

        loadData();
        return true;
    }

    protected abstract void loadData();

    protected abstract int getMaxPeriod();

    protected void setPeriodDates() {
        fromEdit.setText(periodDateFormat.format(fromDate).toUpperCase());
        toEdit.setText(periodDateFormat.format(toDate).toUpperCase());
    }

    protected boolean validatePeriodDates(Date date, Date newDate) {
        boolean isFromDate = date == this.fromDate;
        Date fromDate = isFromDate ? newDate : this.fromDate;
        Date toDate = !isFromDate ? newDate : this.toDate;
        if (!fromDate.before(toDate)) {
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.reports_error_from_to_dates));
            return false;
        }

        final boolean inDays = isPeriodInDays(fromDate, toDate);
        final int periodsCount = Math.round((toDate.getTime() - fromDate.getTime()) / (inDays ? DAY_IN_MILLIS : HOUR_IN_MILLIS) + 0.5f);
        int maxPeriod = getMaxPeriod();
        if (periodsCount > maxPeriod) {
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(inDays ? R.string.reports_error_period_too_large_days : R.string.reports_error_period_too_large_hrs, maxPeriod));
            cropPeriodDates(fromDate, toDate, inDays, isFromDate, maxPeriod);
        }

        return true;
    }

    protected static boolean isPeriodInDays(Date fromDate, Date toDate) {
        return (toDate.getTime() - fromDate.getTime()) > DAY_IN_MILLIS;
    }

    protected static void cropPeriodDates(Date fromDate, Date toDate, boolean inDays, boolean fromDateChanged, int maxPeriod) {
        if (fromDateChanged)
            toDate.setTime(fromDate.getTime() + (maxPeriod - 1) * (inDays ? DAY_IN_MILLIS : HOUR_IN_MILLIS));
        else
            fromDate.setTime(toDate.getTime() - (maxPeriod - 1) * (inDays ? DAY_IN_MILLIS : HOUR_IN_MILLIS));
    }
}