package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.FindPrinterCommand;
import com.kaching123.tcr.commands.device.FindPrinterCommand.BaseFindPrinterCallback;
import com.kaching123.tcr.commands.device.PrinterInfo;
import com.kaching123.tcr.commands.store.settings.AddPrintersCommand;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.telly.groundy.TaskHandler;

import java.util.ArrayList;

/**
 * Created by gdubina on 11.02.14.
 */
@EFragment
public class FindPrinterFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = "FindPrinterFragment";

    private FindPrinterCallback findPrinterCallback = new FindPrinterCallback();

    protected ListView listView;
    protected View progressBlock;

    private PrintersAdapter adapter;
    private TaskHandler currentTask;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.settings_find_printers_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.pref_printers_list;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_select;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                ArrayList<PrinterInfo> printers = new ArrayList<PrinterInfo>(checked.size());
                for (int i = 0; i < checked.size(); i++) {
                    if (checked.valueAt(i)) {
                        int position = checked.keyAt(i);
                        printers.add(adapter.getItem(position));
                    }
                }
                StringBuilder notReachablePrinters = new StringBuilder();
                for (PrinterInfo printer : printers) {
                    String tabletNetworkIp = getApp().getCurrentIp();
                    tabletNetworkIp = tabletNetworkIp.substring(0, tabletNetworkIp.lastIndexOf('.'));
                    if (!tabletNetworkIp.equals(printer.ip.substring(0, printer.ip.lastIndexOf('.')))) {
                        notReachablePrinters.append("\n").append(printer.fullAddress);
                    }
                }
                if(notReachablePrinters.length() > 0) {
                    notReachablePrinters.append("\n");
                    AlertDialogFragment.showAlert(getActivity(), R.string.alert_message_printer_is_not_in_same_network_title,
                            getString(R.string.alert_message_printer_is_not_in_same_network, notReachablePrinters.toString()));
                }

                AddPrintersCommand.start(getActivity(), printers);
                return true;
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        listView = (ListView) v.findViewById(R.id.list_view);
        progressBlock = v.findViewById(R.id.progress_block);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter = new PrintersAdapter(getActivity()));
    }

    @Override
    public void onStart() {
        super.onStart();
        currentTask = FindPrinterCommand.start(getActivity(), findPrinterCallback);
        progressBlock.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelCurTask();
    }

    private void cancelCurTask() {
        progressBlock.setVisibility(View.GONE);
        if (currentTask != null) {
            currentTask.cancel(getActivity(), 0, null);
        }
        currentTask = null;
    }

    public class FindPrinterCallback extends BaseFindPrinterCallback {

        @Override
        protected void onSearchFinished() {
            currentTask = null;
            progressBlock.setVisibility(View.GONE);
        }

        @Override
        protected void handleAddPrinter(PrinterInfo info) {
            if (info == null) {
                return;
            }
            for (int i = 0; i < adapter.getCount(); i++) {
                PrinterInfo e = adapter.getItem(i);
                if (e.fullAddress.equals(info.fullAddress)) {
                    return;
                }
            }
            adapter.add(info);
        }
    }

    private class PrintersAdapter extends ArrayAdapter<PrinterInfo> {

        public PrintersAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.simple_list_item_multiple_choice_dark, parent, false);
            }
            PrinterInfo info = getItem(position);
            TextView textView = (TextView) convertView;
            textView.setText(info.fullAddress);
            /*itemView = (PrinterInfoListItem) convertView;
            itemView.bind(info.fullAddress, info.macAddress);*/
            return convertView;
        }

    }

    public static void show(FragmentActivity activity) {
        DialogUtil.show(activity, DIALOG_NAME, FindPrinterFragment_.builder().build());
    }

}
