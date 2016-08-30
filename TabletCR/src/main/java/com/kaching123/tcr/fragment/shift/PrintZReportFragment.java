package com.kaching123.tcr.fragment.shift;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.activity.ZReportActivity;
import com.kaching123.tcr.commands.print.digital.PrintDigitalZReportCommand;
import com.kaching123.tcr.commands.print.digital.SendDigitalZReportCommand;
import com.kaching123.tcr.commands.print.pos.PrintZReportCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;

import org.androidannotations.annotations.EFragment;


/**
 * Created by alboyko on 26.11.2015.
 */

@EFragment
public class PrintZReportFragment extends PrintBaseReportFragment {

    private static final String DIALOG_NAME = PrintZReportFragment.class.getSimpleName();


    @Override
    protected int getDialogTitle() {
        if (ReportType.Z_REPORT_CURRENT_SHIFT == reportType || ReportType.Z_REPORT_DAILY_SALES == reportType) {
            return R.string.zreport;
        }
        return R.string.dlg_print_zreport;
    }

    public static void show(FragmentActivity activity, String shiftGuid, ReportsActivity.ReportType reportType) {
        DialogUtil.show(activity, DIALOG_NAME, PrintZReportFragment_.builder()
                .shiftGuid(shiftGuid)
                .reportType(reportType)
                .build());
    }

    public static void show(FragmentActivity activity, String shiftGuid) {
        DialogUtil.show(activity, DIALOG_NAME, PrintZReportFragment_.builder()
                .shiftGuid(shiftGuid)
                .build());
    }


    @Override
    protected void posPrint(boolean ignorePaperEnd, boolean searchByMac) {
        if (getActivity() == null)
            return;

        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        PrintZReportCommand.start(getActivity(), shiftGuid, reportType, ignorePaperEnd, searchByMac, new PrintReportCallback());
    }

    @Override
    protected void sendDigital() {
        if (getActivity() == null)
            return;
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        SendDigitalZReportCommand.start(getActivity(), shiftGuid, reportType, new SendDigitalZReportCallback());
    }

    @Override
    protected void digitalPrint() {
        if (getActivity() == null)
            return;
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        PrintDigitalZReportCommand.start(getActivity(), shiftGuid, reportType, new PrintDigitalZReportCallback());
    }

    @Override
    protected void showOnScreen(Uri attachment) {
        if (getActivity() == null)
            return;
        ZReportActivity.start(getActivity(), attachment, reportType);
        dismiss();
    }

    private class PrintDigitalZReportCallback extends PrintDigitalZReportCommand.BasePrintDigitalZReportCallback {

        @Override
        protected void onDigitalPrintSuccess(Uri attachment) {
            printDigitalReportCallbackSuccess(attachment);
        }

        @Override
        protected void onDigitalPrintError() {
            printDigitalReportCallbackFailure();
        }
    }

    private class SendDigitalZReportCallback extends SendDigitalZReportCommand.SendPrintDigitalZReportCallback {

        @Override
        protected void handleSuccess() {
            sendDigitalReportCallbackSuccess();
        }

        @Override
        protected void handleFailure() {
            sendDigitalReportCallbackFailure();
        }
    }
}
