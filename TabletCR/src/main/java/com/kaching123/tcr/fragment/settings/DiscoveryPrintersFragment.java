package com.kaching123.tcr.fragment.settings;

import android.app.Fragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;
import com.kaching123.tcr.R;

@EFragment(R.layout.settings_discovery_printers_fragment)
@OptionsMenu(R.menu.discover_printer_activity)
public class DiscoveryPrintersFragment extends Fragment {

    /*@App
    protected TcrApplication app;

    @ViewById
    protected View progressBlock;

    @ViewById
    protected ListView listView;

    @ViewById
    protected TextView printerIp;

    @ViewById
    protected TextView printerMac;

    @ViewById
    protected View printerSearchBlock;

    @ViewById
    protected View printerStatusBlock;

    protected MenuItem actionCancel;
    protected MenuItem actionSearch;
    protected MenuItem actionRefresh;

    private FindPrinterCallback findPrinterCallback = new FindPrinterCallback();


    private TaskHandler currentTask;

    private ReceiverWrapper discoveryReceiver = new ReceiverWrapper(new IntentFilter(FindPrinterCommand.ACTION_PRINTER)) {

        @Override
        public void onReceive(Context context, Intent intent) {
            findPrinterCallback.addPrinter(intent.getExtras());
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setAdapter(adapter = new PrintersAdapter(getActivity()));
        bindCurPrinterView();
    }

    private void bindCurPrinterView() {
        if (app.getShopPref().printerIp().exists()) {
            printerIp.setText(getString(R.string.pref_printer_ip_tmpl, app.getShopPref().printerIp().get()));
            printerMac.setText(getString(R.string.pref_printer_mac_tmpl, app.getShopPref().printerMac().get()));
            printerMac.setVisibility(View.VISIBLE);
            swapBlocks(true);
        } else {
            printerIp.setText(R.string.pref_current_printer_empty);
            printerMac.setVisibility(View.GONE);
            swapBlocks(false);
        }
    }

    private void swapBlocks(boolean showStatus) {
        printerStatusBlock.setVisibility(showStatus ? View.VISIBLE : View.GONE);
        printerSearchBlock.setVisibility(showStatus ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        discoveryReceiver.register(getActivity());
        if (!app.getShopPref().printerIp().exists()) {
            currentTask = FindPrinterCommand.start(getActivity(), findPrinterCallback);
        } else {
            GetPrinterStatusCommand.start(getActivity(), printerStatusCallback);
        }
        updateMenuActions();
    }

    @Override
    public void onStop() {
        super.onStop();
        discoveryReceiver.unregister(getActivity());
        cancelCurTask();
    }

    private void cancelCurTask() {
        if (currentTask != null) {
            currentTask.cancel(getActivity(), 0, null);
        }
        currentTask = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        actionRefresh = menu.findItem(R.id.action_refresh);
        actionCancel = menu.findItem(R.id.action_cancel);
        actionSearch = menu.findItem(R.id.action_search);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        updateMenuActions();
    }

    private void updateMenuActions() {
        if(actionCancel == null){
            return;
        }
        actionCancel.setVisible(currentTask != null);
        actionRefresh.setVisible(currentTask == null && app.getShopPref().printerIp().exists());
        actionSearch.setVisible(currentTask == null);
    }

    @ItemClick
    protected void listViewItemClicked(PrinterInfo info) {
        app.getShopPref().edit().printerIp().put(info.text1).printerPort().put(info.port).printerMac().put(info.macAddress).apply();
        bindCurPrinterView();
        actionCancelSelected();
        GetPrinterStatusCommand.start(getActivity(), printerStatusCallback);
    }

    @OptionsItem
    protected void actionCancelSelected() {
        cancelCurTask();
        progressBlock.setVisibility(View.GONE);
        updateMenuActions();
    }

    @OptionsItem
    protected void actionSearchSelected() {
        progressBlock.setVisibility(View.VISIBLE);
        cancelCurTask();
        adapter.clear();
        currentTask = FindPrinterCommand.start(getActivity(), findPrinterCallback);
        updateMenuActions();
        swapBlocks(false);
    }

    @OptionsItem
    protected void actionRefreshSelected() {
        GetPrinterStatusCommand.start(getActivity(), printerStatusCallback);
        swapBlocks(true);
    }

    public class FindPrinterCallback extends BaseFindPrinterCallback {

        public void addPrinter(Bundle data) {
            PrinterInfo info = FindPrinterCommand.unpackPrinterInfo(data);
            if (info == null) {
                return;
            }
            for(int i = 0; i < adapter.getCount(); i++){
                PrinterInfo e = adapter.getItem(i);
                if(e.fullAddress.equals(info.fullAddress)){
                    return;
                }
            }
            adapter.add(info);
        }

        @Override
        protected void onSearchFinished() {
            currentTask = null;
            progressBlock.setVisibility(View.GONE);
            updateMenuActions();
        }
    }



*/
}
