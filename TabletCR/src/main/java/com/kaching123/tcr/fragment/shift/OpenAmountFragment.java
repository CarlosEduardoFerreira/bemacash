package com.kaching123.tcr.fragment.shift;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.OpenDrawerCommand;
import com.kaching123.tcr.commands.device.OpenDrawerCommand.BaseOpenDrawerCallback;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.fragment.PrintCallbackHelper;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.edit.DecimalEditFragment;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 06.12.13.
 */
@EFragment
public class OpenAmountFragment extends DecimalEditFragment {

    private static final String DIALOG_NAME = OpenAmountFragment.class.getSimpleName();

    private static final BigDecimal MAX_VALUE = new BigDecimal("9999999.99");

    private OnEditAmountListener editAmountListener;

    private boolean confirmed;

    private BigDecimal openAmount;

    private OpenDrawerCallback openDrawerCallback = new OpenDrawerCallback();

    @Override
    protected BigDecimal getMaxValue() {
        return MAX_VALUE;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.shift_open_amount_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.dlg_open_amount;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_accept;
    }

    @Override
    protected OnDialogClickListener getNegativeButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                if (editAmountListener != null) {
                    editAmountListener.onCancel();
                }
                return true;
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);
    }

    protected boolean onSubmitForm() {
        boolean result = super.onSubmitForm();
        return !result ? result : confirmed;
    }

    @Override
    protected void callCommand(String guid, BigDecimal value) {
        if (confirmed = (value.compareTo(BigDecimal.ZERO) <= 0)) {
            if (editAmountListener != null) {
                editAmountListener.onConfirm(value);
            }
        } else {
            openAmount = value;
            runCommand();
        }
    }

    private void runCommand(){
        try2OpenDrawer(false);
    }

    private void try2OpenDrawer(boolean searchByMac){
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_open_drawer));
        OpenDrawerCommand.start(getActivity(), searchByMac, openDrawerCallback, false);
    }

    @Override
    protected BigDecimal validateForm() {
        BigDecimal value = getDecimalValue();
        BigDecimal maxValue = getMaxValue();
        return value != null && (maxValue == null || value.compareTo(maxValue) != 1) ? value : null;
    }

    public void setEditAmountListener(OnEditAmountListener editAmountListener) {
        this.editAmountListener = editAmountListener;
    }

    public static void show(FragmentActivity activity, OnEditAmountListener listener) {
        OpenAmountFragment fragment = OpenAmountFragment_.builder().decimalValue(BigDecimal.ZERO).build();
        fragment.setEditAmountListener(listener);
        DialogUtil.show(activity, DIALOG_NAME, fragment);
    }

    public static void hide(FragmentActivity activity) {
        DialogUtil.hide(activity, DIALOG_NAME);
    }

    public static interface OnEditAmountListener {
        void onConfirm(BigDecimal value);

        void onPutCash(boolean ignoreDrawable, BigDecimal value);

        void onCancel();
    }

    public class OpenDrawerCallback extends BaseOpenDrawerCallback {

        @Override
        protected void onDrawerIPnoFound() {
            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlert(
                    getActivity(),
                    R.string.open_drawer_error_title,
                    getString(R.string.error_message_printer_ip_not_found),
                    R.string.btn_ok,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            try2OpenDrawer(true);
                            return true;
                        }
                    }
            );
        }

        @Override
        protected void onDrawerOpened(boolean needSync) {
            onDrawerOpened(false, needSync);
        }

        private void onDrawerOpened(boolean ignoreDrawable, boolean needSync) {
            WaitDialogFragment.hide(getActivity());
            if (editAmountListener != null)
                editAmountListener.onPutCash(ignoreDrawable, openAmount);
            dismiss();
        }

        @Override
        protected void onDrawerOpenError(PrinterError error) {
            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlertWithSkip(getActivity(), R.string.open_drawer_error_title, getString(PrintCallbackHelper.getPrinterErrorMessage(error)), new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            runCommand();
                            return true;
                        }
                    }, new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            onDrawerOpened(true);
                            return true;
                        }
                    }
            );
        }
    }
}
