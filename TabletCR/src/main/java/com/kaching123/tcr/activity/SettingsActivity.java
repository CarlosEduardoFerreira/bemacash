package com.kaching123.tcr.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.kaching123.tcr.AutoUpdateService;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsArrayAdapter;
import com.kaching123.tcr.fragment.UnavailabledOptionFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.SyncWaitDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragmentWithCallback;
import com.kaching123.tcr.fragment.settings.AboutFragment;
import com.kaching123.tcr.fragment.settings.DataUsageStatFragment;
import com.kaching123.tcr.fragment.settings.DiagnoseFragment;
import com.kaching123.tcr.fragment.settings.DisplayFragment;
import com.kaching123.tcr.fragment.settings.DrawerSettingsFragment;
import com.kaching123.tcr.fragment.settings.FindDeviceFragment;
import com.kaching123.tcr.fragment.settings.PaxListFragment;
import com.kaching123.tcr.fragment.settings.PrinterListFragment;
import com.kaching123.tcr.fragment.settings.ScaleFragment;
import com.kaching123.tcr.fragment.settings.ScannerFragment;
import com.kaching123.tcr.fragment.settings.SyncSettingsFragment;
import com.kaching123.tcr.fragment.settings.TrainingModeSettingsFragment;
import com.kaching123.tcr.fragment.settings.USBMsrFragment;
import com.kaching123.tcr.model.Permission;
import com.kaching123.tcr.model.PlanOptions;
import com.kaching123.tcr.service.IScannerBinder;
import com.kaching123.tcr.service.ScannerBinder;
import com.kaching123.tcr.service.ScannerListener;
import com.kaching123.tcr.service.ScannerService;
import com.kaching123.tcr.service.USBScannerService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by gdubina on 07/11/13.
 */
@EActivity(R.layout.settings_activity)
public class SettingsActivity extends ScannerBaseActivity implements SyncSettingsFragment.ManualCheckUpdateListener, WaitDialogFragmentWithCallback.OnDialogDismissListener, IScannerBinder {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    static {
        permissions.add(Permission.ADMIN);
    }

    private Fragment fragment = null;

