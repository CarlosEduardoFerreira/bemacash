package com.kaching123.tcr.activity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsArrayAdapter;
import com.kaching123.tcr.fragment.settings.AboutFragment;
import com.kaching123.tcr.fragment.settings.DataUsageStatFragment;
import com.kaching123.tcr.fragment.settings.DisplayFragment;
import com.kaching123.tcr.fragment.settings.DrawerSettingsFragment;
import com.kaching123.tcr.fragment.settings.PaxListFragment;
import com.kaching123.tcr.fragment.settings.PrinterListFragment;
import com.kaching123.tcr.fragment.settings.ScannerFragment;
import com.kaching123.tcr.fragment.settings.SyncSettingsFragment;
import com.kaching123.tcr.fragment.settings.TrainingModeSettingsFragment;
import com.kaching123.tcr.model.Permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by gdubina on 07/11/13.
 */
@EActivity(R.layout.settings_activity)
public class SettingsActivity extends SuperBaseActivity {

    private final static HashSet<Permission> permissions = new HashSet<Permission>();

    static {
        permissions.add(Permission.ADMIN);
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
                new NavigationItem(getString(R.string.pref_printer_title), getString(R.string.pref_printer_summary)),
                new NavigationItem(getString(R.string.pref_pax_title), getString(R.string.pref_pax_summary)),
                new NavigationItem(getString(R.string.pref_drawer_header_title), getString(R.string.pref_drawer_header_summary)),
                new NavigationItem(getString(R.string.pref_display_header_title), getString(R.string.pref_display_header_summary)),
                new NavigationItem(getString(R.string.pref_scanner_header_title), getString(R.string.pref_scanner_header_summary)),
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
        switch(pos){
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
                fragment = DataUsageStatFragment.instance();
                break;
            case 7:
                fragment = TrainingModeSettingsFragment.instance();
                break;
            case 8:
                fragment = AboutFragment.instance();
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_details, fragment).commit();
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
            UiHolder holder = (UiHolder)v.getTag();
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