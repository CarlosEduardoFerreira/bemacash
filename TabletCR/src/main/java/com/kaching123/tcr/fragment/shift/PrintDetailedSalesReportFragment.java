package com.kaching123.tcr.fragment.shift;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.DetailedReportActivity;
import com.kaching123.tcr.commands.print.digital.PrintDigitalDetailedReportCommand;
import com.kaching123.tcr.commands.print.digital.SendDigitalDetailedReportCommand;
import com.kaching123.tcr.commands.print.pos.PrintDetailedSalesReportCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

/**
 * Created by mboychenko on 24.08.2016.
 */

@EFragment
public class PrintDetailedSalesReportFragment extends PrintBaseReportFragment {

    private static final String DIALOG_NAME = PrintDetailedSalesReportFragment.class.getSimpleName();

    @FragmentArg
    protected long fromDate;
    @FragmentArg
    protected long toDate;
    @FragmentArg
    protected long registerId;

    @Override
    protected int getDialogTitle() {
        return R.string.report_detailed_sales;
    }

    public static void show(FragmentActivity activity, long registerId, long fromDate, long toDate) {
        DialogUtil.show(activity, DIALOG_NAME, PrintDetailedSalesReportFragment_.builder()
                .registerId(registerId)
                .fromDate(fromDate)
                .toDate(toDate)
                .build());
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.print_report_dialog_fragment;
    }

    @Override
    protected void posPrint(boolean ignorePaperEnd, boolean searchByMac) {
        if (getActivity() == null)
            return;

        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        PrintDetailedSalesReportCommand.start(getActivity(), registerId, fromDate, toDate, ignorePaperEnd, searchByMac, new PrintReportCallback());
    }

    @Override
    protected void sendDigital() {
        if (getActivity() == null)
            return;
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        SendDigitalDetailedReportCommand.start(getActivity(), registerId, fromDate, toDate, new SendDigitalReportCallback());

    }

    @Override
    protected void digitalPrint() {
        if (getActivity() == null)
            return;
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        PrintDigitalDetailedReportCommand.start(getActivity(), registerId, fromDate, toDate, new PrintDigitalReportCallback());
    }

    @Override
    protected void showOnScreen(Uri attachment) {
        if (getActivity() == null)
            return;

        DetailedReportActivity.start(getActivity(), attachment);
        dismiss();
    }

    private class PrintDigitalReportCallback extends PrintDigitalDetailedReportCommand.BasePrintDigitalDetailedReportCallback{

        @Override
        protected void onDigitalPrintSuccess(Uri attachment) {
            printDigitalReportCallbackSuccess(attachment);
        }

        @Override
        protected void handleSuccessWithInfo(String info) {
            if(info.equals(PrintDigitalDetailedReportCommand.NO_SALES_FOR_THIS_FILTER)) {
                showAlertNoSalesForFilter();
            }
        }

        @Override
        protected void onDigitalPrintError() {
            printDigitalReportCallbackFailure();
        }
    }

    private class SendDigitalReportCallback extends SendDigitalDetailedReportCommand.BaseSendPrintDigitalReportCallback {

        @Override
        protected void handleSuccess() {
            sendDigitalReportCallbackSuccess();
        }

        @Override
        protected void handleSuccessWithInfo(String msg) {
            if(msg.equals(SendDigitalDetailedReportCommand.NO_SALES_FOR_THIS_FILTER)) {
                showAlertNoSalesForFilter();
            }
        }

        @Override
        protected void handleFailure() {
            sendDigitalReportCallbackFailure();
        }
    }
}
