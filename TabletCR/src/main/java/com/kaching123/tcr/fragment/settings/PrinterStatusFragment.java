package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.kaching123.pos.data.PrinterStatusEx;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.commands.device.GetPrinterStatusCommand;
import com.kaching123.tcr.commands.device.GetPrinterStatusCommand.BasePrinterStatusCallback;
import com.kaching123.tcr.commands.device.PrinterCommand.PrinterError;
import com.kaching123.tcr.fragment.PrintCallbackHelper;
import com.kaching123.tcr.fragment.PrintCallbackHelper.IRetryWithSkipCallback;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.PrintCallbackHelper2.IPrintCallback;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

import java.util.ArrayList;

/**
 * Created by gdubina on 11.02.14.
 */
@EFragment
public class PrinterStatusFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = "PrinterStatusFragment";

    private PrinterStatusCallback printerStatusCallback = new PrinterStatusCallback();

    protected ListView printerStatusList;

    protected View progressBlock;

    @FragmentArg
    protected String printerIp;

    @FragmentArg
    protected int printerPort;

    @FragmentArg
    protected String printerMac;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.settings_printer_status_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.printer_status_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_ok;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        progressBlock = v.findViewById(R.id.progress_block);
        printerStatusList = (ListView) v.findViewById(R.id.list_view);
        checkPrinterState(false);
    }

    private void checkPrinterState(boolean searchByMac){
        GetPrinterStatusCommand.start(getActivity(), printerIp, printerPort, printerMac, searchByMac, printerStatusCallback);
    }

    public class PrinterStatusCallback extends BasePrinterStatusCallback {

        @Override
        protected void onPrinterStatusSuccess(PrinterStatusEx statusInfo) {
            printerStatusList.setAdapter(new PrinterStatusAdapter(getActivity(), convert(statusInfo)));
            progressBlock.setVisibility(View.GONE);
        }

        @Override
        protected void onPrinterStatusError(PrinterError error) {
            PrintCallbackHelper.onPrintErrorOnlySkip(getActivity(), error, null, null, new IRetryWithSkipCallback() {
                @Override
                public void onRetry(String fromPrinter) {
                }

                @Override
                public void onSkip(String fromPrinter) {
                }
            });
            setFailure();
        }

        @Override
        protected void onPrinterIpNotFound() {
            progressBlock.setVisibility(View.GONE);
            PrintCallbackHelper2.onPrinterIPnotFound(getActivity(), new IPrintCallback() {
                @Override
                public void onRetry(boolean searchByMac, boolean ignorePaperEnd) {
                    progressBlock.setVisibility(View.VISIBLE);
                    checkPrinterState(searchByMac);
                }

                @Override
                public void onCancel() {
                    setFailure();
                }
            });
        }

        private void setFailure(){
            printerStatusList.setAdapter(new PrinterStatusAdapter(getActivity(), convert(null)));
            progressBlock.setVisibility(View.GONE);
        }
    }

    private class PrinterStatusAdapter extends ObjectsCursorAdapter<PrinterStatusLine> {

        public PrinterStatusAdapter(Context context, ArrayList<PrinterStatusLine> lines) {
            super(context);
            changeCursor(lines);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return PrinterStatusListItem_.build(getActivity());
        }

        @Override
        protected View bindView(View convertView, int position, PrinterStatusLine item) {
            PrinterStatusListItem view = (PrinterStatusListItem) convertView;
            view.bind(item.labelTextId, item.valueTextId, item.valueTextColor);
            return view;
        }
    }

    private static ArrayList<PrinterStatusLine> convert(PrinterStatusEx statusInfo) {
        ArrayList<PrinterStatusLine> result = new ArrayList<PrinterStatusLine>();

        result.add(new PrinterStatusLine(R.string.printer_status_online_label, statusInfo != null && !statusInfo.printerStatus.printerIsOffline));
        result.add(new PrinterStatusLine(R.string.printer_status_paper_present_label, statusInfo != null && !statusInfo.offlineStatus.noPaper));
        result.add(new PrinterStatusLine(R.string.printer_status_cover_closed_label, statusInfo != null && statusInfo.offlineStatus.coverIsClosed));
        result.add(new PrinterStatusLine(R.string.printer_status_temperature_normal_label, statusInfo != null && !statusInfo.printerHead.headIsOverhead));
        result.add(new PrinterStatusLine(R.string.printer_status_cutter_ok_label, statusInfo != null && !statusInfo.errorStatus.cutterErrorIsDetected && !statusInfo.errorStatus.cutterIsAbsent));
        result.add(new PrinterStatusLine(R.string.printer_status_drawer_is_closed_label, statusInfo != null && statusInfo.offlineStatus.drawerIsClosed));
        result.add(new PrinterStatusLine(R.string.printer_status_paper_full_label, statusInfo != null && !statusInfo.offlineStatus.paperIsNearEnd));
        return result;
    }

    private static class PrinterStatusLine {
        int labelTextId;
        int valueTextId;
        int valueTextColor;

        private PrinterStatusLine(int labelTextId, int valueTextId, int valueTextColor) {
            this.labelTextId = labelTextId;
            this.valueTextId = valueTextId;
            this.valueTextColor = valueTextColor;
        }

        private PrinterStatusLine(int labelTextId, boolean value) {
            this.labelTextId = labelTextId;
            this.valueTextId = value ? R.string.printer_status_yes_value : R.string.printer_status_no_value;
            this.valueTextColor = value ? R.color.dlg_text_green : R.color.dlg_text_red;
        }

        private PrinterStatusLine(int labelTextId, boolean value, boolean invertColor) {
            this.labelTextId = labelTextId;
            this.valueTextId = value ? R.string.printer_status_yes_value : R.string.printer_status_no_value;
            if (invertColor) {
                this.valueTextColor = value ? R.color.dlg_text_red : R.color.dlg_text_green;
            } else {
                this.valueTextColor = value ? R.color.dlg_text_green : R.color.dlg_text_red;
            }
        }
    }

    public static void show(FragmentActivity activity, String printerIp, int port, String macAddress) {
        DialogUtil.show(activity, DIALOG_NAME, PrinterStatusFragment_.builder().printerIp(printerIp).printerPort(port).printerMac(macAddress).build());
    }
}
