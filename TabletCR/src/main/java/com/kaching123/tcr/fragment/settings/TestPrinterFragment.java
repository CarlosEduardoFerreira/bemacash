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

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.ConfigurePrinterCommand;
import com.kaching123.tcr.commands.device.FindPrinterCommand;
import com.kaching123.tcr.commands.device.FindPrinterCommand.BaseFindPrinterCallback;
import com.kaching123.tcr.commands.device.GetPrinterStatusCommand;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.commands.device.PrinterInfo;
import com.kaching123.tcr.commands.print.pos.BasePrintCommand;
import com.kaching123.tcr.commands.print.pos.PrintOrderCommand;
import com.kaching123.tcr.commands.store.settings.AddPrintersCommand;
import com.kaching123.tcr.fragment.PrintCallbackHelper2;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.PrinterModel;
import com.telly.groundy.TaskHandler;

import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by long.jiao on 8.14.15.
 */
@EFragment
public class TestPrinterFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = "TestPrinterFragment";

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
                        PrinterInfo info = adapter.getItem(position);
                        printers.add(info);
                        PrinterModel model = new PrinterModel(
                                UUID.randomUUID().toString(),
                                info.ip,
                                info.port,
                                info.macAddress,
                                info.subNet,
                                info.gateway,
                                info.dhcp,
                                null,
                                null);
//                        WaitDialogFragment.show(getActivity(), getString(R.string.wait_printing));
                        ConfigurePrinterCommand.start(
                                getActivity(),
                                model,
                                callback
                        );
                    }
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
        DialogUtil.show(activity, DIALOG_NAME, TestPrinterFragment_.builder().build());
    }

    public ConfigurePrinterCommand.PrinterConfigureBaseCallback callback = new ConfigurePrinterCommand.PrinterConfigureBaseCallback() {

        @Override
        protected void handleSuccess() {
//            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.show(getActivity(), AlertDialogFragment.DialogType.COMPLETE, R.string.printer_config_dialog_title,
                    getString(R.string.printer_config_update_ok), R.string.btn_ok, new OnDialogClickListener() {

                        @Override
                        public boolean onClick() {
                            return true;
                        }
                    });
            dismiss();
        }

        @Override
        protected void handleFailure() {
//            WaitDialogFragment.hide(getActivity());
            AlertDialogFragment.showAlert(getActivity(), R.string.error_dialog_title, getString(R.string.printer_config_update_error));
        }
    };


}
