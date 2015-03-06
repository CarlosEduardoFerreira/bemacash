package com.kaching123.tcr.fragment.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import com.kaching123.tcr.fragment.SuperBaseDialogFragment;

import java.util.Calendar;

/**
 * Created by pkabakov on 15.01.14.
 */
@EFragment
public class TimePickerFragment extends SuperBaseDialogFragment implements TimePickerDialog.OnTimeSetListener {

    private static final String DIALOG_NAME = TimePickerFragment.class.getSimpleName();

    private TimePickerDialog.OnTimeSetListener onTimeSetListener;

    @FragmentArg
    Calendar calendar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (onTimeSetListener != null) {
            onTimeSetListener.onTimeSet(view, hourOfDay, minute);
            onTimeSetListener = null;
        }
    }

    public void setOnTimeSetListener(TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        this.onTimeSetListener = onTimeSetListener;
    }

    public static void show(FragmentActivity activity, Calendar calendar, TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        DialogUtil.show(activity, DIALOG_NAME, TimePickerFragment_.builder().calendar(calendar).build()).setOnTimeSetListener(onTimeSetListener);
    }

}
