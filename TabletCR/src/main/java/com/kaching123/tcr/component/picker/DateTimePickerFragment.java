package com.kaching123.tcr.component.picker;

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

    public void setListener(OnDateTimeSetListener listener) {
        this.listener = listener;
    }

    public static void show(FragmentActivity activity, Date dateTime, OnDateTimeSetListener  listener){
        DialogUtil.show(activity, DIALOG_NAME, DateTimePickerFragment_.builder().dateTime(dateTime).build()).setListener(listener);
    }

    public static interface OnDateTimeSetListener{
        boolean onDateTimeSet(Date datetime);
    }
}
