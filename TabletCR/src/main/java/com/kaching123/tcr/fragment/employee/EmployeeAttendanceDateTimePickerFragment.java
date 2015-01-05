package com.kaching123.tcr.fragment.employee;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by vkompaniets on 06.06.2014.
 */
@EFragment
public class EmployeeAttendanceDateTimePickerFragment extends StyledDialogFragment {

    public static final String DIALOG_NAME = "ATTENDANCE_DATE_TIME_PICKER_DIALOG";

    @ViewById
    protected DatePicker datePicker;

    @ViewById
    protected TimePicker timePicker;

    @FragmentArg
    protected Date dateTime;

    @FragmentArg
    protected boolean isClockin;

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
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

    }

    private void enableSkipButton(boolean enable){
        enableButton(getNeutralButton(), enable, normalBtnColor);
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.component_picker_datetime;
    }

    @Override
    protected int getDialogTitle() {
        return isClockin ? R.string.clockin_datetime_picker_title : R.string.clockout_datetime_picker_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return isClockin ? R.string.btn_next : R.string.btn_ok;
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
                    return listener.onDateTimeSet(calendar.getTimeInMillis());
                }
                return true;
            }
        };
    }

    @Override
    protected OnDialogClickListener getSkipButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (listener != null){
                    return listener.onDateTimeSet(0);
                }
                return true;
            }
        };
    }

    @Override
    protected boolean hasSkipButton() {
        return !isClockin;
    }

    @Override
    protected int getSkipButtonTitle() {
        return R.string.clockout_datetime_picker_skip_btn_title;
    }

    public void setListener(OnDateTimeSetListener listener) {
        this.listener = listener;
    }

    public static void show(FragmentActivity activity, Date dateTime, boolean isClockin, OnDateTimeSetListener listener){
        DialogUtil.show(activity, DIALOG_NAME, EmployeeAttendanceDateTimePickerFragment_.builder().dateTime(dateTime).isClockin(isClockin).build()).setListener(listener);
    }

    public static interface OnDateTimeSetListener{
        boolean onDateTimeSet(long datetime);
    }
}
