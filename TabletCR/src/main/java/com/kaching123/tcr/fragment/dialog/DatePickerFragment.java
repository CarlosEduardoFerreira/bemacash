package com.kaching123.tcr.fragment.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.DatePicker;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.SuperBaseDialogFragment;

import java.util.Calendar;

/**
 * Created by pkabakov on 15.01.14.
 */
@EFragment
public class DatePickerFragment extends SuperBaseDialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final String DIALOG_NAME = DatePickerFragment.class.getSimpleName();

    private DatePickerDialog.OnDateSetListener onDateSetListener;

    @FragmentArg
    Calendar calendar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), R.style.TransparentDiaolg ,this, year, month, day);

    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (onDateSetListener != null) {
            onDateSetListener.onDateSet(view, year, monthOfYear, dayOfMonth);
            onDateSetListener = null;
        }
    }

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener onDateSetListener) {
        this.onDateSetListener = onDateSetListener;
    }

    public static void show(FragmentActivity activity, Calendar calendar, DatePickerDialog.OnDateSetListener onDateSetListener) {
        DialogUtil.show(activity, DIALOG_NAME, DatePickerFragment_.builder().calendar(calendar).build()).setOnDateSetListener(onDateSetListener);
    }

}
