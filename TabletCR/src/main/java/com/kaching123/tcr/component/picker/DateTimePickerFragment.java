package com.kaching123.tcr.component.picker;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by gdubina on 24.01.14.
 */
@EFragment
public class DateTimePickerFragment extends StyledDialogFragment {

    public static final String DIALOG_NAME = "DATE_TIME_PICKER_DIALOG";
    @ViewById
    protected DatePicker datePicker;

    @ViewById
    protected TimePicker timePicker;

    @FragmentArg
    protected Date dateTime;

    @FragmentArg
    protected Date minDate;

    private Calendar calendar = Calendar.getInstance();

    private OnDateTimeSetListener listener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.datetime_picker_dlg_width),
                getDialog().getWindow().getAttributes().height);
        calendar.setTime(dateTime);
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        if (minDate != null) {
            datePicker.setMinDate(minDate.getTime());
            fixDatePickerCalendarView(datePicker);
        }
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    private void fixDatePickerCalendarView(DatePicker datePicker) {
        CalendarView calendarView = datePicker.getCalendarView();
        if (calendarView == null)
            return;

        long calendarViewDate = calendarView.getDate();
        int calendarViewFirstDayOfWeek = calendarView.getFirstDayOfWeek();

        calendarView.setDate(calendarViewDate + 1000L * 60 * 60 * 24 * 40, false, true);
        calendarView.setFirstDayOfWeek((calendarViewFirstDayOfWeek + 1) % 7);
        calendarView.setDate(calendarViewDate, false, true);
        calendarView.setFirstDayOfWeek(calendarViewFirstDayOfWeek);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.component_picker_datetime;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.datetime_picker_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_ok;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if(listener != null){
                    calendar.set(Calendar.YEAR, datePicker.getYear());
                    calendar.set(Calendar.MONTH, datePicker.getMonth());
                    calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                    calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                    return listener.onDateTimeSet(calendar.getTime());
                }
                return true;
            }
        };
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if(listener != null && listener instanceof OnDateTimeActionsListener){
                    return ((OnDateTimeActionsListener)listener).onCancel();
                }
                return true;
            }
        };
    }


    public void setListener(OnDateTimeSetListener listener) {
        this.listener = listener;
    }

    public static void show(FragmentActivity activity, Date dateTime, OnDateTimeSetListener listener){
        DialogUtil.show(activity, DIALOG_NAME, DateTimePickerFragment_.builder().dateTime(dateTime).build()).setListener(listener);
    }

    public static void show(FragmentActivity activity, Date dateTime, Date minDate, OnDateTimeSetListener listener){
        DialogUtil.show(activity, DIALOG_NAME, DateTimePickerFragment_.builder().dateTime(dateTime).minDate(minDate).build()).setListener(listener);
    }

    public interface OnDateTimeSetListener{
        boolean onDateTimeSet(Date datetime);
    }

    public interface OnDateTimeActionsListener extends OnDateTimeSetListener{
        boolean onCancel();
    }
}
