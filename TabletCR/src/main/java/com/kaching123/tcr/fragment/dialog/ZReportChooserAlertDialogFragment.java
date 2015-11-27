package com.kaching123.tcr.fragment.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;


/**
 * Created by alboyko on 26.11.2015.
 */

@EFragment
public class ZReportChooserAlertDialogFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = "ZReportTypeFragment";

    public interface ZReportTypeChooserListener {
        void onZReportTypeChosen(ReportType zReportType);
    }

    @ViewById(R.id.chooser_radio_group)
    RadioGroup chooserGroup;

    @ViewById(R.id.chooser_first_radio_button)
    RadioButton currentShiftRadioButton;

    @ViewById(R.id.chooser_second_radio_button)
    RadioButton daySaleRadioButton;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.chooser_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.zreport_chooser_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_ok;
    }

    private ZReportTypeChooserListener zReportTypeChooseListener;

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (zReportTypeChooseListener != null) {
                    if (chooserGroup.getCheckedRadioButtonId() == R.id.chooser_first_radio_button) {
                        zReportTypeChooseListener.onZReportTypeChosen(ReportType.Z_REPORT_CURRENT_SHIFT);
                    } else if (chooserGroup.getCheckedRadioButtonId() == R.id.chooser_second_radio_button) {
                        zReportTypeChooseListener.onZReportTypeChosen(ReportType.Z_REPORT_DAILY_SALES);
                    }
                }
                return true;
            }
        };
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            zReportTypeChooseListener = ((ZReportTypeChooserListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " or parent fragments must implement ZReportTypeChooseListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        currentShiftRadioButton.setText(R.string.zreport_chooser_current_shift_sales);
        daySaleRadioButton.setText(R.string.zreport_chooser_sale_for_a_day);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    public static void show(FragmentActivity activity) {
        DialogUtil.show(activity, DIALOG_NAME, ZReportChooserAlertDialogFragment_.builder().build());
    }
}
