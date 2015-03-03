package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v4.preference.PreferenceFragment;
import android.widget.Toast;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.SyncWaitDialogFragment;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.service.SyncCommand;
import com.kaching123.tcr.service.UploadTask;
import com.kaching123.tcr.util.ReceiverWrapper;

/**
 * Created by gdubina on 07/11/13.
 */
@EFragment
@OptionsMenu(R.menu.settings_sync_fragment)
public class SyncSettingsFragment extends PreferenceFragment {
    private static final IntentFilter intentFilter = new IntentFilter();
    static {
        intentFilter.addAction(UploadTask.ACTION_UPLOAD_COMPLETED);
        intentFilter.addAction(SyncCommand.ACTION_SYNC_COMPLETED);
    }

    @App
    protected TcrApplication app;

    private boolean isUploadSuccess;

    private ReceiverWrapper progressReceiver = new ReceiverWrapper(intentFilter) {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() == null || intent == null)
                return;

            if (UploadTask.ACTION_UPLOAD_COMPLETED.equals(intent.getAction())) {
                isUploadSuccess = intent.getBooleanExtra(UploadTask.EXTRA_SUCCESS, false);
                return;
            }
            if (SyncCommand.ACTION_SYNC_COMPLETED.equals(intent.getAction())) {
                SyncWaitDialogFragment.hide(getActivity());

                boolean success = intent.getBooleanExtra(SyncCommand.EXTRA_SUCCESS, false);
                if (success && isUploadSuccess) {
                    AlertDialogFragment.showComplete(getActivity(), R.string.sync_success_title, getString(R.string.sync_success_message));

                    if (app.isTipsEnabledWasChanged()) {
                        app.setTipsEnabledWasChanged(false);
                        Toast.makeText(getActivity(), app.isTipsEnabled() ? R.string.warning_message_tips_enabled : R.string.warning_message_tips_disabled, Toast.LENGTH_LONG).show();
                    }
                } else {
                    AlertDialogFragment.showAlert(getActivity(), R.string.sync_error_title, getString(R.string.sync_error_message));
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        progressReceiver.register(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        progressReceiver.unregister(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_sync_pref_fragment);
        Preference timePref = findPreference(getString(R.string.pref_sync_time_key));

        assert timePref != null;

        timePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int mins = Integer.parseInt(newValue.toString());
                app.getShopPref().syncPeriod().put(mins);
                OfflineCommandsService.scheduleSyncAction(getActivity());
                return true;
            }
        });
    }

    @OptionsItem
    protected void actionSyncSelected(){
        SyncWaitDialogFragment.show(getActivity(), getString(R.string.pref_sync_wait));
        isUploadSuccess = false;
        OfflineCommandsService.startUpload(getActivity(), true);
        OfflineCommandsService.startDownload(getActivity(), true);
    }

    public static Fragment instance() {
        return SyncSettingsFragment_.builder().build();
    }
}
