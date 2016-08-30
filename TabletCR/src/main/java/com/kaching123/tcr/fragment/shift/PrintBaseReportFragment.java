package com.kaching123.tcr.fragment.shift;

import android.net.Uri;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand;
import com.kaching123.tcr.commands.print.pos.PrintDetailedSalesReportCommand;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * Created by mboychenko on 24.08.2016.
 */
@EFragment
public abstract class PrintBaseReportFragment extends StyledDialogFragment {

    @ViewById
    protected CheckBox printBox;
    @ViewById
    protected CheckBox emailBox;
    @ViewById
    protected CheckBox screenBox;

    @FragmentArg
    protected String shiftGuid;

    @FragmentArg
    protected ReportsActivity.ReportType reportType;

    protected boolean email;
    protected boolean screen;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.shift_print_xreport_dialog_fragment;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_close;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_confirm;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.shift_dlg_width),
                getResources().getDimensionPixelOffset(R.dimen.shift_dlg_heigth));
        setCancelable(false);
    }

    @AfterViews
    protected void initViews() {
        enablePositiveButtons(false);
        printBox.setOnCheckedChangeListener(onCheckedChangeListener);
        emailBox.setOnCheckedChangeListener(onCheckedChangeListener);
        screenBox.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            enablePositiveButtons(printBox.isChecked() || emailBox.isChecked() || screenBox.isChecked());
        }

    };

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                onConfirm();
                return false;
            }
        };
    }

    private void onConfirm() {
        boolean print = printBox.isChecked();
        email = emailBox.isChecked();
        screen = screenBox.isChecked();

        if (print) {
            posPrint(false, false);
        } else if (email) {
            sendDigital();
        } else if (screen) {
            digitalPrint();
        }
    }

    private void go2Step2() {
        email = emailBox.isChecked();
        screen = screenBox.isChecked();
        if (email) {
            sendDigital();
        } else if (screen) {
            digitalPrint();
        } else {
            dismiss();
        }
    }

    private void go2Step3() {
        screen = screenBox.isChecked();
        if (screen) {
            digitalPrint();
        } else {
            dismiss();
        }
    }

    protected abstract void posPrint(boolean ignorePaperEnd, boolean searchByMac);
    protected abstract void sendDigital();
    protected abstract void digitalPrint();
    protected abstract void showOnScreen(Uri attachment);

    protected class PrintReportCallback extends BasePrintCommand.BasePrintCallback {

        private PrintCallbackHelper2.IPrintCallback retryListener = new PrintCallbackHelper2.IPrintCallback() {
            @Override
            public void onRetry(boolean ignorePaperEnd, boolean searchByMac) {
                posPrint(ignorePaperEnd, searchByMac);
            }

            @Override
            public void onCancel() {
                onPrintSuccess();
            }
        };

        @Override
        protected void onPrintSuccessWithInfo(String msg) {
            if (getActivity() == null)
                return;
            WaitDialogFragment.hide(getActivity());

            if(msg.equals(PrintDetailedSalesReportCommand.NO_SALES_FOR_THIS_FILTER)) {
                showAlertNoSalesForFilter();
            }
        }

        @Override
        protected void onPrintSuccess() {
            if (getActivity() == null)
                return;

            WaitDialogFragment.hide(getActivity());
            go2Step2();
        }

        @Override
        protected void onPrintError(PrinterCommand.PrinterError error) {
            if (getActivity() == null)
                return;
            PrintCallbackHelper2.onPrintError(getActivity(), error, retryListener);
        }

        @Override
        protected void onPrinterDisconnected() {
            if (getActivity() == null)
                return;
            PrintCallbackHelper2.onPrinterDisconnected(getActivity(), retryListener);
        }

        @Override
        protected void onPrinterIPnotFound() {
            if (getActivity() == null)
                return;
            PrintCallbackHelper2.onPrinterIPnotFound(getActivity(), retryListener);
        }

        @Override
        protected void onPrinterNotConfigured() {
            if (getActivity() == null)
                return;
            PrintCallbackHelper2.onPrinterNotConfigured(getActivity(), retryListener);
        }

        @Override
        protected void onPrinterPaperNearTheEnd() {
            if (getActivity() == null)
                return;
            PrintCallbackHelper2.onPrinterPaperNearTheEnd(getActivity(), retryListener);
        }

    }

    protected void sendDigitalReportCallbackSuccess(){
        if (getActivity() == null)
            return;

        WaitDialogFragment.hide(getActivity());
        go2Step3();
    }

    protected void sendDigitalReportCallbackFailure(){
        if (getActivity() == null)
            return;

        WaitDialogFragment.hide(getActivity());
        AlertDialogFragment.showAlert(
                getActivity(),
                R.string.error_dialog_title,
                getString(R.string.error_message_email),
                R.string.btn_try_again,
                new OnDialogClickListener() {

                    @Override
                    public boolean onClick() {
                        sendDigital();
                        return true;
                    }
                }
        );
    }

    protected void printDigitalReportCallbackSuccess(Uri attachment){
        if (getActivity() == null)
            return;

        WaitDialogFragment.hide(getActivity());
        if(attachment != null) {
            showOnScreen(attachment);
        }
    }

    protected void printDigitalReportCallbackFailure(){
        if (getActivity() == null)
            return;

        WaitDialogFragment.hide(getActivity());
        AlertDialogFragment.showAlert(
                getActivity(),
                R.string.error_dialog_title,
                getString(R.string.error_print_digital_order),
                R.string.btn_try_again,
                new OnDialogClickListener() {

                    @Override
                    public boolean onClick() {
                        digitalPrint();
                        return true;
                    }
                }
        );
    }

    protected void showAlertNoSalesForFilter(){
        if (getActivity() == null)
            return;

        WaitDialogFragment.hide(getActivity());
        AlertDialogFragment.showAlert(
                getActivity(),
                R.string.alert_dialog_title,
                getString(R.string.no_sales_for_filter));
    }
}
