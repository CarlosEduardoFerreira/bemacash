package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.FindPaxCommand;
import com.kaching123.tcr.commands.store.settings.EditPaxCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.model.PaxModel;
import com.telly.groundy.TaskHandler;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.ViewById;

/**
 * Created by gdubina on 11.02.14.
 */
@EFragment
public class FindPAXFragment extends StyledDialogFragment {

    @ViewById
    protected TextView paxNotFoundText;

    @FragmentArg
    protected int timeout;

    private static final String DIALOG_NAME = "FindPaxFragment";

    private FindPaxCallback findPrinterCallback = new FindPaxCallback();

    protected ListView listView;
    protected View progressBlock;

    private PaxListAdapter adapter;
    private TaskHandler currentTask;

    @Override
    protected int getDialogContentLayout() {
        return R.layout.settings_find_pax_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.pref_paxes_list;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_select;
    }

    private PaxModel paxModel;

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                WaitDialogFragment.show(getActivity(), getString(R.string.pax_connect_wait_dialog_title));
                getPositiveButton().setEnabled(false);
                getNegativeButton().setEnabled(false);
                EditPaxCommand.start(getActivity(), paxModel, new EditPaxCommand.PaxEditCommandBaseCallback() {
                    @Override
                    protected void handleSuccess() {
                        Toast.makeText(getActivity(), getString(R.string.pax_configured), Toast.LENGTH_LONG).show();
                        WaitDialogFragment.hide(getActivity());
                        FindPAXFragment.this.dismiss();
                    }

                    @Override
                    protected void handleError() {
                        WaitDialogFragment.hide(getActivity());
                        Toast.makeText(getActivity(), getString(R.string.pax_not_configured), Toast.LENGTH_LONG).show();
                        FindPAXFragment.this.dismiss();
                    }
                });
                return false;
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
        listView.setAdapter(adapter = new PaxListAdapter(getActivity()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                getPositiveButton().setTextColor(normalBtnColor);
                getPositiveButton().setEnabled(true);
            }
        });
    }

    @AfterViews
    public void init() {
        paxNotFoundText.setText(getString(R.string.pax_not_found_message));
        paxNotFoundText.setVisibility(View.GONE);
        getPositiveButton().setTextColor(disabledBtnColor);
        getPositiveButton().setEnabled(false);

    }

    @Override
    public void onStart() {
        super.onStart();
        currentTask = FindPaxCommand.start(getActivity(), timeout, findPrinterCallback);
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

    public class FindPaxCallback extends FindPaxCommand.BaseFindPaxCallback {

        @Override
        protected void onSearchFinished() {
            currentTask = null;
            progressBlock.setVisibility(View.GONE);
            if (adapter.getCount() == 0)
                paxNotFoundText.setVisibility(View.VISIBLE);
        }

        @Override
        public void handleAddPax(PaxModel info) {
            paxNotFoundText.setVisibility(View.GONE);
            if (info == null) {
                return;
            }
            for (int i = 0; i < adapter.getCount(); i++) {
                PaxModel e = adapter.getItem(i);
                if (e.serial.equals(info.serial)) {
                    return;
                }
            }
            paxModel = info;
            adapter.add(info);
        }
    }

    private class PaxListAdapter extends ArrayAdapter<PaxModel> {

        public PaxListAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.simple_list_item_one_choice_dark, parent, false);
            }
            PaxModel info = getItem(position);
            CheckedTextView textView = (CheckedTextView) convertView;
            textView.setText(info.ip + ":" + info.port);
            /*itemView = (PrinterInfoListItem) convertView;
            itemView.bind(info.fullAddress, info.macAddress);*/
            return convertView;
        }

    }


    public static void show(FragmentActivity activity, int timeout) {
        DialogUtil.show(activity, DIALOG_NAME, FindPAXFragment_.builder().timeout(timeout).build());
    }

}
