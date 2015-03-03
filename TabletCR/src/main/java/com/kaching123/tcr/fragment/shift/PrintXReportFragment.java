package com.kaching123.tcr.fragment.shift;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.activity.XReportActivity;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.commands.print.digital.PrintDigitalXReportCommand;
import com.kaching123.tcr.commands.print.digital.PrintDigitalXReportCommand.BasePrintDigitalXReportCallback;
import com.kaching123.tcr.commands.print.digital.SendDigitalXReportCommand;
import com.kaching123.tcr.commands.print.digital.SendDigitalXReportCommand.SendPrintDigitalXReportCallback;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand.BasePrintCallback;
import com.kaching123.tcr.commands.print.pos.PrintXReportCommand;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;

/**
 * Created by pkabakov on 06.12.13.
 */
@EFragment
public class PrintXReportFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = PrintXReportFragment.class.getSimpleName();

    @ViewById
    protected CheckBox printBox;
    @ViewById
    protected CheckBox emailBox;
    @ViewById
    protected CheckBox screenBox;

    @FragmentArg
    protected String shiftGuid;

    @FragmentArg
    protected ReportType xReportType;

    private boolean email;
    private boolean screen;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.shift_print_xreport_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        if (ReportType.X_REPORT_CURRENT_SHIFT == xReportType || ReportType.X_REPORT_DAILY_SALES == xReportType) {
            return R.string.xreport;
        }
        return R.string.dlg_print_zreport;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_close;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_confirm;
    }


    public static void show(FragmentActivity activity, String shiftGuid, ReportType xReportType) {
        DialogUtil.show(activity, DIALOG_NAME, PrintXReportFragment_.builder()
                .shiftGuid(shiftGuid)
                .xReportType(xReportType)
                .build());
    }

    public static void show(FragmentActivity activity, String shiftGuid) {
        DialogUtil.show(activity, DIALOG_NAME, PrintXReportFragment_.builder()
                .shiftGuid(shiftGuid)
                .build());
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

    private void posPrint(boolean ignorePaperEnd, boolean searchByMac) {
        if (getActivity() == null)
            return;

        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        PrintXReportCommand.start(getActivity(), shiftGuid, xReportType, ignorePaperEnd, searchByMac, new PrintXReportCallback());
    }

    private void sendDigital() {
        if (getActivity() == null)
            return;
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        SendDigitalXReportCommand.start(getActivity(), shiftGuid, xReportType, new SendDigitalXReportCallback());
    }

    private void digitalPrint() {
        if (getActivity() == null)
            return;
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        PrintDigitalXReportCommand.start(getActivity(), shiftGuid, xReportType, new PrintDigitalXReportCallback());
    }

    private void showOnScreen(Uri attachment) {
        if (getActivity() == null)
            return;

        XReportActivity.start(getActivity(), attachment, xReportType);
        dismiss();
    }

    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            enablePositiveButtons(printBox.isChecked() || emailBox.isChecked() || screenBox.isChecked());
        }

    };

    private class PrintXReportCallback extends BasePrintCallback {

        private IPrintCallback retryListener = new IPrintCallback() {
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
        protected void onPrintSuccess() {
            if (getActivity() == null)
                return;

            WaitDialogFragment.hide(getActivity());
            go2Step2();
        }

        @Override
        protected void onPrintError(PrinterError error) {
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

    private class PrintDigitalXReportCallback extends BasePrintDigitalXReportCallback {

        @Override
        protected void onDigitalPrintSuccess(Uri attachment) {
            if (getActivity() == null)
                return;

            WaitDialogFragment.hide(getActivity());
            PrintXReportFragment.this.showOnScreen(attachment);
        }

        @Override
        protected void onDigitalPrintError() {
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
    }

    private class SendDigitalXReportCallback extends SendPrintDigitalXReportCallback {

        @Override
        protected void handleSuccess() {
            if (getActivity() == null)
                return;

            WaitDialogFragment.hide(getActivity());
            go2Step3();
        }

        @Override
        protected void handleFailure() {
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
    }

}
