package com.kaching123.tcr.fragment.shift;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;

import com.kaching123.tcr.R;
import com.kaching123.tcr.activity.ReportsActivity.ReportType;
import com.kaching123.tcr.activity.XReportActivity;
import com.kaching123.tcr.commands.print.digital.PrintDigitalXReportCommand;
import com.kaching123.tcr.commands.print.digital.PrintDigitalXReportCommand.BasePrintDigitalXReportCallback;
import com.kaching123.tcr.commands.print.digital.SendDigitalXReportCommand;
import com.kaching123.tcr.commands.print.digital.SendDigitalXReportCommand.SendPrintDigitalXReportCallback;
import com.kaching123.tcr.commands.print.pos.PrintXReportCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

/**
 * Created by pkabakov on 06.12.13.
 */
@EFragment
public class PrintXReportFragment extends PrintBaseReportFragment {

    private static final String DIALOG_NAME = PrintXReportFragment.class.getSimpleName();
    @FragmentArg
    protected long registerID;

    @FragmentArg
    protected long fromDate;

    @FragmentArg
    protected long toDate;
    @Override
    protected int getDialogTitle() {
        if (ReportType.X_REPORT_CURRENT_SHIFT == reportType || ReportType.X_REPORT_DAILY_SALES == reportType) {
            return R.string.xreport;
        }
        return R.string.dlg_print_zreport;
    }

    public static void show(FragmentActivity activity, String shiftGuid, ReportType reportType, long registerId) {
        DialogUtil.show(activity, DIALOG_NAME, PrintXReportFragment_.builder()
                .shiftGuid(shiftGuid)
                .registerID(registerId)
                .reportType(reportType)
                .build());
    }

    public static void show(FragmentActivity activity, String shiftGuid, ReportType reportType, long registerID, long fromDate, long toDate) {
        DialogUtil.show(activity, DIALOG_NAME, PrintXReportFragment_.builder()
                .shiftGuid(shiftGuid)
                .reportType(reportType)
                .registerID(registerID)
                .fromDate(fromDate)
                .toDate(toDate)
                .build());
    }

    public static void show(FragmentActivity activity, String shiftGuid) {
        DialogUtil.show(activity, DIALOG_NAME, PrintXReportFragment_.builder()
                .shiftGuid(shiftGuid)
                .build());
    }

    @Override
    protected void posPrint(boolean ignorePaperEnd, boolean searchByMac) {
        if (getActivity() == null)
            return;

        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        PrintXReportCommand.start(getActivity(), shiftGuid, reportType, ignorePaperEnd, searchByMac, new PrintReportCallback(), getXreportSaleEnabled(),getItemXreportSaleEnabled(), registerID, fromDate, toDate);
    }

    @Override
    protected void sendDigital() {
        if (getActivity() == null)
            return;
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        SendDigitalXReportCommand.start(getActivity(), shiftGuid, reportType, getXreportSaleEnabled(),getItemXreportSaleEnabled(), new SendDigitalXReportCallback(), registerID, fromDate, toDate);
    }

    @Override
    protected void digitalPrint() {
        if (getActivity() == null)
            return;
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        PrintDigitalXReportCommand.start(getActivity(), shiftGuid, reportType, new PrintDigitalXReportCallback(), getXreportSaleEnabled(),getItemXreportSaleEnabled(), registerID, fromDate, toDate);
    }

    private boolean getItemXreportSaleEnabled() {
        return getApp().getShopPref().enableEreportItemSale().get();
    }

    private boolean getXreportSaleEnabled() {
        return getApp().getShopPref().enableEreportDepartSale().get();
    }

    @Override
    protected void showOnScreen(Uri attachment) {
        if (getActivity() == null)
            return;

        XReportActivity.start(getActivity(), attachment, reportType);
        dismiss();
    }

    private class PrintDigitalXReportCallback extends BasePrintDigitalXReportCallback {

        @Override
        protected void onDigitalPrintSuccess(Uri attachment) {
            printDigitalReportCallbackSuccess(attachment);
        }

        @Override
        protected void onDigitalPrintError() {
            printDigitalReportCallbackFailure();
        }
    }

    private class SendDigitalXReportCallback extends SendPrintDigitalXReportCallback {

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