    private boolean canDestroy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        canDestroy = true;
    }

    public boolean canDestroy(){
        return canDestroy;
    }

    @Override
    public void onDestroy() {
        canDestroy = false;
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                init();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Set<Permission> getPermissions() {
        return permissions;
    }

    @ViewById
    protected ListView navigationList;

    @AfterViews
    protected void init() {
        navigationList.setAdapter(new NavigationAdapter(this, Arrays.asList(
                new NavigationItem(getString(R.string.pref_sync_title), getString(R.string.pref_sync_summary)),
                new NavigationItem(getString(R.string.pref_hardware_header_title), getString(R.string.pref_hardware_header_summary), true),
                new NavigationItem(getString(R.string.pref_datausage_header_title), getString(R.string.pref_datausage_header_summary)),
                new NavigationItem(getString(R.string.pref_training_mode_header_title), getString(R.string.pref_training_mode_header_summary)),
                new NavigationItem(getString(R.string.pref_about_header_title), getString(R.string.pref_about_header_summary)))
        ));

        navigationList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateDetails(position);
            }
        });

        navigationList.setItemChecked(0, true);
        updateDetails(0);
    }

    protected void initDevices() {
        navigationList.setAdapter(new NavigationAdapter(this, Arrays.asList(
                new NavigationItem(getString(R.string.pref_printer_title), getString(R.string.pref_printer_summary)),
                new NavigationItem(getString(R.string.pref_display_header_title), getString(R.string.pref_display_header_summary)),
                new NavigationItem(getString(R.string.pref_pax_title), getString(R.string.pref_pax_summary)),
                new NavigationItem(getString(R.string.pref_drawer_header_title), getString(R.string.pref_drawer_header_summary)),
                new NavigationItem(getString(R.string.pref_scanner_header_title), getString(R.string.pref_scanner_header_summary)),
                new NavigationItem(getString(R.string.pref_msr_header_title), getString(R.string.pref_msr_header_summary)),
                new NavigationItem(getString(R.string.pref_scale_header_title), getString(R.string.pref_scale_header_summary)),
//                new NavigationItem(getString(R.string.pref_kds_header_title), getString(R.string.pref_kds_header_summary)),
                new NavigationItem(getString(R.string.pref_devices_diagnose_title), getString(R.string.pref_devices_diagnose_summary))
        )));

        navigationList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateHardwareDetails(position);
            }
        });

        navigationList.setItemChecked(0, true);
        updateHardwareDetails(0);
    }

    private void updateDetails(int pos) {
        switch (pos) {
            case 0:
                fragment = SyncSettingsFragment.instance();
                break;
            case 1:
                initDevices();
                break;
            case 2:
                fragment = DataUsageStatFragment.instance();
                break;
            case 3:
                fragment = TrainingModeSettingsFragment.instance();
                break;
            case 4:
                fragment = AboutFragment.instance();
                break;

        }
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_details, fragment).commit();
    }

    private void updateHardwareDetails(int pos) {
        switch (pos) {
            case 0:
                fragment = PrinterListFragment.instance();
                break;
            case 1:
                fragment = DisplayFragment.instance();
                break;
            case 2:
                fragment = PaxListFragment.instance();
                break;
            case 3:
                fragment = DrawerSettingsFragment.instance();
                break;
            case 4:
                fragment = ScannerFragment.instance();
                break;
            case 5:
                fragment = USBMsrFragment.instance();
                break;
            case 6:
                fragment = PlanOptions.isScaleConnectionAllowed() ?
                        ScaleFragment.instance() : UnavailabledOptionFragment.instance();
                break;
           /* case 7:
                fragment = KDSListFragment.instance();
                break;*/
            case 7:
                fragment = DiagnoseFragment.instance();
                break;

        }
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_details, fragment).commit();
    }
    protected ScannerBinder scannerBinder;
    @Override
    public void setScannerListener(ScannerListener scannerListener) {
        Logger.d("ScannerBaseActivity: setScannerListener()");
        if (scannerBinder != null)
            scannerBinder.setScannerListener(scannerListener);
        else
            Logger.d("ScannerBaseActivity: setScannerListener(): failed - not binded!");
    }

    @Override
    public void onCheckClick() {
        SyncWaitDialogFragment.show(SettingsActivity.this, getString(R.string.pref_check_update_wait));
        stopService(new Intent(SettingsActivity.this, AutoUpdateService.class));
        startCheckUpdateService(true);
    }

    @Override
    public void barcodeReceivedFromSerialPort(String barcode) {
//        Logger.d("BaseCashierActivity barcodeReceivedFromSerialPort onReceive:" + barcode);
        DiagnoseFragment fragment = (DiagnoseFragment) getSupportFragmentManager().findFragmentById(R.id.settings_details);
        fragment.receivedScannerCallback(barcode);
    }

    @Override
    public void onDialogDismissed(String barcode) {
//        WaitDialogFragmentWithCallback.show(this,getString(R.string.wait_dialog_title));
        ((DiagnoseFragment) fragment).setScannerRead(barcode);
    }

    @Override
    public boolean tryReconnectScanner() {
        Logger.d("ScannerBaseActivity: tryReconnectScanner()");
        if (scannerBinder != null)
            return scannerBinder.tryReconnectScanner();
        else
            Logger.d("ScannerBaseActivity: tryReconnectScanner(): failed - not binded!");
        return false;
    }

    @Override
    public void disconnectScanner() {
        Logger.d("ScannerBaseActivity: disconnectScanner()");
        if (scannerBinder != null)
            scannerBinder.disconnectScanner();
        else
            Logger.d("ScannerBaseActivity: disconnectScanner(): failed - not binded!");
    }

    @Override
    public void onBarcodeReceived(String barcode) {
        barcode = barcode.replaceAll("[^A-Za-z0-9]", "");
        ((DiagnoseFragment)fragment).setScannerRead(barcode);
    }

    private class NavigationAdapter extends ObjectsArrayAdapter<NavigationItem> {

        public NavigationAdapter(Context context, List<NavigationItem> list) {
            super(context, list);
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            View v = View.inflate(getContext(), R.layout.settings_navigation_item_line2, null);
            v.setTag(new UiHolder(
                    (TextView) v.findViewById(R.id.text1),
                    (TextView) v.findViewById(R.id.text2),
                    (TextView) v.findViewById(R.id.indicator)
            ));
            return v;
        }

        @Override
        protected View bindView(View v, int position, NavigationItem item) {
            UiHolder holder = (UiHolder) v.getTag();
            holder.text1.setText(item.title);
            holder.text2.setText(item.subTitle);
            if (item.hasIndicator)
                holder.indicator.setVisibility(View.VISIBLE);
            return v;
        }
    }

    private class UiHolder {

        TextView text1;

        TextView text2;

        TextView indicator;

        private UiHolder(TextView text1, TextView text2, TextView indicator) {
            this.text1 = text1;
            this.text2 = text2;
            this.indicator = indicator;
        }
    }

    private class NavigationItem {
        String title;
        String subTitle;
        boolean hasIndicator;

        private NavigationItem(String title, String subTitle) {
            this.title = title;
            this.subTitle = subTitle;
            this.hasIndicator = false;
        }

        private NavigationItem(String title, String subTitle, boolean hasIndicator) {
            this.title = title;
            this.subTitle = subTitle;
            this.hasIndicator = hasIndicator;
        }
    }

    public static void start(Context context) {
        SettingsActivity_.intent(context).start();
    }


    private boolean isUSBScanner;
    public void bindToScannerService() {
        Logger.d("ScannerBaseActivity: bindToScannerService()");
        boolean scannerConfigured = !TextUtils.isEmpty(getApp().getShopPref().scannerAddress().get());
        Log.d("BemaCarl4","SettingsActivity.bindToScannerService.scannerConfigured: " + scannerConfigured);

        if (scannerConfigured) {
            Log.d("BemaCarl4","SettingsActivity.bindToScannerService.scannerAddress().get(): " + getApp().getShopPref().scannerAddress().get());
            Log.d("BemaCarl4","SettingsActivity.bindToScannerService.USB_SCANNER_ADDRESS: " + FindDeviceFragment.USB_SCANNER_ADDRESS);
            Log.d("BemaCarl4","SettingsActivity.bindToScannerService.USB_SCANNER_ADDRESS: " + FindDeviceFragment.USB_HID_SCANNER_ADDRESS);
            Log.d("BemaCarl4","SettingsActivity.bindToScannerService.USB_SCANNER_ADDRESS: " + FindDeviceFragment.SEARIL_PORT_SCANNER_ADDRESS);

            if (getApp().getShopPref().scannerAddress().get().equalsIgnoreCase(FindDeviceFragment.USB_SCANNER_ADDRESS)) {
                Log.d("BemaCarl4","SettingsActivity.bindToScannerService.scannerServiceConnection1: " + scannerServiceConnection);
                setUSBScanner(true);
                USBScannerService.bind(this, scannerServiceConnection);
            }else if(getApp().getShopPref().scannerAddress().get().equalsIgnoreCase(FindDeviceFragment.USB_HID_SCANNER_ADDRESS)) {
                Log.d("BemaCarl4", "SettingsActivity.bindToScannerService.scannerServiceConnection2: " + scannerServiceConnection);
                setUSBScanner(true);
            }else if(!getApp().getShopPref().scannerAddress().get().equalsIgnoreCase(FindDeviceFragment.SEARIL_PORT_SCANNER_ADDRESS)) {
                Log.d("BemaCarl4","SettingsActivity.bindToScannerService.scannerServiceConnection3: " + scannerServiceConnection);
                ScannerService.bind(this, scannerServiceConnection);
            }

        }else {
            Log.d("BemaCarl4", "SettingsActivity.bindToScannerService: failed - scanner is not configured!");
        }
    }

    public void unbindFromScannerService() {
        Log.d("BemaCarl4", "SettingsActivity.unbindFromScannerService");
        Logger.d("ScannerBaseActivity: unbindFromScannerService()");
        if (scannerBinder != null) {
            scannerBinder.disconnectScanner();
            scannerBinder = null;
            unbindService(scannerServiceConnection);
        } else {
            Logger.d("ScannerBaseActivity: unbindFromScannerService(): ignore and exit - not binded");
        }
    }

    private void setUSBScanner(boolean flag) {
        isUSBScanner = flag;
    }

    private boolean getUSBScanner()
    {
        return isUSBScanner;
    }

    private ServiceConnection scannerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Logger.d("ScannerBaseActivity: scannerServiceConnection: onServiceConnected()");
            scannerBinder = (ScannerBinder) binder;
            setScannerListener(scannerListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Logger.d("ScannerBaseActivity: scannerServiceConnection: onServiceDisconnected()");
            scannerBinder = null;
        }
    };

    private ScannerListener scannerListener = new ScannerListener() {

        @Override
        public void onDisconnected() {
            Logger.d("ScannerBaseActivity: scannerListener: onDisconnected()");
            if (isFinishing() || isDestroyed()) {
                Logger.d("ScannerBaseActivity: scannerListener: onDisconnected(): ignore and exit - activity is finishing");
                return;
            }

            AlertDialogFragment.showAlert(
                    SettingsActivity.this,
                    R.string.error_dialog_title,
                    getString(R.string.error_message_scanner_disconnected),
                    R.string.btn_try_again,
                    new StyledDialogFragment.OnDialogClickListener() {

                        @Override
                        public boolean onClick() {
                            tryReconnectScanner();
                            return true;
                        }

                    }
            );
        }

        @Override
        public void onBarcodeReceived(String barcode) {
            Logger.d("ScannerBaseActivity: scannerListener: onBarcodeReceived()" + barcode);
            if (isFinishing() || isDestroyed()) {
                Logger.d("ScannerBaseActivity: scannerListener: onBarcodeReceived(): ignore and exit - activity is finishing");
                return;
            }
            barcode = barcode.replaceAll("[^A-Za-z0-9]", "");
            ((DiagnoseFragment)fragment).setScannerRead(barcode);
        }

    };
}
