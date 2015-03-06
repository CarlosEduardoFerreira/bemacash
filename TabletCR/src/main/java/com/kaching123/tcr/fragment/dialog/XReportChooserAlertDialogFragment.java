package com.kaching123.tcr.fragment.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;

/**
 * Created by idyuzheva on 30.06.2014.
 */

@EFragment
public class XReportChooserAlertDialogFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = "XReportTypeFragment";

    public interface XReportTypeChooseListener {
        void onXReportTypeChosen(ReportType xReportType);
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
        return R.string.xreport_chooser_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_ok;
    }

    private XReportTypeChooseListener xReportTypeChooseListener;

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (xReportTypeChooseListener != null) {
                    if (chooserGroup.getCheckedRadioButtonId() == R.id.chooser_first_radio_button) {
                        xReportTypeChooseListener.onXReportTypeChosen(ReportType.X_REPORT_CURRENT_SHIFT);
                    } else if (chooserGroup.getCheckedRadioButtonId() == R.id.chooser_second_radio_button) {
                        xReportTypeChooseListener.onXReportTypeChosen(ReportType.X_REPORT_DAILY_SALES);
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
            xReportTypeChooseListener = ((XReportTypeChooseListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " or parent fragments must implement XReportTypeChooseListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        currentShiftRadioButton.setText(R.string.xreport_chooser_current_shift_sales);
        daySaleRadioButton.setText(R.string.xreport_chooser_sale_for_a_day);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    public static void show(FragmentActivity activity) {
        DialogUtil.show(activity, DIALOG_NAME, XReportChooserAlertDialogFragment_.builder().build());
    }

}

