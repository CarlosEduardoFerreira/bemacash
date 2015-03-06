package com.kaching123.tcr.fragment.user;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;


/**
 * Created by pkabakov on 06.01.14.
 */
@EFragment
public class TimesheetNewFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = TimesheetNewFragment.class.getSimpleName();


    @ViewById
    protected EditText login;

    /*@ViewById
    protected EditText password;*/

    private OnTimesheetListener onTimesheetListener;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.timesheet_chekin_or_out_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.timesheet_new_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_checkin;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_cancel;
    }
    @Override
    protected boolean hasSkipButton() {
        return true;
    }
    @Override
    protected boolean hasNegativeButton() {
        return true;
    }

    @Override
    protected boolean hasPositiveButton() {
        return true;
    }
   @Override
   protected int getSkipButtonTitle()
   {
       return R.string.btn_checkout;
   }


    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                return Btn_Check_In_Selected();
            }
        };
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                return Btn_Cancal_Selected();
            }
        };
    }

    @Override
    protected OnDialogClickListener getSkipButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                return Btn_Check_Out_Selected();
            }
        };
    }

    public void setOnTimesheetListener(OnTimesheetListener onTimesheetListener) {
        this.onTimesheetListener = onTimesheetListener;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.holdon_dlg_width),
                getDialog().getWindow().getAttributes().height);
    }

    @AfterViews
    protected void onIniView() {

    }

    public static void show(FragmentActivity activity, OnTimesheetListener onTimesheetListener) {
        TimesheetNewFragment fragment = TimesheetNewFragment_.builder().build();
        fragment.setOnTimesheetListener(onTimesheetListener);
        DialogUtil.show(activity, DIALOG_NAME, fragment);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    protected boolean Btn_Cancal_Selected() {
            System.out.println("trace----cancel---");
            onTimesheetListener.onCancelSelected();
        return true;
    }
    protected boolean Btn_Check_In_Selected() {
        System.out.println("trace----in---");
        onTimesheetListener.onCheckInSelected();
        return false;
    }
    protected boolean Btn_Check_Out_Selected() {
        System.out.println("trace----out---");
        onTimesheetListener.onCheckOutSelected();
        return true;
    }

    public interface OnTimesheetListener {

        public void onCheckInSelected();
        public void onCheckOutSelected();
        public void onCancelSelected();

    }

}
