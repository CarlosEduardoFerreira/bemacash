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

/**
 * Created by pkabakov on 06.12.13.
 */
@EFragment
public class PrintXReportFragment extends PrintBaseReportFragment {

    private static final String DIALOG_NAME = PrintXReportFragment.class.getSimpleName();

    @Override
    protected int getDialogTitle() {
        if (ReportType.X_REPORT_CURRENT_SHIFT == reportType || ReportType.X_REPORT_DAILY_SALES == reportType) {
            return R.string.xreport;
        }
        return R.string.dlg_print_zreport;
    }

    public static void show(FragmentActivity activity, String shiftGuid, ReportType reportType) {
        DialogUtil.show(activity, DIALOG_NAME, PrintXReportFragment_.builder()
                .shiftGuid(shiftGuid)
                .reportType(reportType)
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
        PrintXReportCommand.start(getActivity(), shiftGuid, reportType, ignorePaperEnd, searchByMac, new PrintReportCallback(), getXreportSaleEnabled(),getItemXreportSaleEnabled());
    }

    @Override
    protected void sendDigital() {
        if (getActivity() == null)
            return;
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        SendDigitalXReportCommand.start(getActivity(), shiftGuid, reportType, getXreportSaleEnabled(),getItemXreportSaleEnabled(), new SendDigitalXReportCallback());
    }

    @Override
    protected void digitalPrint() {
        if (getActivity() == null)
            return;
        WaitDialogFragment.show(getActivity(), getString(R.string.wait_message_print_zreport));
        PrintDigitalXReportCommand.start(getActivity(), shiftGuid, reportType, new PrintDigitalXReportCallback(), getXreportSaleEnabled(),getItemXreportSaleEnabled());
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
