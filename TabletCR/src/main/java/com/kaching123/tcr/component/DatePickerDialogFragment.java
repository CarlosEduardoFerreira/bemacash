/*
 * Copyright 2012 David Cesarino de Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaching123.tcr.component;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.DatePicker;

import com.kaching123.tcr.fragment.dialog.DialogUtil;

import java.util.Calendar;

/**
 * <p>Provides a usable {@link DatePickerDialog} wrapped as a {@link DialogFragment},
 * using the compatibility package v4. Its main advantage is handling Issue 34833 
 * automatically for you.</p>
 * 
 * <p>Current implementation (because I wanted that way =) ):</p>
 * 
 * <ul>
 * <li>Only two buttons, a {@code BUTTON_POSITIVE} and a {@code BUTTON_NEGATIVE}.
 * <li>Buttons labeled from {@code android.R.string.ok} and {@code android.R.string.cancel}.
 * </ul>
 * 
 * <p><strong>Usage sample:</strong></p>
 * 
 * <pre>class YourActivity extends Activity implements OnDateSetListener
 * 
 * // ...
 * 
 * Bundle b = new Bundle();
 * b.putInt(DatePickerDialogFragment.YEAR, 2012);
 * b.putInt(DatePickerDialogFragment.MONTH, 6);
 * b.putInt(DatePickerDialogFragment.DATE, 17);
 * DialogFragment picker = new DatePickerDialogFragment();
 * picker.setArguments(b);
 * picker.show(getActivity().getSupportFragmentManager(), "fragment_date_picker");</pre>
 * 
 * @author davidcesarino@gmail.com
 * @version 2015.0904
 * @see <a href="http://code.google.com/p/android/issues/detail?id=34833">Android Issue 34833</a>
 * @see <a href="http://stackoverflow.com/q/11444238/489607"
 * >Jelly Bean DatePickerDialog — is there a way to cancel?</a>
 *
 */
public class DatePickerDialogFragment extends DialogFragment {

    private static final String DIALOG_NAME = DatePickerDialogFragment.class.getSimpleName();
    
    public static final String YEAR = "Year";
    public static final String MONTH = "Month";
    public static final String DATE = "Day";
    
    private OnDateSetListener mListener;

    @Override
    public void onDetach() {
        this.mListener = null;
        super.onDetach();
    }

    @TargetApi(11)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle b = getArguments();
        int y = b.getInt(YEAR);
        int m = b.getInt(MONTH);
        int d = b.getInt(DATE);
        
        final DatePickerDialog picker = new DatePickerDialog(getActivity(),
                getConstructorListener(), y, m, d);

        if (isAffectedVersion()) {
            picker.setButton(DialogInterface.BUTTON_POSITIVE,
                    getActivity().getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatePicker dp = picker.getDatePicker();
                            mListener.onDateSet(dp,
                                    dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                        }
                    });
            picker.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getActivity().getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
        }
        return picker;
    }

    private static boolean isAffectedVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    private OnDateSetListener getConstructorListener() {
        return isAffectedVersion() ? null : mListener;
    }

    public static void show(FragmentActivity activity, Calendar initialDate, OnDateSetListener listener){
        Bundle b = new Bundle(3);
        b.putInt(YEAR, initialDate.get(Calendar.YEAR));
        b.putInt(MONTH, initialDate.get(Calendar.MONTH));
        b.putInt(DATE, initialDate.get(Calendar.DATE));

        DatePickerDialogFragment picker = new DatePickerDialogFragment();
        picker.setArguments(b);
        picker.mListener = listener;

        DialogUtil.show(activity, DIALOG_NAME, picker);
    }
}
