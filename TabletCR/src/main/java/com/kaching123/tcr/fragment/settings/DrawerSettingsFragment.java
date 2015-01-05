package com.kaching123.tcr.fragment.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v4.preference.PreferenceFragment;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;

/**
 * Created by gdubina on 07/11/13.
 */
public class DrawerSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_drawer_pref_fragment);
        Preference drawerPref = findPreference(getString(R.string.pref_drawer_key));
        assert drawerPref != null;

        drawerPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int value = Integer.parseInt(newValue.toString());
                TcrApplication.get().getShopPref().drawerPinHigh().put(value);
                return true;
            }
        });
    }

    public static Fragment instance() {
        return new DrawerSettingsFragment();
    }
}
