package com.kaching123.tcr.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
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
import com.kaching123.tcr.fragment.dialog.SyncWaitDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragmentWithCallback;
import com.kaching123.tcr.fragment.settings.AboutFragment;
import com.kaching123.tcr.fragment.settings.DataUsageStatFragment;
import com.kaching123.tcr.fragment.settings.DiagnoseFragment;
import com.kaching123.tcr.fragment.settings.DisplayFragment;
import com.kaching123.tcr.fragment.settings.DrawerSettingsFragment;
import com.kaching123.tcr.fragment.settings.PaxListFragment;
import com.kaching123.tcr.fragment.settings.PrinterListFragment;
import com.kaching123.tcr.fragment.settings.ScaleFragment;
import com.kaching123.tcr.fragment.settings.ScannerFragment;
import com.kaching123.tcr.fragment.settings.SyncSettingsFragment;
import com.kaching123.tcr.fragment.settings.TrainingModeSettingsFragment;
import com.kaching123.tcr.fragment.settings.USBMsrFragment;
import com.kaching123.tcr.model.Permission;

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
public class SettingsActivity extends SuperBaseActivity implements SyncSettingsFragment.ManualCheckUpdateListener {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    static {
        permissions.add(Permission.ADMIN);
    }

    private Fragment fragment = null;

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
                new NavigationItem(getString(R.string.pref_printer_title), getString(R.string.pref_printer_summary)),
                new NavigationItem(getString(R.string.pref_pax_title), getString(R.string.pref_pax_summary)),
                new NavigationItem(getString(R.string.pref_drawer_header_title), getString(R.string.pref_drawer_header_summary)),
                new NavigationItem(getString(R.string.pref_display_header_title), getString(R.string.pref_display_header_summary)),
                new NavigationItem(getString(R.string.pref_scanner_header_title), getString(R.string.pref_scanner_header_summary)),
                new NavigationItem(getString(R.string.pref_msr_header_title), getString(R.string.pref_msr_header_summary)),
                new NavigationItem(getString(R.string.pref_scale_header_title), getString(R.string.pref_scale_header_summary)),
                new NavigationItem(getString(R.string.pref_devices_diagnose_title), getString(R.string.pref_devices_diagnose_summary)),
                new NavigationItem(getString(R.string.pref_datausage_header_title), getString(R.string.pref_datausage_header_summary)),
                new NavigationItem(getString(R.string.pref_training_mode_header_title), getString(R.string.pref_training_mode_header_summary)),
                new NavigationItem(getString(R.string.pref_about_header_title), getString(R.string.pref_about_header_summary))
        )));

        navigationList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateDetails(position);
            }
        });

        navigationList.setItemChecked(0, true);
        updateDetails(0);
    }

    private void updateDetails(int pos) {
        Fragment fragment = null;
        switch (pos) {
            case 0:
                fragment = SyncSettingsFragment.instance();
                break;
            case 1:
                fragment = PrinterListFragment.instance();
                break;
            case 2:
                fragment = PaxListFragment.instance();
                break;
            case 3:
                fragment = DrawerSettingsFragment.instance();
                break;
            case 4:
                fragment = DisplayFragment.instance();
                break;
            case 5:
                fragment = ScannerFragment.instance();
                break;
            case 6:
                fragment = USBMsrFragment.instance();
                break;
            case 7:
                fragment = ScaleFragment.instance();
                break;
            case 8:
                fragment = DiagnoseFragment.instance();
                break;
            case 9:
                fragment = DataUsageStatFragment.instance();
                break;
            case 10:
                fragment = TrainingModeSettingsFragment.instance();
                break;
            case 11:
                fragment = AboutFragment.instance();
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_details, fragment).commit();
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
        WaitDialogFragmentWithCallback.show(this,getString(R.string.wait_dialog_title));
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
                    (TextView) v.findViewById(R.id.text2)
            ));
            return v;
        }

        @Override
        protected View bindView(View v, int position, NavigationItem item) {
            UiHolder holder = (UiHolder) v.getTag();
            holder.text1.setText(item.title);
            holder.text2.setText(item.subTitle);
            return v;
        }
    }

    private static class UiHolder {

        TextView text1;

        TextView text2;

        private UiHolder(TextView text1, TextView text2) {
            this.text1 = text1;
            this.text2 = text2;
        }
    }

    private static class NavigationItem {
        String title;
        String subTitle;

        private NavigationItem(String title, String subTitle) {
            this.title = title;
            this.subTitle = subTitle;
        }
    }

    public static void start(Context context) {
        SettingsActivity_.intent(context).start();
    }
}
